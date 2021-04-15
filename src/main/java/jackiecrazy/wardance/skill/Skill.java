package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.event.SkillCooldownEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Locale;

/*
 * for chanting, make a dummy skill which does nothing but play a sound, if even that. This is the one that'll be displayed to players.
 * at the end of that skill, onCast is properly called, ignoring canCast() because that'll be set to false for the actual skill.
 * if it's a discrete and immediate effect, of course, you can just proc it when the effect ends.
 *
 * Every "skill upgrade" is actually a different skill. This means you can override getParentSkill() and get upgrades to show up.
 * A list of skills are compiled at world load. First the skills with no parent skill are loaded, then their children.
 * There are no grandchildren. If you make a grandchild, it won't show up.
 */
public abstract class Skill extends ForgeRegistryEntry<Skill> {
    public Skill() {

    }

    @Nullable
    public Skill getParentSkill() {
        return null;
    }

    public boolean canCast(LivingEntity caster) {
        ISkillCapability cap = CasterData.getCap(caster);
        for (String s : getIncompatibleTags(caster, null).getAllElements())
            if (cap.isTagActive(s)) return false;
        return true;
    }

    public void onCooledDown(LivingEntity caster, float overflow) {}

    public TranslationTextComponent requirement(LivingEntity caster) {
        return new TranslationTextComponent(TextFormatting.RED + this.getRegistryName().toString() + "_cannot_cast");
    }

    public abstract Tag<String> getTags(LivingEntity caster, SkillData stats);//requires breath, debuffing, healing, aoe, etc. Also determines proc time (parry, attack, etc)

    public abstract Tag<String> getIncompatibleTags(LivingEntity caster, SkillData stats);//uses sharp weapon, uses breath, undead using holy spell, etc

    public abstract boolean onCast(LivingEntity caster, SkillData stats);

    public boolean tick(LivingEntity caster, SkillData stats) {
        if (stats.getDuration() <= 0) {
            CasterData.getCap(caster).removeActiveSkill(this);
            return false;
        }
        return true;
    }

    /**
     * responsible for setting the cooldown as well.
     */
    public abstract void onEffectEnd(LivingEntity caster, SkillData stats);

    public abstract void onSuccessfulProc(LivingEntity caster, SkillData stats, @Nullable LivingEntity target, Event procPoint);

    protected void setCooldown(LivingEntity caster, float duration){
        SkillCooldownEvent sce=new SkillCooldownEvent(caster, duration);
        MinecraftForge.EVENT_BUS.post(sce);
        CasterData.getCap(caster).setSkillCooldown(this, sce.getCooldown());
    }

    protected void activate(LivingEntity caster, float duration){
        CasterData.getCap(caster).activateSkill(new SkillData(this, duration));
    }

    protected void markUsed(LivingEntity caster){
        CasterData.getCap(caster).markSkillUsed(this);
    }
}