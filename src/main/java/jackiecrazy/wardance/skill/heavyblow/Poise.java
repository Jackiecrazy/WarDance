package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Poise extends HeavyBlow {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "disableShield", ProcPoints.melee, ProcPoints.on_hurt, "boundCast", ProcPoints.normal_attack, ProcPoints.countdown, ProcPoints.modify_crit, ProcPoints.recharge_normal, ProcPoints.on_being_parried, ProcPoints.on_parry)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack", "noCrit")));

    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    protected void onCrit(CriticalHitEvent proc, SkillData stats, LivingEntity caster, LivingEntity target) {

    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (stats.isCondition() && procPoint instanceof ParryEvent && procPoint.getPhase() == EventPriority.LOWEST && ((ParryEvent) procPoint).getAttacker() == caster) {
            CombatData.getCap(caster).setPostureGrace(0);
            CombatData.getCap(caster).addPosture(((ParryEvent) procPoint).getPostureConsumption());
        }else if (procPoint instanceof CriticalHitEvent) {
            if (isCrit((CriticalHitEvent) procPoint) && state != STATE.COOLING && procPoint.getPhase() == EventPriority.LOWEST) {
                onCrit((CriticalHitEvent) procPoint, stats, caster, target);
            } else if (state == STATE.COOLING && procPoint.getPhase() == EventPriority.HIGHEST) {
                stats.decrementDuration();
            }
        }
    }
}
