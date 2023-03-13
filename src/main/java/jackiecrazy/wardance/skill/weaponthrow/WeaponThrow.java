package jackiecrazy.wardance.skill.weaponthrow;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.SetTag;
import net.minecraftforge.eventbus.api.Event;

import jackiecrazy.wardance.skill.Skill.STATE;

import javax.annotation.Nonnull;

public class WeaponThrow extends Skill {


    @Override
    public SetTag<String> getTags(LivingEntity caster) {
        return null;
    }

    @Nonnull
    @Override
    public SetTag<String> getSoftIncompatibility(LivingEntity caster) {
        return null;
    }


    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        //procs on projectileimpactevent, deals (attack damage) to prevent more bad stuff happening
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
