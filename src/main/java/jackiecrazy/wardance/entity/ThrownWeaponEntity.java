package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.SweepAttack;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class ThrownWeaponEntity extends FlyingWeaponEntity {
    private MotionManager FORWARD = new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), Vec3.ZERO, 0), 1);
    private boolean dormant = false;
    private double gravity = 0;
    private int pierce = 0;
    private int bounce = 0;
    private boolean lodge_block = true;
    private boolean lodge_entity = true;
    private int auto_recall = -1;
    private boolean recalling = false;
    private boolean fake = false;

    public ThrownWeaponEntity(EntityType<? extends FlyingItemEntity> type,
                              Level level) {
        super(type, level);
        setInteractionRange(1);
        setEffect(FlyingWeaponEffect.WEAPON);
        setIntangible(false);
    }

    public ThrownWeaponEntity setFake(boolean fake) {
        this.fake = fake;
        if (fake) {
            setEffect(FlyingWeaponEffect.BIG_SHADOW);
        } else setEffect(FlyingWeaponEffect.WEAPON);
        return this;
    }

    public ThrownWeaponEntity setHitInfo(HitInfo hi) {
        cacheInfo = hi;
        return this;
    }

    public ThrownWeaponEntity setAutoRecall(int auto_recall) {
        this.auto_recall = auto_recall;
        return this;
    }

    public ThrownWeaponEntity setGravity(double gravity) {
        this.gravity = gravity;
        return this;
    }

    public ThrownWeaponEntity setPierce(int pierce) {
        this.pierce = pierce;
        return this;
    }

    public ThrownWeaponEntity setBounce(int bounce) {
        this.bounce = bounce;
        return this;
    }

    public ThrownWeaponEntity setLodgeBlock(boolean lodge_block) {
        this.lodge_block = lodge_block;
        return this;
    }

    public ThrownWeaponEntity setLodgeEntity(boolean lodge_entity) {
        this.lodge_entity = lodge_entity;
        return this;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    public boolean isReal() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity ent) {
        if (!ent.level().isClientSide && ent instanceof Player p && p.getMainHandItem().isEmpty() && isReal()) {
            return pickup(p);
        }
        return false;
    }

    @Override
    public void tick() {
        if (getState() == STATE.FOLLOW && getMotionTarget() instanceof LivingEntity e && e.isDeadOrDying()) {
            alreadyHit.clear();
            setIntangible(false);
            setState(STATE.THROW_NATURAL);
            setIdlePose(FORWARD);
            dormant = false;
            gravity = -0.04f;
        }
        super.tick();
        tickAndRecall();
        if (!level().isClientSide) {
            if (gravity != 0 && !intangible())
                addDeltaMovement(new Vec3(0, gravity, 0));
            if (getOwner() instanceof Player p) {
                if (p.distanceToSqr(this) > 32 * 32) pickup(p);
                if (p.distanceToSqr(this) < 4 && recalling) pickup(p);
            }
        }
    }

    @Override
    public void setDeltaMovement(@NotNull Vec3 vec3) {
        super.setDeltaMovement(vec3);
        //makes sure weapons don't start clipping into walls when they get hit by explosions etc.
        if (dormant&&vec3.lengthSqr()>0) {
            dormant = false;
            setIntangible(false);
        }
    }

    private void doneHitting() {
        getIdlePose().setAngularVelocity(Vec3.ZERO.toVector3f());
        if (dormant) return;
        //entity lodge check
        if (lodge_entity && !alreadyHit.isEmpty()) {
            final Entity lodgedMob = alreadyHit.get(alreadyHit.size() - 1);
            setState(STATE.FOLLOW);
            //setInteractionRange(0);
            setMotionTarget(lodgedMob);
            // Mob yaw in radians
            float yawRad = (float) Math.toRadians(-lodgedMob.getYRot());
            Quaternionf mobRotInv = new Quaternionf().rotateY(yawRad); // inverse yaw
            Vector3f relF = getDeltaMovement().toVector3f();
            mobRotInv.transform(relF);

            Vec3 localOffset = new Vec3(relF.x, relF.y, relF.z).multiply(-1, 1, 1).normalize();
            //if(localOffset.lengthSqr()<0.001)localOffset=new Vec3(0,0,1);
            setIdlePose(new MotionManagers.FixedMM(new MotionFrame(localOffset, new Vec3(0, 0, -lodgedMob.getBbWidth() / 1.75)), 1));
            setUniversalOffset(Vec3.ZERO);
            setDeltaMovement(Vec3.ZERO);
            setIntangible(true);
            dormant = true;
        } else {
            //otherwise lose all velocity and start dropping to the ground
            setDeltaMovement(Vec3.ZERO);
            gravity = (float) Math.min(gravity, -0.04);
        }
    }

    private void tickAndRecall() {
        if (recalling || getDeltaMovement().lengthSqr() > 0.001) return;
        if (auto_recall == 0) {
            //recall
            recalling = true;
            setMotionTarget(getOwner());
            setUniversalOffset(Vec3.ZERO);
            setIdlePose(new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 1, 0.3), new Vec3(0, 0, 0)), 3));
            getIdlePose().setAngularVelocity(new Vector3f(-1, 0, 0));
            setState(STATE.FOLLOW);
            pierce = 99999;//to prevent getting stuck
        }
        auto_recall--;
    }

    private boolean pierce() {
        if (pierce > 0) {
            pierce--;
            return true;
        }
        return false;
    }

    private boolean ricochet() {
        if (bounce <= 0) {
            return false;
        }
        final List<Entity> candidates = level().getEntities(this, this.getBoundingBox().inflate(8), (a ->
                a instanceof LivingEntity && !GeneralUtils.viewBlocked(this, a, false) && !TargetingUtils.isAlly(this, a)));
        Entity lastHit = alreadyHit.size() == 0 ? null : alreadyHit.get(alreadyHit.size() - 1);
        Entity target = candidates.stream().filter(a -> a != lastHit)
                .min(Comparator.comparingInt(alreadyHit::indexOf))
                .orElse(null);//prioritize targets that have not been hit recently
        if (target != null) {
            bounce--;
            alreadyHit.removeIf(a -> a == target);
            double strength = getDeltaMovement().length() * 0.9;
            yeet(target.getEyePosition(), strength);
            return true;
        }
        return false;
    }

//    public boolean canCollideWith(Entity e) {
//        return true;
//    }

    public boolean canBeCollidedWith() {
        return true;
    }

    public boolean pickup(Player player) {
        //if holding nothing, prioritize this slot
        int slot = -1;
        InteractionHand h = InteractionHand.MAIN_HAND;
        boolean success = player.getAbilities().instabuild | fake;
        if (player.getMainHandItem().isEmpty()) slot = player.getInventory().selected;
        else if (player.getOffhandItem().isEmpty()) {
            //special offhand handling
            slot = Inventory.SLOT_OFFHAND;
            h = InteractionHand.OFF_HAND;
            if (!success)
                player.setItemInHand(InteractionHand.OFF_HAND, getPickResult());
            success = true;
        }
        CombatUtils.allowCombatHotbarPickup = true;
        if (!success)
            success = player.getInventory().add(slot, getPickResult());
        CombatUtils.allowCombatHotbarPickup = false;
        if (success) {
            this.remove(RemovalReason.UNLOADED_WITH_PLAYER);

            //pickup flourish
            ItemStack held = player.getMainHandItem();
            int ticks = player.attackStrengthTicker;
            try {
                CombatUtils.quickSwap(player, getHeldItem());
                CombatUtils.setHandCooldown(player, InteractionHand.MAIN_HAND, 2, false);
                CombatUtils.setAttackType(player, WeaponStats.AttackType.PICKUP_FLOURISH);
                FlyingWeaponData.getCap(player).getWeapon(InteractionHand.MAIN_HAND).clearPath();
                FlyingWeaponData.getCap(player).forceRefreshWeapons();
                CombatUtils.processWeaponInteraction(player, null, InteractionHand.MAIN_HAND, player.getAttributeValue(ForgeMod.ENTITY_REACH.get()));
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                CombatUtils.quickSwap(player, held);
                player.attackStrengthTicker = ticks;
            }
            player.resetFallDistance();
            //TimeSlowData.getCap(p).alterSpeed(40, 0.3);
        }
        return success;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy() && getOwner() instanceof Player p && !pickup(p)) {
            return;
        }
        super.remove(reason);
    }

    @Override
    protected void extraOnHit(LivingEntity e, Entity target) {
        if (getHeldItem().getItem() instanceof BlockItem)
            target.setDeltaMovement(getDeltaMovement());
        if (!pierce() && !ricochet())
            doneHitting();
    }

    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        if (intangible()) return false;
        return super.onHitEntity(targets);

    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        if (intangible()) return;
        if (ricochet()) return;
        if (lodge_block || hitFace == Direction.UP) {

//            setDeltaMovement(Vec3.ZERO);
//            setPos(location);

            //todo open this for datapacking
            ParticleUtils.playSweepParticle(FootworkParticles.IMPACT.get(), this, this.position(), 0, 3, Color.WHITE, 0);
            List<Entity> selfTarget = level().getEntities(getOwner(), getBoundingBox().inflate(0.3f), e -> e != getOwner() && e.isAlive() && e.isAttackable());
            onHitEntity(selfTarget);

            setIntangible(true);
            dormant = true;
            getIdlePose().setAngularVelocity(Vec3.ZERO.toVector3f());
            setDeltaMovement(Vec3.ZERO);
            gravity = 0;
        } else doneHitting();
    }
}
