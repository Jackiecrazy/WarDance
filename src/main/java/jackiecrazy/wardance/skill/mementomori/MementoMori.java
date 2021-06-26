package jackiecrazy.wardance.skill.mementomori;

import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

import java.util.Arrays;
import java.util.HashSet;

public class MementoMori extends Skill {
    /*
    memento mori: your might and spirit generation speeds increase proportionally with lost health (including wounding)
rapid clotting: charm 3: gain 1 armor every point of health lost
panic: when taking damage under 50%, consume 5 spirit to create a smoke cloud that blinds the enemy, with radius increasing as health decreases
death denial: upon receiving fatal damage, become immune to all damage and all healing for 5 seconds
meltdown: active skill. Consumes all your spirit and deals percentage health damage equal to your lost health, spread between all enemies
     */
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("passive", SkillTags.change_might)));
    private final Tag<String> no = Tag.getEmptyTag();
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
        activate(caster, 0);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        activate(caster, 0);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        //on attack might event, check health percentage and increment
        if(procPoint instanceof AttackMightEvent){

        }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, STATE state) {
        //add spirit if not on cooldown
        return super.equippedTick(caster, state);
    }
}
