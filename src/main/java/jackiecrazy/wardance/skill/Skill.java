package jackiecrazy.wardance.skill;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.move.Move;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.advancement.WarAdvancements;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.event.SkillCooldownEvent;
import jackiecrazy.wardance.event.SkillResourceEvent;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;

public abstract class Skill extends Move {
    public static final HashMap<SkillArchetype, List<Skill>> variationMap = new HashMap<>();
    public static final HashMap<SkillCategory, List<Skill>> categoryMap = new HashMap<>();
    protected static final HashSet<String> none = new HashSet<>();
    protected static final HashSet<String> offensivePhysical = makeTag(SkillTags.offensive, SkillTags.physical);
    protected static final HashSet<String> defensivePhysical = makeTag(SkillTags.defensive, SkillTags.physical);
    protected static final HashSet<String> offensive = makeTag(SkillTags.offensive);
    protected static final HashSet<String> defensive = makeTag(SkillTags.defensive);
    protected static final HashSet<String> passive = makeTag(SkillTags.passive);
    protected static final HashSet<String> special = makeTag(SkillTags.special);
    protected static final HashSet<String> state = makeTag(SkillTags.state);
    protected static final HashSet<String> style = makeTag(SkillTags.style);
    private ResourceLocation registryName;
    private SkillCategory category = SkillColors.none;

    public Skill() {
        //archetype only, color is handled by category declaration
        List<Skill> insert = variationMap.get(getArchetype());
        if (insert == null) {
            insert = new ArrayList<>();
        }
        insert.add(this);
        variationMap.put(this.getArchetype(), insert);
    }
    private boolean challenge;

    public boolean hasChallenge(){
        return challenge;
    }
    public Skill setChallenge(){
        challenge=true;
        return this;
    }

    protected static HashSet<String> makeTag(String... stuff) {
        return new HashSet<>(Arrays.asList(stuff));
    }

    @Nullable
    public static Skill getSkill(String name) {
        return getSkill(new ResourceLocation(name));
    }

    @Nullable
    public static Skill getSkill(ResourceLocation name) {
        return WarSkills.SUPPLIER.get().getValue(name);
    }

    public boolean isFamily(Skill s) {
        if (s == null) return false;
        return getArchetype().equals(s.getArchetype());
    }

    public boolean isPassive(LivingEntity caster) {
        return getTags().contains("passive");
    }

    @Nonnull
    public SkillArchetype getArchetype() {
        return SkillArchetypes.none;
    }

    @Nonnull
    public SkillCategory getCategory() {
        return category;
    }

    public Skill setCategory(SkillCategory sc) {
        category = sc;
        List<Skill> insert = categoryMap.get(getCategory());
        if (insert == null) {
            insert = new ArrayList<>();
        }
        insert.add(this);
        categoryMap.put(getCategory(), insert);
        return this;
    }

    public boolean isLearnable(LivingEntity caster) {
        return true;
    }

    public CastStatus castingCheck(LivingEntity caster, SkillData sd) {
        ISkillCapability cap = CasterData.getCap(caster);
        switch (cap.getSkillState(this)) {
            case ACTIVE -> {
                return CastStatus.ACTIVE;
            }
            case COOLING -> {
                return CastStatus.COOLDOWN;
            }
            case HOLSTERED -> {
                return CastStatus.HOLSTERED;
            }
        }
        SkillStyle style = cap.getStyle();
        if (style != null && !style.canCast(caster, this)) return CastStatus.STYLE;
        for (String s : getSoftIncompatibility(caster))
            if (cap.isTagActive(s))
                return CastStatus.CONFLICT;
        if (caster.isSilent() && getTags().contains("chant")) return CastStatus.SILENCE;
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
        for (String tag : this.getTags())
            if (s.getSoftIncompatibility(caster).contains(tag)) return false;
        for (String tag : s.getTags())
            if (this.getSoftIncompatibility(caster).contains(tag)) return false;
        return true;
    }

    public boolean isEquippableWith(Skill s, LivingEntity caster) {
        if (s == null) return true;
        if (s == this) return true; //of course I'm compatible with myself excuse me
        for (String tag : this.getTags())
            if (s.getHardIncompatibility().contains(tag)) return false;
        for (String tag : s.getTags())
            if (this.getHardIncompatibility().contains(tag)) return false;
        return true;
    }

    public void onCooledDown(LivingEntity caster, float overflow) {
        caster.level().playSound(null, caster, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.AMBIENT, 0.3f + WarDance.rand.nextFloat(), 0.5f + WarDance.rand.nextFloat());
    }

    protected boolean showArchetypeDescription() {
        return getArchetype() != SkillArchetypes.none;
    }

    public Component description() {
        MutableComponent ret = Component.empty();
        if (showArchetypeDescription())
            ret = getArchetype().description().append("\n");
        return ret.append(Component.translatable("wardance." + this.getRegistryName().getPath() + ".desc"));
    }

    public ResourceLocation getRegistryName() {
        if (registryName == null) {
            registryName = WarSkills.SUPPLIER.get().getKey(this);
        }
        return registryName;
    }

    /**
     * @param caster only nonnull if it's in the casting bar!
     */
    public MutableComponent getDisplayName(LivingEntity caster) {
        return Component.translatable("wardance." + getRegistryName().getPath() + ".name");
    }

    public ResourceLocation icon() {
        if (getArchetype() != SkillArchetypes.none) return getArchetype().icon();
        return new ResourceLocation("wardance:textures/skill/" + getRegistryName().getPath() + ".png");
    }

    public Color getColor() {
        return getCategory().getColor();
    }

    public abstract HashSet<String> getTags();//requires breath, bound, debuffing, healing, aoe, etc.

    @Nonnull
    public HashSet<String> getSoftIncompatibility(LivingEntity caster){
        return none;
    }

    public HashSet<String> getHardIncompatibility() {
        return none;
    }

    protected void completeChallenge(LivingEntity caster) {
        if(caster instanceof ServerPlayer sp)
            WarAdvancements.CHALLENGE_ONLY.trigger(sp, CasterData.getCap(caster).getSkillData(this).orElse(SkillData.DUMMY));
    }

    /**
     * @return true to mark the skill (and thus capability) as dirty
     */
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return false;
    }

    public boolean markTick(@Nullable LivingEntity caster, LivingEntity target, SkillData sd) {
        return false;
    }

    public boolean showsMark(SkillData mark, LivingEntity target) {
        return true;
    }
    public boolean fakeMark(LivingEntity caster, LivingEntity target, SkillData stats) {
        return false;
    }

    protected void removeMark(LivingEntity target) {
        Marks.getCap(target).removeMark(this);
    }

    public void onEquip(LivingEntity caster) {
        SkillData put = new SkillData(this, 0).setState(STATE.INACTIVE).setCaster(caster);
        onStateChange(caster, put, STATE.INACTIVE, STATE.COOLING);
        put.markDirty();
        CasterData.getCap(caster).getAllSkillData().put(this, put);
    }

    public void onUnequip(LivingEntity caster, SkillData stats) {
    }

    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {

    }

    public abstract boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to);

    @Nullable
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            sd.setDuration(sd.getDuration() + existing.getDuration());
            sd.setMaxDuration(sd.getDuration());
        }
        return sd;
    }

    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {

    }

    //***************************************//
    //   convenience functions start here    //
    //***************************************//

    /**
     * returns true if it does something expected, in which case this will just toggle the state.
     */
    protected boolean instantCast(SkillData mod, STATE from, STATE to) {
        //what
        if (from == to) {
            return true;
        }
        //switching between inactive and active
        if (to == STATE.HOLSTERED) {
            mod.setState(STATE.ACTIVE);
            return true;
        }
        //switching between cooling and inactive, duration test to prevent modification by holstering
        if (to == STATE.INACTIVE && from == STATE.COOLING && mod.getDuration() <= 0) {
            mod.setState(STATE.INACTIVE);
            return true;
        }
        return false;
    }

    /**
     * returns true if it does something expected, in which case this will just toggle the state.
     */
    protected boolean passive(SkillData mod, STATE from, STATE to) {
        //what
        if (from == to) {
            return true;
        }
        //switching between cooling and inactive, duration test to prevent modification by holstering
        if (to == STATE.INACTIVE && from == STATE.COOLING && mod.getDuration() <= 0) {
            mod.setState(STATE.INACTIVE);
            return true;
        }
        return false;
    }

    /**
     * returns true if it does something expected, in which case this will just toggle the state.
     */
    protected boolean boundCast(SkillData mod, STATE from, STATE to) {
        //what
        if (from == to) {
            return true;
        }
        //switching between inactive and holstered
        if ((from == STATE.INACTIVE && to == STATE.HOLSTERED) || (from == STATE.HOLSTERED && to == STATE.INACTIVE)) {
            mod.setState(to);
            return true;
        }
        //switching between cooling and inactive, duration test to prevent modification by holstering
        if (to == STATE.INACTIVE && from == STATE.COOLING && mod.getDuration() <= 0) {
            //caster.world.playMovingSound(null, caster, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 0.3f + WarDance.rand.nextFloat(), 0.5f + WarDance.rand.nextFloat());
            mod.setState(STATE.INACTIVE);
            return true;
        }
        return false;
    }

    protected void attackCooldown(Event e, LivingEntity caster, SkillData stats) {
        if (e instanceof LivingAttackEvent && ((LivingAttackEvent) e).getEntity() != caster && stats.getState() == STATE.COOLING && e.getPhase() == EventPriority.HIGHEST) {
            stats.decrementDuration();
        }
    }

    protected boolean cooldownTick(SkillData stats) {
        if (stats.getState() == STATE.COOLING && stats.getDuration() > 0) {
            stats.decrementDuration(0.05f);
            int round = (int) (stats.getDuration() * 20);
            return stats.getDuration() < 3 || round % 20 == 0;
        }
        return false;
    }

    protected boolean activeTick(SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            stats.decrementDuration(0.05f);
            int round = (int) (stats.getDuration() * 20);
            return stats.getDuration() < 3 || round % 20 == 0;
        }
        return false;
    }

    protected boolean markTickDown(SkillData stats) {
        stats.decrementDuration(0.05f);
        int round = (int) (stats.getDuration() * 20);
        return (round % 20 == 0);
    }

    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return false;
    }

    protected boolean cast(LivingEntity caster) {
        return cast(caster, -999);
    }

    protected boolean cast(LivingEntity caster, float duration) {
        return cast(caster, null, duration);
    }

    protected boolean cast(LivingEntity caster, LivingEntity target, float duration) {
        return cast(caster, target, duration, false, 0);
    }

    /*
    upon casting, send event to determine effectiveness
    get effectiveness, feed into activation with custom transformations on a skill-by-skill basis
     */
    protected boolean cast(LivingEntity caster, @Nullable LivingEntity target, float duration, boolean flag, float arbitrary) {
        SkillResourceEvent sre = new SkillResourceEvent(caster, target, this);
        MinecraftForge.EVENT_BUS.post(sre);
        if (!sre.isCanceled() && CombatData.getCap(caster).getMight() >= sre.getMight() && CombatData.getCap(caster).getSpirit() >= sre.getSpirit()) {
            SkillCastEvent sce = initializeCast(caster, target, SkillUtils.getSkillEffectiveness(caster), sre.getMight(), sre.getSpirit(), duration, flag, arbitrary);

            MinecraftForge.EVENT_BUS.post(sce);
            if (sce.getMight() > 0)
                CombatData.getCap(caster).consumeMight(sce.getMight());
            if (sce.getSpirit() > 0)
                CombatData.getCap(caster).consumeSpirit(sce.getSpirit());
            activate(caster, (float) sce.getEffectiveness(), sce.getDuration(), sce.isFlag(), sce.getArbitrary());
            if(caster instanceof ServerPlayer sp)
                WarAdvancements.SKILL_CAST_TRIGGER.trigger(sp, target, getExistingData(caster));
            return true;
        }
        return false;
    }

    protected void setCooldown(LivingEntity caster, SkillData a, float duration) {
        if (caster == null) return;
        SkillCooldownEvent sce = new SkillCooldownEvent(caster, this, duration);
        MinecraftForge.EVENT_BUS.post(sce);
//        if (getParentSkill() != null)
//            CasterData.getCap(caster).setSkillCooldown(getParentSkill(), sce.getCooldown());
        a.flagCondition(false);
        a.setArbitraryFloat(0);
        a.setState(STATE.COOLING);
        final float dur = CasterData.getCap(caster).getSkillData(this).isPresent() ? sce.getCooldown() : duration;
        a.setMaxDuration(dur);
        a.setDuration(dur);
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

    protected boolean activate(LivingEntity caster, float duration, boolean flag, float something) {
        return activate(caster, (float) caster.getAttributeValue(FootworkAttributes.SKILL_EFFECTIVENESS.get()), duration, flag, something);
    }

    /**
     * @return whether the skill was successfully cast
     */
    protected boolean activate(LivingEntity caster, float effectiveness, float duration, boolean flag, float something) {
        //default implementation scales duration
        caster.level().playSound(null, caster, SoundEvents.FIRECHARGE_USE, SoundSource.AMBIENT, 0.3f + WarDance.rand.nextFloat(), 0.5f + WarDance.rand.nextFloat());
        CasterData.getCap(caster).getSkillData(this).ifPresent(a -> {
            a.setDuration(duration);
            a.setMaxDuration(duration);
            a.flagCondition(flag);
            a.setArbitraryFloat(something);
            a.setState(STATE.ACTIVE);
            a.setEffectiveness(effectiveness);
        });
        return true;
    }

    protected boolean silentActivate(LivingEntity caster, float duration, boolean flag, float something) {
        CasterData.getCap(caster).getSkillData(this).ifPresent(a -> {
            a.setDuration(duration);
            a.setMaxDuration(duration);
            a.flagCondition(flag);
            a.setArbitraryFloat(something);
            a.setState(STATE.ACTIVE);
            a.setEffectiveness(SkillUtils.getSkillEffectiveness(caster));
        });
        return true;
    }

    protected void markUsed(LivingEntity caster) {
        markUsed(caster, false);
    }

    protected void markUsed(LivingEntity caster, boolean silent) {
        if (GeneralConfig.debug)
            WarDance.LOGGER.debug(this.getRegistryName() + " has ended");
        if (!silent)
            caster.level().playSound(null, caster, SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat());
        CasterData.getCap(caster).getSkillData(this).ifPresent(a -> {
            a.setDuration(-999);
            a.setState(STATE.ACTIVE);
        });
    }

    protected void mark(LivingEntity caster, LivingEntity target, float duration) {
        mark(caster, target, duration, 0);
    }

    protected void mark(LivingEntity caster, LivingEntity target, float duration, float arbitrary) {
        Marks.getCap(target).mark(new SkillData(this, duration).setCaster(caster).setArbitraryFloat(arbitrary));
    }

    protected void mark(LivingEntity caster, LivingEntity target, float duration, float arbitrary, boolean bool) {
        Marks.getCap(target).mark(new SkillData(this, duration).setCaster(caster).setArbitraryFloat(arbitrary).flagCondition(bool));
    }

    protected boolean isMarked(LivingEntity target) {
        return Marks.getCap(target).isMarked(this);
    }

    protected SkillData getExistingMark(LivingEntity target) {
        return Marks.getCap(target).getActiveMark(this).orElse(SkillData.DUMMY);
    }

    protected SkillData getExistingData(LivingEntity caster) {
        return CasterData.getCap(caster).getSkillData(this).orElse(SkillData.DUMMY);
    }

    /**
     * returns an event with all enhancements from effectiveness already applied. Override as needed.
     */
    protected SkillCastEvent initializeCast(LivingEntity caster, @Nullable LivingEntity target, double effectiveness, float might, float spirit, float duration, boolean flag, float arbitrary) {
        return new SkillCastEvent(caster, target, this, effectiveness, might, spirit, duration, flag, arbitrary);
    }

    protected boolean hasMark(LivingEntity target) {
        return Marks.getCap(target).getActiveMark(this).isPresent();
    }

    protected boolean hasSkill(LivingEntity caster) {
        return CasterData.getCap(caster).getSkillData(this).isPresent();
    }

    public enum CastStatus {
        ALLOWED,
        COOLDOWN,
        CONFLICT,
        HOLSTERED,
        SPIRIT,
        MIGHT,
        SILENCE,
        ACTIVE,
        STYLE,
        OTHER
    }

    public enum STATE {
        INACTIVE,
        HOLSTERED,
        ACTIVE,
        COOLING
    }


}