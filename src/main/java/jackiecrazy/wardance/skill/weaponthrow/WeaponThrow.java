package jackiecrazy.wardance.skill.weaponthrow;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class WeaponThrow extends Skill {


    @Override
    public HashSet<String> getTags(LivingEntity caster) {
        return null;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
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
