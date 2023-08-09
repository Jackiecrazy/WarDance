package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;


public class FlameDance extends WarCry {
    private static final UUID attackSpeed = UUID.fromString("338a5b6f-46c2-44b6-913f-f15c5e59cd48");
    private final HashSet<String> tag = makeTag("chant", ProcPoints.melee, ProcPoints.on_being_hurt, ProcPoints.modify_crit, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> no = makeTag(ProcPoints.melee, ProcPoints.on_parry);

    @Override
    protected int getDuration(float might) {
        return (int) (might * 2);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent lae && !lae.getSource().isBypassArmor() && procPoint.getPhase() == EventPriority.HIGHEST && lae.getEntity() == target) {
            mark(caster, target, 0.1f, 1);
            //kaboom!
            if (!DamageUtils.isSkillAttack(lae.getSource()) && CombatData.getCap(caster).getMight() == CombatData.getCap(caster).getMaxMight()) {
                DamageSource kaboom = new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setProcSkillEffects(true).setSkillUsed(this).setPostureDamage(0).bypassArmor();
                final float f = getExistingMark(target).getArbitraryFloat() * SkillUtils.getSkillEffectiveness(caster);
                target.hurt(kaboom, f * ((int) (2 + f / 10)) / 2f);
                target.hurtTime = target.hurtDuration = target.invulnerableTime = 0;
                removeMark(target);
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
        return sd;
    }
}
