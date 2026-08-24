package jackiecrazy.wardance.entity.skill;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.config.MobSpecs;
import jackiecrazy.wardance.entity.CustomProjectile;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class BaseballEntity extends CustomProjectile {
    public BaseballEntity setFriendly(boolean friendly) {
        this.friendly = friendly;
        return this;
    }

    protected boolean friendly = false;

    @Override
    public boolean fuzzyTargeting() {
        return super.fuzzyTargeting();
    }

    public BaseballEntity(EntityType<? extends FlyingItemEntity> type,
                          Level level) {
        super(type, level);
        activeSkill = WarSkills.THROW.get();
        lodge_block = lodge_entity = true;
        pierce = bounce = 0;
        setCosmeticItem(new ItemStack(Items.OAK_DOOR));
    }

    @Override
    public boolean pickup(Player player) {
        boolean ret = super.pickup(player);
        double height = player.getBbHeight();
        if (getTetheringEntity() != null)
            height += getTetheringEntity().getBbHeight();
        this.setUniversalOffset(new Vec3(0, height, 0.5));
        return ret;
    }

    @Override
    public void drag(Entity target, double strength, int duration) {
        if (target instanceof Player p && !CombatData.getCap(p).isStunned() && p.isShiftKeyDown())
            friendly = true;
        super.drag(target, strength, duration);
    }

    @Override
    public boolean hasEffect(FlyingWeaponEffect f) {
        return false;
    }

    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        if (level().isClientSide()) return false;
        alreadyHit.add(getTetheringEntity());
        targets = targets.stream().distinct()
                .filter(tg -> tg != this && tg != owner && !alreadyHit.contains(tg) &&
                        ((tg instanceof ThrownWeaponEntity twe && twe.isAttackable()) || (!TargetingUtils.isAlly(tg, owner) &&
                                !tg.getType().is(MobSpecs.IGNORED_BY_SWEEP) &&
                                //tg.hasPassenger(owner)&&
                                !tg.isInvulnerable()))).toList();
        if (!targets.isEmpty()) {
            alreadyHit.add(targets.get(0));
            doneHitting();
            return true;
        }
        return false;
    }

    protected void landed(@Nullable LivingEntity elb) {
        Entity target = getTetheringEntity(), caster = getOwner();
        if (elb != null) {
            CombatData.getCap(elb).consumePosture(getOwner(), 7, ICombatCapability.BreachLevel.STUN);
            elb.hurt(new CombatDamageSource(getOwner()).setProxy(target).setDamageTyping(FootworkDamageArchetype.PHYSICAL).setProcSkillEffects(true).setSkillUsed(activeSkill).setProcAttackEffects(true), friendly?12:6);
            if(friendly&&elb instanceof Mob m && target instanceof LivingEntity le) {
                m.setTarget(le);
                m.setLastHurtByMob(le);
            }
        }
        if (target != null && caster != null) {
            if (target.isRemoved() || caster.isRemoved()) {
                remove(RemovalReason.UNLOADED_WITH_PLAYER);
                return;
            }
            if(!friendly)
            target.hurt(new CombatDamageSource(caster).setDamageTyping(FootworkDamageArchetype.PHYSICAL).setProcSkillEffects(true).setSkillUsed(activeSkill).setProcAttackEffects(true), 10);
        }
    }


    @Override
    public void tick() {
        if (getTetheringEntity() == null) {
            discard();
            return;
        }
        if(level().isClientSide())return;
//        if (tickCount < 20)
            getTetheringEntity().setPos(position())/*.moveTo(position())*/;
        if (getState() == FlyingItemEntity.STATE.FOLLOW && getMotionTarget() == getOwner() && isIdle()) {
            autoYeet();
        } else if (getTetheringEntity() instanceof LivingEntity target) {
            //friendly throws can be canceled
            if(friendly&&!target.isShiftKeyDown()){
                discard();
                return;
            }
            if(dormant && !level().isClientSide && GeneralUtils.getDistSqCompensated(this, target) < 2) {
                //reached target, run any on-hits
                landed(getMotionTarget() instanceof LivingEntity le && le != getOwner() ? le : null);
                discard();
            }
        }
        super.tick();
    }
}
