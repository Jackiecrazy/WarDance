package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.capability.stylish.StylishData;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class AttackCanceler {
    @Inject(method = "attack", at = @At(value = "HEAD"), cancellable = true)
    private void combatModeOverride(Player p, Entity e, CallbackInfo ci) {
        if(StylishData.getCap(p).isCombatMode()){
            //cancel directly attacking in combat mode
            ci.cancel();
        }
    }
}
