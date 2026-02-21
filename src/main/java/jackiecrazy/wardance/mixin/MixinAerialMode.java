package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.aerial.AerialHandler;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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