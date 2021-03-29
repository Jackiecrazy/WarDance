package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.capability.CasterData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * for chanting, make a dummy skill which does nothing but play a sound, if even that. This is the one that'll be displayed to players.
 * at the end of that skill, onCast is properly called, ignoring canCast() because that'll be set to false for the actual skill.
 * if it's a one time only effect, of course, then you can just proc it when the effect ends.
 */
public abstract class Skill extends ForgeRegistryEntry<Skill> {
    public abstract boolean canCast(LivingEntity caster, LivingEntity target, int variation);

    public abstract Tag<String>[] getTags(LivingEntity caster, LivingEntity target, SkillData stats);//requires breath, debuffing, healing, aoe, etc. Also determines proc time (parry, attack, etc)

    public abstract Tag<String>[] getIncompatibleTags(LivingEntity caster, LivingEntity target, SkillData stats);//uses sharp weapon, uses breath, undead using holy spell, etc

    public abstract int onCast(LivingEntity caster, LivingEntity target, SkillData stats);

    public boolean tick(LivingEntity caster, SkillData stats) {
        if (stats.getDuration() <= 0) {
            CasterData.getCap(caster).removeActiveSkill(this);
            return false;
        }
        return true;
    }

    public abstract void onEffectEnd(LivingEntity caster, SkillData stats);

    public abstract int getVariation(LivingEntity caster);
}