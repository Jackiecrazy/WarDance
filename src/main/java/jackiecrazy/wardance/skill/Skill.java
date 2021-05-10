package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.event.SkillCooldownEvent;
import jackiecrazy.wardance.networking.CastSkillPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public static final HashMap<Skill, List<Skill>> variationMap = new HashMap<>();

    public Skill() {
        if (this.getParentSkill() == null && !variationMap.containsKey(this)) {
            variationMap.put(this, new ArrayList<>());
        } else if (this.getParentSkill() != null) {
            List<Skill> insert = variationMap.get(getParentSkill());
            if (insert == null) insert = new ArrayList<>();
            insert.add(this);
            variationMap.put(this.getParentSkill(), insert);
        }
    }

    @Nullable
    public static Skill getSkill(String name) {
        return getSkill(new ResourceLocation(name));
    }

    @Nullable
    public static Skill getSkill(ResourceLocation name) {
        return GameRegistry.findRegistry(Skill.class).getValue(name);
    }

    @Nullable
    public Skill getParentSkill() {
        return null;
    }

    public boolean canCast(LivingEntity caster) {
        ISkillCapability cap = CasterData.getCap(caster);
        for (String s : getIncompatibleTags(caster).getAllElements())
            if (cap.isTagActive(s)) return false;
        return !cap.isSkillCoolingDown(this) && (getParentSkill() == null || getParentSkill().canCast(caster));
    }

    public void onCooledDown(LivingEntity caster, float overflow) {}

    public ITextComponent description() {
        return new TranslationTextComponent(this.getRegistryName().toString() + ".desc");
    }

    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(this.getRegistryName().toString() + ".name");
    }

    public ResourceLocation icon() {
        return new ResourceLocation(this.getRegistryName().toString() + "_icon");
    }

    public abstract Tag<String> getTags(LivingEntity caster);//requires breath, debuffing, healing, aoe, etc. Also determines proc time (parry, attack, etc)

    public abstract Tag<String> getIncompatibleTags(LivingEntity caster);//uses sharp weapon, uses breath, undead using holy spell, etc

    public boolean checkAndCast(LivingEntity caster) {
        if (!canCast(caster)) return false;
        return onCast(caster);
    }

    public abstract boolean onCast(LivingEntity caster);

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

    protected void setCooldown(LivingEntity caster, float duration) {
        SkillCooldownEvent sce = new SkillCooldownEvent(caster, this, duration);
        MinecraftForge.EVENT_BUS.post(sce);
        CasterData.getCap(caster).setSkillCooldown(getParentSkill() == null ? this : getParentSkill(), sce.getCooldown());
    }

    protected void activate(LivingEntity caster, float duration) {
        //System.out.println("enabling for " + duration);
        CasterData.getCap(caster).activateSkill(new SkillData(this, duration));
    }

    protected void markUsed(LivingEntity caster) {
        CasterData.getCap(caster).markSkillUsed(this);
    }


}