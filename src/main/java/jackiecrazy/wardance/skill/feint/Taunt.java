package jackiecrazy.wardance.skill.feint;

import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.ConsumePostureEvent;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.UUID;

public class Taunt extends Skill {
    static final UUID TAUNT = UUID.fromString("67fe7ef6-a398-4c62-9bb1-42edaa80e7b1");
    private final HashSet<String> tag = makeTag("melee", "noDamage", "boundCast", ProcPoints.afflict_tick, ProcPoints.change_parry_result, ProcPoints.recharge_time, "normalAttack", "chant", "countdown");
    private final HashSet<String> thing = makeTag(SkillTags.offensive, SkillTags.chant);

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 0;
    }

    @Override
    public HashSet<String> getTags() {
        return thing;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (cooldownTick(stats)) {
            return true;
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        return markTickDown(sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof ConsumePostureEvent e && target != null && hasMark(target)) {
            SkillData mark = getExistingMark(target);
            e.setAmount(e.getAmount() * (1 + 0.15f * mark.getArbitraryFloat()));
            if (e.getAmount() > CombatData.getCap(caster).getPosture() && mark.getArbitraryFloat() >= 10 && e.getEntity() == caster)
                completeChallenge(caster);
        }
        if (procPoint instanceof LivingHurtEvent e && target != null && hasMark(target)) {
            SkillData mark = getExistingMark(target);
            e.setAmount(e.getAmount() * (1 + 0.15f * mark.getArbitraryFloat()));
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity target = SkillUtils.aimLiving(caster, 8);
        if (to == STATE.ACTIVE && target != null && cast(caster, target, -999)) {
            mark(caster, target, duration(), prev.getEffectiveness());
            if (caster.level instanceof ServerLevel sl) {
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.VINDICATOR_CELEBRATE, SoundSource.PLAYERS, 0.8f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, target.getX(), target.getY(), target.getZ(), 20, target.getBbWidth(), target.getBbHeight() / 2, target.getBbWidth(), 0f);
            }
            if (caster instanceof Player p) {
                //display a random insult
                p.displayClientMessage(Component.translatable("wardance.taunt." + WarDance.rand.nextInt(10)), true);
            }
            markUsed(caster);
        }
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 2);
        return boundCast(prev, from, to);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            sd.setDuration(10);
            sd.setMaxDuration(sd.getDuration());
            sd.addArbitraryFloat(existing.getArbitraryFloat());
        }
        target.setLastHurtByMob(caster);
        SkillUtils.modifyAttribute(target, Attributes.MOVEMENT_SPEED, TAUNT, 0.1 * sd.getArbitraryFloat() / sd.getEffectiveness(), AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (target instanceof Mob mob) {
            mob.setTarget(caster);
            GoalCapabilityProvider.getCap(caster).ifPresent(a -> a.setForcedTarget(target));
        }
        return sd;
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        GoalCapabilityProvider.getCap(target).ifPresent(a -> a.setForcedTarget(null));
        SkillUtils.removeAttribute(target, Attributes.MOVEMENT_SPEED, TAUNT);
        super.onMarkEnd(caster, target, sd);
    }

    protected int duration() {
        return 10;
    }
}
