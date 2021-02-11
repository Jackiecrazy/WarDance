//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package jackiecrazy.wardance.utils;

import java.util.*;

import jdk.nashorn.internal.ir.Block;
import net.minecraft.client.renderer.FaceDirection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GeneralUtils {
    public static double getSpeedSq(Entity e) {
        if (e.getRidingEntity() != null)
            return e.getLowestRidingEntity().getMotion().lengthSquared();
        return e.getMotion().lengthSquared();
    }

    @Nonnull
    public static RayTraceResult raytraceAnything(World world, LivingEntity attacker, double range) {
        Vector3d start = attacker.getEyePosition(0.5f);
        Vector3d look = attacker.getLookVec().scale(range + 2);
        Vector3d end = start.add(look);
        Entity entity = null;
        List<Entity> list = world.getEntitiesInAABBexcluding(attacker, attacker.getBoundingBox().expand(look.x, look.y, look.z).grow(1.0D), null);
        double d0 = 0.0D;

        for (Entity entity1 : list) {
            if (entity1 != attacker) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox();
                Optional<Vector3d> raytraceresult = axisalignedbb.rayTrace(start, end);
                if (raytraceresult.isPresent()) {
                    double d1 = getDistSqCompensated(entity1, attacker);

                    if ((d1 < d0 || d0 == 0.0D) && d1 < range * range) {
                        entity = entity1;
                        d0 = d1;
                    }
                }
            }
        }
        if (entity != null) return new EntityRayTraceResult(entity);
        look = attacker.getLookVec().scale(range);
        end = start.add(look);
        RayTraceResult rtr = world.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));
        if (rtr != null) {
            return rtr;
        }
        return new BlockRayTraceResult(end, Direction.UP, new BlockPos(end), false);
    }

    /**
     * modified getdistancesq to account for thicc mobs
     */
    public static double getDistSqCompensated(Entity from, Entity to) {
        double x = from.getPosX() - to.getPosX();
        x = Math.max(Math.abs(x) - ((from.getWidth() / 2) + (to.getWidth() / 2)), 0);
        //stupid inconsistent game
        double y = (from.getPosY() + from.getHeight() / 2) - (to.getPosY() + to.getHeight() / 2);
        y = Math.max(Math.abs(y) - (from.getHeight() / 2 + to.getHeight() / 2), 0);
        double z = from.getPosZ() - to.getPosZ();
        z = Math.max(Math.abs(z) - (from.getWidth() / 2 + to.getWidth() / 2), 0);
        double me = x * x + y * y + z * z;
        double you = from.getDistanceSq(to);
        return Math.min(me, you);
    }

    /**
     * modified getdistancesq to account for thicc mobs
     */
    public static double getDistSqCompensated(Entity from, Vector3d to) {
        double x = from.getPosX() - to.x;
        x = Math.max(Math.abs(x) - ((from.getWidth() / 2)), 0);
        //stupid inconsistent game
        double y = (from.getPosY() + from.getHeight() / 2) - (to.y);
        y = Math.max(Math.abs(y) - (from.getHeight() / 2), 0);
        double z = from.getPosZ() - to.z;
        z = Math.max(Math.abs(z) - (from.getWidth() / 2), 0);
        return x * x + y * y + z * z;
    }

    /**
     * modified getdistancesq to account for thicc mobs
     */
    public static double getDistSqCompensated(Entity from, BlockPos to) {
        double x = from.getPosX() - to.getX();
        x = Math.max(Math.abs(x) - ((from.getWidth() / 2)), 0);
        //stupid inconsistent game
        double y = (from.getPosY() + from.getHeight() / 2) - (to.getY());
        y = Math.max(Math.abs(y) - (from.getHeight() / 2), 0);
        double z = from.getPosZ() - to.getZ();
        z = Math.max(Math.abs(z) - (from.getWidth() / 2), 0);
        return x * x + y * y + z * z;
    }

    public static Entity raytraceEntity(World world, LivingEntity attacker, double range) {
        Vector3d start = attacker.getEyePosition(0.5f);
        Vector3d look = attacker.getLookVec().scale(range + 2);
        Vector3d end = start.add(look);
        Entity entity = null;
        List<Entity> list = world.getEntitiesInAABBexcluding(attacker, attacker.getBoundingBox().expand(look.x, look.y, look.z).grow(1.0D), null);
        double d0 = -1.0D;//necessary to prevent small derps

        for (Entity entity1 : list) {
            if (entity1 != attacker) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox();
                Optional<Vector3d> raytraceresult = axisalignedbb.rayTrace(start, end);
                if (raytraceresult.isPresent()) {
                    double d1 = getDistSqCompensated(entity1, attacker);

                    if ((d1 < d0 || d0 == -1.0D) && d1 < range * range) {
                        entity = entity1;
                        d0 = d1;
                    }
                }
            }
        }
        return entity;
    }

    public static List<Entity> raytraceEntities(World world, LivingEntity attacker, double range) {
        Vector3d start = attacker.getEyePosition(0.5f);
        Vector3d look = attacker.getLookVec().scale(range + 2);
        Vector3d end = start.add(look);
        ArrayList<Entity> ret = new ArrayList<>();
        List<Entity> list = world.getEntitiesInAABBexcluding(attacker, attacker.getBoundingBox().expand(look.x, look.y, look.z).grow(1.0D), EntityPredicates.IS_ALIVE);

        for (Entity entity1 : list) {
            if (entity1 != attacker && getDistSqCompensated(attacker, entity1) < range * range) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox();
                Optional<Vector3d> raytraceresult = axisalignedbb.rayTrace(start, end);
                if (raytraceresult.isPresent()) {
                    ret.add(entity1);
                }
            }
        }
        return ret;
    }

    public static Vector3d getPointInFrontOf(Entity target, Entity from, double distance) {
        Vector3d end = target.getPositionVec().add(from.getPositionVec().subtract(target.getPositionVec()).normalize().scale(distance));
        return getClosestAirSpot(from.getPositionVec(), end, from);
    }

    /**
     * returns the coordinate closest to the end point of the vector that fits the entity
     * From and To should be from the feet and at the center, respectively.
     * After that, it performs 4 ray casts: one from the bottom, one at the top, and two at the sides.
     * Two sides are omitted if you're 1 block wide or less, another ray cast is done for every block of height you have
     * So a player will be casted 3 times: once at the foot, once at the midriff, and once at the head
     * The closest RayTraceResult will be used, with compensation if it didn't hit the top of a block
     */
    public static Vector3d getClosestAirSpot(Vector3d from, Vector3d to, Entity e) {
        Vector3d ret = to;
        //extend the to vector slightly to make it hit what it originally hit
        to = to.add(to.subtract(from).normalize().scale(2));
        double widthParse = e.getWidth() / 2;
        double heightParse = e.getHeight();
        if (widthParse <= 0.5) widthParse = 0;
        if (heightParse <= 1) heightParse = 0;
        for (double addX = -widthParse; addX <= widthParse; addX += 0.5) {
            for (double addZ = -widthParse; addZ <= widthParse; addZ += 0.5) {
                for (double addY = e.getHeight() / 2; addY <= heightParse; addY += 0.5) {
                    Vector3d mod = new Vector3d(addX, addY, addZ);
                    BlockRayTraceResult r = e.world.rayTraceBlocks(new RayTraceContext(from.add(mod), to.add(mod), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, e));
                    if (r != null && r.getType() == RayTraceResult.Type.BLOCK && !r.getHitVec().equals(from.add(mod))) {
                        Vector3d hit = r.getHitVec().subtract(mod);
                        switch (r.getFace()) {
                            case NORTH:
                                hit = hit.add(0, 0, -1);
                                break;
                            case SOUTH:
                                hit = hit.add(0, 0, 1);
                                break;
                            case EAST:
                                hit = hit.add(1, 0, 0);
                                break;
                            case WEST:
                                hit = hit.add(-1, 0, 0);
                                break;
                            case UP:
                                //hit.add(0, -1, 0); //apparently unnecessary.
                                break;
                            case DOWN:
                                hit = hit.add(0, -1, 0);
                                break;
                        }
                        if (from.squareDistanceTo(hit) < from.squareDistanceTo(ret)) {
                            ret = hit;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * returns the BlockPos at the center of an AABB
     */
    public static BlockPos posFromAABB(AxisAlignedBB aabb) {
        return new BlockPos((aabb.maxX + aabb.minX) / 2, (aabb.maxY + aabb.minY) / 2, (aabb.maxZ + aabb.minZ) / 2);
    }


    /**
     * returns true if entity2 is within a (angle) degree sector in front of entity1
     */
    public static boolean isFacingEntity(Entity entity1, Entity entity2, int angle) {
        if (angle < 0) return isBehindEntity(entity2, entity1, -angle);
        Vector3d posVec = entity2.getPositionVec().add(0, entity2.getEyeHeight(), 0);
        Vector3d lookVec = entity1.getLook(1.0F);
        Vector3d relativePosVec = posVec.subtractReverse(entity1.getPositionVec().add(0, entity1.getEyeHeight(), 0)).normalize();
        //relativePosVec = new Vector3d(relativePosVec.x, 0.0D, relativePosVec.z);

        double dotsq = ((relativePosVec.dotProduct(lookVec) * Math.abs(relativePosVec.dotProduct(lookVec))) / (relativePosVec.lengthSquared() * lookVec.lengthSquared()));
        double cos = MathHelper.cos(rad(angle / 2f));
        return dotsq < -(cos * cos);
    }

    /**
     * returns true if entity is within a 90 degree sector behind the reference
     */
    public static boolean isBehindEntity(Entity entity, Entity reference, int angle) {
        Vector3d posVec = entity.getPositionVec().add(0, entity.getEyeHeight(), 0);
        Vector3d lookVec = getBodyOrientation(reference);
        Vector3d relativePosVec = posVec.subtractReverse(reference.getPositionVec().add(0, reference.getEyeHeight(), 0)).normalize();
        relativePosVec = new Vector3d(relativePosVec.x, 0.0D, relativePosVec.z);
        double dotsq = ((relativePosVec.dotProduct(lookVec) * Math.abs(relativePosVec.dotProduct(lookVec))) / (relativePosVec.lengthSquared() * lookVec.lengthSquared()));
        double cos = MathHelper.cos(rad(angle / 2f));
        return dotsq > cos * cos;
    }

    public static float rad(float angle) {
        return (float) (angle * Math.PI / 180d);
    }

    /**
     * literally a copy-paste of {@link Entity#getLookVec()} for {@link LivingEntity}, since they calculate from their head instead
     */
    public static Vector3d getBodyOrientation(Entity e) {
        float f = MathHelper.cos(-e.rotationYaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-e.rotationYaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-e.rotationPitch * 0.017453292F);
        float f3 = MathHelper.sin(-e.rotationPitch * 0.017453292F);
        return new Vector3d((double) (f1 * f2), (double) f3, (double) (f * f2));
    }

    public static float deg(float rad) {
        return (float) (rad * 180d / Math.PI);
    }

    /**
     * returns true if entity2 is within a (horAngle) degree sector in front of entity1, and within (vertAngle)
     * if horAngle is negative, it'll invoke isBehindEntity instead.
     */
    public static boolean isFacingEntity(Entity entity1, Entity entity2, int horAngle, int vertAngle) {
        if (horAngle < 0) return isBehindEntity(entity2, entity1, -horAngle, Math.abs(vertAngle));
        double xDiff = entity1.getPosX() - entity2.getPosX(), zDiff = entity1.getPosZ() - entity2.getPosZ();
        if (vertAngle != 360) {
            Vector3d posVec = entity2.getPositionVec().add(0, entity2.getEyeHeight(), 0);
            //y calculations
            double distIgnoreY = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            double relativeHeadVec = entity2.getPosY() - entity1.getPosY() - entity1.getEyeHeight() + entity2.getHeight();
            double relativeFootVec = entity2.getPosY() - entity1.getPosY() - entity1.getEyeHeight();
            double angleHead = -MathHelper.atan2(relativeHeadVec, distIgnoreY);
            double angleFoot = -MathHelper.atan2(relativeFootVec, distIgnoreY);
            //straight up is -90 and straight down is 90
            double maxRot = rad(entity1.rotationPitch + vertAngle / 2f);
            double minRot = rad(entity1.rotationPitch - vertAngle / 2f);
            if (angleHead > maxRot || angleFoot < minRot) return false;
        }
        //xz begins
        //subtract half of width from calculations in the xz plane so wide mobs that are barely in frame still get lambasted
//        double xDiffCompensated;
//        if (xDiff < 0) {
//            xDiffCompensated = Math.min(-0.1, xDiff + entity1.width / 2 + entity2.width / 2);
//        } else {
//            xDiffCompensated = Math.max(0.1, xDiff - entity1.width / 2 - entity2.width / 2);
//        }
//        double zDiffCompensated;
//        if (zDiff < 0) {
//            zDiffCompensated = Math.min(-0.1, zDiff + entity1.width / 2 + entity2.width / 2);
//        } else {
//            zDiffCompensated = Math.max(0.1, zDiff - entity1.width / 2 - entity2.width / 2);
//        }
//        double yDiffCompensated;
//        if (entity1.posY-entity2.posY < 0) {
//            yDiffCompensated = Math.min(-0.1, entity1.posY-entity2.posY + entity1.height / 2 + entity2.height / 2);
//        } else {
//            yDiffCompensated = Math.max(0.1, entity1.posY-entity2.posY - entity1.height / 2 - entity2.height / 2);
//        }
        Vector3d lookVec = entity1.getLook(1.0F);
        Vector3d bodyVec = getBodyOrientation(entity1);
        //lookVec=new Vector3d(lookVec.x, 0, lookVec.z);
        //bodyVec=new Vector3d(bodyVec.x, 0, bodyVec.z);
        Vector3d relativePosVec = new Vector3d(xDiff, entity1.getPosY() - entity2.getPosY(), zDiff);
        double dotsqLook = ((relativePosVec.dotProduct(lookVec) * Math.abs(relativePosVec.dotProduct(lookVec))) / (relativePosVec.lengthSquared() * lookVec.lengthSquared()));
        double dotsqBody = ((relativePosVec.dotProduct(bodyVec) * Math.abs(relativePosVec.dotProduct(bodyVec))) / (relativePosVec.lengthSquared() * bodyVec.lengthSquared()));
        double cos = MathHelper.cos(rad(horAngle / 2));
        return dotsqBody < -(cos * cos) || dotsqLook < -(cos * cos);
    }

    public static boolean isBehindEntity(Entity entity, Entity reference, int horAngle, int vertAngle) {
        if (horAngle < 0) return isFacingEntity(reference, entity, -horAngle, Math.abs(vertAngle));
        Vector3d posVec = reference.getPositionVec().add(0, reference.getEyeHeight(), 0);
        //y calculations
        double xDiff = reference.getPosX() - entity.getPosX(), zDiff = reference.getPosZ() - entity.getPosZ();
        double distIgnoreY = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double relativeHeadVec = reference.getPosY() - entity.getPosY() - entity.getEyeHeight() + reference.getHeight();
        double relativeFootVec = reference.getPosY() - entity.getPosY() - entity.getEyeHeight();
        double angleHead = -MathHelper.atan2(relativeHeadVec, distIgnoreY);
        double angleFoot = -MathHelper.atan2(relativeFootVec, distIgnoreY);
        //straight up is -90 and straight down is 90
        double maxRot = rad(reference.rotationPitch + vertAngle / 2f);
        double minRot = rad(reference.rotationPitch - vertAngle / 2f);
        if (angleHead > maxRot || angleFoot < minRot) return false;
        //xz begins
        //subtract half of width from calculations in the xz plane so wide mobs that are barely in frame still get lambasted
        double xDiffCompensated;
        if (xDiff < 0) {
            xDiffCompensated = Math.min(-0.1, xDiff + entity.getWidth() / 2 + reference.getWidth() / 2);
        } else {
            xDiffCompensated = Math.max(0.1, xDiff - entity.getWidth() / 2 - reference.getWidth() / 2);
        }
        double zDiffCompensated;
        if (zDiff < 0) {
            zDiffCompensated = Math.min(-0.1, zDiff + entity.getWidth() / 2 + reference.getWidth() / 2);
        } else {
            zDiffCompensated = Math.max(0.1, zDiff - entity.getWidth() / 2 - reference.getWidth() / 2);
        }
        Vector3d bodyVec = getBodyOrientation(reference);
        Vector3d lookVec = reference.getLook(1f);
        Vector3d relativePosVec = new Vector3d(xDiffCompensated, 0, zDiffCompensated);
        double dotsqLook = ((relativePosVec.dotProduct(lookVec) * Math.abs(relativePosVec.dotProduct(lookVec))) / (relativePosVec.lengthSquared() * lookVec.lengthSquared()));
        double dotsqBody = ((relativePosVec.dotProduct(bodyVec) * Math.abs(relativePosVec.dotProduct(bodyVec))) / (relativePosVec.lengthSquared() * bodyVec.lengthSquared()));
        double cos = MathHelper.cos(rad(horAngle / 2));
        return dotsqBody > cos * cos || dotsqLook > cos * cos;
    }

    public static BlockPos[] bresenham(BlockPos from, BlockPos to) {

        double p_x = from.getX();
        double p_y = from.getY();
        double p_z = from.getZ();
        double d_x = to.getX() - from.getX();
        double d_y = to.getY() - from.getY();
        double d_z = to.getZ() - from.getZ();
        int N = (int) Math.ceil(Math.max(Math.abs(d_x), Math.max(Math.abs(d_y), Math.abs(d_z))));
        double s_x = d_x / N;
        double s_y = d_y / N;
        double s_z = d_z / N;
        //System.out.println(N);
        BlockPos[] out = new BlockPos[N];
        if (out.length == 0) {
            //System.out.println("nay!");
            return out;
        }
        out[0] = new BlockPos((int) p_x, (int) p_y, (int) p_z);
        for (int ii = 1; ii < N; ii++) {
            p_x += s_x;
            p_y += s_y;
            p_z += s_z;
            out[ii] = new BlockPos((int) p_x, (int) p_y, (int) p_z);
        }
        return out;
    }

    public static float getCosAngleSq(Vector3d from, Vector3d to) {
        double top = from.dotProduct(to) * from.dotProduct(to);
        double bot = from.lengthSquared() * to.lengthSquared();
        return (float) (top / bot);
    }

    /**
     * drops a skull of the given type. For players it will retrieve their skin
     */
    public static ItemStack dropSkull(LivingEntity elb) {
        ItemStack ret = null;
        if (elb instanceof AbstractSkeletonEntity) {
            if (elb instanceof WitherSkeletonEntity)
                ret = new ItemStack(Items.WITHER_SKELETON_SKULL);
            else ret = new ItemStack(Items.SKELETON_SKULL);
        } else if (elb instanceof ZombieEntity)
            ret = new ItemStack(Items.ZOMBIE_HEAD);
        else if (elb instanceof CreeperEntity)
            ret = new ItemStack(Items.CREEPER_HEAD);
        else if (elb instanceof EnderDragonEntity)
            ret = new ItemStack(Items.DRAGON_HEAD);
        else if (elb instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity) elb;
            ret = new ItemStack(Items.PLAYER_HEAD);
            ret.setTag(new CompoundNBT());
            ret.getTag().putString("SkullOwner", p.getName().getString());
        }
        return ret;
    }

    public static double getAttributeValueHandSensitive(LivingEntity e, Attribute a, Hand h) {
        if (e.getAttribute(a) == null) return 4;
        if (h == Hand.MAIN_HAND) return e.getAttributeValue(a);
        ModifiableAttributeInstance mai = new ModifiableAttributeInstance(a, (n) -> {
        });
        Collection<AttributeModifier> stuff = e.getHeldItemMainhand().getAttributeModifiers(EquipmentSlotType.MAINHAND).get(a);
        apply:
        for (AttributeModifier am : e.getAttribute(Attributes.ATTACK_DAMAGE).getModifierListCopy()) {
            for (AttributeModifier f : stuff) if (f.getID().equals(am.getID())) continue apply;
            mai.applyNonPersistentModifier(am);
        }
        for (AttributeModifier f : e.getHeldItemOffhand().getAttributeModifiers(EquipmentSlotType.MAINHAND).get(a))
            mai.applyNonPersistentModifier(f);
        return mai.getValue();
    }
}
