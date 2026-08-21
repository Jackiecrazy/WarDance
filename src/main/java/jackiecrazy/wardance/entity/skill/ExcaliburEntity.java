package jackiecrazy.wardance.entity.skill;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.render.RenderItemGroup;
import jackiecrazy.footwork.move.motionframe.render.RenderNode;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ExcaliburEntity extends FlyingWeaponEntity {
    public ExcaliburEntity(EntityType<? extends FlyingItemEntity> type,
                           Level level) {
        super(type, level);
    }

    @Override
    public void queuePath(MotionManager path, int inTick, int outTick) {
        super.queuePath(path, inTick, outTick);
        invalidateWhenDone();
    }

    @Override
    public void remove(RemovalReason reason) {
        if(reason==RemovalReason.DISCARDED)
            invalidateWhenDone();
        else super.remove(reason);
    }


    @Override
    public void setCosmeticItem(ItemStack stack) {
        super.setCosmeticItem(new RenderItemGroup(new RenderNode.ItemNode(stack, Vec3.ZERO, new Vec3(0,0.38,0))));
    }
}
