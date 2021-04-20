package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

import java.util.Arrays;
import java.util.HashSet;

public class Poise extends HeavyBlow {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "boundCast", "normalAttack", "countdown", "onParry", "modifyCrit", "rechargeWithAttack", "onBeingParried")));
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
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getEntityLiving() == caster) {
            ((ParryEvent) procPoint).setPostureConsumption(0);
        }
    }
}
