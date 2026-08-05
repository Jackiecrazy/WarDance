package jackiecrazy.wardance.move.actions;

import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.argument.entity.CasterEntityArgument;
import jackiecrazy.footwork.move.argument.entity.TargetEntityArgument;
import jackiecrazy.footwork.move.argument.number.FixedNumberArgument;
import jackiecrazy.footwork.move.condition.Condition;
import jackiecrazy.footwork.move.condition.FalseCondition;
import jackiecrazy.footwork.move.utils.ActionContext;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AerialModeAction extends Action {
    private Argument<Double> duration;//= new FixedNumberArgument(30);
    private Argument<Entity> recipient= CasterEntityArgument.INSTANCE;
    private Condition toggle;

    @Override
    public int perform(ActionContext actionContext) {
        Entity flyer=this.recipient.resolve(actionContext);
        if(duration!=null){
            int ticks = duration.resolve(actionContext).intValue();
            AerialModeData.getCap(flyer).setAerialMode(ticks);
        }
        else if (toggle!=null) {
            Boolean toggle = this.toggle.resolve(actionContext);
            AerialModeData.getCap(flyer).setAerialMode(Boolean.TRUE.equals(toggle));
        }
        return 0;
    }
}
