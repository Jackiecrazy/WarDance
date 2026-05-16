package jackiecrazy.wardance.utils;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.weapon.interactions.SweepAttack;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.joml.Vector4d;

import java.util.List;

public class SweepAnimationBuilder {
    private static final List<MotionFrame> STAB = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, -0.5)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1.5)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1.5))
    );
    private static final List<MotionFrame> PUNCH = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, -0.5), 60),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1.5), 0)
    );
    private static final List<MotionFrame> CIRCLE = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vector4d(0, 0, 1, 90)),
            new MotionFrame(new Vec3(-1, 0, 0), new Vec3(0, 0, 1), new Vector4d(-1, 0, 0, 90)),
            new MotionFrame(new Vec3(0, 0, -1), new Vec3(0, 0, 1), new Vector4d(0, 0, -1, 90)),
            new MotionFrame(new Vec3(1, 0, 0), new Vec3(0, 0, 1), new Vector4d(1, 0, 0, 90)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vector4d(0, 0, 1, 90)));
    private static final List<MotionFrame> LOOP = List.of(
            //new MotionFrame(new Vec3(0, -1, 0), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(0, 0, -1), new Vec3(0, 0, 1), new Vector4d(0, 0, -1, 180)),
            new MotionFrame(new Vec3(0, 1, 0.1), new Vec3(0, 0, 1), 0),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(0, -2, 1), new Vec3(0, 0, 1)));
    private static final List<MotionFrame> SLASH = List.of(
            new MotionFrame(new Vec3(1, 0.6, 1), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(-1, -0.4, 0), new Vec3(0, 0, 1), new Vector4d(-1, -0.4, 1, 45)));
    private static final List<MotionFrame> BACKSLASH = List.of(new MotionFrame(new Vec3(-1, 0.6, 1), new Vec3(0, 0, 1), -45), new MotionFrame(new Vec3(1, -0.4, 0), new Vec3(0, 0, 1), new Vector4d(1, -1, 1, -45)));
    private static final List<MotionFrame> CHOP = List.of(
            new MotionFrame(new Vec3(0, 1, 0.2), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(0, -0.5, 1), new Vec3(0, 0, 1)));
    private static final MotionManager circle_finish = new MotionManagers.DefinitionMM(new MotionGroup(CIRCLE, EasingFunctionEnum.IN_OUT_CUBIC, 10));
    private static final MotionManager LINE = new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunctionEnum.IN_CUBIC, 10));
    private static final MotionManager CONE1 = new MotionManagers.DefinitionMM(new MotionGroup(SLASH, EasingFunctionEnum.IN_OUT_CUBIC, 10));
    private static final MotionManager CONE2 = new MotionManagers.DefinitionMM(new MotionGroup(BACKSLASH, EasingFunctionEnum.IN_OUT_CUBIC, 10));
    private static final MotionManager IMPACT = new MotionManagers.DefinitionMM(new MotionGroup(CHOP, EasingFunctionEnum.IN_CUBIC, 10));
    public static int flip = 1;

    private static Vec3 generateFrame(float pitch, float yaw) {
        Vec3 base = new Vec3(0, 0, 1);//forward pointing
        return base.xRot(Mth.DEG_TO_RAD * pitch).yRot(Mth.DEG_TO_RAD * yaw).normalize();
    }

    public static void scheduleFinisher(LivingEntity e,
                                        InteractionHand hand,
                                        SweepAttack base) {
        final HitInfo preFinish = base.preFinishCopy().getHitInfo();
        final HitInfo finish = base.finisherCopy(CombatData.getCap(e).consumeSpirit(CombatData.getCap(e).getMaxSpirit())).getHitInfo();
        //final HitInfo finish = base.finisherCopy(true).getHitInfo();
        final double range = e.getAttributeValue(ForgeMod.ENTITY_REACH.get());
        //FlyingWeaponData.getCap(e).getWeapon(hand).lock(e);
        switch (base.getType()) {
            case CONE -> {
                //flourish thrice and stab
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, SweepAttack.SWEEPTYPE.CONE, base.getBase() + 3 * base.getScaling(), preFinish, range), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(15, SweepAttack.SWEEPTYPE.LINE, 3, finish, range + 2), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
            }
            case CLEAVE -> {
                //tcs
                final FrameEffects fx = new FrameEffects().setHit(finish).setRange(range);
                fx.setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW, FlyingWeaponEffect.LOCK_ORIENTATION);
                FlyingWeaponData.getCap(e).scheduleAction(hand, new MotionManagers.DefinitionMM(new MotionGroup(LOOP, EasingFunctionEnum.IN_CUBIC, 20, fx)), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
            }
            case IMPACT -> {
                //spin and slam down
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(15, SweepAttack.SWEEPTYPE.CIRCLE, base.getBase() + 3 * base.getScaling(), preFinish, range), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(20, SweepAttack.SWEEPTYPE.CLEAVE, 60, finish, range), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
            }
            case CIRCLE -> {
                //beeeeeg circle
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(40, SweepAttack.SWEEPTYPE.CIRCLE, base.getBase() + 3 * base.getScaling(), finish, range + 2), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
            }
            case LINE -> {
                //triple jab followed by big jab
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, SweepAttack.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling(), preFinish, range), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(5, SweepAttack.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling(), preFinish, range), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(10, SweepAttack.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling(), finish, range + 2), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
            }
            case NONE -> {
                //flurry of blows
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, SweepAttack.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling(), preFinish, range), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, SweepAttack.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling(), preFinish, range), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, SweepAttack.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling(), preFinish, range), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(10, SweepAttack.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling(), finish, range + 2), FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW);
            }
        }
    }

    private static double signedAngle(Vec3 a, Vec3 b, Vec3 edgeNormal) {
        Vec3 cross = a.cross(b);
        double dot = a.dot(b);
        double sign = edgeNormal.dot(cross);
        return Math.atan2(sign, dot);
    }

    public static MotionManager temp_getMMFromType(int time, SweepAttack.SWEEPTYPE type, double area,
                                                   HitInfo info,
                                                   double range) {
        //clamp time. The remaining time is expended in recovery.
        //time = Mth.clamp(time, 2, 5);
        flip *= -1;
        final Vec3 midFrame = generateFrame(0, 0);
        final FrameEffects fx = new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL).setHit(info).setRange(range);
        if (info != null)
            fx.setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.BIG_SHADOW, FlyingWeaponEffect.LOCK_ORIENTATION);
        switch (type) {
            case CONE:

                final Vec3 startFrame = generateFrame(10, (float) (-area/2) * flip);
                final Vec3 endFrame = generateFrame(-10, (float) (area/2) * flip);
                final Vec3 up = new Vec3(0, -1, 0);
//                double dot = Mth.clamp(up.dot(endFrame.subtract(startFrame).normalize()), -1.0, 1.0);
                double angleRadians = signedAngle(up, endFrame.subtract(startFrame), new Vec3(0,0,-1));//-Math.acos(dot);
                double angleDegrees = Math.toDegrees(angleRadians);// * flip;
                return new MotionManagers.DefinitionMM(new MotionGroup(List.of(
                        new MotionFrame(startFrame, new Vec3(0, 0, 1), (int) angleDegrees),
                        new MotionFrame(midFrame, new Vec3(0, 0, 1), (int) angleDegrees),
                        new MotionFrame(endFrame, new Vec3(0, 0, 1), (int) angleDegrees)),
                                                                       EasingFunctionEnum.IN_CUBIC, time, fx));
            case LINE:
                return new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunctionEnum.IN_OUT_CUBIC, time, fx));
            case CIRCLE:
                return new MotionManagers.DefinitionMM(new MotionGroup(CIRCLE, EasingFunctionEnum.IN_OUT_CUBIC, time, fx));
            case CLEAVE:
                return new MotionManagers.DefinitionMM(new MotionGroup(List.of(
                        new MotionFrame(generateFrame((float) Math.min(180, area * 2), 5 * flip), new Vec3(0, 0, 1)),
                        new MotionFrame(midFrame, new Vec3(0, 0, 1)),
                        new MotionFrame(generateFrame((float) Math.max(-90, -area / 2), -5 * flip), new Vec3(0, 0, 1))),
                                                                       EasingFunctionEnum.IN_CUBIC, time, fx));
            case IMPACT:
                return new MotionManagers.DefinitionMM(new MotionGroup(CHOP, EasingFunctionEnum.IN_CUBIC, time, fx));
            default:
                return new MotionManagers.DefinitionMM(new MotionGroup(PUNCH, EasingFunctionEnum.IN_OUT_CUBIC, time, fx));
        }
    }
}
