package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.action.RemoveFromExistenceAction;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.items.WarItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WindBladeEntity extends ThrownWeaponEntity {
    private List<LivingEntity> targets = new ArrayList<>();
    private int lastAttackTime;

    public WindBladeEntity(EntityType<? extends FlyingItemEntity> type,
                           Level level) {
        super(type, level);
        setPierce(9999);
        setState(STATE.THROW_TRACK);
        setFlourish(false);
        setFake(true);
        setGravity(0);
        setEffect(FlyingWeaponEffect.TRAIL);
        //setEffect();
        setTrailColor(Color.WHITE);
        setHeldItem(new ItemStack(WarItems.PROJECTILE.get()));
        getIdlePose().setAngularVelocity(new Vector3f(0,25,0));
        if (!level.isClientSide)
            setEmbedActions(List.of(new RemoveFromExistenceAction()));
    }

    @Override
    public boolean skipAttackInteraction(Entity ent) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void addTargets(Collection<LivingEntity> t) {
        targets.addAll(t);
        findNewTarget();
    }

    private void findNewTarget() {
        if (!targets.isEmpty()) {
            final LivingEntity track = targets.remove(WarDance.rand.nextInt(targets.size()));
            setMotionTarget(track);
        } else {
            //find new targets to hit
            targets.addAll(level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(16), EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(a -> !TargetingUtils.isAlly(a, getOwner()))));
            //no targets? keep flying and try again next tick
        }
    }

    @Override
    public void tick() {
        super.tick();
//        level().addParticle(ParticleTypes.SWEEP_ATTACK, xOld, yOld, zOld, 0, 0, 0);
        if (getMotionTarget() == null || getMotionTarget() == getOwner() || getMotionTarget().isRemoved()) {
            findNewTarget();
        }
        if (getMotionTarget() == null || level().isClientSide) return;
        //hack to allow a curving back wind blade to hit again
        if (tickCount - lastAttackTime > 10)
            alreadyHit.remove(getMotionTarget());
        final Vec3 target = position().vectorTo(getOwner().getEyePosition());

//        Quaternionf rotation = new Quaternionf().rotationTo(getDeltaMovement().toVector3f(), target.toVector3f());
//        setIdlePose(new MotionManagers.FixedMM(new MotionFrame(target, Vec3.ZERO, rotation), 10).setAngularVelocity(new Vector3f(0,0,1)));
        if(tickCount>80)remove(RemovalReason.UNLOADED_WITH_PLAYER);
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean pickup(Player player) {
        remove(RemovalReason.UNLOADED_WITH_PLAYER);
        return false;
    }

    @Override
    public boolean canPickup() {
        return false;
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        remove(RemovalReason.UNLOADED_WITH_PLAYER);
    }

    @Override
    protected void onHitEntity(LivingEntity e, Entity target) {
        super.onHitEntity(e, target);
        lastAttackTime = tickCount;
        if (target == getMotionTarget())
            findNewTarget();
    }

    @Override
    public void yeet(Vec3 to, double strength) {
        super.yeet(to, strength);
        setHitInfo(new HitInfo());
    }
}
