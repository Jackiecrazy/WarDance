package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GhostBlockEntity extends ThrownWeaponEntity {

    public GhostBlockEntity(EntityType<? extends FlyingItemEntity> type,
                            Level level) {
        super(type, level);
    }

    @Override
    public boolean pickup(Player p) {
        FlyingWeaponData.getCap(p).setHeldBlock(this);
        this.setUniversalOffset(new Vec3(0, p.getBbHeight(), 0.5));
        setDeltaMovement(Vec3.ZERO);
        setTransitioning(true);
        setState(STATE.FOLLOW);
        return true;
    }

    @Override
    public boolean isReal() {
        return !transitioning();
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        if(transitioning())return;
        super.onHitBlock(blockPos, hitFace, location);
        //shatter

        if (level() instanceof ServerLevel sl) {
            BlockState base = level().getBlockState(blockPos);
            if (getHeldItem().getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                base = block.defaultBlockState();
            }
            for (int i = 0; i < 60; i++) {
                Vec3 velocity = location
                        .add(level().random.nextGaussian() * 0.2,
                             level().random.nextGaussian() * 0.2,
                             level().random.nextGaussian() * 0.2)
                        .normalize()
                        .scale(0.15);

                Vec3i pain = hitFace.getNormal();
                Vec3 loc=location.add(pain.getX(), pain.getY(), pain.getZ());
                sl.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, base),
                        loc.x, loc.y, loc.z,
                        1,
                        velocity.x, velocity.y, velocity.z,
                        0
                );
            }
        }
        shockwave(5);
        remove(RemovalReason.KILLED);
    }

    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        if(transitioning())return false;
        boolean ret = super.onHitEntity(targets);
        if (ret) {
            //shatter
            if (level() instanceof ServerLevel sl && getHeldItem().getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                BlockState base = block.defaultBlockState();
                for (int i = 0; i < 10; i++) {
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
            shockwave(5);
            remove(RemovalReason.KILLED);

        }
        return ret;
    }

    private void shockwave(double radius) {
        for (Entity t : level().getEntities(this, this.getBoundingBox().inflate(radius), (a -> !TargetingUtils.isAlly(a, getOwner())))) {
            float strength = 1.3f;
            if (t instanceof LivingEntity e) {
                strength = Math.min(strength, 0.2f + Mth.clamp(strength * 1 - CombatData.getCap(e).getPosturePercentage(), 0, 1));
            }
            CombatUtils.knockBack(t, this, strength, true, false);

        }
    }
}
