package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;

public class Pummel extends ShieldBash {
    private static final UUID pummel = UUID.fromString("abe24c38-73e3-4551-9df4-e06e117612c1");

    @Override
    public boolean isPassive(LivingEntity caster) {
        return true;
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
    }

    @Override
    public void onEquip(LivingEntity caster) {
        updateCasterShieldDamage(caster, null);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_DAMAGE, pummel, 0, AttributeModifier.Operation.ADDITION);
        super.onUnequip(caster, stats);
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 0;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        //updateCasterShieldDamage(caster, null);
        return false;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof AttackEntityEvent lae && lae.getTarget() == target && procPoint.getPhase() == EventPriority.HIGHEST) {
            updateCasterShieldDamage(caster, target);
        }
        if(procPoint instanceof LivingDeathEvent lae && lae.getEntity() == target && DamageUtils.isMeleeAttack(lae.getSource()) && procPoint.getPhase() == EventPriority.HIGHEST){
            if(WeaponStats.isShield(caster,InteractionHand.MAIN_HAND))
                completeChallenge(caster);
        }
        stats.setState(STATE.INACTIVE);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return passive(prev, from, to);
    }

    private void updateCasterShieldDamage(LivingEntity caster, @Nullable LivingEntity target) {
        final ItemStack stack = caster.getMainHandItem();
        final boolean shield = WeaponStats.isShield(caster, stack);
        float attack = 0;
        if (shield)
            attack = CombatUtils.getPostureAtk(caster, target, InteractionHand.MAIN_HAND, null, 0, stack)*SkillUtils.getSkillEffectiveness(caster);
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_DAMAGE, pummel, attack, AttributeModifier.Operation.ADDITION);
    }
}
