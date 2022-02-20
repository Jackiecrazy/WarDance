package jackiecrazy.wardance.skill.guillotine;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.StaggerEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class MastersLesson extends Guillotine {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoints.on_hurt, ProcPoints.normal_attack, ProcPoints.on_stagger, ProcPoints.on_cast, "melee", "execution")));

    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof StaggerEvent && ((StaggerEvent) procPoint).getEntityLiving() == target) {
            ((StaggerEvent) procPoint).setCount(1);
            ((StaggerEvent) procPoint).setLength(200);
        } else if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntityLiving() == target) {
            if (CombatData.getCap(target).getStaggerTime() > 0)
                procPoint.setCanceled(true);
            else if (procPoint.getPhase() == EventPriority.HIGHEST && Marks.getCap(target).isMarked(this)) {
                CombatData.getCap(target).consumePosture(((LivingAttackEvent) procPoint).getAmount());
                procPoint.setCanceled(true);
            }
        }
    }
}
