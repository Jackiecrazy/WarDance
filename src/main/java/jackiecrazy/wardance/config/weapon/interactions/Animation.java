package jackiecrazy.wardance.config.weapon.interactions;

import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.wardance.utils.TemporaryMoveTranslator;

import java.util.List;

public class Animation extends WeaponInteractions.WeaponInteraction {
    public static final Animation CIRCLE = new Animation().setAction(TemporaryMoveTranslator.temp_getMMFromType(10, SweepAttack.SWEEPTYPE.CIRCLE, 3, HitInfo.BREACH, 5));
    public static final Animation FLURRY_BREACH = new Animation().setAction(
            TemporaryMoveTranslator.temp_getMMFromType(3, SweepAttack.SWEEPTYPE.LINE, 3, HitInfo.THROWN, 3),
            TemporaryMoveTranslator.temp_getMMFromType(3, SweepAttack.SWEEPTYPE.LINE, 3, HitInfo.THROWN, 3),
            TemporaryMoveTranslator.temp_getMMFromType(3, SweepAttack.SWEEPTYPE.LINE, 3, HitInfo.THROWN, 3),
            TemporaryMoveTranslator.temp_getMMFromType(10, SweepAttack.SWEEPTYPE.LINE, 5, HitInfo.BREACH, 5)
    );
    public static final WeaponInteractions.WeaponInteraction FLURRY = new Animation().setAction(
            TemporaryMoveTranslator.temp_getMMFromType(3, SweepAttack.SWEEPTYPE.LINE, 3, HitInfo.THROWN, 3),
            TemporaryMoveTranslator.temp_getMMFromType(3, SweepAttack.SWEEPTYPE.LINE, 3, HitInfo.THROWN, 3),
            TemporaryMoveTranslator.temp_getMMFromType(3, SweepAttack.SWEEPTYPE.LINE, 3, HitInfo.THROWN, 3)
            ).addOverride(new WeaponInteractions.InteractionOverride(WeaponInteractions.BREACH_CONDITION, FLURRY_BREACH));

    public List<MotionManager> getActions() {
        return actions;
    }

    List<MotionManager> actions = null;

    public Animation() {
        super();
    }

    @Override
    public TYPE getInteractionType() {
        return TYPE.ANIMATE;
    }

    public Animation setActions(List<MotionManager> actions) {
        this.actions = actions;
        return this;
    }

    public Animation setAction(MotionManager... action){
        this.actions=List.of(action);
        return this;
    }

    @Override
    public WeaponInteractions.WeaponInteraction clone() {
        Animation clone=new Animation();
        clone.actions=this.actions;
        return clone;
    }
}
