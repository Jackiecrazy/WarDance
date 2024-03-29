package jackiecrazy.wardance.skill.judgment;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.HashSet;

public class MastersLesson extends Judgment {
    private final HashSet<String> tag = makeTag("physical", ProcPoints.on_hurt, ProcPoints.normal_attack, ProcPoints.on_stagger, ProcPoints.on_cast, "melee", "execution");

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof StunEvent && ((StunEvent) procPoint).getEntity() == target) {
            ((StunEvent) procPoint).setLength(200);
        } else if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntity() == target) {
            if (CombatData.getCap(target).isVulnerable())
                procPoint.setCanceled(true);
            else if (procPoint.getPhase() == EventPriority.HIGHEST && Marks.getCap(target).isMarked(this)) {
                CombatData.getCap(target).consumePosture(((LivingAttackEvent) procPoint).getAmount());
                procPoint.setCanceled(true);
            }
        }
    }
}
