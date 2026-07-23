package jackiecrazy.wardance.capability.aerial;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.movement.AerialModePacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

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
        if (self.onGround() || self.getStepHeight() <= 0.0F) {
            return collided;
        }

        if (self instanceof Player e && StylishData.getCap(e).isCombatMode()) {
            final IAerialMode cap = AerialModeData.getCap(e);
            //todo convert velocity that is orthogonal to wall into vertical
            //clamp vector if on wall
            if (cap.getWallDir() != null && cap.getState() == IAerialMode.WallState.CLING) {
                Vec3 movement = self.getDeltaMovement();
                Direction firstFace = cap.getWallDir();
                Direction secondFace = firstFace;
                double max = 0.4;
                if (!isSupportedByWall(self.level(), e, self.getBoundingBox(), cap.getWallDir().getOpposite(), 0.3)) {//self.level().getBlockCollisions(self, self.getBoundingBox().inflate(0.1).expandTowards(normal)).iterator().hasNext()) {
                    //turning point on the wall.
                    cap.setWallDir(Direction.getNearest(movement.x, 0, movement.z).getOpposite());
                    secondFace = cap.getWallDir();
                }
                if (firstFace != null) {
                    if (firstFace != secondFace) {
                        //turning point, lock movement to the two faces
                        Vec3 firstDir = Vec3.atLowerCornerOf(firstFace.getNormal()).normalize();
                        Vec3 secondDir = Vec3.atLowerCornerOf(secondFace.getNormal()).normalize();
                        Vec3 test = firstDir.add(secondDir);
                        Vec3 testMove = movement.multiply(test.x, test.y, test.z);
                        if (testMove.x < 0) {
                            baseVec = baseVec.multiply(0, 1, 1);
                            collided = collided.multiply(0, 1, 1);
                            movement = movement.multiply(0, 1, 1);//moving outwards
                        }
                        if (testMove.z < 0) {
                            baseVec = baseVec.multiply(1, 1, 0);
                            collided = collided.multiply(1, 1, 0);
                            movement = movement.multiply(1, 1, 0);//moving outwards
                        }
                    } else if (cap.getWallDir().getAxis() == Direction.Axis.X) {
                        double change = Mth.clamp((movement.y + cap.getWallDir().getAxisDirection().getStep() * movement.x) * 0.9, -max, max);
                        baseVec = baseVec.multiply(0, 1, 1);
                        collided = collided.multiply(0, 1, 1);
                        movement = (movement.multiply(0, 0, 1).add(0, change, 0));
                    } else if (cap.getWallDir().getAxis() == Direction.Axis.Z) {
                        double change = Mth.clamp((movement.y + cap.getWallDir().getAxisDirection().getStep() * movement.z) * 0.9, -max, max);
                        baseVec = baseVec.multiply(1, 1, 0);
                        collided = collided.multiply(1, 1, 0);
                        movement = (movement.multiply(1, 0, 0).add(0, change, 0));
                    }
                    e.setDeltaMovement(movement);
                }
            }
        }

        boolean horizBlocked = collided.x != baseVec.x || collided.z != baseVec.z;
        if (!horizBlocked) return collided;  // No obstacle? No need to air-step.

        // Prep: Current AABB + entity collisions (reuse vanilla's list logic).
        AABB aabb = self.getBoundingBox();
        List<VoxelShape> collisions = self.level().getEntityCollisions(self, aabb.expandTowards(collided));

        float stepHeight = self.getStepHeight();  // Vanilla attrib (mods boost it auto).

        // Air-Step Logic: Mirror vanilla's step paths, but for air.
        // Path 1: Try horiz at stepHeight altitude.
        //todo this is expensive try to save
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

            AerialModeData.getCap(self).setState(IAerialMode.WallState.NONE);
            //return the air step
            return airVec;
        } else if (self instanceof LivingEntity e && StylishData.getCap(e).isCombatMode()) {
            //try to wall run/cling when hitting a wall
            final IAerialMode cap = AerialModeData.getCap(e);

            if ((baseVec.x != collided.x || baseVec.z != collided.z)) {
                IAerialMode.WallState state;
                // Approximate the face we just hit (horizontal only)
                Vec3 blocked = collided.subtract(baseVec).normalize();
                Direction hitFace = Direction.getNearest(blocked.x, 0, blocked.z).getOpposite();
                if (hitFace.getAxis().isHorizontal()) {
                    Vec3 normal = Vec3.atLowerCornerOf(hitFace.getNormal()).normalize();
                    Vec3 velocity = e.getDeltaMovement();
                    IAerialMode.WallState newState = null;
                    //if hit from wall jump, cling. If hit from any dodge,
                    //if sideways velocity is greater than inwards velocity wall slide, otherwise cling
                    double intoWallSpeed = velocity.dot(normal);  // negative dot = moving into wall
                    Vec3 parallel = velocity.subtract(normal.scale(velocity.dot(normal)));
                    double parallelSpeed = parallel.length();

                    // Decide state based on which is stronger
                    if (cap.getState() == IAerialMode.WallState.WALL_JUMP && lastDir != hitFace) {
                        newState = IAerialMode.WallState.CLING;
                        e.setDeltaMovement(Vec3.ZERO);
                    } else if (cap.getState() == IAerialMode.WallState.STICKY) {
                        if (intoWallSpeed > parallelSpeed) {
                            // Dominant into-wall → CLING (stick hard, stop movement)
                            newState = IAerialMode.WallState.CLING;
                            e.setDeltaMovement(Vec3.ZERO);
                        } else {
                            newState = IAerialMode.WallState.WALL_SLIDE;
                            e.setDeltaMovement(parallel.normalize().scale(velocity.length()));
                        }
                    }

                    // Apply state + direction if we decided to cling/slide
                    if (newState != null && newState.wall) {
                        cap.setState(newState);
                    }
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
        final boolean vanillaJump = pl.onGround() || pl.level().containsAnyLiquid(box) || pl.isPassenger() || pl.getAbilities().flying;
        if (vanillaJump) {
            //no jumping and reset the available multijumps.
            resetMultiJumps(pl);
            if (AerialModeData.getCap(pl).isAerialMode()) {
                AerialModeData.getCap(pl).setAerialMode(false);
                CombatChannel.INSTANCE.sendToServer(new AerialModePacket(false));
            }
        } else {
            final IAerialMode cap = AerialModeData.getCap(pl);
            if (pl.input.jumping) {
                if (!jumpKey && jumpCount > 0 && motion.y < 0.333) {
                    pl.jumpFromGround();
                    jumpCount--;
                    if (!StylishData.getCap(pl).isCombatMode()) return;
                    Direction wall = cap.getWallDir();
                    if (wall != null) {
                        //add some wall velocity
                        Vec3 wallFlip = Vec3.atLowerCornerOf(wall.getOpposite().getNormal()).scale(1);
                        //add the player's look vector
                        Vec3 look = pl.getLookAngle();
                        //figure out which axis is correct
//                        if (wallFlip.x != 0 && wallFlip.x < 0 != look.x < 0) look = look.multiply(-1, 1, 1);
//                        if (wallFlip.z != 0 && wallFlip.z < 0 != look.z < 0) look = look.multiply(1, 1, -1);
//                        //fix the y
//                        look = look.multiply(1, 0, 1).add(0, 0.4, 0);
//                        wallFlip = wallFlip.add(look);
                        pl.addDeltaMovement(wallFlip.add(0, 0.3, 0));
                        lastDir = cap.getWallDir();
                        cap.setState(IAerialMode.WallState.WALL_JUMP);
                    }
                    AerialModeData.getCap(pl).setAerialMode(true);
                    CombatChannel.INSTANCE.sendToServer(new AerialModePacket(true));

                    pl.resetFallDistance();
                }

                jumpKey = true;
            } else {
                if (cap.getState().wall)
                    resetMultiJumps(pl);
                jumpKey = false;
            }
        }
    }

    public static double getJumpPerc(LocalPlayer pl) {
        return jumpCount / pl.getAttributeValue(WarAttributes.AIR_JUMPS.get());
    }

    public static void resetMultiJumps(LocalPlayer pl) {
        jumpCount = (int) pl.getAttributeValue(WarAttributes.AIR_JUMPS.get());
    }

    public static void handleWallRuns(Player self, IAerialMode cap) {
        IAerialMode.WallState state = cap.getState();
        Direction wallDir = cap.getWallDir();

        if (state != IAerialMode.WallState.NONE) {
            self.fallDistance = 0.0F;
            //drainQi(8);  // Adjust cost per tick
            if (!StylishData.getCap(self).isCombatMode()) {
                cap.setState(IAerialMode.WallState.NONE);
                return;
            }
        }

        switch (state) {
            case WALL_SLIDE -> {
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
                if (cap.getWallDir() != null && !isSupportedByWall(self.level(), self, self.getBoundingBox(), cap.getWallDir().getOpposite(), 0.3)) {
                    cap.setState(IAerialMode.WallState.CLING);
                    self.setDeltaMovement(Vec3.ZERO);
                    cap.setWallDir(Direction.getNearest(sliding.x, sliding.y, sliding.z).getOpposite());
                }

                if (length < 0.4) {
                    cap.setState(IAerialMode.WallState.CLING);
                    self.setDeltaMovement(Vec3.ZERO);
                }
            }

            // ─────────────────────────────────────────────
            // spider mode
            // ─────────────────────────────────────────────
            case CLING -> {
                Vec3 move = self.getDeltaMovement();

                //self.setOnGround(true);
                if (self.onGround())
                    cap.setState(IAerialMode.WallState.NONE);


                // Ceiling cling check (upward expand)

                BlockHitResult upHit = predictNextCollision(self.level(), self, self.getBoundingBox(), new Vec3(0, 1, 0), 2.0);
                if (move.y > 0 && upHit != null && upHit.getDirection() == Direction.DOWN) {
                    cap.setState(IAerialMode.WallState.CEILING_CLING);
                    self.setDeltaMovement(Vec3.ZERO);
                }
            }

            // ─────────────────────────────────────────────
            // ceiling mode
            // ─────────────────────────────────────────────
            case CEILING_CLING -> {
                // Slow free movement on ceiling
                self.setDeltaMovement(self.getDeltaMovement().multiply(1, 0, 1));  // Gentle downward stick

                // Check forward for vertical edge to transition back to cling
                BlockHitResult fwdHit = predictNextCollision(self.level(), self, self.getBoundingBox(), self.getLookAngle().normalize(), 0.8);
                if (fwdHit != null && fwdHit.getDirection().getAxis().isHorizontal()) {
                    cap.setState(IAerialMode.WallState.CLING);
                    cap.setWallDir(fwdHit.getDirection());
                }
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
            else if (cap.getState().wall && self.isShiftKeyDown()) {
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
