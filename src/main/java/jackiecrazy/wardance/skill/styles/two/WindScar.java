package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.event.DodgeEvent;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.entity.WindBladeEntity;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;

public class WindScar extends WarCry {
    private static final AttributeModifier reach = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "wind scar bonus", 1.5, AttributeModifier.Operation.ADDITION);
    private final HashSet<String> tag = makeTag("chant", ProcPoints.on_being_hurt, ProcPoints.melee, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> no = makeTag((("sweep")));

    @Override
    public void onEquip(LivingEntity caster) {
        SkillUtils.addAttribute(caster, ForgeMod.ENTITY_REACH.get(), reach);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.removeAttribute(caster, ForgeMod.ENTITY_REACH.get(), reach);
        super.onUnequip(caster, stats);
    }

    @Override
    protected int getDuration(float might) {
        return (int) (3 * might);
    }

    @Override
    public boolean markTick(@Nullable LivingEntity caster, LivingEntity target, SkillData sd) {
        markTickDown(sd);
        return false;//not displayed and thus no need to update on the client
    }

    @Override
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return false;
    }

    @Override
    public @Nullable SkillData onMarked(LivingEntity caster,
                                        LivingEntity target,
                                        SkillData sd,
                                        @Nullable SkillData existing) {
        if (existing != null) {
            sd.addArbitraryFloat(existing.getArbitraryFloat() + 1);
        }
        sd.setDuration(4);
        sd.setMaxDuration(4);
        return sd;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.ACTIVE);
        return true;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint.getPhase() != EventPriority.LOWEST) return;
        if (procPoint instanceof LivingAttackEvent hurt && hurt.getEntity() == target) {
            stats.addTarget(target);
            if(!CombatData.getCap(caster).alreadyProc("oncePerAttack")) {
                switch (CombatUtils.getAttackState(caster)) {
                    case AERIAL, SPRINTING -> windPressure(caster, stats, 1);
                    case THROW, PICKUP_FLOURISH, DRAW_ATTACK -> windPressure(caster, stats, 2);
                }
            }
            if (hurt.getSource().getDirectEntity() instanceof Projectile && (!(hurt.getSource() instanceof CombatDamageSource cds) || cds.getSkillUsed() != this))
                windPressure(caster, stats, 1);
        }
        if (procPoint instanceof LivingHurtEvent hurt && hurt.getEntity() == target) {
            //reduce damage for repeated hits
            if (hurt.getSource() instanceof CombatDamageSource cds && cds.getSkillUsed() == this)
                mark(caster, target, 4f);
        }
        if (procPoint instanceof DodgeEvent && !caster.level().isClientSide) {
            windPressure(caster, stats, 1);
        }
        if (procPoint instanceof PlayInteractionEvent.Pre p) {

        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    private void windPressure(LivingEntity caster, SkillData d, int amount) {
        if (d.getState() != STATE.ACTIVE) {
            d.setDuration(0);
            d.setMaxDuration(10);
            d.setArbitraryFloat(0);
            d.setState(STATE.ACTIVE);
        }
        d.addDuration(amount);
        d.setMaxDuration(10);
        if (d.getDuration() >= 10) {
            //create wind blades
            final float numofBlades = StylishData.getCap(caster).getCombo() * 3f * SkillUtils.getSkillEffectiveness(caster);
            //make a fan shape
            Vec3 up = new Vec3(0, 1, 0);
            Vec3 forward = caster.getLookAngle();
            //cross two different ways to get two corners
            Vec3 firstHalf = up.cross(forward);
            Vec3 secondHalf = forward.cross(up);
            caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.45f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            for (int x = 0; x < numofBlades; x++) {
                Vec3 interpol = firstHalf.lerp(secondHalf, x / numofBlades).add(Vec3.ZERO.lerp(up, x / numofBlades));
                WindBladeEntity fwe = new WindBladeEntity(WarEntities.WIND_BLADE.get(), caster.level());
                fwe.setSkillUsed(this).setOwner(caster);
                Vec3 look = caster.getLookAngle().reverse();
                fwe.moveTo(caster.getX() + look.x + interpol.x, caster.getEyeY() + look.y + interpol.y, caster.getZ() + look.z + interpol.z);
                fwe.yeet(caster.getEyePosition().add(look).add(interpol.scale(3)), 2.5);
                fwe.setState(FlyingItemEntity.STATE.THROW_TRACK);
                fwe.addTargets(d.getTargets());
                fwe.setInteractionRange(1f);
                caster.level().addFreshEntity(fwe);
            }
            d.clearTargets();
            d.setDuration(0);
        }
        d.markDirty();
    }
}
