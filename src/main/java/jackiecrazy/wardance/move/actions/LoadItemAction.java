package jackiecrazy.wardance.move.actions;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.move.utils.ActionContext;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.entity.GhostBlockEntity;
import jackiecrazy.wardance.entity.WarEntities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LoadItemAction extends Action {
    private List<Action> on_impact = List.of();
    private Argument<ItemStack> stack;
    private MotionManager pose = new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 0)), 5);

    @Override
    public int perform(ActionContext actionContext) {
        if (actionContext.performer() instanceof LivingEntity le) {
            ItemStack picked = stack.resolve(actionContext);
            GhostBlockEntity fwe = new GhostBlockEntity(WarEntities.FLYING_BLOCK.get(), le.level());
            fwe.setHeldItem(picked);
            //level().destroyBlock(hookedHit.getBlockPos(), false, p);
            Vec3 pos = le.position();
            fwe.setOwner(le);
            fwe.setPosRaw(pos.x, pos.y, pos.z);
            fwe.setInteractionRange(1);
            fwe.setState(FlyingItemEntity.STATE.THROW_NATURAL);
            fwe.setIntangible(true);
            fwe.setIdlePose(pose);
            le.level().addFreshEntity(fwe);
            FlyingWeaponData.getCap(le).setHeldBlock(fwe);
        }
        return 0;
    }
}
