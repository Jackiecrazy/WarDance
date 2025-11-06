package jackiecrazy.wardance.utils;

import jackiecrazy.footwork.move.motionframe.MotionGroup;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.utils.EasingFunction;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponCapability;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.WeaponStats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4d;

import java.util.List;

public class TemporaryMoveTranslator {
    private static final List<MotionFrame> STAB = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, -0.5)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1.5)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1.5))
    );
    private static final List<MotionFrame> PUNCH = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, -0.5),60),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1.5),0)
    );
    private static final List<MotionFrame> CIRCLE = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vector4d(0, 0, 1, 90)),
            new MotionFrame(new Vec3(-1, 0, 0), new Vec3(0, 0, 1), new Vector4d(-1, 0, 0, 90)),
            new MotionFrame(new Vec3(0, 0, -1), new Vec3(0, 0, 1), new Vector4d(0, 0, -1, 90)),
            new MotionFrame(new Vec3(1, 0, 0), new Vec3(0, 0, 1), new Vector4d(1, 0, 0, 90)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vector4d(0, 0, 1, 90)));
    private static final List<MotionFrame> LOOP = List.of(
            //new MotionFrame(new Vec3(0, -1, 0), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(0, 0, -1), new Vec3(0, 0, 1),new Vector4d(0, 0, -1, 180)),
            new MotionFrame(new Vec3(0, 1, 0.1), new Vec3(0, 0, 1),0),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(0, -2, 1), new Vec3(0, 0, 1)));
    private static final List<MotionFrame> SLASH = List.of(
            new MotionFrame(new Vec3(1, 0.6, 1), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(-1, -0.4, 0), new Vec3(0, 0, 1), new Vector4d(-1, -0.4, 1, 45)));
    private static final List<MotionFrame> BACKSLASH = List.of(new MotionFrame(new Vec3(-1, 0.6, 1), new Vec3(0, 0, 1), -45), new MotionFrame(new Vec3(1, -0.4, 0), new Vec3(0, 0, 1), new Vector4d(1, -1, 1, -45)));
    private static final List<MotionFrame> CHOP = List.of(
            new MotionFrame(new Vec3(0, 1, 0.2), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(0, -0.5, 1), new Vec3(0, 0, 1)));
    private static final MotionManager circle_finish = new MotionManagers.DefinitionMM(new MotionGroup(CIRCLE, EasingFunction.IN_OUT_CUBIC, 10));
    private static final MotionManager LINE = new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunction.IN_CUBIC, 10));
    private static final MotionManager CONE1 = new MotionManagers.DefinitionMM(new MotionGroup(SLASH, EasingFunction.IN_OUT_CUBIC, 10));
    private static final MotionManager CONE2 = new MotionManagers.DefinitionMM(new MotionGroup(BACKSLASH, EasingFunction.IN_OUT_CUBIC, 10));
    private static final MotionManager IMPACT = new MotionManagers.DefinitionMM(new MotionGroup(CHOP, EasingFunction.IN_CUBIC, 10));
    private static int flip = 1;

    private static Vec3 generateFrame(float pitch, float yaw) {
        Vec3 base = new Vec3(0, 0, 1);//forward pointing
        return base.xRot(Mth.DEG_TO_RAD * pitch).yRot(Mth.DEG_TO_RAD * yaw).normalize();
    }

    public static void scheduleFinisher(LivingEntity e,
                                        InteractionHand hand,
                                        WeaponStats.SweepInfo base) {
        final WeaponStats.SweepInfo preFinish = base.preFinishCopy();
        final WeaponStats.SweepInfo finish = base.finisherCopy();
        FlyingWeaponData.getCap(e).getWeapon(hand).lock(e);
        switch (base.getType()) {
            case CONE -> {
                //flourish thrice and stab
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, WeaponStats.SWEEPTYPE.CONE, base.getBase() + 3 * base.getScaling()), preFinish, 5, 10);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, WeaponStats.SWEEPTYPE.CONE, base.getBase() + 3 * base.getScaling()), preFinish, 5, 10);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(5, WeaponStats.SWEEPTYPE.CONE, base.getBase() + 3 * base.getScaling()), preFinish, 5, 10);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(15, WeaponStats.SWEEPTYPE.LINE, 3), finish, 7, 20);
            }
            case CLEAVE -> {
                //tcs
                FlyingWeaponData.getCap(e).scheduleAction(hand, new MotionManagers.DefinitionMM(new MotionGroup(LOOP, EasingFunction.IN_CUBIC, 20)), finish, 5, 9);
            }
            case IMPACT -> {
                //spin twice and slam down
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(15, WeaponStats.SWEEPTYPE.CIRCLE, base.getBase() + 3 * base.getScaling()), preFinish, 5, 9);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(15, WeaponStats.SWEEPTYPE.CIRCLE, base.getBase() + 3 * base.getScaling()), preFinish, 5, 9);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(20, WeaponStats.SWEEPTYPE.CLEAVE, 60), finish, 5, 9);
            }
            case CIRCLE -> {
                //beeeeeg circle
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(40, WeaponStats.SWEEPTYPE.CIRCLE, base.getBase() + 3 * base.getScaling()), finish, 8, 9);
            }
            case LINE -> {
                //triple jab followed by big jab
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, WeaponStats.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling()), preFinish, 5, 3);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(5, WeaponStats.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling()), preFinish, 5, 3);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(10, WeaponStats.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling()), finish, 8, 5);
            }
            case NONE -> {
                //flurry of blows
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, WeaponStats.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling()), preFinish, 5, 3);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, WeaponStats.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling()), preFinish, 5, 3);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(3, WeaponStats.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling()), preFinish, 5, 3);
                FlyingWeaponData.getCap(e).scheduleAction(hand, temp_getMMFromType(10, WeaponStats.SWEEPTYPE.LINE, base.getBase() + 3 * base.getScaling()), finish, 8, 5);
            }
        }
    }

    public static MotionManager temp_getMMFromType(int time, WeaponStats.SWEEPTYPE type, double area) {
        //clamp time. The remaining time is expended in recovery.
        //time = Mth.clamp(time, 2, 5);
        flip *= -1;
        final Vec3 midFrame = generateFrame(0, 0);
        switch (type) {
            case CONE:

                final Vec3 startFrame = generateFrame(10, (float) (-area) * flip);
                final Vec3 endFrame = generateFrame(-10, (float) (area) * flip);
                final Vec3 up = new Vec3(0,1,0);
                double dot = Mth.clamp(up.dot(startFrame.subtract(endFrame).normalize()), -1.0, 1.0);
                double angleRadians = -Math.acos(dot);
                double angleDegrees = Math.toDegrees(angleRadians)*flip;
                return new MotionManagers.DefinitionMM(new MotionGroup(List.of(
                        new MotionFrame(startFrame, new Vec3(0, 0, 1), (int) angleDegrees),
                        new MotionFrame(midFrame, new Vec3(0, 0, 1), (int) angleDegrees),
                        new MotionFrame(endFrame, new Vec3(0, 0, 1), (int) angleDegrees)),
                                                                        EasingFunction.IN_CUBIC, time));
            case LINE:
                return new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunction.IN_OUT_CUBIC, time));
            case CIRCLE:
                return new MotionManagers.DefinitionMM(new MotionGroup(CIRCLE, EasingFunction.IN_OUT_CUBIC, time));
            case CLEAVE:
                return new MotionManagers.DefinitionMM(new MotionGroup(List.of(
                        new MotionFrame(generateFrame((float) Math.min(180,area * 2), 5 * flip), new Vec3(0, 0, 1)),
                        new MotionFrame(midFrame, new Vec3(0, 0, 1)),
                        new MotionFrame(generateFrame((float) Math.max(-90, -area / 2), -5 * flip), new Vec3(0, 0, 1))),
                                                                        EasingFunction.IN_CUBIC, time));
            case IMPACT:
                return new MotionManagers.DefinitionMM(new MotionGroup(CHOP, EasingFunction.IN_CUBIC, time));
            default:
                return new MotionManagers.DefinitionMM(new MotionGroup(PUNCH, EasingFunction.IN_OUT_CUBIC, time));
        }
    }
}
