package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

public class CoupDeGrace extends Skill {
    @Override
    public Tag<String> getTags(LivingEntity caster, SkillData stats) {
        return null;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster, SkillData stats) {
        return null;
    }

    @Override
    public boolean onCast(LivingEntity caster, SkillData stats) {
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {

    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {

    }

    public static double getLife(LivingEntity e) {
        if (e instanceof PlayerEntity) return 2;
        return e.getMaxHealth() / Math.max(1, Math.cbrt(e.getMaxHealth())-2);
    }
}
