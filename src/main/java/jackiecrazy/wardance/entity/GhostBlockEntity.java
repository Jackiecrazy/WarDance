package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.utils.MobilityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GhostBlockEntity extends CustomProjectile {

    public GhostBlockEntity(EntityType<? extends FlyingItemEntity> type,
                            Level level) {
        super(type, level);
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        if (intangible()) return;
        super.onHitBlock(blockPos, hitFace, location);
        shockwave(5);
    }

    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        if (intangible()) return false;
        boolean ret = super.onHitEntity(targets);
        if (ret) {
            shockwave(5);
        }
        return ret;
    }

    protected void shockwave(double radius) {
        if (level() instanceof ServerLevel sl && getHeldItem().getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            BlockState base = block.defaultBlockState();
            for (int i = 0; i < 60; i++) {
                Vec3 velocity = position()
                        .add(level().random.nextGaussian() * 0.2,
                             level().random.nextGaussian() * 0.2,
                             level().random.nextGaussian() * 0.2)
                        .normalize()
                        .scale(0.15);

                sl.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, base),
                        position().x, position().y, position().z,
                        1,
                        velocity.x, velocity.y, velocity.z,
                        0
                );
            }
        }
        for (Entity t : level().getEntities(this, this.getBoundingBox().inflate(radius), (a -> !TargetingUtils.isAlly(a, getOwner())))) {
            float strength = 1.3f;
            if (t instanceof LivingEntity e) {
                strength = Math.min(strength, 0.2f + Mth.clamp(strength * 1 - CombatData.getCap(e).getPosturePercentage(), 0, 1));
            }
            MobilityUtils.knockBack(t, this, strength, true, false);

        }
        //shatter
        remove(RemovalReason.UNLOADED_WITH_PLAYER);
    }
}
