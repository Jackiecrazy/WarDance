package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
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
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoints.melee, ProcPoints.on_death, ProcPoints.afflict_tick, "boundCast", ProcPoints.normal_attack, ProcPoints.modify_crit, ProcPoints.recharge_normal, ProcPoints.on_being_parried)));
    private final Tag<String> no = Tag.getEmptyTag();

    @SubscribeEvent
    public static void spooketh(CriticalHitEvent e) {
        final Skill back = WarSkills.BACKSTAB.get();
        if (CasterData.getCap(e.getPlayer()).getEquippedSkills().contains(back) && !CasterData.getCap(e.getPlayer()).isSkillCoolingDown(back) && e.getTarget() instanceof LivingEntity && CombatUtils.getAwareness(e.getPlayer(), (LivingEntity) e.getTarget()) == CombatUtils.Awareness.UNAWARE) {
            back.onSuccessfulProc(e.getPlayer(), null, (LivingEntity) e.getTarget(), e);
            CasterData.getCap(e.getPlayer()).setSkillCooldown(back, 5);
        }
    }

    @SubscribeEvent
    public static void silenced(LivingDeathEvent e) {
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity elb = (LivingEntity) e.getSource().getTrueSource();
            CasterData.getCap(elb).coolSkill(WarSkills.BACKSTAB.get());
        }
    }

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
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
        if (caster.world.isRemote()) return;
        CombatData.getCap(target).setHandBind(Hand.MAIN_HAND, 30);
        CombatData.getCap(target).setHandBind(Hand.OFF_HAND, 30);
        procPoint.setResult(Event.Result.ALLOW);
        mark(caster, target, 80);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        sd.flagCondition(existing == null ? target.isSilent() : existing.isCondition());
        target.setSilent(true);
        System.out.println("target has been silenced!");
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        target.setSilent(sd.isCondition());
        super.onMarkEnd(caster, target, sd);
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 40);
        return true;
    }
}
