package jackiecrazy.wardance.entity.skill;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.move.motionframe.render.RenderItemGroup;
import jackiecrazy.footwork.move.motionframe.render.RenderNode;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.config.MobSpecs;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TimberfallEntity extends ThrownWeaponEntity {
    protected static final List<MotionFrame> CHOP = List.of(
            new MotionFrame(new Vec3(0, 1, 0), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(0, 1, 0.2), new Vec3(0, 0, 1)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON).setHit(HitInfo.BREACH)),
            new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1)));
    protected static final MotionManager FALL = new MotionManagers.DefinitionMM(new MotionGroup(CHOP, EasingFunctionEnum.IN_CUBIC, 30));
    protected static final MotionManager STICK = new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1)), 30);
    private static final int GROW = 13;
    private static final BlockState LOG = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState();
    private static final RenderItemGroup[] GROWTH_STAGE = {
            new RenderItemGroup(makeNode(Blocks.SPRUCE_SAPLING.defaultBlockState(), 0, -0.5, 0)),
            new RenderItemGroup(makeNode(LEAF, 0, -0.5, 0)),
            new RenderItemGroup(makeNode(LOG, 0, -0.5, 0), makeNode(LEAF, 0, 0.5, 0)),
            new RenderItemGroup(makeNode(LOG, 0, -0.5, 0), makeNode(LOG, 0, 0.5, 0), makeNode(LEAF, 0, 1.5, 0)),
            new RenderItemGroup(makeNode(LOG, 0, -0.5, 0), makeNode(LOG, 0, 0.5, 0), makeNode(LOG, 0, 1.5, 0), makeNode(LEAF, 0, 2.5, 0)
                    , makeNode(LEAF, 1, 0.5, 0), makeNode(LEAF, 1, 0.5, 1), makeNode(LEAF, 1, 0.5, -1)
                    , makeNode(LEAF, 0, 0.5, 1), makeNode(LEAF, 0, 0.5, -1)
                    , makeNode(LEAF, -1, 0.5, 0), makeNode(LEAF, -1, 0.5, 1), makeNode(LEAF, -1, 0.5, -1)
            ),
            new RenderItemGroup(makeNode(LOG, 0, -0.5, 0), makeNode(LOG, 0, 0.5, 0), makeNode(LOG, 0, 1.5, 0), makeNode(LOG, 0, 2.5, 0), makeNode(LEAF, 0, 3.5, 0)
                    , makeNode(LEAF, 1, 1.5, 0), makeNode(LEAF, 1, 1.5, 0), makeNode(LEAF, 1, 1.5, 1), makeNode(LEAF, 1, 1.5, -1)
                    , makeNode(LEAF, 0, 1.5, 1), makeNode(LEAF, 0, 1.5, -1)
                    , makeNode(LEAF, -1, 1.5, 0), makeNode(LEAF, -1, 1.5, 1), makeNode(LEAF, -1, 1.5, -1)
            ),
            new RenderItemGroup(makeNode(LOG, 0, -0.5, 0), makeNode(LOG, 0, 0.5, 0), makeNode(LOG, 0, 1.5, 0), makeNode(LOG, 0, 2.5, 0), makeNode(LOG, 0, 3.5, 0), makeNode(LEAF, 0, 4.5, 0)
                    , makeNode(LEAF, 1, 2.5, 0), makeNode(LEAF, 1, 2.5, 0), makeNode(LEAF, 1, 2.5, 1), makeNode(LEAF, 1, 2.5, -1)
                    , makeNode(LEAF, 0, 2.5, 1), makeNode(LEAF, 0, 2.5, -1)
                    , makeNode(LEAF, -1, 2.5, 0), makeNode(LEAF, -1, 2.5, 1), makeNode(LEAF, -1, 2.5, -1)
            ),
            new RenderItemGroup(makeNode(LOG, 0, -0.5, 0), makeNode(LOG, 0, 0.5, 0), makeNode(LOG, 0, 1.5, 0), makeNode(LOG, 0, 2.5, 0), makeNode(LOG, 0, 3.5, 0), makeNode(LOG, 0, 4.5, 0)
                    , makeNode(LEAF, 0, 5.5, 0), makeNode(LEAF, 1, 5.5, 0), makeNode(LEAF, -1, 5.5, 0), makeNode(LEAF, 0, 5.5, 1), makeNode(LEAF, 0, 5.5, -1)
                    , makeNode(LEAF, 1, 3.5, 0), makeNode(LEAF, 1, 3.5, 0), makeNode(LEAF, 2, 3.5, 0), makeNode(LEAF, 1, 3.5, 1), makeNode(LEAF, 1, 3.5, -1)
                    , makeNode(LEAF, 0, 3.5, 1), makeNode(LEAF, 0, 3.5, -1), makeNode(LEAF, 0, 3.5, 2), makeNode(LEAF, 0, 3.5, -2)
                    , makeNode(LEAF, -1, 3.5, 0), makeNode(LEAF, -2, 3.5, 0), makeNode(LEAF, -1, 3.5, 1), makeNode(LEAF, -1, 3.5, -1)
            )
    };
    //simple logic: throw with no hitinfo and embed on ground.
    // When embedded, change display item and size every few seconds.
    // When hit, start falling again, or pushed, play fall over sequence and deal damage
    private List<LivingEntity> targets = new ArrayList<>();
    private int growTimer = GROW;
    private int height = 0;

    public TimberfallEntity(EntityType<? extends FlyingItemEntity> type,
                            Level level) {
        super(type, level);
        setState(STATE.THROW_NATURAL);
        setEffect(FlyingWeaponEffect.WEAPON);
        setHeldItem(new ItemStack(Items.IRON_AXE));
        setCosmeticItem(GROWTH_STAGE[0]);
        setIdlePose(new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, -1), new Vec3(0, 0, 1)), 3));
        setGravity(-0.1);
        setPierce(9999);
        setAttackable(true);
        pickup_flourish=false;
        setFake(true);
        skillUsed= WarSkills.TIMBERFALL.get();
    }

    private static RenderNode makeNode(BlockState b, double x, double y, double z) {
        return new RenderNode.BlockNode(b, Vec3.ZERO, new Vec3(x, y, z));
    }

    @Override
    public boolean hurt(DamageSource sauce, float amnt) {
        fall();
        return false;
    }

    @Override
    public boolean skipAttackInteraction(Entity ent) {
        fall();
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    public void addTargets(Collection<LivingEntity> t) {
        targets.addAll(t);
        findNewTarget();
    }

    private LivingEntity findNewTarget() {
        //find new target to hit
        targets.addAll(level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(height), EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(a -> !TargetingUtils.isAlly(a, getOwner()))));
        if (targets.isEmpty()) return getOwner();
        return targets.get(WarDance.rand.nextInt(targets.size()));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (dormant && getState() == STATE.THROW_NATURAL) {
            grow();
            if (height > 2) {
                boolean fallen = false;
                for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.003))) {
                    if (!TargetingUtils.isAlly(target, getOwner()) && target.horizontalCollision) {
                        if (!fallen)
                            fallOn(target);
                        fallen = true;
                        CombatData.getCap(target).consumePosture(getOwner(), 12, 0.5f, false);
                        target.hurt(new CombatDamageSource(getOwner()).setPostureDamage(0).setDamageTyping(FootworkDamageArchetype.PHYSICAL).setDamageDealer(null).flagBreach(false).setProcAttackEffects(true).setProcSkillEffects(true).setSkillUsed(skillUsed), (float) getOwner().getAttributeValue(WarAttributes.KICK_DAMAGE.get()));
                        target.setDeltaMovement(Vec3.ZERO);
                    }
                }
            }
        } else if (getState() == STATE.FOLLOW && isIdle()) {
            remove(RemovalReason.UNLOADED_WITH_PLAYER);
        }
    }

    private void grow() {
        if (height >= GROWTH_STAGE.length - 1) return;
        growTimer--;
        if (growTimer <= 0) {
            growTimer = GROW;
            height++;
            setCosmeticItem(GROWTH_STAGE[height]);
            recalc = null;
            refreshDimensions();
        }
    }

    public boolean canBeCollidedWith() {
        return super.canBeCollidedWith() || dormant || getState() == STATE.FOLLOW;
    }

    @Override
    protected void runEmbedActions() {
        grow();
        setUniversalOffset(new Vec3(0, 1, 0));
    }

    @Override
    public void yeet(Vec3 to, double strength) {
        super.yeet(to, strength);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void push(Entity pusher) {
        if (height < 2 || getState() == STATE.FOLLOW) return;
        //collapse
        super.push(pusher);
        fallOn(pusher);
    }

    private void fall() {
        //if manually triggered, find a nearby thing to fall on
        if (height > 2) {
            fallOn(findNewTarget());
        }
    }

//    @Override
//    public EntityDimensions getDimensions(@NotNull Pose p_19975_) {
//        super.getDimensions(p_19975_);
//        if(recalc!=null)
//        recalc=new EntityDimensions(1,recalc.height, false);
//        return recalc;
//    }

    private void fallOn(Entity pusher) {
        if (pusher == null || getState() == STATE.FOLLOW) return;
        lockPos(position());
        setUniversalOffset(new Vec3(0, 0, 0));//reset universal offset. Y-1 is naturally applied during render to center the weapon when thrown
        lockLook(pusher.position().subtract(position()).multiply(1, 0, 1));
        setState(STATE.FOLLOW);
        queuePath(FALL);
        setInteractionRange(height);
        //setIntangible(false);
        queuePath(STICK);
    }

    @Override
    protected boolean updateMotionTargets(boolean forceskip) {
        boolean ret = super.updateMotionTargets(forceskip);
        setIntangible(moveQueue.size()<2);
        return ret;
    }

    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        if (getState() != STATE.FOLLOW||intangible()) return false;
        targets = targets.stream().distinct()
                .filter(tg -> tg != this && tg != owner && !alreadyHit.contains(tg) &&
                        ((tg instanceof ThrownWeaponEntity twe && twe.isAttackable()) || (!TargetingUtils.isAlly(tg, owner) &&
                                !tg.getType().is(MobSpecs.IGNORED_BY_SWEEP) &&
                                //tg.hasPassenger(owner)&&
                                !tg.isInvulnerable()))).toList();
        if (targets.isEmpty()) return false;
        float dmg = height + 4;
        final CombatDamageSource sauce = new CombatDamageSource(getOwner(), this).setPostureDamage(dmg * 4).setAttackingHand(null).setDamageDealer(null).flagBreach(true).setKnockbackPercentage(0).setSkillUsed(skillUsed).setProcSkillEffects(true);
        for (Entity e : targets) {
            e.invulnerableTime = 0;
            e.hurt(sauce, dmg);

            alreadyHit.add(e);
        }
        return true;
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> a) {
        if (IS_INTANGIBLE.equals(a) || COSMETIC.equals(a)) {
            recalc = null;
            this.refreshDimensions();
        }
    }
}
