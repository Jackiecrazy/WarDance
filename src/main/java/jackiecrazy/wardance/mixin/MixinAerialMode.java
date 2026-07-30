package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.aerial.ClientAerialHandler;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
//        boolean horizBlocked = collided.x != orig.x || collided.z != orig.z;
//        if (horizBlocked) {
//            //something stopped the mob from moving, run a custom event for skills
//            CollisionEvent ce = new CollisionEvent((Entity)(Object) this, orig, collided);
//            MinecraftForge.EVENT_BUS.post(ce);
//        }
        if (!level.isClientSide() || !(instance instanceof Player)) return collided;
        return ClientAerialHandler.handleCollisions(instance, orig, collided);
    }

    @SuppressWarnings("all")
    @Inject(method = "fireImmune", at = @At("RETURN"), cancellable = true)
    private void sticky(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && ((Entity) (Object) (this)) instanceof LivingEntity le) {
            Marks.getCap(le).getActiveMark(WarSkills.FLAME_DANCE.get()).ifPresent(a -> {
                if (a.getArbitraryFloat() > 50) cir.setReturnValue(false);
            });
        }
    }

}