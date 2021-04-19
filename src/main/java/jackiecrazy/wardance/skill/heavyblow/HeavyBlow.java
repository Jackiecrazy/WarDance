package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;

public class HeavyBlow extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "boundCast", "normalAttack", "modifyCrit", "rechargeWithAttack", "onBeingParried")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack", "noCrit")));

    @Override
    public Tag<String> getTags(LivingEntity caster, SkillData stats) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster, SkillData stats) {
        return no;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == HeavyBlow.class ? null : WarSkills.HEAVY_BLOW.get();
    }

    @Override
    public boolean onCast(LivingEntity caster, SkillData stats) {
        activate(caster, 40);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 3);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getAttacker() == caster) {
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getDefendingHand(), 30);
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent) {
            procPoint.setResult(Event.Result.ALLOW);
            markUsed(caster);
        }
    }
}