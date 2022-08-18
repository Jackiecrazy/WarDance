package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class FakeExplosion extends Explosion {
    private float radius, damage;
    private World world;

    public FakeExplosion(World worldIn, @Nullable Entity entityIn, double x, double y, double z, float radius, DamageSource ds, float damage) {
        super(worldIn, entityIn, ds, null, x, y, z, radius, false, Mode.NONE);
        this.radius = radius * 2;
        world = worldIn;
        this.damage = damage;

    }

    public static FakeExplosion explode(World world, Entity entityIn, double x, double y, double z, float radius, DamageSource damageSource, float damage) {
        FakeExplosion explosion = new FakeExplosion(world, entityIn, x, y, z, radius, damageSource, damage);
        explosion.explode();
        explosion.finalizeExplosion(true);
        //oh wow, yikes
        SkillUtils.createCloud(world, entityIn, x, y, z, radius * 2, ParticleTypes.EXPLOSION);
        return explosion;
    }

    public void explode() {
        if (getExploder() == null) return;
        List<Entity> list = world.getEntities(this.getExploder(), AxisAlignedBB.unitCubeFromLowerCorner(getPosition()).inflate(radius));
        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, list, radius * 2);
        Vector3d vector3d = getPosition();

        for (Entity entity : list) {
            if (!entity.ignoreExplosion() && !TargetingUtils.isAlly(entity, getExploder())) {
                double percentage = MathHelper.sqrt(entity.distanceToSqr(vector3d)) / radius;
                if (percentage <= 1.0D) {
                    double xDiff = entity.getX() - vector3d.x;
                    double yDiff = entity.getEyeY() - vector3d.y;
                    double zDiff = entity.getZ() - vector3d.z;
                    double dist = MathHelper.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
                    if (dist != 0.0D) {
                        xDiff = xDiff / dist;
                        yDiff = yDiff / dist;
                        zDiff = zDiff / dist;
                        double density = getSeenPercent(vector3d, entity);
                        double densityReducedPerc = (1.0D - percentage) * density;
                        entity.hurt(this.getDamageSource(), (float) (damage * densityReducedPerc));
                        double d11 = densityReducedPerc;
                        if (entity instanceof LivingEntity) {
                            d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity) entity, densityReducedPerc);
                        }
                        if (entity != getDamageSource().getEntity()&&entity != getDamageSource().getDirectEntity())
                            entity.setDeltaMovement(entity.getDeltaMovement().add(xDiff * d11, yDiff * d11, zDiff * d11));
                    }
                }
            }
        }

    }
}
