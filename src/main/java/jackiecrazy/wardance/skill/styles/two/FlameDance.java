package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.EffectUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
    public static void flames(LivingAttackEvent e) {
        if (e.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity seme = (LivingEntity) e.getSource().getEntity();
            LivingEntity uke = e.getEntity();
            if (CasterData.getCap(seme).isSkillUsable(WarSkills.FLAME_DANCE.get())) {
                EffectUtils.attemptAddPot(uke, EffectUtils.stackPot(uke, new MobEffectInstance(FootworkEffects.CORROSION.get(), (int) CombatData.getCap(seme).getRank(), EffectUtils.getEffectiveLevel(uke, FootworkEffects.CORROSION.get()) < 4 ? 0 : -1), EffectUtils.StackingMethod.MAXDURATION), false);
            }
        }
    }

    @Override
    protected int getDuration(float might) {
        return (int) (might * 2);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent lae && !lae.getSource().isBypassArmor() && procPoint.getPhase() == EventPriority.HIGHEST && lae.getEntity() == target) {
            mark(caster, target, 1, 1);
            //kaboom!
            if (CombatData.getCap(caster).getMight() == CombatData.getCap(caster).getMaxMight()) {
                CombatDamageSource kaboom = new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setProcSkillEffects(true).setSkillUsed(this).setProcNormalEffects(true).setPostureDamage(0);
                final float f = getExistingMark(target).getArbitraryFloat();
                target.hurt(kaboom, (f + 1) * f / 2);
            }
        } else if (procPoint instanceof SkillCastEvent sce && procPoint.getPhase() == EventPriority.HIGHEST && sce.getEntity() == caster) {
            mark(caster, target, 1, 1);
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
