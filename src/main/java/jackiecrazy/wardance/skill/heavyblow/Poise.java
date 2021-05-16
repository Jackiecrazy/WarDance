package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Poise extends HeavyBlow {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "boundCast", "disableShield", "normalAttack", "countdown", "onParry", "modifyCrit", "rechargeWithAttack", "onBeingParried")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack", "noCrit", "onParry")));

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
