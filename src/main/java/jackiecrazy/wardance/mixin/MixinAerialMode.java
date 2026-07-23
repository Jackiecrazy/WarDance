package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.aerial.ClientAerialHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Entity.class, priority = 9000)
public abstract class MixinAerialMode {
    @Shadow
    public int tickCount;
    @Shadow
    private boolean onGround;
    @Shadow
    private Vec3 deltaMovement;
    @Shadow
    private Level level;

    @Shadow
    protected abstract Vec3 collide(Vec3 p_20273_);

    // Inject at RETURN: After vanilla collide runs, we can modify if needed.
    @Redirect(method = "move", require = 1,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 injectAirStep(Entity instance, Vec3 orig) {
        boolean temp = instance.onGround();
        //temporarily set onground to true for step up
        onGround = true;
        Vec3 collided = collide(orig);
        onGround = temp;
        if (!level.isClientSide()||!(instance instanceof Player)) return collided;
        return ClientAerialHandler.handleCollisions(instance, orig, collided);
    }


}