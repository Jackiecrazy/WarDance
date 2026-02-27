package jackiecrazy.wardance.capability.aerial;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
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

    @Nullable
    public static BlockHitResult predictNextCollision(Level level,
                                                      @Nullable Entity entity,
                                                      AABB currentAABB,
                                                      Vec3 direction,
                                                      double maxDistance) {
        if (direction.lengthSqr() < 1e-6) return null;
        Vec3 normalized = direction.normalize();

        // Start from entity center or feet for better feet-level detection
        Vec3 start = currentAABB.getCenter().add(0, -currentAABB.getYsize() * 0.4, 0); // biased toward feet
        Vec3 end = start.add(normalized.scale(maxDistance));

        // Get all block collision shapes in the path
        Iterable<VoxelShape> collisions = level.getBlockCollisions(entity, currentAABB.expandTowards(normalized.scale(maxDistance))
                // or filter if you want solid-only
        );

        BlockHitResult closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (VoxelShape shape : collisions) {
            // Clip a short ray against this shape
            BlockHitResult hit = shape.clip(start, end, BlockPos.ZERO); // pos is ignored in most impls
            if (hit == null) continue;
            double distSq = hit.getLocation().distanceToSqr(start);
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = hit;
            }
        }

        if (closest != null && closest.getDirection().getAxis().isHorizontal()) {
            return closest; // Prefer side faces for wall running
        }

        return closest; // or null if only top/bottom
    }

    /**
     * Simpler fallback: Approximate push direction using shape AABB bounds
     * (less accurate but very fast, no ray needed)
     */
    @Nullable
    public static Direction approximateCollisionFace(AABB entityAABB, VoxelShape shape) {
        AABB shapeBounds = shape.bounds(); // Many mappings expose this
        if (shapeBounds == null || shape.isEmpty()) return null;

        Vec3 center = entityAABB.getCenter();

        // Find dominant axis of separation
        double dxMin = shapeBounds.minX - entityAABB.maxX;
        double dxMax = entityAABB.minX - shapeBounds.maxX;
        double dyMin = shapeBounds.minY - entityAABB.maxY;
        double dyMax = entityAABB.minY - shapeBounds.maxY;
        double dzMin = shapeBounds.minZ - entityAABB.maxZ;
        double dzMax = entityAABB.minZ - shapeBounds.maxZ;

        double minPen = Math.min(Math.abs(dxMin), Math.min(Math.abs(dxMax), Math.min(Math.abs(dyMin), Math.min(Math.abs(dyMax), Math.min(Math.abs(dzMin), Math.abs(dzMax))))));

        if (Math.abs(dxMin) <= minPen + 1e-5) return Direction.EAST;
        if (Math.abs(dxMax) <= minPen + 1e-5) return Direction.WEST;
        if (Math.abs(dzMin) <= minPen + 1e-5) return Direction.SOUTH;
        if (Math.abs(dzMax) <= minPen + 1e-5) return Direction.NORTH;
        if (Math.abs(dyMin) <= minPen + 1e-5) return Direction.UP;
        if (Math.abs(dyMax) <= minPen + 1e-5) return Direction.DOWN;

        return null;
    }

    public static void handleWallRuns(Player self, IAerialMode cap) {
        Vec3 vel = self.getDeltaMovement();
        IAerialMode.WallState state = cap.getState();
        Direction wallDir = cap.getWallDir();

        // Common setup: prevent fall damage, drain Qi while active
        if (state != IAerialMode.WallState.NONE) {
            self.fallDistance = 0.0F;
            //drainQi(8);  // Adjust cost per tick
            if (!StylishData.getCap(self).isCombatMode()) {
                cap.setState(IAerialMode.WallState.NONE);
                return;
            }
        }

        if (state == IAerialMode.WallState.NONE) {
            // Trigger check only when dodging toward a wall
            if (!CombatData.getCap(self).isDodging()) return;

            BlockHitResult hit = predictNextCollision(
                    self.level(),
                    self,
                    self.getBoundingBox(),
                    vel.normalize(),  // Use current momentum direction for trigger
                    0.8  // Slightly shorter for "bump into wall" feel
            );

            if (hit != null && hit.getDirection().getAxis().isHorizontal()) {
                cap.setState(IAerialMode.WallState.WALL_RUN);
                cap.setWallDir(hit.getDirection());
                wallDir = hit.getDirection();

                // Project initial velocity onto wall plane
                Vec3 normal = Vec3.atLowerCornerOf(wallDir.getNormal());
                vel = vel.subtract(normal.scale(vel.dot(normal)));
                //self.setPos(hit.getLocation().add(Vec3.atLowerCornerOf(wallDir.getNormal())));
                self.setDeltaMovement(vel);

//                syncStateToServer(cap);
            }
            return;//todo move this to collide()
        }

        // ─────────────────────────────────────────────
        // WALL_RUN state
        // ─────────────────────────────────────────────
        if (state == IAerialMode.WallState.WALL_RUN) {
            Vec3 normal = Vec3.atLowerCornerOf(wallDir.getNormal()).normalize();
            Vec3 look = self.getLookAngle().normalize();
            Vec3 projLook = look.subtract(normal.scale(look.dot(normal)));

            // Move along projected look at sprint speed
            self.setDeltaMovement(projLook.normalize().scale(self.getSpeed()));  // ~sprint speed

            // 1. Check if still supported by the current wall (short inward ray)
            boolean stillOnWall = isSupportedByWall(
                    self.level(),
                    self,
                    self.getBoundingBox(),
                    wallDir,          // expected wall direction
                    1               // max inward distance – tune between 0.2–0.6
            );

            if (!stillOnWall) {
                // Lost wall contact → try orthogonal wrap (left/right) or fall off
                Vec3 perpLeft = new Vec3(-projLook.z, 0, projLook.x).normalize();
                Vec3 perpRight = new Vec3(projLook.z, 0, -projLook.x).normalize();

                BlockHitResult leftHit = predictNextCollision(self.level(), self, self.getBoundingBox(), perpLeft, 1.0);
                if (leftHit != null && leftHit.getDirection().getAxis().isHorizontal()) {
                    cap.setWallDir(leftHit.getDirection());
                    Vec3 newNormal = Vec3.atLowerCornerOf(leftHit.getDirection().getNormal()).normalize();
                    vel = vel.subtract(newNormal.scale(vel.dot(newNormal)));
                    //self.setDeltaMovement(vel.scale(0.9));
                    //syncStateToServer(cap);
                    return;  // Successfully turned
                }

                BlockHitResult rightHit = predictNextCollision(self.level(), self, self.getBoundingBox(), perpRight, 1.0);
                if (rightHit != null && rightHit.getDirection().getAxis().isHorizontal()) {
                    cap.setWallDir(rightHit.getDirection());
                    Vec3 newNormal = Vec3.atLowerCornerOf(rightHit.getDirection().getNormal()).normalize();
                    vel = vel.subtract(newNormal.scale(vel.dot(newNormal)));
                    //self.setDeltaMovement(vel.scale(0.9));
                    //syncStateToServer(cap);
                    return;  // Turned the other way
                }

                // No turn possible → end run, start falling gently
                cap.setState(IAerialMode.WallState.NONE);
                //self.setDeltaMovement(vel.add(0, -0.15, 0));  // Slight downward nudge for smooth transition
                //syncStateToServer(cap);
                return;
            }

            // Predict ahead for edges / ceiling / wrap
            BlockHitResult aheadHit = predictNextCollision(
                    self.level(),
                    self,
                    self.getBoundingBox(),
                    projLook,
                    0.8
            );

            if (aheadHit != null) {
                Direction face = aheadHit.getDirection();

                if (face == Direction.UP) {
                    // Hit top face → stop running
                    cap.setState(IAerialMode.WallState.NONE);
                } else if (face.getAxis().isHorizontal() && face != wallDir) {
                    // Wrap to new wall
                    cap.setWallDir(face);
                    self.setDeltaMovement(vel.scale(0.9));  // Preserve most momentum
                }

                // Small edge step-over (if hit is close and low)
                double hitDist = aheadHit.getLocation().subtract(self.getBoundingBox().getCenter()).length();
                if (hitDist < self.getStepHeight() + 0.2) {
                    //self.setDeltaMovement(vel.add(0, 0.42, 0));  // Small jump boost
                }
            } else {
                //System.out.println("did you hit something?");
            }

            // Ceiling cling check (upward expand)
            BlockHitResult upHit = predictNextCollision(
                    self.level(),
                    self,
                    self.getBoundingBox(),
                    new Vec3(0, 1, 0),
                    2.0
            );
            if (upHit != null && upHit.getDirection() == Direction.DOWN) {
                cap.setState(IAerialMode.WallState.CEILING_CLING);
                self.setDeltaMovement(Vec3.ZERO);
            }
        }

        // ─────────────────────────────────────────────
        // CEILING_CLING state
        // ─────────────────────────────────────────────
        else if (state == IAerialMode.WallState.CEILING_CLING) {
            // Slow free movement on ceiling
            Vec3 move = self.getLookAngle().subtract(0, self.getLookAngle().y, 0).normalize().scale(self.getSpeed());
            self.setDeltaMovement(move);  // Gentle downward stick

            // Check forward for vertical edge to transition back to wall run
            BlockHitResult fwdHit = predictNextCollision(
                    self.level(),
                    self,
                    self.getBoundingBox(),
                    self.getLookAngle().normalize(),
                    1.2
            );
            if (fwdHit != null && fwdHit.getDirection().getAxis().isHorizontal()) {
                cap.setState(IAerialMode.WallState.WALL_RUN);
                cap.setWallDir(fwdHit.getDirection());
            }

            if (self.isShiftKeyDown()) {
                cap.setState(IAerialMode.WallState.NONE);
                //self.setDeltaMovement(self.getDeltaMovement().withY(-0.1));  // Drop
            }
        }

        // ─────────────────────────────────────────────
        // Exit conditions (jump / sneak)
        // ─────────────────────────────────────────────
//        if (self.input.jumping) {
//            cap.setState(IAerialMode.WallState.NONE);
//            Vec3 normal = Vec3.atLowerCornerOf(wallDir.getNormal()).normalize();
//            self.setDeltaMovement(vel.add(normal.scale(0.5)).add(0, 0.42, 0));  // Diagonal leap off
//        } else
        if (self.isShiftKeyDown() && state == IAerialMode.WallState.WALL_RUN) {
            cap.setState(IAerialMode.WallState.CLING);  // Or NONE – your choice
        }
        if (cap.getState() == IAerialMode.WallState.CLING)
            if (!self.isShiftKeyDown())
                cap.setState(IAerialMode.WallState.NONE);  // Release → fall
            else self.setDeltaMovement(Vec3.ZERO);

        //syncStateToServer(cap);
    }

    /**
     * Checks if the player is still supported by a wall in the expected direction.
     * Does a short ray inward (opposite to wall normal) to see if it hits solid.
     */
    private static boolean isSupportedByWall(
            Level level,
            Player entity,
            AABB currentAABB,
            Direction expectedWallDir,
            double maxInwardDist
    ) {
        Vec3 inwardDir = Vec3.atLowerCornerOf(expectedWallDir.getNormal()).normalize().scale(-1);  // Toward wall
        Vec3 start = currentAABB.getCenter().add(0, -currentAABB.getYsize() * 0.3, 0);  // Feet bias
        Vec3 end = start.add(inwardDir.scale(maxInwardDist));

        Iterable<VoxelShape> collisions = level.getBlockCollisions(
                entity,
                currentAABB.expandTowards(inwardDir.scale(maxInwardDist))
        );

        for (VoxelShape shape : collisions) {
            BlockHitResult hit = shape.clip(start, end, BlockPos.ZERO);
            // Accept any solid hit on the expected face (or close enough)
            if (hit != null && (hit.getDirection() == expectedWallDir || hit.getDirection().getOpposite() == expectedWallDir)) {
                return true;
            }
        }
        return false;
    }
}
