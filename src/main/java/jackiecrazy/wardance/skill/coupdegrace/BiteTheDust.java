package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.footwork.event.StaggerEvent;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.HashSet;

public class BiteTheDust extends CoupDeGrace {
    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.coup_de_grace;
    }

    @Override
    public boolean willKillOnCast(LivingEntity caster, LivingEntity target) {
        float damage = 2;
        float currentMark = getExistingMark(target).getDuration();
        while (currentMark > 0) {
            damage *= 2;
            currentMark--;
        }
        return damage > target.getHealth();
    }

    public boolean showsMark(SkillData mark, LivingEntity target){
        return false;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof StaggerEvent && ((StaggerEvent) procPoint).getAttacker() == caster && procPoint.getPhase() == EventPriority.HIGHEST && cast(caster, target, -999)) {
            float damage = 2;
            float currentMark = getExistingMark(target).getDuration();
            while (currentMark > 0) {
                damage *= 2;
                currentMark--;
            }
            target.hurtTime = target.hurtDuration = target.invulnerableTime = 0;
            System.out.println("start!");
            target.hurt(new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.TRUE).setSkillUsed(this).setKnockbackPercentage(0).bypassArmor().bypassMagic(), damage);
            mark(caster, target, 1);
        }
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 0;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.INACTIVE);
        return false;
    }
}
