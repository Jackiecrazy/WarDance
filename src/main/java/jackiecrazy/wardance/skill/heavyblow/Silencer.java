package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Silencer extends HeavyBlow {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoints.disable_shield, ProcPoints.melee, ProcPoints.normal_attack, ProcPoints.modify_crit, ProcPoints.recharge_normal, ProcPoints.afflict_tick)));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @SubscribeEvent
    public static void silenced(LivingDeathEvent e) {
        if (e.getSource().getTrueSource() instanceof LivingEntity && Marks.getCap(e.getEntityLiving()).isMarked(WarSkills.BACKSTAB.get())) {
            LivingEntity elb = (LivingEntity) e.getSource().getTrueSource();
            CasterData.getCap(elb).coolSkill(WarSkills.BACKSTAB.get());
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
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (caster.world.isRemote() || caster == target) return;
        if (CombatUtils.getAwareness(caster, target) != CombatUtils.Awareness.UNAWARE) return;
        CombatData.getCap(target).setHandBind(Hand.MAIN_HAND, 60);
        CombatData.getCap(target).setHandBind(Hand.OFF_HAND, 60);
        procPoint.setResult(Event.Result.ALLOW);
        markUsed(caster);
        mark(caster, target, 60);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        sd.flagCondition(existing == null ? target.isSilent() : existing.isCondition());
        target.setSilent(true);
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        target.setSilent(sd.isCondition());
        super.onMarkEnd(caster, target, sd);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 5);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, STATE state) {
        if(state==STATE.INACTIVE) {
            onCast(caster);
            return true;
        }
        return super.equippedTick(caster, state);
    }
}
