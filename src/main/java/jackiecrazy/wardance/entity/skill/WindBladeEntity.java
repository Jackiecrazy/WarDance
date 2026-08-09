package jackiecrazy.wardance.entity.skill;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.MobSpecs;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import jackiecrazy.wardance.items.WarItems;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
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
import java.util.Optional;

public class WindBladeEntity extends ThrownWeaponEntity {
    private List<LivingEntity> targets = new ArrayList<>();
    private int lastAttackTime;
    private float armorReduction = 0.5f;

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
        setTrailColor(Color.LIGHT_GRAY);
        setHeldItem(new ItemStack(WarItems.PROJECTILE.get()));
        getIdlePose().setAngularVelocity(new Vector3f(0, 25, 0));
        skillUsed = WarSkills.WIND_SCAR.get();
    }

    public WindBladeEntity setSkillUsed(Skill skillUsed) {
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

    private void findNewTarget() {
        if (!targets.isEmpty()) {
            final LivingEntity track = targets.remove(WarDance.rand.nextInt(targets.size()));
            setMotionTarget(track);
        } else {
            //find new targets to hit
            targets.addAll(level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(16),
                                                      EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(this::validTarget)));
            //no targets? keep flying and try again next tick
        }
    }

    private boolean validTarget(Entity a) {
        return a != getOwner()
                && !a.isRemoved()
                && !alreadyHit.contains(a)
                && TargetingUtils.isHostile(a, getOwner());
                //&& ((!(a instanceof LivingEntity le)) || !Marks.getCap(le).isMarked(skillUsed));
    }

    @Override
    public void tick() {
        super.tick();
//        level().addParticle(ParticleTypes.SWEEP_ATTACK, xOld, yOld, zOld, 0, 0, 0);
        if (getMotionTarget() == null || validTarget(getMotionTarget())) {
            findNewTarget();
        }
        if (getMotionTarget() == null || level().isClientSide) return;
        //hack to allow a curving back wind blade to hit again
        if (tickCount - lastAttackTime > 10)
            alreadyHit.remove(getMotionTarget());
        if (tickCount > 80) remove(RemovalReason.UNLOADED_WITH_PLAYER);
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
    protected void runEmbedActions() {
        remove(RemovalReason.UNLOADED_WITH_PLAYER);
    }

    @Override
    protected void extraOnHit(LivingEntity owner, Entity target) {
        super.extraOnHit(owner, target);
        lastAttackTime = tickCount;
        if (target == getMotionTarget())
            findNewTarget();
    }

    @Override
    public void yeet(Vec3 to, double strength) {
        super.yeet(to, strength);
        setHitInfo(new HitInfo());
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
        final CombatDamageSource sauce = new CombatDamageSource(getOwner(), this).setKnockbackPercentage(0).flagBreach(false).setSkillUsed(skillUsed).setProcSkillEffects(true).setProjectile().setArmorReductionPercentage(armorReduction);
        for (Entity e : targets) {
            float damage = 4;
            if(!validTarget(e))continue;
            if (e instanceof LivingEntity le) {
                Optional<SkillData> a = Marks.getCap(le).getActiveMark(skillUsed);
                if (a.isPresent()) {
                    for (int i = 0; i < a.get().getArbitraryFloat(); i++) {
                        damage *= 0.7f;
                    }
                }
            }
            //if (!validTarget(e)) continue;
            e.invulnerableTime = 0;
            e.hurt(sauce, damage);

            alreadyHit.add(e);
        }
        return true;
    }
}
