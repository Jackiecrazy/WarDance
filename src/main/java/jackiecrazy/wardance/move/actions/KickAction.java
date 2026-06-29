package jackiecrazy.wardance.move.actions;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.argument.entity.CasterEntityArgument;
import jackiecrazy.footwork.move.argument.entity.TargetEntityArgument;
import jackiecrazy.footwork.move.argument.stack.EquippedItemArgument;
import jackiecrazy.footwork.move.condition.Condition;
import jackiecrazy.footwork.move.condition.FalseCondition;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.move.utils.ActionContext;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.entity.GhostBlockEntity;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

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
