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

public class AsuraWeaponEntity extends FlyingWeaponEntity {
    private int internalCooldown=10;
    public AsuraWeaponEntity(EntityType<? extends FlyingItemEntity> type,
                             Level level) {
        super(type, level);
    }

    @Override
    public void setCosmeticItem(RenderItemGroup stack) {
        List<RenderNode> copy =new ArrayList<>();
        boolean hasHand=false;
        for(int x=0;x<stack.nodes().length;x++){
            if(stack.nodes()[x] instanceof RenderNode.ItemNode in && in.stack().isEmpty());
            else copy.add(stack.nodes()[x]);
        }
       copy.add(new RenderNode.ItemNode(ItemStack.EMPTY, Vec3.ZERO, new Vec3(0,-1,0.1)));
        final RenderNode[] array = copy.toArray(new RenderNode[0]);
        super.setCosmeticItem(new RenderItemGroup(array));
//        if(!getHeldItem().isEmpty())
//        setCosmeticItem(new RenderItemGroup(new RenderNode.ItemNode(stack, Vec3.ZERO,Vec3.ZERO), new RenderNode.ItemNode(ItemStack.EMPTY, Vec3.ZERO, new Vec3(0,-1.3,0))));
//        else setCosmeticItem(new RenderItemGroup(new RenderNode.ItemNode(ItemStack.EMPTY, Vec3.ZERO, new Vec3(0,-1.3,0))));
    }

    @Override
    public void tick() {
        internalCooldown--;
        super.tick();
    }

    @Override
    public void queuePath(MotionManager path, int inTick, int outTick) {
        if(internalCooldown>0)return;
        super.queuePath(path, inTick, outTick);
    }
}
