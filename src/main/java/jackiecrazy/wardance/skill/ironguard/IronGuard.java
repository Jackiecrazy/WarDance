package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;

public class IronGuard extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "quickCast", "onParry", "rechargeWithParry")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("onParry")));

    @Override
    public Tag<String> getTags(LivingEntity caster, SkillData stats) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster, SkillData stats) {
        return no;
    }

    @Override
    public boolean onCast(LivingEntity caster, SkillData stats) {
        activate(caster, 20);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {

    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, @Nullable LivingEntity target, Event procPoint) {
        if(procPoint instanceof ParryEvent){
            CombatData.getCap(((ParryEvent) procPoint).getAttacker()).consumePosture(((ParryEvent) procPoint).getPostureConsumption());
            ((ParryEvent) procPoint).setPostureConsumption(0);
        }
    }
}
