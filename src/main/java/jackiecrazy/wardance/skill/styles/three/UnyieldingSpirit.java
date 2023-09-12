package jackiecrazy.wardance.skill.styles.three;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.GainSpiritEvent;
import jackiecrazy.footwork.event.RegenSpiritEvent;
import jackiecrazy.wardance.capability.resources.CombatCapability;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.ColorRestrictionStyle;
import jackiecrazy.wardance.utils.DamageUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UnyieldingSpirit extends ColorRestrictionStyle {
    private static final AttributeModifier noMight = new AttributeModifier(UUID.fromString("0683fe69-5348-4a83-95d5-81a2eeb2cca0"), "unyielding spirit", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public UnyieldingSpirit() {
        super(3, false, SkillColors.green);
    }

    private void add(SkillData stats, float amnt) {
        if (stats.getDuration() < 2) {
            int before = (int) stats.getArbitraryFloat();
            stats.addArbitraryFloat(amnt);
            if ((int) stats.getArbitraryFloat() != before)
                stats.markDirty();
            stats.setMaxDuration(2);
            if (stats.getArbitraryFloat() >= 4) {
                stats.setDuration(Math.min(2, stats.getDuration() + 1));
                stats.addArbitraryFloat(-4);
            }
        }
    }

    @Override
    public void onEquip(LivingEntity caster) {
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (caster == target) return;
        if (procPoint instanceof LivingAttackEvent lae && lae.getEntity() == target && DamageUtils.isMeleeAttack(lae.getSource()) && procPoint.getPhase() == EventPriority.HIGHEST) {
            //bonk

            if (CombatData.getCap(caster).consumeSpirit(1) && lae.getEntity() == target) {
                CombatData.getCap(target).consumePosture(caster, 6);
            }
            if (lae.getEntity() == caster) {
                if (!CombatData.getCap(caster).consumeEvade()) {
                    if (stats.getDuration() >= 1) {
                        //consume one charge of itself to fully charge your evade meter
                        stats.decrementDuration();
                        CombatData.getCap(caster).setEvade(CombatCapability.EVADE_CHARGE);
                    }
                } else {
                    //ate the evade by accident, refund it for free
                    CombatData.getCap(caster).setEvade(CombatCapability.EVADE_CHARGE);
                }
            }
        } else if (procPoint instanceof GainSpiritEvent sce && procPoint.getPhase() == EventPriority.LOWEST && sce.getEntity() == caster) {
            add(stats, sce.getQuantity());
        } else if (procPoint instanceof RegenSpiritEvent sce && procPoint.getPhase() == EventPriority.LOWEST && sce.getEntity() == caster) {
            add(stats, sce.getQuantity());
        }
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return true;
    }
}
