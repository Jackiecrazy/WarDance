package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.capability.resources.CombatData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class MixinBlockFrameDisable {
    @Inject(method = "broadcastEntityEvent", at = @At("HEAD"))
    private void slow(Entity ent, byte by, CallbackInfo ci) {
        if (ent instanceof LivingEntity elb && by == 30 && !(ent instanceof Player)) {
            CombatData.getCap(elb).tickProc("cannot_block", 40);
        }
    }
}