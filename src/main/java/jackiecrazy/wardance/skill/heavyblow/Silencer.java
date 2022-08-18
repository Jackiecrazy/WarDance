package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Silencer extends HeavyBlow {
    private final Tag<String> tag = Tag.create(new HashSet<>(Arrays.asList("physical", ProcPoints.disable_shield, ProcPoints.melee, ProcPoints.normal_attack, ProcPoints.modify_crit, ProcPoints.recharge_normal, ProcPoints.afflict_tick)));

    @SubscribeEvent
    public static void silenced(LivingDeathEvent e) {
        if (e.getSource().getEntity() instanceof LivingEntity && Marks.getCap(e.getEntityLiving()).isMarked(WarSkills.SILENCER.get())) {
            LivingEntity elb = (LivingEntity) e.getSource().getEntity();
            CasterData.getCap(elb).changeSkillState(WarSkills.SILENCER.get(), STATE.INACTIVE);
        }
    }

    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }

    @Override
    public boolean isPassive(LivingEntity caster) {
        return true;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof CriticalHitEvent && ((CriticalHitEvent) procPoint).getTarget() == target && state == STATE.INACTIVE) {
            if (caster.level.isClientSide() || caster == target) return;
            if (StealthUtils.INSTANCE.getAwareness(caster, target) != StealthUtils.Awareness.UNAWARE || !cast(caster, target, -999))
                return;
            CombatData.getCap(target).setHandBind(Hand.MAIN_HAND, 60);
            CombatData.getCap(target).setHandBind(Hand.OFF_HAND, 60);
            procPoint.setResult(Event.Result.ALLOW);
            mark(caster, target, 60);
        } else if (state == STATE.COOLING) stats.decrementDuration();
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        sd.flagCondition(existing == null ? target.isSilent() : existing.isCondition());
        target.setSilent(true);
        return super.onMarked(caster, target, sd, existing);
    }

    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration();
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        target.setSilent(sd.isCondition());
        super.onMarkEnd(caster, target, sd);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 5);
        }
        return super.onStateChange(caster, prev, from, to);
    }
}
