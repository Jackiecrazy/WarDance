package jackiecrazy.wardance.move.actions;

import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.argument.entity.CasterEntityArgument;
import jackiecrazy.footwork.move.argument.entity.TargetEntityArgument;
import jackiecrazy.footwork.move.condition.Condition;
import jackiecrazy.footwork.move.condition.FalseCondition;
import jackiecrazy.footwork.move.utils.ActionContext;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class KickAction extends Action {
    private Argument<Entity> kicker= CasterEntityArgument.INSTANCE;
    private Argument<Entity> recipient= TargetEntityArgument.INSTANCE;
    private Condition breach = FalseCondition.INSTANCE;

    @Override
    public int perform(ActionContext actionContext) {
        Entity kicker=this.kicker.resolve(actionContext);
        Entity kickee=this.recipient.resolve(actionContext);
        Boolean breach = this.breach.resolve(actionContext);
        if(!(kicker instanceof LivingEntity e)||kickee==null||breach==null)return 0;
        CombatUtils.kick(e, kickee, breach);
        return 0;
    }
}
