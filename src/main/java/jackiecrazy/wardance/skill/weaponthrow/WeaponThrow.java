package jackiecrazy.wardance.skill.weaponthrow;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

public class WeaponThrow extends Skill {


    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return null;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return null;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return null;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        return false;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {

    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        //procs on projectileimpactevent, deals (attack damage) to prevent more bad stuff happening
    }
}
