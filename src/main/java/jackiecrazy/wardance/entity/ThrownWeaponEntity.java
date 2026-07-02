package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.action.ActionData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.ActionSetWrapper;
import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.move.motionframe.render.RenderNode;
import jackiecrazy.footwork.move.utils.ArgumentContext;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.MovementUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ThrownWeaponEntity extends FlyingWeaponEntity {
    private static final List<Action> NOTHING = new ArrayList<>();
    private static final EntityDimensions dim = new EntityDimensions(1f, 1f, false);
    boolean attackable = false;
    private MotionManager FORWARD = new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), Vec3.ZERO, 0), 1);
    private boolean dormant = false;
    private double gravity = 0;
    private int pierce = 0;
    private int bounce = 0;
    private boolean lodge_block = true;
    private boolean lodge_entity = true;
    private int auto_recall = -1;
    private boolean recalling = false;
    private boolean fake = false, pickup_flourish = false;
    private double maxRange = 32;
    private List<Action> impactActions = List.of();
    private List<Action> embedActions = List.of();
    private EntityDimensions recalc = null;
    private Vec3 hitVec = Vec3.ZERO;

    public ThrownWeaponEntity(EntityType<? extends FlyingItemEntity> type,
                              Level level) {
        super(type, level);
        setInteractionRange(1);
        setEffect(FlyingWeaponEffect.WEAPON);
        setIntangible(false);
        setInvulnerable(false);
    }

    public ThrownWeaponEntity setMaxRange(double maxRange) {
        this.maxRange = maxRange;
        return this;
    }

    public ThrownWeaponEntity setFlourish(boolean flourish) {
        pickup_flourish = flourish;
        return this;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pickup_flourish = tag.getBoolean("pickup");
        fake = tag.getBoolean("fake");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("pickup", pickup_flourish);
        tag.putBoolean("fake", fake);
    }

    private boolean noCosmetics() {
        return getCosmeticItem() == null || (getCosmeticItem().nodes().length == 1 && getCosmeticItem().nodes()[0] instanceof RenderNode.ItemNode in && in.stack().equals(getHeldItem()));
    }

    public ThrownWeaponEntity setAttackable(boolean f) {
        attackable = f;
        return this;
    }

    public ThrownWeaponEntity setFake(boolean fake) {
        this.fake = fake;
        if (fake && noCosmetics()) {
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

    public boolean canPickup() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return attackable||dormant;
    }

    @Override
    public boolean hurt(DamageSource sauce, float amnt) {
        if (attackable) {
            final ArgumentContext addtlctx = new ArgumentContext(null, this).addContext("target", this).addContext("attacker", sauce.getEntity()).addContext("proxy", sauce.getDirectEntity()).addContext("amount", amnt).addContext("source", sauce);
            ActionData.getCap(this).triggerCallback("hurt", addtlctx);
            if (sauce.getEntity() != null) {
                ActionData.getCap(sauce.getEntity()).triggerCallback("hurt_other", addtlctx);
            }
            if (sauce.getDirectEntity() != null && sauce.getDirectEntity() != sauce.getEntity()) {
                ActionData.getCap(sauce.getDirectEntity()).triggerCallback("hurt_other", addtlctx);
            }
        }
        return false;
    }

    @Override
    public boolean skipAttackInteraction(Entity ent) {
        if(isRemoved()||level().isClientSide)return true;
        if (ent instanceof Player p && (p.getMainHandItem().isEmpty()) && canPickup()) {
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
                if (p.distanceToSqr(this) > maxRange * maxRange) {
                    pickup(p);
                    return;
                }
                if (p.distanceToSqr(this) < 4 && recalling) {
                    pickup(p);
                    return;
                }
            }
        }
    }

    @Override
    public void setDeltaMovement(@NotNull Vec3 vec3) {
        super.setDeltaMovement(vec3);
        //makes sure weapons don't start clipping into walls when they get hit by explosions etc.
        if (dormant && vec3.lengthSqr() > 0) {
            dormant = false;
            setIntangible(false);
        }
    }

    @Override
    protected void updateSpin(MotionManager cur) {
        if (intangible()) return;
        super.updateSpin(cur);
    }

    private void doneHitting() {
        //getIdlePose().setAngularVelocity(Vec3.ZERO.toVector3f());
        if (dormant) return;
        //entity lodge check
        if (lodge_entity && !alreadyHit.isEmpty()) {
            final Entity lodgedMob = alreadyHit.get(alreadyHit.size() - 1);
            if (lodgedMob != null) {
                setState(STATE.FOLLOW);
                //setInteractionRange(0);
                setMotionTarget(lodgedMob);
                // Mob yaw in radians
                float yawRad = (float) Math.toRadians(lodgedMob.getYRot());
                Quaternionf mobRotInv = new Quaternionf().rotateY(yawRad); // inverse yaw
                Vector3f relF = getDeltaMovement().toVector3f();
                mobRotInv.transform(relF);

                Vec3 localOffset = new Vec3(relF.x, relF.y, relF.z).multiply(-1, 1, 1).normalize();
                //if(localOffset.lengthSqr()<0.001)localOffset=new Vec3(0,0,1);
                runImpactActions();
                runEmbedActions();
                setIdlePose(new MotionManagers.FixedMM(new MotionFrame(localOffset, new Vec3(0, 0, -lodgedMob.getBbWidth() / 1.75)), 1));
                dormant = true;
                setUniversalOffset(Vec3.ZERO);
                setDeltaMovement(Vec3.ZERO);
                setIntangible(true);
                return;
            }
        }

        //otherwise lose all velocity and start dropping to the ground
        runImpactActions();
        setImpactActions(NOTHING);
        setDeltaMovement(Vec3.ZERO);
        gravity = (float) Math.min(gravity, -0.04);
    }

//    public boolean canCollideWith(Entity e) {
//        return true;
//    }

    private void runImpactActions() {
        if (!impactActions.isEmpty())
            ActionData.getCap(this).mark(getOwner(), new ActionSetWrapper(impactActions));
    }

    private void runEmbedActions() {
        if (!embedActions.isEmpty())
            ActionData.getCap(this).mark(getOwner(), new ActionSetWrapper(embedActions));
        setImpactActions(NOTHING);
        setEmbedActions(NOTHING);
    }

    private void tickAndRecall() {
        if (recalling || getDeltaMovement().lengthSqr() > 0.001) return;
        if (auto_recall == 0) {
            //recall
            recalling = true;
            setMotionTarget(getOwner());
            setUniversalOffset(Vec3.ZERO);
            setIdlePose(new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 1, 0.3), new Vec3(0, 0, 0)), 3));
            getIdlePose().setAngularVelocity(new Vector3f(-10, 0, 0));
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
        Entity lastHit = alreadyHit.isEmpty() ? null : alreadyHit.get(alreadyHit.size() - 1);
        Entity target = candidates.stream().filter(a -> a != lastHit)
                .min(Comparator.comparingInt(alreadyHit::indexOf)
                             .thenComparingDouble(a -> (pierce > 0 ? -1 : 1) * ((Entity) a).distanceToSqr(this)))//prefer far mobs if there is still pierce, otherwise close mobs
                .orElse(null);//prioritize targets that have not been hit recently
        if (target != null) {
            bounce--;
//            alreadyHit.removeIf(a -> a == target);
            alreadyHit.clear();
            if (lastHit != null)
                alreadyHit.add(lastHit);
            double strength = getDeltaMovement().length() * 0.9;
            yeet(target.getEyePosition(), strength);
            return true;
        }
        return false;
    }

    public boolean canBeCollidedWith() {
        return intangible();
    }

    public boolean pickup(Player player) {
        //if holding nothing, prioritize this slot
        int slot = -1;
        InteractionHand h = InteractionHand.MAIN_HAND;
        boolean success = player.getAbilities().instabuild | fake;
        if (!success) {
            //fake items skip all of this inventory insertion stuff
            if (player.getMainHandItem().isEmpty()) slot = player.getInventory().selected;
            else if (QuiverData.getData(player).sheathe(getPickResult(), false)) {
                success = true;
                QuiverData.getData(player).sync(player);
            } else if (player.getOffhandItem().isEmpty()) {
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
        }
        if (success) {
            this.remove(RemovalReason.UNLOADED_WITH_PLAYER);
            WeaponInteractions.InteractionGroup pickupFlourish = WeaponStats.getSweepInfo(getHeldItem(), player, WeaponStats.AttackType.PICKUP_FLOURISH, false, InteractionHand.MAIN_HAND);
            if (pickup_flourish) {
                //it has to be here as it has to happen before the item is even picked up.
                //there might be a better way....
                if (!pickupFlourish.getInteractions().isEmpty()) {
                    //pickup flourish
                    ItemStack held = player.getMainHandItem();
                    int ticks = player.attackStrengthTicker;
                    try {
                        CombatUtils.quickSwap(player, getHeldItem());
                        CombatUtils.setHandCooldown(player, InteractionHand.MAIN_HAND, 2, false);
                        CombatUtils.setAttackType(player, WeaponStats.AttackType.PICKUP_FLOURISH);
                        if (FlyingWeaponData.getCap(player).getWeapon(InteractionHand.MAIN_HAND) != null)
                            FlyingWeaponData.getCap(player).getWeapon(InteractionHand.MAIN_HAND).clearPath();
                        FlyingWeaponData.getCap(player).forceRefreshWeapons();
                        CombatUtils.processWeaponInteraction(player, null, InteractionHand.MAIN_HAND, player.getAttributeValue(ForgeMod.ENTITY_REACH.get()), pickupFlourish);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        CombatUtils.quickSwap(player, held);
                        player.attackStrengthTicker = ticks;
                    }
                } else {
                    //just selectively run these
                    MovementUtils.applyVelocity(pickupFlourish.getVelocity(), player, pickupFlourish.isSetVelocity());
                    pickupFlourish.on_swing().runEffects(player, player, InteractionHand.MAIN_HAND, getHeldItem());
                }
            }
            player.resetFallDistance();
            //TimeSlowData.getCap(p).alterSpeed(40, 0.3);
        }
        return success;
    }

    @Override
    public void remove(RemovalReason reason) {
        //fixme due to elaborate swap sequences, picking up a thrown weapon with a sprinting sweep will delete it
        // hand attacks, sets the weapon, performs pickup flourish, but it's still part of the attack action so at the end the hand gets reset to what it was before, air.
        if (reason.shouldDestroy() && getOwner() instanceof Player p && !pickup(p)) {
            return;
        }
        super.remove(reason);
    }

    @Override
    protected void extraOnHit(LivingEntity e, Entity target) {
        if (getHeldItem().getItem() instanceof BlockItem)
            target.setDeltaMovement(getDeltaMovement());
        runImpactActions();
        if (!pierce() && !ricochet())
            doneHitting();
    }

    @Override
    protected CombatDamageSource damageSource() {
        return super.damageSource().setProcNormalEffects(false);
    }

    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        if (intangible()) return false;
        return super.onHitEntity(targets);

    }

    public void setImpactActions(List<Action> on_impact) {
        this.impactActions = on_impact;
    }

    public void setEmbedActions(List<Action> on_embed) {
        this.embedActions = on_embed;
    }

    @Override
    public void setIntangible(boolean incorporeal) {
        super.setIntangible(incorporeal);
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(@NotNull Pose p_19975_) {
        if (intangible()) {
            if (recalc == null) {
                double minX = 0, maxX = 0, minY = 0, maxY = 0;
                for (RenderNode rn : getCosmeticItem().nodes()) {
                    minY = Math.min(minY, rn.translation().y);
                    maxY = Math.max(maxY, rn.translation().y + (rn instanceof RenderNode.BlockNode bn && bn.state().hasProperty(DoorBlock.HALF) ? 1 : 0));
                    minX = Math.min(minX, rn.translation().x);
                    minX = Math.min(minX, rn.translation().z);
                    maxX = Math.min(maxX, rn.translation().x);
                    maxX = Math.min(maxX, rn.translation().z);
                }
                Vec3 width = new Vec3(0, 0, (maxX - minX));
                Vec3 height = new Vec3(0, maxY - minY, 0);
                Vec3 worldUp = new Vec3(0, 1, 0);
                Vec3 lookRotated = hitVec.cross(worldUp).cross(hitVec);
                Vec3 add = MovementUtils.resolveVelocity(lookRotated, width, false).add(MovementUtils.resolveVelocity(lookRotated, height, false));
                recalc = dim.scale(1 + (float) add.multiply(1, 0, 1).length(), 1 + (float) add.y);
                this.markHurt();
                this.hasImpulse = true;
                setPos(position());
            }
            return recalc;
        } else recalc = null;
        return super.getDimensions(p_19975_);
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        if (intangible()) return;
        runImpactActions();
        if (ricochet()) return;
        if (lodge_block || hitFace == Direction.UP) {
            runEmbedActions();

            setIntangible(true);
            dormant = true;
            hitVec = location.subtract(position());
            //getIdlePose().setAngularVelocity(Vec3.ZERO.toVector3f());
            setDeltaMovement(Vec3.ZERO);
            setPos(location);
            gravity = 0;
        } else doneHitting();
    }

    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> a) {
        if (IS_INTANGIBLE.equals(a)) {
            this.refreshDimensions();
        }
    }
}
