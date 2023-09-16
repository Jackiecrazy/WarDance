package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillArchetypes;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.shieldbash.ShieldBash;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class ShieldCrush extends ShieldBash {
    private static final ResourceLocation rl = new ResourceLocation("wardance:textures/skill/shield_crush.png");
    UUID debuffID = UUID.fromString("abe24c38-73e3-4551-9ef4-e16e117699c1");

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
    }

    @Override
    public ResourceLocation icon() {
        return rl;
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        //immediately end if not shielded
        if (caster == null) {
            removeMark(target);
            return true;
        }
        if (!CombatUtils.isHoldingShield(caster))
            removeMark(target);
        if (CasterData.getCap(caster).getSkillState(this) == STATE.COOLING) removeMark(target);
        //boing
        target.addEffect(new MobEffectInstance(FootworkEffects.ENFEEBLE.get(), 10));
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, debuffID, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        SkillUtils.modifyAttribute(caster, Attributes.KNOCKBACK_RESISTANCE, debuffID, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (caster != null) {
            SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, debuffID, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
            SkillUtils.modifyAttribute(caster, Attributes.KNOCKBACK_RESISTANCE, debuffID, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
            markUsed(caster);
        }
        SkillUtils.modifyAttribute(target, Attributes.MOVEMENT_SPEED, debuffID, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        SkillUtils.modifyAttribute(target, Attributes.KNOCKBACK_RESISTANCE, debuffID, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        SkillUtils.modifyAttribute(target, Attributes.ATTACK_SPEED, debuffID, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        super.onMarkEnd(caster, target, sd);
    }

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.coup_de_grace;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        activeTick(stats);
        if (stats.getState() == STATE.ACTIVE) {
            //check the looked entity
            Entity look = SkillUtils.aimEntity(caster);
            if (look instanceof LivingEntity elb && CombatUtils.isHoldingShield(caster)) {
                float remainingTime = stats.getDuration() / stats.getMaxDuration();
                ItemStack main = caster.getMainHandItem(), off = caster.getOffhandItem();
                float posdam = 2;
                if (WeaponStats.isShield(caster, main))
                    posdam = CombatUtils.getPostureAtk(caster, elb, InteractionHand.MAIN_HAND, null, 0, main);
                if (WeaponStats.isShield(caster, off))
                    posdam = Math.max(posdam, CombatUtils.getPostureAtk(caster, elb, InteractionHand.OFF_HAND, null, 0, off));
                posdam = Math.max(posdam, 2);
                posdam *= stats.getEffectiveness();
                posdam /= remainingTime;
                //crush!
                SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, debuffID, -0.8, AttributeModifier.Operation.MULTIPLY_TOTAL);
                SkillUtils.modifyAttribute(elb, Attributes.MOVEMENT_SPEED, debuffID, -0.8, AttributeModifier.Operation.MULTIPLY_TOTAL);
                SkillUtils.modifyAttribute(caster, Attributes.KNOCKBACK_RESISTANCE, debuffID, 10, AttributeModifier.Operation.ADDITION);
                SkillUtils.modifyAttribute(elb, Attributes.ATTACK_SPEED, debuffID, -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
                SkillUtils.modifyAttribute(elb, Attributes.KNOCKBACK_RESISTANCE, debuffID, 10, AttributeModifier.Operation.ADDITION);
                //up your grindset
                if (CombatData.getCap(elb).consumePosture(0.1f * posdam / 2) < 0) {
                    //crush, end state
                    markUsed(caster);
                }
                if (CombatData.getCap(caster).consumePosture(0.1f / remainingTime) < 0) {
                    //...well, rip
                    markUsed(caster);
                }
            } else {
                //end state
                markUsed(caster);
            }
        }
        if (cooldownTick(stats)) {
            return true;
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if(procPoint instanceof StunEvent se && state==STATE.ACTIVE && se.getEntity()!=caster){
            CombatData.getCap(target).addFracture(caster, Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity e = GeneralUtils.raytraceLiving(caster.level, caster, 3);
        if (to == STATE.ACTIVE && e != null && CombatUtils.isHoldingShield(caster) && cast(caster, e, duration())) {
            mark(caster, e, duration(), 10);
            prev.setArbitraryFloat(0);
            activate(caster, 10);
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 10);
            SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, debuffID, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        return boundCast(prev, from, to);
    }

    protected int duration() {
        return 200;
    }
}
