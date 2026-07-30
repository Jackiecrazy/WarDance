package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionGroup;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.move.motionframe.render.RenderItemGroup;
import jackiecrazy.footwork.move.motionframe.render.RenderNode;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.MobSpecs;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.network.syncher.EntityDataAccessor;
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
import java.util.Optional;

public class TimberfallEntity extends ThrownWeaponEntity {
    protected static final List<MotionFrame> CHOP = List.of(new MotionFrame(new Vec3(0, 1, 0), new Vec3(0, 0, 1)), new MotionFrame(new Vec3(0, 1, 0.2), new Vec3(0, 0, 1)), new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1)));
    protected static final MotionManager FALL = new MotionManagers.DefinitionMM(new MotionGroup(CHOP, EasingFunctionEnum.IN_SINE, 30));
    protected static final MotionManager STICK = new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, 1)), 30);
    private static final int GROW = 20;
    //simple logic: throw with no hitinfo and embed on ground.
    // When embedded, change display item and size every few seconds.
    // When hit, start falling again, or pushed, play fall over sequence and deal damage
    private List<LivingEntity> targets = new ArrayList<>();
    private int growTimer = 0;
    private Skill skillUsed = WarSkills.WIND_SCAR.get();
    private int height = 0;

    public TimberfallEntity(EntityType<? extends FlyingItemEntity> type,
                            Level level) {
        super(type, level);
        setState(STATE.THROW_NATURAL);
        setEffect(FlyingWeaponEffect.WEAPON);
        setHeldItem(new ItemStack(Items.IRON_AXE));
        setCosmeticItem(new ItemStack(Items.OAK_SAPLING));
        setIdlePose(new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, -1), new Vec3(0, 0, 1)), 3));
        setGravity(-0.1);
    }

    public TimberfallEntity setSkillUsed(Skill skillUsed) {
        this.skillUsed = skillUsed;
        return this;
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
        if (dormant && getState() == STATE.THROW_NATURAL)
            grow();
        else if (getState() == STATE.FOLLOW && isIdle()) {
            remove(RemovalReason.UNLOADED_WITH_PLAYER);
        }
    }

    private void grow() {
        if (height > 7) return;
        growTimer--;
        if (growTimer <= 0) {
            growTimer = GROW;
            height++;
            RenderNode[] nodes = new RenderNode[height];
            Vec3 up = new Vec3(0, -1, 0);
            BlockState log = Blocks.OAK_LOG.defaultBlockState();
            for (int a = 0; a < nodes.length; a++) {
                if (a == 6) log = Blocks.OAK_LEAVES.defaultBlockState();
                nodes[a] = new RenderNode.BlockNode(log, Vec3.ZERO, up);
                up = up.add(0, 1, 0);
            }
            setCosmeticItem(new RenderItemGroup(nodes));
            recalc = null;
            refreshDimensions();
        }
    }

    public boolean canBeCollidedWith() {
        return super.canBeCollidedWith() || dormant;
    }

    @Override
    protected void runEmbedActions() {

        grow();
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

    private void fallOn(Entity pusher) {
        lockPos(position());
        lockLook(position().subtract(pusher.position()).multiply(1, 0, 1));
        setState(STATE.FOLLOW);
        queuePath(FALL);
        setIntangible(false);
        //queuePath(STICK);
    }


    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        targets = targets.stream().distinct()
                .filter(tg -> tg != this && tg != owner && !alreadyHit.contains(tg) &&
                        ((tg instanceof ThrownWeaponEntity twe && twe.isAttackable()) || (!TargetingUtils.isAlly(tg, owner) &&
                                !tg.getType().is(MobSpecs.IGNORED_BY_SWEEP) &&
                                //tg.hasPassenger(owner)&&
                                !tg.isInvulnerable()))).toList();
        if (targets.isEmpty()) return false;
        final CombatDamageSource sauce = new CombatDamageSource(getOwner(), this).setKnockbackPercentage(0).setSkillUsed(skillUsed).setProcSkillEffects(true);
        for (Entity e : targets) {
            float damage = 4;
            if (e instanceof LivingEntity le) {
                Optional<SkillData> a = Marks.getCap(le).getActiveMark(skillUsed);
                if (a.isPresent()) {
                    for (int i = 0; i < a.get().getArbitraryFloat(); i++) {
                        damage *= 0.7f;
                    }
                }
            }
            e.hurt(sauce, damage);

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
