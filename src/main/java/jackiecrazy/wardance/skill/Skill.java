package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
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
import java.util.*;
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
    protected static final Tag<String> empty=Tag.getEmptyTag();
    protected static final Tag<String> offensivePhysical=makeTag(SkillTags.offensive, SkillTags.physical);
    protected static final Tag<String> defensivePhysical=makeTag(SkillTags.defensive, SkillTags.physical);
    protected static final Tag<String> offensive=makeTag(SkillTags.offensive);
    protected static final Tag<String> defensive=makeTag(SkillTags.defensive);
    protected static final Tag<String> passive=makeTag(SkillTags.passive);
    protected static final Tag<String> special=makeTag(SkillTags.special);
    protected static final Tag<String> state=makeTag(SkillTags.state);
    public static final HashMap<Skill, List<Skill>> variationMap = new HashMap<>();

    protected static Tag<String> makeTag(String... stuff){
        return Tag.getTagFromContents(new HashSet<>(Arrays.asList(stuff)));
    }

    public Skill() {
        //SkillCategory
        if (this.getParentSkill() == null && !variationMap.containsKey(this)) {
            List<Skill> toadd = new ArrayList<>();
            toadd.add(this);
            variationMap.put(this, toadd);
        } else if (this.getParentSkill() != null) {
            List<Skill> insert = variationMap.get(getParentSkill());
            if (insert == null) {
                insert = new ArrayList<>();
                insert.add(getParentSkill());
            }
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
        return getProcPoints(caster).contains("passive");
    }

    @Nullable
    public Skill getParentSkill() {
        return null;
    }//TODO move this to a SkillCategory class so base skills can have different names

    @Nonnull
    public final Skill tryGetParentSkill() {
        return getParentSkill() == null ? this : getParentSkill();
    }

    public boolean isSelectable(LivingEntity caster) {
        return true;
    }

    public CastStatus castingCheck(LivingEntity caster) {
        ISkillCapability cap = CasterData.getCap(caster);
        if (cap.isSkillActive(this)) return CastStatus.ACTIVE;
        for (String s : getIncompatibleTags(caster).getAllElements())
            if (cap.isTagActive(s))
                return CastStatus.CONFLICT;
        if (caster.isSilent() && getTags(caster).contains("chant")) return CastStatus.SILENCE;
        if (cap.isSkillCoolingDown(this))
            return CastStatus.COOLDOWN;
//        if (getParentSkill() != null)
//            return getParentSkill().castingCheck(caster);
        if (CombatData.getCap(caster).getSpirit() < spiritConsumption(caster))
            return CastStatus.SPIRIT;
        if (CombatData.getCap(caster).getMight() < mightConsumption(caster))
            return CastStatus.MIGHT;
        return CastStatus.ALLOWED;
    }

    public float spiritConsumption(LivingEntity caster) {
        return 0;
    }

    public float mightConsumption(LivingEntity caster) {
        return 0;
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

    public ITextComponent baseName() {
        return new TranslationTextComponent(this.getRegistryName().toString() + ".parent");
    }

    public ITextComponent baseDescription() {
        return new TranslationTextComponent(this.getRegistryName().toString() + ".base");
    }

    /**
     * @param caster only nonnull if it's in the casting bar!
     */
    public ITextComponent getDisplayName(LivingEntity caster) {
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

    public abstract Tag<String> getProcPoints(LivingEntity caster);

    public abstract Tag<String> getTags(LivingEntity caster);//requires breath, debuffing, healing, aoe, etc.

    public abstract Tag<String> getIncompatibleTags(LivingEntity caster);

    public boolean checkAndCast(LivingEntity caster) {
        if (castingCheck(caster) != CastStatus.ALLOWED) return false;
        if (!isPassive(caster) && MinecraftForge.EVENT_BUS.post(new SkillCastEvent(caster, this))) return false;
        caster.world.playMovingSound(null, caster, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.AMBIENT, 0.3f + WarDance.rand.nextFloat(), 0.5f + WarDance.rand.nextFloat());
        return onCast(caster);
    }

    public abstract boolean onCast(LivingEntity caster);

    /**
     * @return whether the client should be updated.
     */
    public boolean activeTick(LivingEntity caster, SkillData d) {
        if (d.getSkill().getProcPoints(caster).contains(ProcPoints.countdown) && d.getDuration() > 0) {
            d.decrementDuration();
            if (d.getDuration() <= 0) markUsed(caster);
            return true;
        }
        return false;
    }

    public boolean equippedTick(LivingEntity caster, STATE state) {
        return false;
    }

    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (this.getProcPoints(caster).contains(ProcPoints.afflict_tick)) {
            sd.decrementDuration();
        }
        if (sd.getDuration() <= 0) {
            removeMark(target);
            return true;
        }
        return false;
    }

    protected void removeMark(LivingEntity target) {
        Marks.getCap(target).removeMark(this);
    }

    /**
     * @return whether the client should be updated.
     */
    public boolean coolingTick(LivingEntity caster, SkillCooldownData d) {
        if (d.getSkill().getProcPoints(caster).contains(ProcPoints.recharge_time)) {
            return onCooldownProc(caster, d, null);
        }
        return false;
    }

    /**
     * responsible for setting the cooldown as well.
     */
    public abstract void onEffectEnd(LivingEntity caster, SkillData stats);

    public void onAdded(LivingEntity caster, SkillData stats) {
        onEffectEnd(caster, stats);
    }

    public void onRemoved(LivingEntity caster, SkillData stats) {
        onEffectEnd(caster, stats);
    }

    public abstract void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint);

    public boolean onCooldownProc(LivingEntity caster, SkillCooldownData stats, Event procPoint) {
        CasterData.getCap(caster).decrementSkillCooldown(this, 1);
        return true;
    }

    protected void setCooldown(LivingEntity caster, float duration) {
        SkillCooldownEvent sce = new SkillCooldownEvent(caster, this, duration);
        MinecraftForge.EVENT_BUS.post(sce);
//        if (getParentSkill() != null)
//            CasterData.getCap(caster).setSkillCooldown(getParentSkill(), sce.getCooldown());
        CasterData.getCap(caster).setSkillCooldown(this, sce.getCooldown());
    }

    protected boolean activate(LivingEntity caster, float duration) {
        return activate(caster, duration, false, 0);
    }

    protected boolean activate(LivingEntity caster, float duration, boolean flag) {
        return activate(caster, duration, flag, 0);
    }

    protected boolean activate(LivingEntity caster, float duration, float something) {
        return activate(caster, duration, false, something);
    }

    /**
     * @return whether the skill was successfully cast
     */
    protected boolean activate(LivingEntity caster, float duration, boolean flag, float something) {
        //System.out.println("enabling for " + duration);
        if (CasterData.getCap(caster).isSkillUsable(this)) {
            CasterData.getCap(caster).activateSkill(new SkillData(this, duration).flagCondition(flag).setArbitraryFloat(something).setCaster(caster));
            return true;
        }
        return false;
    }

    protected void markUsed(LivingEntity caster) {
        WarDance.LOGGER.debug(this.getRegistryName()+" has ended");
        caster.world.playMovingSound(null, caster, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat());
        CasterData.getCap(caster).markSkillUsed(this);
    }

    protected void mark(LivingEntity caster, LivingEntity target, float duration) {
        mark(caster, target, duration, 0);
    }

    protected void mark(LivingEntity caster, LivingEntity target, float duration, float arbitrary) {
        Marks.getCap(target).mark(new SkillData(this, duration).setCaster(caster).setArbitraryFloat(arbitrary));
    }

    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null)
            sd.setDuration(sd.getDuration() + existing.getDuration());
        return sd;
    }

    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {

    }

    public enum CastStatus {
        ALLOWED,
        COOLDOWN,
        CONFLICT,
        SPIRIT,
        MIGHT,
        SILENCE,
        ACTIVE,
        OTHER
    }

    public enum STATE {
        INACTIVE,
        ACTIVE,
        COOLING
    }


}