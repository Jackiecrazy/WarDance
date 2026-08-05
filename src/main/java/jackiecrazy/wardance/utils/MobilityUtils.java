package jackiecrazy.wardance.utils;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.event.DodgeEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.MovementUtils;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.entity.GrappleEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.function.Predicate;

public class MobilityUtils {

    /**
     * Checks the +x, -x, +y, -y, +z, -z, in that order
     */
//    public static boolean willHitWall(Entity elb) {
//        double allowance = 1;
//        AABB aabb = elb.getBoundingBox();
//        Iterable<VoxelShape> boxes = elb.level().getBlockCollisions(elb, aabb.expandTowards(elb.getDeltaMovement()));
//        for (VoxelShape a : boxes) {
//            boxes.
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
//        AABB aabb = elb.getEntityBoundingBox();
//        Vec3d fromToElb = elb.getPositionVector().subtract(from.getPositionVector()).normalize();
//        List<AABB> boxes = elb.world.getCollisionBoxes(elb, aabb.expand(fromToElb.x, fromToElb.y, fromToElb.z));
//        for (AABB a : boxes) {
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
//        AABB aabb = elb.getEntityBoundingBox();
//        List<AABB> boxes = elb.world.getCollisionBoxes(elb, aabb.expand(elb.motionX, elb.motionY, elb.motionZ));
//        for (AABB a : boxes) {
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
//        if (elb.onGround()||elb.isRiding()) return false;
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

    /**
     * Checks the +x, -x, +y, -y, +z, -z, in that order
     *
     * @param elb
     * @return
     */
    public static Entity collidingEntity(Entity elb, Predicate<Entity> predicate) {
        AABB aabb = elb.getBoundingBox();
        Vec3 vec = elb.getDeltaMovement();
        List<Entity> entities = elb.level().getEntities(elb, aabb.inflate(vec.x * 4, vec.y * 6, vec.z * 4), predicate);
        double dist = 0;
        Entity pick = null;
        for (Entity e : entities) {
            if (e.distanceToSqr(elb) < dist || dist == 0) {
                pick = e;
                dist = e.distanceToSqr(elb);
            }
        }
        return pick;
    }

    public static Entity collidingEntity(Entity elb) {
        return collidingEntity(elb, Entity::isAlive);
    }

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
//        AABB aabb = elb.getEntityBoundingBox();
//        List<AABB> boxes = elb.world.getCollisionBoxes(elb, aabb.grow(allowance / 2));
//        for (AABB a : boxes) {
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
        ICombatCapability itsc = CombatData.getCap(elb);
        if (!StylishData.getCap(elb).isCombatMode()) return false;
        DodgeEvent e = new DodgeEvent(elb, DodgeEvent.Direction.FORWARD, 1.5);
        MinecraftForge.EVENT_BUS.post(e);
        if (e.isCanceled()) return false;
        Vec3 v = elb.getLookAngle().subtract(0, elb.getLookAngle().y, 0).normalize().scale(e.getForce());
        itsc.consumePosture(0);
        itsc.setDodgeTime((int) (CombatConfig.rollTime*elb.getAttributeValue(WarAttributes.DODGE_EXTEND.get())));
        if (elb instanceof Player)
            ((Player) elb).setForcedPose(Pose.SLEEPING);
        elb.setSprinting(true);
        elb.setDeltaMovement(v.x, 0, v.z);
        elb.hurtMarked = true;
        return true;
    }

    public static boolean attemptDodge(LivingEntity elb, int side) {
        /*
        stepping around logic:
        known: sidestep distance is 5, distance to mob is x
        acquire angle theta via cosine rule
        use theta to find the angle of other angles
        add said angle to yaw
        twiddle till it works :v
         */
        ICombatCapability itsc = CombatData.getCap(elb);
        //cannot dodge
        if (!CombatConfig.dodge) return false;
        //let elenai do it
        if (WarCompat.elenaiDodge) return false;
        //can only dodge outside of combat mode if you're stunned or if elenai compat is on
        if (!StylishData.getCap(elb).isCombatMode() && (itsc.getStunTime() == 0)) return false;
        //dodge time check
        if (itsc.getDodgeTime() <= -CombatConfig.rollCooldown) {
            //CombatData.getCap(elb).consumePosture(ReworkConstants.SPIRIT_QI, (float) (elb.getAttributeValue(WarAttributes.DODGE_EFFICIENCY.get())/2));
            elb.extinguishFire();
            if(AerialModeData.getCap(elb).isAerialMode())
            AerialModeData.getCap(elb).setAerialMode(true);
            if (side == 99 && elb.onGround()) return attemptSlide(elb);
            Entity target = GeneralUtils.raytraceEntity(elb.level(), (Entity) elb, 32);
            float adjustment = 0;
            if (target != null) {
                float distsq = (float) (elb.distanceToSqr(target));
                float toacos = (distsq + distsq - 36) / (2 * distsq);//magic number wee
                float acos = (float) Math.acos(toacos);
                adjustment = GeneralUtils.deg(acos) / 2f;
            }
            double x = 0, y = 0.2, z = 0;
            float angle = 0;
            DodgeEvent.Direction d = DodgeEvent.Direction.NONE;
            switch (side) {
                //todo directly send angle

                case 0://left
                    angle = 90 * Mth.DEG_TO_RAD;
                    d = DodgeEvent.Direction.LEFT;
                    break;
                case 1://back
                    angle = 180 * Mth.DEG_TO_RAD;
                    d = DodgeEvent.Direction.BACK;
                    break;
                case 2://right
                    angle = -90 * Mth.DEG_TO_RAD;
                    d = DodgeEvent.Direction.RIGHT;
                    break;
                case 3://forward
                    d = DodgeEvent.Direction.FORWARD;
                    break;
            }
            DodgeEvent e = new DodgeEvent(elb, d, 0.25);
            MinecraftForge.EVENT_BUS.post(e);
            if (e.isCanceled()) return false;
            Vec3 look = elb.getLookAngle().multiply(e.getForce(), 0, e.getForce()).yRot(angle).normalize();
            itsc.setDodgeTime((int) (CombatConfig.rollTime*elb.getAttributeValue(WarAttributes.DODGE_EXTEND.get())));
            //if (d == DodgeEvent.Direction.FORWARD) e.setForce((float) (e.getForce() * 1.5f));
            x = look.x;
            z = look.z;

            if(e.getDirection()!= DodgeEvent.Direction.NONE) {
                elb.setDeltaMovement(elb.getDeltaMovement().multiply(1, 0, 1));
                elb.push(x, y, z);
            }
            itsc.consumePosture(0);
            //leave stun
            itsc.stun(0);
            elb.resetFallDistance();
            return true;
        }
        return false;
    }

    /**
     * knocks the target back, with regards to the attacker's relative angle to the target, and adding y knockback
     */
    public static void knockBack(Entity to,
                                 Entity from,
                                 float strength,
                                 boolean considerRelativeAngle,
                                 boolean bypassAllChecks) {
        if (to == null || from == null) return;
        Vec3 distVec = to.position().add(0, to.getBbHeight() / 2, 0).vectorTo(from.position().add(0, from.getBbHeight() / 2, 0)).multiply(1, 0.5, 1).normalize();
        if (to instanceof LivingEntity && !bypassAllChecks) {
            if (considerRelativeAngle)
                MovementUtils.knockBack((LivingEntity) to, strength, distVec.x, distVec.y, distVec.z, false);
            else {
                double xRatio = (double) Mth.sin(from.getYRot() * 0.017453292F);
                double zRatio = (double) (-Mth.cos(from.getYRot() * 0.017453292F));
                MovementUtils.knockBack(((LivingEntity) to), (float) strength * 0.5F, xRatio, 0, zRatio, false);
            }
        } else {
            //eh
            if (considerRelativeAngle) {
                to.lerpMotion(distVec.x * -strength, to.onGround() ? 0.1 : distVec.y * -strength, distVec.z * -strength);
            } else {
                to.push(-Mth.sin(-from.getYRot() * 0.017453292F - (float) Math.PI) * 0.5, 0.1, -Mth.cos(-from.getYRot() * 0.017453292F - (float) Math.PI) * 0.5);
            }
            to.hurtMarked = true;
        }
    }

    public static void swingin(GrappleEntity hookEntity, Player player) {
        // --- variables ---
        Vec3 hookPos = hookEntity.position();
        Vec3 playerEyePos = player.getEyePosition();
        Vec3 vecToHook = hookPos.subtract(playerEyePos);
        Vec3 unitVector = vecToHook.normalize();

//        AerialModeData.getCap(player).alterGravity(5, 0);

        double maxDistance = hookEntity.getTetherLength();
        double dist = vecToHook.length();

        Vec3 velocity = player.getDeltaMovement().multiply(1.05, 0.9, 1.05);
//        if(velocity.y<0.1)//manually add gravity?????
//            velocity=velocity.add(0,-player.getAttributeBaseValue(ForgeMod.ENTITY_GRAVITY.get()),0);
        double vRadial = velocity.dot(unitVector);
        Vec3 vTangential = velocity.subtract(unitVector.scale(vRadial));

        double vTangentialMultiplier = 1.01;


        if (dist > maxDistance) {
            double stretch = dist - maxDistance;

            vTangentialMultiplier = 1.047;

            double new_vRadial = stretch * 0.055;
            if (!(vRadial > new_vRadial)) vRadial = new_vRadial;
        }


        if (!player.onGround() && !player.isFallFlying()) {
            vTangential = vTangential.scale(vTangentialMultiplier);
            vRadial = vRadial * 0.99;
        }

        Vec3 finalVelocity = vTangential.add(unitVector.scale(vRadial));//.multiply(0.5, 1.11, 0.5);

        player.setDeltaMovement(finalVelocity);


        if (!player.level().isClientSide()) {
            // --- server logic for fall damage reset ---
            player.resetFallDistance();
            if (!player.onGround()) {
                player.hurtMarked = false;
                if ((dist + 0.6) > maxDistance) {
                    if (unitVector.y > -0.15) {
                        player.resetFallDistance();
                    }
                }
            }
        }
    }
}
