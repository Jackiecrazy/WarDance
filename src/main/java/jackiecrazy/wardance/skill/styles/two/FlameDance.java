package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.ConsumePostureEvent;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class FlameDance extends WarCry {
    private static final UUID attackSpeed = UUID.fromString("338a5b6f-46c2-44b6-913f-f15c5e59cd48");
    private final HashSet<String> tag = makeTag("chant", ProcPoints.melee, ProcPoints.on_being_hurt, ProcPoints.modify_crit, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> no = makeTag(ProcPoints.melee, ProcPoints.on_parry);

    @SubscribeEvent
    public static void echoes(LivingHurtEvent e) {
        LivingEntity target = e.getEntity();
        Marks.getCap(target).getActiveMark(WarSkills.FLAME_DANCE.get()).ifPresent(a -> e.setAmount(e.getAmount() * (1 + a.getArbitraryFloat() * 0.03f * a.getEffectiveness())));
    }

    @SubscribeEvent
    public static void echoes(ConsumePostureEvent e) {
        LivingEntity target = e.getEntity();
        Marks.getCap(target).getActiveMark(WarSkills.FLAME_DANCE.get()).ifPresent(a -> e.setAmount(e.getAmount() * (1 + a.getArbitraryFloat() * 0.03f * a.getEffectiveness())));
    }

    @Override
    protected int getDuration(float might) {
        return (int) (might * 2);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (caster == target) return;
        if (procPoint instanceof LivingAttackEvent lae && (DamageUtils.isMeleeAttack(lae.getSource()) || DamageUtils.isSkillAttack(lae.getSource())) && !lae.getSource().is(DamageTypeTags.BYPASSES_ARMOR) && procPoint.getPhase() == EventPriority.HIGHEST && lae.getEntity() == target) {
            mark(caster, target, 0.1f, 1);
            //kaboom!
            if (CombatData.getCap(caster).getMight() == CombatData.getCap(caster).getMaxMight()) {
                if (!DamageUtils.isSkillAttack(lae.getSource())) {
                    DamageSource kaboom = new CombatDamageSource(caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setProcSkillEffects(true).setSkillUsed(this).setPostureDamage(0).bypassArmor();
                    float f = getExistingMark(target).getArbitraryFloat() * SkillUtils.getSkillEffectiveness(caster);
                    if(getExistingMark(target).getArbitraryFloat()>49){
                        completeChallenge(caster);
                    }
                    target.hurt(kaboom, f * ((int) (2 + f / 10)) / 2f);
                    target.hurtTime = target.hurtDuration = target.invulnerableTime = 0;
                    removeMark(target);
                    if (caster.level() instanceof ServerLevel server) {
                        server.sendParticles(ParticleTypes.SMALL_FLAME, target.getX(), target.getY(), target.getZ(), (int) f * 5, target.getBbWidth(), target.getBbHeight(), target.getBbWidth(), 0f);
                    }
                }
            }
        } else if (procPoint instanceof SkillCastEvent sce && procPoint.getPhase() == EventPriority.HIGHEST && sce.getEntity() == caster) {
            mark(caster, target, 0.1f, 1);
        }
        super.onProc(caster, procPoint, state, stats, target);
    }


    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            return activeTick(stats);
        }
        return super.equippedTick(caster, stats);
    }

    @Nullable
    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null)
            sd.addArbitraryFloat(existing.getArbitraryFloat());
        sd.setArbitraryFloat(Math.min(sd.getArbitraryFloat(), 50));
        return sd;
    }
}
