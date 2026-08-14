package jackiecrazy.wardance.skill.bursts;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.entity.GrappleEntity;
import jackiecrazy.wardance.event.ConsumePostureEvent;
import jackiecrazy.wardance.event.GrappleEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public class Asura extends Skill {
    /*
    grow two more arms and make all weapons visible. Each attack strikes with all three arms from one side and always breaches.
    your grapples will always yank and you gain invul and twohanding
    lasts 10 seconds, each kill extends it
     */
    private static final AttributeModifier berserk = new AttributeModifier(UUID.fromString("a2124c38-73e3-4551-9df4-e06e117600c1"), "berserk twohanding bonus", 3, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier berserk1 = new AttributeModifier(UUID.fromString("a2124c38-73e3-4551-9df4-e06e117600c1"), "berserk attack speed bonus", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private final HashSet<String> tag = makeTag(SkillTags.offensive, SkillTags.physical);

    @Override
    public float mightCost(LivingEntity caster) {
        return 1;
    }

    @Override
    public HashSet<String> getTags() {
        return tag;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            if(!StylishData.getCap(caster).drainAdrenaline(0.005f/stats.getEffectiveness())) {
                markUsed(caster);
                return true;
            }
            if (caster.getMainHandItem().is(WeaponStats.TWO_HANDED) && caster.getOffhandItem().is(WeaponStats.TWO_HANDED))
                completeChallenge(caster);
//            if(FlyingWeaponData.getCap(caster).hasGrapple())
//                FlyingWeaponData.getCap(caster).getGrapple().setHookStrength(10);
        }
        return false;
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster, SkillData sd) {
        if(!StylishData.getCap(caster).maxAdrenaline())return CastStatus.OTHER;
        return super.castingCheck(caster, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent lae && lae.getEntity() == target && DamageUtils.isMeleeAttack(lae.getSource()) && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (state == STATE.HOLSTERED && cast(caster, target, 5 * SkillUtils.getSkillEffectiveness(caster) * (2 - (caster.getHealth() / caster.getMaxHealth())))) {
                SkillUtils.addAttribute(caster, WarAttributes.TWO_HANDING.get(), berserk);
                SkillUtils.addAttribute(caster, Attributes.ATTACK_SPEED, berserk1);
                CombatUtils.triggerSteveTime(caster, 30);
            }
        }
        if(state == STATE.ACTIVE) {
//            if (procPoint instanceof LivingDeathEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
//                StylishData.getCap(caster).;
//            }
//            if (procPoint instanceof StunEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
//                stats.setDuration(stats.getMaxDuration());
//            }
            if (procPoint instanceof GrappleEvent e && e.getEntity()==caster){
                e.getGrapple().setHookStrength(300);
                e.getGrapple().retract(GrappleEntity.ACTION.YANK);
            }
            if (procPoint instanceof LivingHurtEvent e && e.getEntity()==caster){
                e.setAmount(0);
            }
            if (procPoint instanceof ConsumePostureEvent e && e.getEntity()==caster){
                e.setPostureConsumption(0);
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            StylishData.getCap(caster).resetAdrenaline();
            SkillUtils.removeAttribute(caster, WarAttributes.TWO_HANDING.get(), berserk);
            SkillUtils.removeAttribute(caster, Attributes.ATTACK_SPEED, berserk1);
            return true;
        }
        return boundCast(prev, from, to);
    }


}
