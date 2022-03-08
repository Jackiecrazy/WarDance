package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

import jackiecrazy.wardance.skill.Skill.STATE;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Timberfall extends WarCry {
    private final Tag<String> tag = Tag.create(new HashSet<>(Arrays.asList("chant", ProcPoints.melee, ProcPoints.modify_crit, ProcPoints.on_hurt, ProcPoints.attack_might, ProcPoints.on_being_hurt, ProcPoints.recharge_time, ProcPoints.recharge_sleep)));
    private final Tag<String> no = Tag.empty();//.getTagFromContents(new HashSet<>(Collections.emptyList()));

    @SubscribeEvent
    public static void timberfall(AttackMightEvent e) {
        if (e.getAttacker() != null && CasterData.getCap(e.getAttacker()).isSkillUsable(WarSkills.TIMBERFALL.get())) {
            CombatData.getCap(e.getEntityLiving()).consumePosture(e.getQuantity() * 5);
        }
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof CriticalHitEvent && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.HIGHEST && ((CriticalHitEvent) procPoint).getEntityLiving() == caster) {
            procPoint.setResult(Event.Result.ALLOW);
            ((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier() * 1.4f);
            stats.decrementDuration();
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    public Color getColor() {
        return Color.orange;
    }

    @Override
    protected int getDuration(float might) {
        return (int) might*2-2;
    }
}
