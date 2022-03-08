//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package jackiecrazy.wardance.utils;

import jackiecrazy.wardance.capability.resources.CombatData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class GeneralUtils {
    public static double getSpeedSq(Entity e) {
        if (e.getVehicle() != null)
            if (e.getRootVehicle() instanceof LivingEntity)
                return CombatData.getCap((LivingEntity) e.getRootVehicle()).getMotionConsistently().lengthSqr();
            else return e.getRootVehicle().getDeltaMovement().lengthSqr();
        if (e instanceof LivingEntity)
            return Math.max(CombatData.getCap((LivingEntity) e).getMotionConsistently().lengthSqr(), e.getDeltaMovement().lengthSqr());
        return e.getDeltaMovement().lengthSqr();
    }

    @Nullable
    public static EntityType getEntityTypeFromResourceLocation(ResourceLocation rl) {
        if (ForgeRegistries.ENTITIES.containsKey(rl))
            return ForgeRegistries.ENTITIES.getValue(rl);
        return null;
    }

    @Nullable
    public static ResourceLocation getResourceLocationFromEntityType(EntityType et) {
        if (ForgeRegistries.ENTITIES.containsValue(et))
            return ForgeRegistries.ENTITIES.getKey(et);
        return null;
    }

    @Nullable
    public static ResourceLocation getResourceLocationFromEntity(Entity et) {
        final EntityType<?> type = et.getType();
        if (ForgeRegistries.ENTITIES.containsValue(type)) {
            final ResourceLocation key = ForgeRegistries.ENTITIES.getKey(type);
            return key;
        }
        return null;
    }

    @Nonnull
    public static RayTraceResult raytraceAnything(World world, LivingEntity attacker, double range) {
        Vector3d start = attacker.getEyePosition(0.5f);
        Vector3d look = attacker.getLookAngle().scale(range + 2);
        Vector3d end = start.add(look);
        Entity entity = null;
        List<Entity> list = world.getEntities(attacker, attacker.getBoundingBox().expandTowards(look.x, look.y, look.z).inflate(1.0D), null);
        double d0 = 0.0D;

        for (Entity entity1 : list) {
            if (entity1 != attacker) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox();
                Optional<Vector3d> raytraceresult = axisalignedbb.clip(start, end);
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
        look = attacker.getLookAngle().scale(range);
        end = start.add(look);
        RayTraceResult rtr = world.clip(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));
        if (rtr != null) {
            return rtr;
        }
        return new BlockRayTraceResult(end, Direction.UP, new BlockPos(end), false);
    }

    /**
     * modified getdistancesq to account for thicc mobs
     */
    public static double getDistSqCompensated(Entity from, Entity to) {
        double x = from.getX() - to.getX();
        x = Math.max(Math.abs(x) - ((from.getBbWidth() / 2) + (to.getBbWidth() / 2)), 0);
        //stupid inconsistent game
        double y = (from.getY() + from.getBbHeight() / 2) - (to.getY() + to.getBbHeight() / 2);
        y = Math.max(Math.abs(y) - (from.getBbHeight() / 2 + to.getBbHeight() / 2), 0);
        double z = from.getZ() - to.getZ();
        z = Math.max(Math.abs(z) - (from.getBbWidth() / 2 + to.getBbWidth() / 2), 0);
        double me = x * x + y * y + z * z;
        double you = from.distanceToSqr(to);
        return Math.min(me, you);
    }

    public static float getMaxHealthBeforeWounding(LivingEntity of) {
        return of.getMaxHealth() + CombatData.getCap(of).getWounding();
    }

    /**
     * modified getdistancesq to account for thicc mobs
     */
    public static double getDistSqCompensated(Entity from, Vector3d to) {
        double x = from.getX() - to.x;
        x = Math.max(Math.abs(x) - ((from.getBbWidth() / 2)), 0);
        //stupid inconsistent game
        double y = (from.getY() + from.getBbHeight() / 2) - (to.y);
        y = Math.max(Math.abs(y) - (from.getBbHeight() / 2), 0);
        double z = from.getZ() - to.z;
        z = Math.max(Math.abs(z) - (from.getBbWidth() / 2), 0);
        return x * x + y * y + z * z;
    }

    /**
     * modified getdistancesq to account for thicc mobs
     */
    public static double getDistSqCompensated(Entity from, BlockPos to) {
        double x = from.getX() - to.getX();
        x = Math.max(Math.abs(x) - ((from.getBbWidth() / 2)), 0);
        //stupid inconsistent game
        double y = (from.getY() + from.getBbHeight() / 2) - (to.getY());
        y = Math.max(Math.abs(y) - (from.getBbHeight() / 2), 0);
        double z = from.getZ() - to.getZ();
        z = Math.max(Math.abs(z) - (from.getBbWidth() / 2), 0);
        return x * x + y * y + z * z;
    }

    public static Entity raytraceEntity(World world, LivingEntity attacker, double range) {
        Vector3d start = attacker.getEyePosition(0.5f);
        Vector3d look = attacker.getLookAngle().scale(range + 2);
        Vector3d end = start.add(look);
        Entity entity = null;
        List<Entity> list = world.getEntities(attacker, attacker.getBoundingBox().expandTowards(look.x, look.y, look.z).inflate(1.0D), null);
        double d0 = -1.0D;//necessary to prevent small derps

        for (Entity entity1 : list) {
            if (entity1 != attacker) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox();
                Optional<Vector3d> raytraceresult = axisalignedbb.clip(start, end);
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

    public static LivingEntity raytraceLiving(LivingEntity attacker, double range) {
        return raytraceLiving(attacker.level, attacker, range);
    }

    public static LivingEntity raytraceLiving(World world, LivingEntity attacker, double range) {
        Vector3d start = attacker.getEyePosition(0.5f);
        Vector3d look = attacker.getLookAngle().scale(range + 2);
        Vector3d end = start.add(look);
        LivingEntity entity = null;
        List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().expandTowards(look.x, look.y, look.z).inflate(1.5D), null);
        double d0 = -1.0D;//necessary to prevent small derps

        for (LivingEntity entity1 : list) {
            if (entity1 != attacker) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox();
                Optional<Vector3d> raytraceresult = axisalignedbb.clip(start, end);
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

    /**
     * Checks the +x, -x, +y, -y, +z, -z, in that order
     *
     * @param elb
     * @return
     */
    public static Entity collidingEntity(Entity elb) {
        AxisAlignedBB aabb = elb.getBoundingBox();
        Vector3d motion = elb.getDeltaMovement().normalize().scale(0.5);
        List<Entity> entities = elb.level.getEntities(elb, aabb.expandTowards(motion.x, motion.y, motion.z), EntityPredicates.ENTITY_STILL_ALIVE);
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

    public static List<Entity> raytraceEntities(World world, LivingEntity attacker, double range) {
        Vector3d start = attacker.getEyePosition(0.5f);
        Vector3d look = attacker.getLookAngle().scale(range + 2);
        Vector3d end = start.add(look);
        ArrayList<Entity> ret = new ArrayList<>();
        List<Entity> list = world.getEntities(attacker, attacker.getBoundingBox().expandTowards(look.x, look.y, look.z).inflate(1.0D), EntityPredicates.ENTITY_STILL_ALIVE);

        for (Entity entity1 : list) {
            if (entity1 != attacker && getDistSqCompensated(attacker, entity1) < range * range) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox();
                Optional<Vector3d> raytraceresult = axisalignedbb.clip(start, end);
                if (raytraceresult.isPresent()) {
                    ret.add(entity1);
                }
            }
        }
        return ret;
    }

    public static Vector3d getPointInFrontOf(Entity target, Entity from, double distance) {
        Vector3d end = target.position().add(from.position().subtract(target.position()).normalize().scale(distance));
        return getClosestAirSpot(from.position(), end, from);
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
        double widthParse = e.getBbWidth() / 2;
        double heightParse = e.getBbHeight();
        if (widthParse <= 0.5) widthParse = 0;
        if (heightParse <= 1) heightParse = 0;
        for (double addX = -widthParse; addX <= widthParse; addX += 0.5) {
            for (double addZ = -widthParse; addZ <= widthParse; addZ += 0.5) {
                for (double addY = e.getBbHeight() / 2; addY <= heightParse; addY += 0.5) {
                    Vector3d mod = new Vector3d(addX, addY, addZ);
                    BlockRayTraceResult r = e.level.clip(new RayTraceContext(from.add(mod), to.add(mod), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, e));
                    if (r != null && r.getType() == RayTraceResult.Type.BLOCK && !r.getLocation().equals(from.add(mod))) {
                        Vector3d hit = r.getLocation().subtract(mod);
                        switch (r.getDirection()) {
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
                        if (from.distanceToSqr(hit) < from.distanceToSqr(ret)) {
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
        if (angle >= 360) return true;//well, duh.
        if (angle < 0) return isBehindEntity(entity2, entity1, -angle);
        Vector3d posVec = entity2.position().add(0, entity2.getEyeHeight(), 0);
        Vector3d lookVec = entity1.getViewVector(1.0F);
        Vector3d relativePosVec = posVec.vectorTo(entity1.position().add(0, entity1.getEyeHeight(), 0)).normalize();
        //relativePosVec = new Vector3d(relativePosVec.x, 0.0D, relativePosVec.z);

        double dotsq = ((relativePosVec.dot(lookVec) * Math.abs(relativePosVec.dot(lookVec))) / (relativePosVec.lengthSqr() * lookVec.lengthSqr()));
        double cos = MathHelper.cos(rad(angle / 2f));
        return dotsq < -(cos * cos);
    }

    /**
     * returns true if entity is within a 90 degree sector behind the reference
     */
    public static boolean isBehindEntity(Entity entity, Entity reference, int angle) {
        if (angle >= 360) return true;//well, duh.
        Vector3d posVec = entity.position().add(0, entity.getEyeHeight(), 0);
        Vector3d lookVec = getBodyOrientation(reference);
        Vector3d relativePosVec = posVec.vectorTo(reference.position().add(0, reference.getEyeHeight(), 0)).normalize();
        relativePosVec = new Vector3d(relativePosVec.x, 0.0D, relativePosVec.z);
        double dotsq = ((relativePosVec.dot(lookVec) * Math.abs(relativePosVec.dot(lookVec))) / (relativePosVec.lengthSqr() * lookVec.lengthSqr()));
        double cos = MathHelper.cos(rad(angle / 2f));
        return dotsq > cos * cos;
    }

    public static float rad(float angle) {
        return (float) (angle * Math.PI / 180d);
    }

    /**
     * literally a copy-paste of {@link Entity#getLookAngle()} ()} for {@link LivingEntity}, since they calculate from their head instead
     */
    public static Vector3d getBodyOrientation(Entity e) {
        float f = MathHelper.cos(-e.yRot * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-e.yRot * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-e.xRot * 0.017453292F);
        float f3 = MathHelper.sin(-e.xRot * 0.017453292F);
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
        horAngle = Math.min(horAngle, 360);
        vertAngle = Math.min(vertAngle, 360);
        if (horAngle < 0) return isBehindEntity(entity2, entity1, -horAngle, Math.abs(vertAngle));
        double xDiff = entity1.getX() - entity2.getX(), zDiff = entity1.getZ() - entity2.getZ();
        if (vertAngle != 360) {
            Vector3d posVec = entity2.position().add(0, entity2.getEyeHeight(), 0);
            //y calculations
            double distIgnoreY = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            double relativeHeadVec = entity2.getY() - entity1.getY() - entity1.getEyeHeight() + entity2.getBbHeight();
            double relativeFootVec = entity2.getY() - entity1.getY() - entity1.getEyeHeight();
            double angleHead = -MathHelper.atan2(relativeHeadVec, distIgnoreY);
            double angleFoot = -MathHelper.atan2(relativeFootVec, distIgnoreY);
            //straight up is -90 and straight down is 90
            double maxRot = rad(entity1.xRot + vertAngle / 2f);
            double minRot = rad(entity1.xRot - vertAngle / 2f);
            if (angleHead > maxRot || angleFoot < minRot) return false;
        }
        if (horAngle != 360) {
            Vector3d lookVec = entity1.getViewVector(1.0F);
            Vector3d bodyVec = getBodyOrientation(entity1);
            //lookVec=new Vector3d(lookVec.x, 0, lookVec.z);
            //bodyVec=new Vector3d(bodyVec.x, 0, bodyVec.z);
            Vector3d relativePosVec = entity2.position().subtract(entity1.position());
            double angleLook = MathHelper.atan2(lookVec.z, lookVec.x);
            double angleBody = MathHelper.atan2(bodyVec.z, bodyVec.x);
            double anglePos = MathHelper.atan2(relativePosVec.z, relativePosVec.x);
            angleBody += Math.PI;
            angleLook += Math.PI;
            anglePos += Math.PI;
            double rad = rad(horAngle / 2f);
            if (Math.abs(angleLook - anglePos) > rad && Math.abs(angleBody - anglePos) > rad) return false;
        }
        return true;
    }

    public static boolean isBehindEntity(Entity entity, Entity reference, int horAngle, int vertAngle) {
        if (horAngle < 0) return isFacingEntity(reference, entity, -horAngle, Math.abs(vertAngle));
        Vector3d posVec = reference.position().add(0, reference.getEyeHeight(), 0);
        //y calculations
        double xDiff = reference.getX() - entity.getX(), zDiff = reference.getZ() - entity.getZ();
        double distIgnoreY = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double relativeHeadVec = reference.getY() - entity.getY() - entity.getEyeHeight() + reference.getBbHeight();
        double relativeFootVec = reference.getY() - entity.getY() - entity.getEyeHeight();
        double angleHead = -MathHelper.atan2(relativeHeadVec, distIgnoreY);
        double angleFoot = -MathHelper.atan2(relativeFootVec, distIgnoreY);
        //straight up is -90 and straight down is 90
        double maxRot = rad(reference.xRot + vertAngle / 2f);
        double minRot = rad(reference.xRot - vertAngle / 2f);
        if (angleHead > maxRot || angleFoot < minRot) return false;
        //xz begins
        //subtract half of width from calculations in the xz plane so wide mobs that are barely in frame still get lambasted
        double xDiffCompensated;
        if (xDiff < 0) {
            xDiffCompensated = Math.min(-0.1, xDiff + entity.getBbWidth() / 2 + reference.getBbWidth() / 2);
        } else {
            xDiffCompensated = Math.max(0.1, xDiff - entity.getBbWidth() / 2 - reference.getBbWidth() / 2);
        }
        double zDiffCompensated;
        if (zDiff < 0) {
            zDiffCompensated = Math.min(-0.1, zDiff + entity.getBbWidth() / 2 + reference.getBbWidth() / 2);
        } else {
            zDiffCompensated = Math.max(0.1, zDiff - entity.getBbWidth() / 2 - reference.getBbWidth() / 2);
        }
        Vector3d bodyVec = getBodyOrientation(reference);
        Vector3d lookVec = reference.getViewVector(1f);
        Vector3d relativePosVec = new Vector3d(xDiffCompensated, 0, zDiffCompensated);
        double dotsqLook = ((relativePosVec.dot(lookVec) * Math.abs(relativePosVec.dot(lookVec))) / (relativePosVec.lengthSqr() * lookVec.lengthSqr()));
        double dotsqBody = ((relativePosVec.dot(bodyVec) * Math.abs(relativePosVec.dot(bodyVec))) / (relativePosVec.lengthSqr() * bodyVec.lengthSqr()));
        double cos = MathHelper.cos(rad(horAngle / 2f));
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
        double top = from.dot(to) * from.dot(to);
        double bot = from.lengthSqr() * to.lengthSqr();
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
        if (h == Hand.MAIN_HAND) return getAttributeValueSafe(e, a);
        ModifiableAttributeInstance mai = new ModifiableAttributeInstance(a, (n) -> {
        });
        Collection<AttributeModifier> ignore = e.getMainHandItem().getAttributeModifiers(EquipmentSlotType.MAINHAND).get(a);
        apply:
        for (AttributeModifier am : e.getAttribute(a).getModifiers()) {
            for (AttributeModifier f : ignore) if (f.getId().equals(am.getId())) continue apply;
            mai.addTransientModifier(am);
        }
        for (AttributeModifier f : e.getOffhandItem().getAttributeModifiers(EquipmentSlotType.MAINHAND).get(a)) {
            mai.removeModifier(f.getId());
            mai.addTransientModifier(f);
        }
        return mai.getValue();
    }

    public static double getAttributeValueSafe(LivingEntity e, Attribute a) {
        if (e.getAttribute(a) != null) return e.getAttributeValue(a);
        return 0;
    }

    public static boolean isKitMain(ItemStack is) {
        //if (is.getItem() == Items.IRON_AXE) return true;
        return is.getTag() != null && is.getTag().getBoolean("kit");
    }
}
