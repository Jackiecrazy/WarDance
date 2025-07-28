package jackiecrazy.wardance.utils;

import jackiecrazy.footwork.move.motionframe.MotionDefinition;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.utils.EasingFunction;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.WeaponStats;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4d;

import java.util.List;

public class TemporaryMoveTranslator {

    private static final List<MotionFrame> STAB = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, -0.5)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1.5))
    );
    private static final List<MotionFrame> CIRCLE = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vector4d(0, 0, 1, 90)),
            new MotionFrame(new Vec3(-1, 0, 0), new Vec3(0, 0, 1), new Vector4d(-1, 0, 0, 90)),
            new MotionFrame(new Vec3(0, 0, -1), new Vec3(0, 0, 1), new Vector4d(0, 0, -1, 90)),
            new MotionFrame(new Vec3(1, 0, 0), new Vec3(0, 0, 1), new Vector4d(1, 0, 0, 90)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vector4d(0, 0, 1, 90)));
    private static final List<MotionFrame> SLASH = List.of(
            new MotionFrame(new Vec3(1, 0.6, 1), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(-1, -0.4, 0), new Vec3(0, 0, 1), new Vector4d(-1, -0.4, 1, 45)));
    private static final List<MotionFrame> BACKSLASH = List.of(new MotionFrame(new Vec3(-1, 0.6, 1), new Vec3(0, 0, 1), -45), new MotionFrame(new Vec3(1, -0.4, 0), new Vec3(0, 0, 1), new Vector4d(1, -1, 1, -45)));
    private static final List<MotionFrame> CHOP = List.of(
            new MotionFrame(new Vec3(0, 1, 0.2), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(0, -0.5, 1), new Vec3(0, 0, 1)));
    private static final MotionManager CIRCLe=new MotionManagers.DefinitionMM(new MotionDefinition(CIRCLE, EasingFunction.IN_OUT_CUBIC, 10));
    private static final MotionManager LINE= new MotionManagers.DefinitionMM(new MotionDefinition(STAB, EasingFunction.IN_CUBIC, 10));
    private static final MotionManager CONE1=  new MotionManagers.DefinitionMM(new MotionDefinition(SLASH, EasingFunction.IN_OUT_CUBIC, 10));
    private static final MotionManager CONE2=  new MotionManagers.DefinitionMM(new MotionDefinition(BACKSLASH, EasingFunction.IN_OUT_CUBIC, 10));
    private static final MotionManager IMPACT=  new MotionManagers.DefinitionMM(new MotionDefinition(CHOP, EasingFunction.IN_CUBIC, 10));

    private static Vec3 generateFrame(float pitch, float yaw){
        Vec3 base = new Vec3(0,0,1);//forward pointing
        return base.xRot(pitch).yRot(yaw).normalize();
    }


    public static MotionManager temp_getMMFromType(int time, WeaponStats.SWEEPTYPE type, double area){
        //take off 3 ticks for the start. Yes, this means it's possible to loop attacks and skip recovery if you time it tight.
        time-=3;
        float flip = WarDance.rand.nextBoolean()?1:-1;
        switch (type){
            case CONE:
                return new MotionManagers.DefinitionMM(new MotionDefinition(List.of(
                        new MotionFrame(generateFrame(-10, (float) (-area)*flip), new Vec3(0, 0, 1)),
                        new MotionFrame(generateFrame(10, (float) (area)*flip), new Vec3(0, 0, 1))),
                                                                            EasingFunction.IN_CUBIC, time));
            case LINE:
                return new MotionManagers.DefinitionMM(new MotionDefinition(STAB, EasingFunction.OUT_CUBIC, time));
            case CIRCLE:
                return new MotionManagers.DefinitionMM(new MotionDefinition(CIRCLE, EasingFunction.IN_OUT_CUBIC, time));
            case CLEAVE:
                return new MotionManagers.DefinitionMM(new MotionDefinition(List.of(
                        new MotionFrame(generateFrame((float) (area/2), 30*flip), new Vec3(0, 0, 1)),
                        new MotionFrame(generateFrame((float) (-area/2), -30*flip), new Vec3(0, 0, 1))),
                                                                            EasingFunction.IN_CUBIC, time));
            case IMPACT:
                new MotionManagers.DefinitionMM(new MotionDefinition(CHOP, EasingFunction.IN_CUBIC, time));
            default:
                return CONE1;
        }
    }
}
