package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.EntityAwarenessEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class ArchersParadox extends Skill {
    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1.5s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
apathy: your max spirit is 4, your spirit instantly refills after cooldown, you are immune to burnout.
     */

    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", SkillTags.on_projectile_impact, SkillTags.change_awareness)));
    private final Tag<String> no = Tag.getEmptyTag();

    @Override
    public Color getColor() {
        return Color.YELLOW;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return WarSkills.MORALE.get();
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 30);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        activate(caster, 30);
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        if(d.isCondition())
            d.decrementDuration();
        if(d.getDuration()==0){
            d.flagCondition(false);
            d.setDuration(30);
        }
        return false;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if(stats.isCondition())return;
        if (procPoint instanceof ProjectileImpactEvent) {
            CombatData.getCap(caster).addSpirit(1);
            stats.flagCondition(true);
        } else if (procPoint instanceof EntityAwarenessEvent) {
            if(((EntityAwarenessEvent) procPoint).getAwareness()!= CombatUtils.Awareness.ALERT) {
                CombatData.getCap(caster).addSpirit(1);
                stats.flagCondition(true);
            }
        }
    }
}
