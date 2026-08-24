package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.Animation;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4d;

import java.util.List;

public class FieryLunge extends FiveElementFist {
    private static final List<MotionFrame> SWEEP = List.of(
            new MotionFrame(new Vec3(0.0F, 0.0F, 1.0F),
                            new Vec3(0.0F, 0.0F, 0.2F),
                            new Vector4d(-1.0, 0.4, 0, 0))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON).setHit(new HitInfo(0, 1, 1, false, false, 1))),
            new MotionFrame(new Vec3(0.0F, 0.0F, 1.0),
                            new Vec3(0.0F, 0.0F, 1.0F),
                            new Vector4d(-1.0, 0.4, -0.2, 0)));
    private static final MotionManager FIRE = new MotionManagers.DefinitionMM(new MotionGroup(SWEEP, EasingFunctionEnum.IN_SINE, 5));
    public static final WeaponInteractions.InteractionGroup FIRE_LUNGE = new Animation().setAction(FIRE).asGroup().withSwingEffect(new HitEffects().setDodge_frames(10).setVelocity(new Vec3(0, 0, 0.5)));

    @Override
    WeaponInteractions.InteractionGroup getSweep() {
        return FIRE_LUNGE;
    }

    @Override
    WeaponStats.AttackState toReplace() {
        return WeaponStats.AttackState.SPRINTING;
    }

//    @Override
//    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
//        super.onProc(caster, procPoint, state, stats, target);
//        if (procPoint instanceof DamageKnockbackEvent e && procPoint.getPhase() == EventPriority.HIGHEST && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND) && e.getEntity() == target) {
//            caster.setDeltaMovement(caster.getDeltaMovement().add(caster.position().vectorTo(target.position()).scale(0.17)));
//            CombatData.getCap(caster).setGuardTime((int) (10*caster.getAttributeValue(WarAttributes.DODGE_EXTEND.get())));
//            caster.hurtMarked = true;
//        }
//    }
//
//    @Override
//    protected void doAttack(LivingEntity caster, LivingEntity target) {
//        CombatData.getCap(target).setHandBind(InteractionHand.MAIN_HAND, (int) (SkillUtils.getSkillEffectiveness(caster) * 10));
//    }
}
