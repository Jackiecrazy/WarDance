package jackiecrazy.wardance.capability.aerial;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public class AerialHandler {
    private static boolean collidesWithBlock(Level level, AABB box) {
        return !level.noCollision(box);
    }

    public static void handleAerials(Entity self, Vec3 rawDelta, CallbackInfoReturnable<Vec3> cir) {
//        if (pl.horizontalCollision && pl.getDeltaMovement().y > -0.2) {
//            if (collidesWithBlock(pl.level(), pl.getBoundingBox().inflate(0.01, -pl.maxUpStep() + 0.02, 0.01))) {
//                pl.setOnGround(true);
//            }
//        }
//
//        if (pl.isSprinting() && pl.getDeltaMovement().length() > 0.08)
//            pl.horizontalCollision = false;
        // Early exits: Skip if grounded (vanilla handles), no step height, or no Spirit.
        if (self.onGround() || self.getStepHeight() <= 0.0F) {
            return;
        }

        // Airborne check: Minor downward or neutral/ascending vel (tweak thresh for "falling gently").
        Vec3 vel = self.getDeltaMovement();
        if (vel.y < -0.2) return;  // Too fast fall? Skip to avoid cheese ascents.

        // Get vanilla's base clipped delta.
        Vec3 baseVec = cir.getReturnValue();

        // Flags like vanilla: Did horiz clip happen? (Obstacle ahead.)
        boolean horizBlocked = rawDelta.x != baseVec.x || rawDelta.z != baseVec.z;
        if (!horizBlocked) return;  // No obstacle? No need to air-step.

        // Prep: Current AABB + entity collisions (reuse vanilla's list logic).
        AABB aabb = self.getBoundingBox();
        List<VoxelShape> collisions = self.level().getEntityCollisions(self, aabb.expandTowards(rawDelta));

        float stepHeight = self.getStepHeight();  // Vanilla attrib (mods boost it auto).

        // Air-Step Logic: Mirror vanilla's step paths, but for air.
        // Path 1: Try horiz at stepHeight altitude.
        Vec3 airStep1 = Entity.collideBoundingBox(self, new Vec3(rawDelta.x, stepHeight, rawDelta.z), aabb, self.level(), collisions);

        // Path 2: Pure up-step first, then horiz from there.
        Vec3 airStep2 = Entity.collideBoundingBox(self, new Vec3(0.0D, stepHeight, 0.0D), aabb.expandTowards(rawDelta.x, 0.0D, rawDelta.z), self.level(), collisions);
        if (airStep2.y < stepHeight) {
            Vec3 airStep3 = self.collideBoundingBox(self, new Vec3(rawDelta.x, 0.0D, rawDelta.z), aabb.move(airStep2), self.level(), collisions).add(airStep2);
            if (airStep3.horizontalDistanceSqr() > airStep1.horizontalDistanceSqr()) {
                airStep1 = airStep3;  // Best horiz progress.
            }
        }

        // If air-step allows more horiz than base...
        if (airStep1.horizontalDistanceSqr() > baseVec.horizontalDistanceSqr()) {
            // Final drop-back like vanilla.
            Vec3 finalDrop = self.collideBoundingBox(self, new Vec3(0.0D, -airStep1.y + rawDelta.y, 0.0D), aabb.move(airStep1), self.level(), collisions);
            Vec3 airVec = airStep1.add(finalDrop);

            // Momentum Preserve: Don't reset vel fully—scale horiz ~90%, minor up-boost.
            // (Call setVelocity in your mod's tick mixin; here just return adjusted delta.)
            // For wall: Check if step was "up-side" via raycast (add below).

            // Drain Spirit per step (e.g., 5 points).
            //self.drainSpirit(5.0F);  // Tweak amount.

            // Set return: Override vanilla with air-stepped delta.
            cir.setReturnValue(airVec);
        }
    }
}
