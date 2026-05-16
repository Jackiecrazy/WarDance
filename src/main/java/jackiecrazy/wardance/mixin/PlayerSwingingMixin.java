package jackiecrazy.wardance.mixin;


import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.entity.GrappleEntity;
import jackiecrazy.wardance.utils.MobilityUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Player.class)
public class PlayerSwingingMixin {

    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravel(Vec3 travelVector, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        GrappleEntity hookEntity= FlyingWeaponData.getCap(player).getGrapple();

        // --- HOOK HANDLING ---
        if (hookEntity != null && hookEntity.swinging()) {
            MobilityUtils.swingin(hookEntity, player);
        }
    }

}