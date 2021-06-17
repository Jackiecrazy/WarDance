package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.event.SkillCooldownEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
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

    public boolean isFamily(Skill s) {
        if (s == null) return false;
        if (s.getParentSkill() != null && (s.getParentSkill() == this || s.getParentSkill() == this.getParentSkill()))
            return true;
        else if (tryGetParentSkill() == s.tryGetParentSkill())
            return true;
        return false;
    }

    public boolean isPassive(LivingEntity caster) {
        return getTags(caster).contains("passive");
    }

    @Nullable
    public Skill getParentSkill() {
        return null;
    }

    @Nonnull
    public final Skill tryGetParentSkill() {
        return getParentSkill() == null ? this : getParentSkill();
    }

    public CastStatus castingCheck(LivingEntity caster) {
        ISkillCapability cap = CasterData.getCap(caster);
        for (String s : getIncompatibleTags(caster).getAllElements())
            if (cap.isTagActive(s)) return CastStatus.CONFLICT;
        if (cap.isSkillCoolingDown(this))
            return CastStatus.COOLDOWN;
        if(getParentSkill()!=null)
            return getParentSkill().castingCheck(caster);
        return CastStatus.ALLOWED;
    }

    public boolean isCompatibleWith(Skill s, LivingEntity caster) {
        if (s == null) return true;
        for (String tag : this.getTags(caster).getAllElements())
            if (s.getIncompatibleTags(caster).contains(tag)) return false;
        for (String tag : s.getTags(caster).getAllElements())
            if (this.getIncompatibleTags(caster).contains(tag)) return false;
        return true;
    }

    public void onCooledDown(LivingEntity caster, float overflow) {
        caster.world.playMovingSound(null, caster, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 0.3f + WarDance.rand.nextFloat(), 0.5f + WarDance.rand.nextFloat());
    }

    public ITextComponent description() {
        return new TranslationTextComponent(this.getRegistryName().toString() + ".desc");
    }

    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(this.getRegistryName().toString() + ".name");
    }

    public ResourceLocation icon() {
        ResourceLocation rl = getRegistryName();
        if (getParentSkill() != null) rl = getParentSkill().getRegistryName();
        return new ResourceLocation(rl.getNamespace() + ":" + "textures/skill/" + rl.getPath() + ".png");
    }

    public Color getColor() {
        return Color.WHITE;
    }

    public abstract Tag<String> getTags(LivingEntity caster);//requires breath, debuffing, healing, aoe, etc. Also determines proc time (parry, attack, etc)

    public abstract Tag<String> getIncompatibleTags(LivingEntity caster);//uses sharp weapon, uses breath, undead using holy spell, etc

    public boolean checkAndCast(LivingEntity caster) {
        if (castingCheck(caster)!=CastStatus.ALLOWED) return false;
        if (MinecraftForge.EVENT_BUS.post(new SkillCastEvent(caster, this))) return false;
        caster.world.playMovingSound(null, caster, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.AMBIENT, 0.3f + WarDance.rand.nextFloat(), 0.5f + WarDance.rand.nextFloat());
        return onCast(caster);
    }

    public abstract boolean onCast(LivingEntity caster);

    /**
     * @return whether the client should be updated.
     */
    public boolean activeTick(LivingEntity caster, SkillData d) {
        if (d.getSkill().getTags(caster).contains(ProcPoint.countdown)) {
            d.decrementDuration();
            return true;
        }
        return false;
    }

    public boolean equippedTick(LivingEntity caster, STATE state) {
        return false;
    }

    /**
     * @return whether the client should be updated.
     */
    public boolean coolingTick(LivingEntity caster, SkillCooldownData d) {
        if (d.getSkill().getTags(caster).contains(ProcPoint.recharge_time)) {
            d.decrementDuration(1);
            return true;
        }
        return false;
    }

    /**
     * responsible for setting the cooldown as well.
     */
    public abstract void onEffectEnd(LivingEntity caster, SkillData stats);

    public abstract void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint);

    protected void setCooldown(LivingEntity caster, float duration) {
        SkillCooldownEvent sce = new SkillCooldownEvent(caster, this, duration);
        MinecraftForge.EVENT_BUS.post(sce);
//        if (getParentSkill() != null)
//            CasterData.getCap(caster).setSkillCooldown(getParentSkill(), sce.getCooldown());
        caster.world.playMovingSound(null, caster, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat());
        CasterData.getCap(caster).setSkillCooldown(this, sce.getCooldown());
    }

    protected void activate(LivingEntity caster, float duration) {
        //System.out.println("enabling for " + duration);
        if (CasterData.getCap(caster).isSkillUsable(this))
            CasterData.getCap(caster).activateSkill(new SkillData(this, duration));
    }

    protected void markUsed(LivingEntity caster) {
        CasterData.getCap(caster).markSkillUsed(this);
    }

    public enum CastStatus {
        ALLOWED,
        COOLDOWN,
        CONFLICT,
        OTHER
    }

    public enum STATE {
        INACTIVE,
        ACTIVE,
        COOLING
    }


}