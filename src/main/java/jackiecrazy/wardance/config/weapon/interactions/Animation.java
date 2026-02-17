package jackiecrazy.wardance.config.weapon.interactions;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.utils.TemporaryMoveTranslator;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Animation extends WeaponInteractions.WeaponInteraction {
    public static final Animation CIRCLE = new Animation().setAction(TemporaryMoveTranslator.temp_getMMFromType(10, SweepAttack.SWEEPTYPE.CIRCLE, 3, HitInfo.BREACH, 5));
    private static final List<MotionFrame> STAB = List.of(
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, -0.5)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1.5)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1.5))
    );
    public static final Animation FLURRY_BREACH = new Animation().setAction(
            new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunctionEnum.OUT_CUBIC, 3, new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.BIG_SHADOW).setHit(HitInfo.THROWN))),
            new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunctionEnum.OUT_CUBIC, 3, new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.BIG_SHADOW).setHit(HitInfo.THROWN))),
            new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunctionEnum.OUT_CUBIC, 3, new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.BIG_SHADOW).setHit(HitInfo.THROWN))),
            new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunctionEnum.OUT_CUBIC, 10, new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.BIG_SHADOW).setHit(HitInfo.BREACH)))
    );
    public static final WeaponInteractions.WeaponInteraction FLURRY = new Animation().setAction(
            new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunctionEnum.OUT_CUBIC, 3, new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL).setHit(HitInfo.THROWN))),
            new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunctionEnum.OUT_CUBIC, 3, new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL).setHit(HitInfo.THROWN))),
            new MotionManagers.DefinitionMM(new MotionGroup(STAB, EasingFunctionEnum.OUT_CUBIC, 3, new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL).setHit(HitInfo.THROWN)))
            ).addOverride(new WeaponInteractions.InteractionOverride(WeaponInteractions.BREACH_CONDITION, FLURRY_BREACH));

    public List<MotionManager> getAnimations() {
        return animations;
    }

    List<MotionManager> animations = null;

    public Animation() {
        super();
    }

    @Override
    public TYPE getInteractionType() {
        return TYPE.ANIMATE;
    }

    public Animation setAnimations(List<MotionManager> animations) {
        this.animations = animations;
        return this;
    }

    public Animation setAction(MotionManager... action){
        this.animations =List.of(action);
        return this;
    }

    @Override
    public WeaponInteractions.WeaponInteraction clone() {
        Animation clone=new Animation();
        clone.animations =this.animations;
        return clone;
    }
}
