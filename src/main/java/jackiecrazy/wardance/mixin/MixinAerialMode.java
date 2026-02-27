package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.aerial.AerialHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinAerialMode {

    // Inject at RETURN: After vanilla collide runs, we can modify if needed.
    @Inject(method = "collide",
            at = @At("RETURN"), cancellable = true)
    private void injectAirStep(Vec3 rawDelta, CallbackInfoReturnable<Vec3> cir) {
        Entity self = (Entity) (Object) this;  // Cast to access instance.
        AerialHandler.handleAerials(self, rawDelta, cir);
    }
}