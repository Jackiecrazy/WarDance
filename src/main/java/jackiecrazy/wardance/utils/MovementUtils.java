package jackiecrazy.wardance.utils;

import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.capability.ICombatCapability;
import jackiecrazy.wardance.config.CombatConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class MovementUtils {

    public static boolean hasInvFrames(LivingEntity elb) {
        return -(CombatData.getCap(elb).getRollTime()) > CombatConfig.rollEndsAt;
    }

//    /**
//     * Checks the +x, -x, +y, -y, +z, -z, in that order
//     */
//    public static boolean willHitWall(Entity elb) {
//        double allowance = 1;
//        AxisAlignedBB aabb = elb.getEntityBoundingBox();
//        List<AxisAlignedBB> boxes = elb.world.getCollisionBoxes(elb, aabb.expand(elb.motionX, elb.motionY, elb.motionZ));
//        for (AxisAlignedBB a : boxes) {
//            if (aabb.calculateXOffset(a, allowance) != allowance) return true;
//            if (aabb.calculateXOffset(a, -allowance) != -allowance) return true;
//            if (aabb.calculateZOffset(a, allowance) != allowance) return true;
//            if (aabb.calculateZOffset(a, -allowance) != -allowance) return true;
//            if (aabb.calculateYOffset(a, allowance) != allowance) return true;
//        }
//        return false;
//    }
//
//    public static boolean willCollide(Entity elb) {
//        double allowance = 1;
//        //return willHitWall(elb) || collidingEntity(elb) != null;
//        return elb.world.collidesWithAnyBlock(elb.getEntityBoundingBox().expand(elb.motionX * allowance, elb.motionY * allowance, elb.motionZ * allowance));// || collidingEntity(elb) != null;
//    }
//
//    public static boolean willHitWallFrom(Entity elb, Entity from) {
//        double allowance = 1;
//        AxisAlignedBB aabb = elb.getEntityBoundingBox();
//        Vec3d fromToElb = elb.getPositionVector().subtract(from.getPositionVector()).normalize();
//        List<AxisAlignedBB> boxes = elb.world.getCollisionBoxes(elb, aabb.expand(fromToElb.x, fromToElb.y, fromToElb.z));
//        for (AxisAlignedBB a : boxes) {
//            if (aabb.calculateXOffset(a, allowance) != allowance) return true;
//            if (aabb.calculateXOffset(a, -allowance) != -allowance) return true;
//            if (aabb.calculateZOffset(a, allowance) != allowance) return true;
//            if (aabb.calculateZOffset(a, -allowance) != -allowance) return true;
//        }
//        return false;
//    }
//
//    public static boolean[] collisionStatusVelocitySensitive(LivingEntity elb) {
//        double allowance = 1.1;
//        boolean[] ret = {false, false, false, false, false, false};
//        AxisAlignedBB aabb = elb.getEntityBoundingBox();
//        List<AxisAlignedBB> boxes = elb.world.getCollisionBoxes(elb, aabb.expand(elb.motionX, elb.motionY, elb.motionZ));
//        for (AxisAlignedBB a : boxes) {
//            if (aabb.calculateXOffset(a, allowance) != allowance) ret[0] = true;
//            if (aabb.calculateXOffset(a, -allowance) != -allowance) ret[1] = true;
//            if (aabb.calculateYOffset(a, allowance) != allowance) ret[2] = true;
//            if (aabb.calculateYOffset(a, -allowance) != -allowance) ret[3] = true;
//            if (aabb.calculateZOffset(a, allowance) != allowance) ret[4] = true;
//            if (aabb.calculateZOffset(a, -allowance) != -allowance) ret[5] = true;
//        }
//        return ret;
//    }


//    public static boolean attemptJump(LivingEntity elb) {
//        //if you're on the ground, I'll let vanilla handle you
//        if (elb.isOnGround()||elb.isRiding()) return false;
//        ITaoStatCapability itsc = CombatData.getCap(elb);
//        if (!itsc.isInCombatMode()) return false;
//        //qi has to be nonzero
//        if (itsc.getQi() == 0) return false;
//        //mario mario, wherefore art thou mario? Ignores all other jump condition checks
//        Entity ent = collidingEntity(elb);
//        if (ent instanceof LivingEntity) {
//            kick(elb, (LivingEntity) ent);
//        } else {
//            //if you're exhausted or just jumped, you can't jump again
//            if ((itsc.getJumpState() == ITaoStatCapability.JUMPSTATE.EXHAUSTED || itsc.getJumpState() == ITaoStatCapability.JUMPSTATE.JUMPING))
//                return false;
//            if (itsc.getQi() > 3)
//                itsc.setJumpState(ITaoStatCapability.JUMPSTATE.JUMPING);
//            else itsc.setJumpState(ITaoStatCapability.JUMPSTATE.EXHAUSTED);
//        }
//        itsc.setClingDirections(new ITaoStatCapability.ClingData(false, false, false, false));
//        if (elb instanceof EntityPlayer)
//            ((EntityPlayer) elb).jump();
//        else try {
//            jump.invoke(elb);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        double speed = Math.sqrt(NeedyLittleThings.getSpeedSq(elb));
//        if (isTouchingWall(elb)) {
//            boolean[] dir = collisionStatus(elb);
//            Direction face = elb.getHorizontalFacing();
//            boolean facingWall = false;
//            switch (face) {
//                case WEST:
//                    facingWall = dir[0];
//                    break;
//                case EAST:
//                    facingWall = dir[1];
//                    break;
//                case NORTH:
//                    facingWall = dir[4];
//                    break;
//                case SOUTH:
//                    facingWall = dir[5];
//                    break;
//            }
//            //Vec3d look=elb.getLookVec();
//            if (dir[0] && !facingWall) {//east
//                elb.motionX += speed / 2;
//            }
//            if (dir[1] && !facingWall) {//west
//                elb.motionX -= speed / 2;
//            }
//            if (dir[4] && !facingWall) {//south
//                elb.motionZ += speed / 2;
//            }
//            if (dir[5] && !facingWall) {//north
//                elb.motionZ -= speed / 2;
//            }
//            if (!facingWall) elb.motionY /= 2;
//        }
//        elb.velocityChanged = true;
//        TaoCasterData.forceUpdateTrackingClients(elb);
//        return true;
//    }

//    public static boolean isInBulletTime(LivingEntity elb){
//        ITaoStatCapability cap=CombatData.getCap(elb);
//        return cap.getRollCounter()<20+2*cap.getQi();
//    }

    //    /**
//     * Checks the +x, -x, +y, -y, +z, -z, in that order
//     *
//     * @param elb
//     * @return
//     */
//    public static Entity collidingEntity(Entity elb) {
//        AxisAlignedBB aabb = elb.getEntityBoundingBox();
//        List<Entity> entities = elb.world.getEntitiesInAABBexcluding(elb, aabb.expand(elb.motionX, elb.motionY * 6, elb.motionZ), VALID_TARGETS::test);
//        double dist = 0;
//        Entity pick = null;
//        for (Entity e : entities) {
//            if (e.getDistanceSq(elb) < dist || dist == 0) {
//                pick = e;
//                dist = e.getDistanceSq(elb);
//            }
//        }
//        return pick;
//    }
//
//    public static void kick(LivingEntity elb, LivingEntity uke) {
//        if (elb.isRiding()) return;
//        uke.attackEntityFrom(DamageSource.FALLING_BLOCK, 1);
//        CombatData.getCap(uke).consumePosture(5, true, elb);
//        for (int i = 0; i < 10; ++i) {
//            double d0 = Taoism.unirand.nextGaussian() * 0.02D;
//            double d1 = Taoism.unirand.nextGaussian() * 0.02D;
//            double d2 = Taoism.unirand.nextGaussian() * 0.02D;
//            elb.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, uke.posX + (double) (Taoism.unirand.nextFloat() * uke.width * 2.0F) - (double) uke.width, uke.posY + 1.0D + (double) (Taoism.unirand.nextFloat() * uke.height), uke.posZ + (double) (Taoism.unirand.nextFloat() * uke.width * 2.0F) - (double) uke.width, d0, d1, d2);
//        }
//        elb.world.playSound(null, uke.posX, uke.posY, uke.posZ, SoundEvents.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, SoundCategory.PLAYERS, 0.5f + Taoism.unirand.nextFloat() * 0.5f, 0.85f + Taoism.unirand.nextFloat() * 0.3f);
//    }
//
//    public static boolean isTouchingWall(Entity elb) {
//        boolean[] b = collisionStatus(elb);
//        return !elb.onGround && !b[2] && !b[3] && ((b[0] || b[1]) || (b[4] || b[5]));
//    }
//
//    public static boolean[] collisionStatus(Entity elb) {
//        double allowance = 0.1;
//        boolean[] ret = {false, false, false, false, false, false};
//        AxisAlignedBB aabb = elb.getEntityBoundingBox();
//        List<AxisAlignedBB> boxes = elb.world.getCollisionBoxes(elb, aabb.grow(allowance / 2));
//        for (AxisAlignedBB a : boxes) {
//            if (aabb.calculateXOffset(a, allowance) != allowance) ret[0] = true;
//            if (aabb.calculateXOffset(a, -allowance) != -allowance) ret[1] = true;
//            if (aabb.calculateYOffset(a, allowance) != allowance) ret[2] = true;
//            if (aabb.calculateYOffset(a, -allowance) != -allowance) ret[3] = true;
//            if (aabb.calculateZOffset(a, allowance) != allowance) ret[4] = true;
//            if (aabb.calculateZOffset(a, -allowance) != -allowance) ret[5] = true;
//        }
//        return ret;
//    }
    public static boolean attemptSlide(LivingEntity elb) {
        if (!elb.isOnGround()) return false;
        ICombatCapability itsc = CombatData.getCap(elb);
        if (!itsc.isCombatMode()) return false;
        itsc.consumePosture(0);
        itsc.setRollTime(-CombatConfig.rollCooldown);
        if (elb instanceof PlayerEntity) {
            PlayerEntity e = (PlayerEntity) elb;
            e.setForcedPose(Pose.SWIMMING);
        }
        Vector3d v = elb.getLookVec().subtract(0, elb.getLookVec().y, 0).normalize();
        //TODO tweak slide amount to make it fair
        elb.setMotion((1 + itsc.getQi() / 4) * v.x, 0, (1 + itsc.getQi() / 4) * v.z);
        elb.velocityChanged = true;
        return true;
    }

    public static boolean attemptDodge(LivingEntity elb, int side, boolean roll) {
        /*
        stepping around logic:
        known: sidestep distance is 5, distance to mob is x
        acquire angle theta via cosine rule
        use theta to find the angle of other angles
        add said angle to yaw
        twiddle till it works :v
         */
        if (side == 99) attemptSlide(elb);
        ICombatCapability itsc = CombatData.getCap(elb);
        if (!itsc.isCombatMode()) return false;
        if (itsc.getRollTime() == 0) {//
            itsc.setRollTime((roll ? -1 : 1) * CombatConfig.rollCooldown);
//            Entity target = GeneralUtils.raytraceEntity(elb.world, elb, 32);
//            float adjustment = 0;
//            if (target != null) {
//                float distsq = (float) (elb.getDistanceSq(target));
//                float toacos = (distsq + distsq - 36) / (2 * distsq);//magic number wee
//                float acos=(float) Math.acos(toacos);
//                adjustment = GeneralUtils.deg(acos) / 2f;
//            }
            double x = 0, y = 0.3, z = 0;
            switch (side) {
                case 0://left
                    x = MathHelper.cos(GeneralUtils.rad(elb.rotationYaw));
                    z = MathHelper.sin(GeneralUtils.rad(elb.rotationYaw));
                    break;
                case 1://back
                    x = MathHelper.cos(GeneralUtils.rad(elb.rotationYaw - 90));
                    z = MathHelper.sin(GeneralUtils.rad(elb.rotationYaw - 90));
                    break;
                case 2://right
                    x = MathHelper.cos(GeneralUtils.rad(elb.rotationYaw - 180));
                    z = MathHelper.sin(GeneralUtils.rad(elb.rotationYaw - 180));
                    break;
                case 3://forward
                    x = MathHelper.cos(GeneralUtils.rad(elb.rotationYaw + 90));
                    z = MathHelper.sin(GeneralUtils.rad(elb.rotationYaw + 90));
                    break;
            }
            float multiplier = side == 1 ? 1f : 2f;
            x *= 0.6 * multiplier;
            z *= 0.6 * multiplier;

            //NeedyLittleThings.setSize(elb, min, min);
            elb.addVelocity(x, y, z);
            elb.velocityChanged = true;
//            elb.motionX=x;
//            elb.motionY=y;
//            elb.motionZ=z;
            itsc.consumePosture(0);
            if (elb instanceof PlayerEntity && roll) {
                PlayerEntity e = (PlayerEntity) elb;
                e.setForcedPose(Pose.SWIMMING);
            }
            return true;
        }
        return false;
    }
}
