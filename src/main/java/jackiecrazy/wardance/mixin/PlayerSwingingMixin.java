package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponCapability;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.entity.GrappleEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerSwingingMixin {

    private static final double SPRING_CONSTANT = 0.5;
    private static final double DAMPING_CONSTANT = 1;
    private static final double PUSH_FACTOR = 0.08;
    private static final double RADIAL_CONTROL = 0.33;

    @Inject(method = "travel", at = @At("TAIL"))
    private void onTravel(Vec3 travelVector, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        GrappleEntity ge = FlyingWeaponData.getCap(player).getGrapple();

        // === HOOK HANDLING ===
        if (ge != null && ge.hooked() && !player.level().isClientSide()) {

            Vec3 hookPos = ge.position();
            Vec3 eyePos = player.getEyePosition();
            Vec3 toHook = hookPos.subtract(eyePos);

            double dist = toHook.length();
            double ropeLen = ge.getTetherLength();

            if (dist >= ropeLen * 0.995) {
                Vec3 ropeDir = toHook.normalize();
                Vec3 velocity = player.getDeltaMovement();

                // Remove radial component ONLY
                double radialSpeed = velocity.dot(ropeDir);
                if (radialSpeed > 0) {
                    Vec3 correctedVelocity = velocity.subtract(ropeDir.scale(radialSpeed));
                    player.setDeltaMovement(correctedVelocity);
                }

                // Small inward bias to keep rope taut
                player.setDeltaMovement(
                        player.getDeltaMovement().add(ropeDir.scale(0.02))
                );

                player.resetFallDistance();
            }

        }
    }
}
