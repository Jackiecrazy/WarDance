package jackiecrazy.wardance.capability.aerial;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.QiCosts;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.combat.AerialSpiritPacket;
import jackiecrazy.wardance.utils.ReworkConstants;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.compress.archivers.sevenz.CLI;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

public class ClientAerialHandler {
    private static int jumpCount = 0;
    private static boolean jumpKey = false;
    private static Direction lastDir = Direction.DOWN;

    private static boolean collidesWithBlock(Level level, AABB box) {
        return !level.noCollision(box);
    }

    public static Vec3 handleCollisions(Entity self, Vec3 baseVec, Vec3 collided) {
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
            return collided;
        }
        // Airborne check: Minor downward or neutral/ascending vel (tweak thresh for "falling gently").
        Vec3 vel = self.getDeltaMovement();
        //if (vel.y < -0.2) return;  // Too fast fall? Skip to avoid cheese ascents.

        // Get vanilla's base clipped delta.

        // Flags like vanilla: Did horiz clip happen? (Obstacle ahead.)
        boolean horizBlocked = collided.x != baseVec.x || collided.z != baseVec.z;
        if (!horizBlocked) return collided;  // No obstacle? No need to air-step.

        // Prep: Current AABB + entity collisions (reuse vanilla's list logic).
        AABB aabb = self.getBoundingBox();
        List<VoxelShape> collisions = self.level().getEntityCollisions(self, aabb.expandTowards(collided));

        float stepHeight = self.getStepHeight();  // Vanilla attrib (mods boost it auto).

        // Air-Step Logic: Mirror vanilla's step paths, but for air.
        // Path 1: Try horiz at stepHeight altitude.
        Vec3 airStep1 = Entity.collideBoundingBox(self, new Vec3(collided.x, stepHeight, collided.z), aabb, self.level(), collisions);

        // Path 2: Pure up-step first, then horiz from there.
        Vec3 airStep2 = Entity.collideBoundingBox(self, new Vec3(0.0D, stepHeight, 0.0D), aabb.expandTowards(collided.x, 0.0D, collided.z), self.level(), collisions);
        if (airStep2.y < stepHeight) {
            Vec3 airStep3 = self.collideBoundingBox(self, new Vec3(collided.x, 0.0D, collided.z), aabb.move(airStep2), self.level(), collisions).add(airStep2);
            if (airStep3.horizontalDistanceSqr() > airStep1.horizontalDistanceSqr()) {
                airStep1 = airStep3;  // Best horiz progress.
            }
        }

        // If air-step allows more horiz than base...
        if (airStep1.horizontalDistanceSqr() > baseVec.horizontalDistanceSqr()) {
            // Final drop-back like vanilla.
            Vec3 finalDrop = self.collideBoundingBox(self, new Vec3(0.0D, -airStep1.y + collided.y, 0.0D), aabb.move(airStep1), self.level(), collisions);
            Vec3 airVec = airStep1.add(finalDrop);

            // Momentum Preserve: Don't reset vel fully—scale horiz ~90%, minor up-boost.
            // (Call setVelocity in your mod's tick mixin; here just return adjusted delta.)
            // For wall: Check if step was "up-side" via raycast (add below).

            // Drain Spirit per step (e.g., 5 points).
            //self.drainSpirit(5.0F);  // Tweak amount.

            // Set return: Override vanilla with air-stepped delta.
            AerialModeData.getCap(self).setState(IAerialMode.WallState.NONE);
            return airVec;
        } else if (self instanceof LivingEntity e && StylishData.getCap(e).isCombatMode()) {
            final IAerialMode cap = AerialModeData.getCap(e);
            //todo convert velocity that is orthogonal to wall into vertical
            //clamp vector if on wall
            if(cap.getWallDir()!=null){
                //if on axis, moving into face=up, moving same dir=down
                //multiply axis dir and relevant axis velocity then invert into y
                if(cap.getWallDir().getAxis()== Direction.Axis.X){
                    double change=cap.getWallDir().getAxisDirection().getStep()*-1*baseVec.x;
                    baseVec=baseVec.multiply(0,0,1).add(0, change, 0);
                    collided=collided.multiply(0,0,1).add(0, change, 0);
                }
                else if(cap.getWallDir().getAxis()== Direction.Axis.X){
                    double change=cap.getWallDir().getAxisDirection().getStep()*-1*baseVec.x;
                    baseVec=baseVec.multiply(0,0,1).add(0, change, 0);
                    collided=collided.multiply(0,0,1).add(0, change, 0);
                }

            }

            if ((baseVec.x != collided.x || baseVec.z != collided.z)) {
                // Approximate the face we just hit (horizontal only)
                Vec3 blocked = collided.subtract(baseVec).normalize();
                Direction hitFace = Direction.getNearest(blocked.x, 0, blocked.z).getOpposite();
                if (hitFace.getAxis().isHorizontal()) {
                    //if hit from wall jump, cling. If hit from any dodge, slide.
                    if (cap.getState() == IAerialMode.WallState.STICKY) {
                        cap.setState(IAerialMode.WallState.WALL_SLIDE);
                        Vec3 sliding = e.getDeltaMovement();
                        Vec3 normal = Vec3.atLowerCornerOf(hitFace.getNormal()).normalize();
                        Vec3 decelerating = sliding.subtract(normal.scale(sliding.dot(normal))).normalize().scale(sliding.length());
                        e.setDeltaMovement(decelerating);
                    } else if (cap.getState() == IAerialMode.WallState.WALL_JUMP && lastDir != hitFace) {
                        cap.setState(IAerialMode.WallState.CLING);
                        e.setDeltaMovement(Vec3.ZERO);
                    } //else WarDance.LOGGER.debug("slide failed: not sticky");
                    if (cap.getState().wall)
                        cap.setWallDir(hitFace);
                } else if (hitFace == Direction.DOWN)
                    cap.setState(IAerialMode.WallState.NONE);
            }
        }
        return collided;
    }

    @Nullable
    private static BlockHitResult predictNextCollision(Level level,
                                                       @Nullable Entity entity,
                                                       AABB currentAABB,
                                                       Vec3 direction,
                                                       double maxDistance) {
        if (direction.lengthSqr() < 1e-6) return null;
        Vec3 normalized = direction.normalize();

        // Start from entity center or feet for better feet-level detection
        Vec3 start = currentAABB.getCenter().subtract(0, currentAABB.getYsize() / 2, 0); // biased toward feet
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

    public static void handleAirJumps(LocalPlayer pl) {
        Vec3 pos = pl.position();
        Vec3 motion = pl.getDeltaMovement();

        AABB box = new AABB(pos.x, pos.y + (pl.getEyeHeight() * .8), pos.z, pos.x, pos.y + pl.getBbHeight(), pos.z);
        if (!StylishData.getCap(pl).isCombatMode()) return;
        final boolean vanillaJump = pl.onGround() || pl.level().containsAnyLiquid(box) || pl.isPassenger() || pl.getAbilities().flying;
        if (vanillaJump) {
            //no jumping and reset the available multijumps.
            resetMultiJumps(pl);
        } else {
            final IAerialMode cap = AerialModeData.getCap(pl);
            if (pl.input.jumping) {
                if (!jumpKey && jumpCount > 0 && motion.y < 0.333) {
                    pl.jumpFromGround();
                    Direction wall = cap.getWallDir();
                    if (wall != null) {
                        //add some wall velocity
                        Vec3 wallFlip = Vec3.atLowerCornerOf(wall.getNormal()).scale(0.33);
                        //add the player's look vector
                        Vec3 look = pl.getLookAngle();
                        //figure out which axis is correct
                        if (wallFlip.x != 0 && wallFlip.x < 0 != look.x < 0) look = look.multiply(-1, 1, 1);
                        if (wallFlip.z != 0 && wallFlip.z < 0 != look.z < 0) look = look.multiply(1, 1, -1);
                        //fix the y
                        look = look.multiply(1, 0, 1).add(0, 0.6, 0);
                        wallFlip = wallFlip.add(look);
                        pl.addDeltaMovement(wallFlip);
                        lastDir = cap.getWallDir();
                        cap.setState(IAerialMode.WallState.WALL_JUMP);
                    }
                    jumpCount--;

                    pl.resetFallDistance();
                    //send packet
                    CombatChannel.INSTANCE.sendToServer(new AerialSpiritPacket(QiCosts.JUMP));
                }

                jumpKey = true;
            } else {
                if (cap.getState().wall)
                    resetMultiJumps(pl);
                jumpKey = false;
            }
        }
    }

    public static void resetMultiJumps(LocalPlayer pl) {
        jumpCount= (int) pl.getAttributeValue(FootworkAttributes.AIR_JUMPS.get());
    }

    public static void handleWallRuns(Player self, IAerialMode cap) {
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

        if (state == IAerialMode.WallState.WALL_SLIDE) {
            Vec3 normal = Vec3.atLowerCornerOf(wallDir.getNormal()).normalize();
            Vec3 sliding = self.getDeltaMovement();
            double length = sliding.length();//*0.99;
            sliding = sliding.multiply(1, 0.3, 1);
            Vec3 decelerating = sliding.subtract(normal.scale(sliding.dot(normal))).normalize().scale(length);

            // Move along projected look at sprint speed
            self.setDeltaMovement(decelerating);


            // Predict ahead for edges / ceiling / wrap
            BlockHitResult aheadHit = predictNextCollision(self.level(), self, self.getBoundingBox(), normal, 0.8);

            if (aheadHit != null) {
                Direction face = aheadHit.getDirection();
                if (face == Direction.UP) {
                    // Hit top face → stop running
                    cap.setState(IAerialMode.WallState.NONE);
                }
            }
            //self.setOnGround(true);
            if (self.onGround())
                cap.setState(IAerialMode.WallState.NONE);

            //if (!self.level().getBlockCollisions(self, self.getBoundingBox().inflate(0.1).expandTowards(normal)).iterator().hasNext()) {
            if(!isSupportedByWall(self.level(), self, self.getBoundingBox(), cap.getWallDir().getOpposite(), 0.3)){
                cap.setState(IAerialMode.WallState.CLING);
                self.setDeltaMovement(Vec3.ZERO);
                cap.setWallDir(Direction.getNearest(sliding.x, sliding.y, sliding.z).getOpposite());
            }

            if (length < 0.5) {
                cap.setState(IAerialMode.WallState.CLING);
                self.setDeltaMovement(Vec3.ZERO);
            }
        }

        // ─────────────────────────────────────────────
        // spider mode
        // ─────────────────────────────────────────────
        else if (state == IAerialMode.WallState.CLING) {
            Vec3 normal = Vec3.atLowerCornerOf(wallDir.getNormal()).normalize();
            Vec3 input = self.getDeltaMovement();
            Vec3 look = self.getLookAngle();
            Vec3 projLook = input.subtract(normal.scale(input.dot(normal)));
            //todo convert motion to ladder-esque

            // Move along projected look at sprint speed
            self.setDeltaMovement(projLook);  // ~sprint speed
            if (!isSupportedByWall(self.level(), self, self.getBoundingBox(), cap.getWallDir().getOpposite(), 0.3)){//self.level().getBlockCollisions(self, self.getBoundingBox().inflate(0.1).expandTowards(normal)).iterator().hasNext()) {
                cap.setWallDir(Direction.getNearest(projLook.x, projLook.y, projLook.z).getOpposite());
            }

            //self.setOnGround(true);
            if (self.onGround())
                cap.setState(IAerialMode.WallState.NONE);


            // Ceiling cling check (upward expand)

            BlockHitResult upHit = predictNextCollision(self.level(), self, self.getBoundingBox(), new Vec3(0, 1, 0), 2.0);
            if (look.y > 0 && upHit != null && upHit.getDirection() == Direction.DOWN) {
                cap.setState(IAerialMode.WallState.CEILING_CLING);
                self.setDeltaMovement(Vec3.ZERO);
            }
        }

        // ─────────────────────────────────────────────
        // ceiling mode
        // ─────────────────────────────────────────────
        else if (state == IAerialMode.WallState.CEILING_CLING) {
            // Slow free movement on ceiling
            self.setDeltaMovement(self.getDeltaMovement().multiply(1, 0, 1));  // Gentle downward stick

            // Check forward for vertical edge to transition back to cling
            BlockHitResult fwdHit = predictNextCollision(self.level(), self, self.getBoundingBox(), self.getLookAngle().normalize(), 0.8);
            if (fwdHit != null && fwdHit.getDirection().getAxis().isHorizontal()) {
                cap.setState(IAerialMode.WallState.CLING);
                cap.setWallDir(fwdHit.getDirection());
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
        if (cap.getState() != IAerialMode.WallState.NONE) {
            if (self.onGround())
                cap.setState(IAerialMode.WallState.NONE);
            else if (self.isShiftKeyDown()) {
                self.setDeltaMovement(Vec3.ZERO);
                cap.setState(IAerialMode.WallState.NONE);
            }
        }
    }

    /**
     * Checks if the player is still supported by a wall in the expected direction.
     * Does a short ray inward (opposite to wall normal) to see if it hits solid.
     */
    private static boolean isSupportedByWall(Level level,
                                             Player entity,
                                             AABB currentAABB,
                                             Direction expectedWallDir,
                                             double maxInwardDist) {
        Vec3 inwardDir = Vec3.atLowerCornerOf(expectedWallDir.getNormal()).normalize().scale(-1);  // Toward wall
        Vec3 start = currentAABB.getCenter().add(0, -currentAABB.getYsize() * 0.3, 0);  // Feet bias
        Vec3 end = start.add(inwardDir.scale(maxInwardDist));

        Iterable<VoxelShape> collisions = level.getBlockCollisions(entity, currentAABB.expandTowards(inwardDir.scale(maxInwardDist)));

        for (VoxelShape shape : collisions) {
            return true;
        }
        return false;
    }
}
