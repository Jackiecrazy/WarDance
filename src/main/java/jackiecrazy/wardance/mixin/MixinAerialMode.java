package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.capability.aerial.ClientAerialHandler;
import jackiecrazy.wardance.capability.aerial.IAerialMode;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.skill.ProcPoints;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = Entity.class, priority = 9000)
public abstract class MixinAerialMode {
    @Shadow
    public int tickCount;
    @Shadow
    private boolean onGround;
    @Shadow
    private Vec3 deltaMovement;

    @Shadow
    protected abstract Vec3 collide(Vec3 p_20273_);

    // Inject at RETURN: After vanilla collide runs, we can modify if needed.
    @Redirect(method = "move", require = 1,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 injectAirStep(Entity instance, Vec3 orig) {
        boolean temp = instance.onGround();
        //temporarily set onground to true for step up
        instance.setOnGround(true);
        Vec3 collided = collide(orig);
        instance.setOnGround(temp);
        return ClientAerialHandler.handleCollisions(instance, orig, collided);
    }


    /**
     * @author
     * @reason I HATE EVERYTHING
     */
//    @Overwrite
//    public void setDeltaMovement(Vec3 vec) {
//        IAerialMode cap = AerialModeData.getCap((Entity) (Object) this);
//        if (cap.getWallDir() != null) {
//            double max = 0.2;
//            if (cap.getWallDir().getAxis() == Direction.Axis.X) {
//                double change = Mth.clamp(vec.y+cap.getWallDir().getAxisDirection().getStep() * vec.x, -max, max);
//                vec = vec.multiply(0, 0, 1).add(0, change, 0);
//            } else if (cap.getWallDir().getAxis() == Direction.Axis.Z) {
//                double change = Mth.clamp(vec.y+cap.getWallDir().getAxisDirection().getStep() * vec.z, -max, max);
//                vec = vec.multiply(1, 0, 0).add(0, change, 0);
//            }
//        }
//        deltaMovement = vec;
//    }
}