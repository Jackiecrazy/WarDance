package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Poise extends HeavyBlow {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "disableShield", SkillTags.melee, SkillTags.on_hurt, "boundCast", SkillTags.normal_attack, SkillTags.countdown, SkillTags.modify_crit, SkillTags.recharge_normal, SkillTags.on_being_parried, SkillTags.on_parry)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack", "noCrit")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        CombatData.getCap(caster).consumeMight(1);
        CombatData.getCap(caster).consumeSpirit(spiritConsumption(caster));
        activate(caster, 60);
        return true;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getDefendingHand()!=null && ((ParryEvent) procPoint).getEntityLiving() == caster) {
            ((ParryEvent) procPoint).setPostureConsumption(0);
        }
    }
}
