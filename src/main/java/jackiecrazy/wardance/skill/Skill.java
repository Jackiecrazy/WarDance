package jackiecrazy.wardance.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;

public abstract class Skill{
    public abstract boolean canCast(LivingEntity caster, LivingEntity target);
    public abstract Tag<String>[] getTags(LivingEntity caster, LivingEntity target);//requires breath, debuffing, healing, aoe, etc
    public abstract Tag<String>[] getIncompatibleTags(LivingEntity caster, LivingEntity target);//uses sharp weapon, uses breath, undead using holy spell, etc
    public abstract int castingTime(LivingEntity caster, LivingEntity target);
    public abstract boolean shouldAct(LivingEntity caster, LivingEntity target);
    public abstract boolean act(LivingEntity caster, LivingEntity target);
    public abstract int getVariation(LivingEntity caster);
}