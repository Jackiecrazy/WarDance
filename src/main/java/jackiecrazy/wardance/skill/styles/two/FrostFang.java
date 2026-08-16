package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.capability.timeslow.TimeSlowData;
import jackiecrazy.footwork.event.EntityAwarenessEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE;
import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = FORGE)
public class FrostFang extends WarCry {
    /*
     * doubled damage on first melee strike,
     * sticks a mark on the target that slows them transfers half damage received to internal instead
     * (basically halves damage taken).
     * the mark goes down by a bit per second. extra if you're not in range or they're targeting something else.
     * Mark also goes down somewhat when they are hit by another attacker
     * at specific mark breakpoints it will ding.
     * Attacks within ~2 seconds of the ding gain increased damage and ticks the mark down more.
     *
     * every x seconds, mark a target nearby for 2 seconds. Mark glows and causes target to take double damage
     * successful attacks convert mark to frostbite and halves the cooldown to the next mark
     * Frostbite deals freezing damage, slows the target in time, converts half damage received to internal, and prevents them being marked again
     */
    private static final AttributeModifier luck = new AttributeModifier(UUID.fromString("77723885-afb9-4937-9c02-612ee5b6135a"), "frost fang bonus", 2, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier speed = new AttributeModifier(UUID.fromString("07430131-9baa-47b4-a51c-9a6f48d564f4"), "frost fang bonus", 0.4, AttributeModifier.Operation.MULTIPLY_BASE);
    private final HashSet<String> tag = makeTag("chant", ProcPoints.melee, ProcPoints.on_being_hurt, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> chant = makeTag(SkillTags.chant, SkillTags.melee, SkillTags.state);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void frozen(LivingHurtEvent e) {
        LivingEntity uke = e.getEntity();
        Marks.getCap(uke).getActiveMark(WarSkills.FROST_FANG.get()).ifPresent((a) -> {
            if (a.isCondition()) {
                e.setAmount(e.getAmount() / 2);
                CombatData.getCap(uke).recordDamage(e.getAmount());
            }
        });
    }

    @Override
    protected int getDuration(float might) {
        return (int) (might * 20);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint.getPhase() != EventPriority.HIGHEST) return;
        float damageBuff = 1.5f;
        if (procPoint instanceof MeleePostureEvent.Pre cpe && cpe.getEntity() != caster && hasMark(target) && !getExistingMark(target).isCondition()) {
            cpe.setPostureConsumption(cpe.getPostureConsumption() * damageBuff);
            if (caster.level() instanceof ServerLevel server)
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()).setPos(target.blockPosition()), target.getX(), target.getY(), target.getZ(), (int) stats.getArbitraryFloat() * 20, target.getBbWidth(), target.getBbHeight() / 2, target.getBbWidth(), 0.5f);
            CombatData.getCap(target).tickProc("frostFang");
            //trigger slow
            getExistingMark(target).flagCondition(true);
            final float slow = 0.5f;
            getExistingMark(target).setDuration(15 * slow).setMaxDuration(15 * slow).markDirty();
            TimeSlowData.getCap(target).alterSpeed(15 * 20, slow);
            //faster cooldown
            stats.setDuration(stats.getDuration() / 2);
            stats.markDirty();
        }else if(procPoint instanceof EntityAwarenessEvent e&&e.getAttacker()==caster&&CombatData.getCap(e.getEntity()).alreadyProc("frostFang")){
            e.setAwareness(e.getAwareness()== StealthUtils.Awareness.ALERT? StealthUtils.Awareness.DISTRACTED: StealthUtils.Awareness.UNAWARE);
        } else if (procPoint instanceof LivingHurtEvent cpe && cpe.getEntity() != caster && CombatData.getCap(target).alreadyProc("frostFang")) {
            if (!cpe.isCanceled()) {
                if (cpe.getSource() instanceof CombatDamageSource cds) {
                    cds.setProcSkillEffects(true);
                }
                cpe.setAmount(cpe.getAmount() * damageBuff);
            }
        }
    }

    @Override
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return mark.isCondition();
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to !=STATE.ACTIVE) {
            prev.setState(STATE.ACTIVE);
            prev.setDuration(7);
            prev.setArbitraryFloat(0);
            prev.setMaxDuration(7);
            return true;
        }
        return false;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            boolean ret = activeTick(stats);
            if (stats.getDuration() <= 0&& StylishData.getCap(caster).isCombatMode()) {
                float luck = (float) caster.getAttributeValue(Attributes.LUCK);
                float upperBound = Math.max(10+luck/2, 8.5f-luck+1);
                float dur = WarDance.rand.nextFloat(8.5f-luck, upperBound);
                stats.setDuration(dur);
                stats.setMaxDuration(dur);
                stats.markDirty();
                final float reach = 8;
                final List<LivingEntity> allEntities = caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(reach)).stream().filter(a -> !hasMark(a) && !TargetingUtils.isAlly(a, caster)).toList();
                if (allEntities.isEmpty()) return true;
                final Optional<LivingEntity> viewable = allEntities.stream().filter(a -> !GeneralUtils.viewBlocked(caster, a, false)).findAny();
                LivingEntity target = viewable.orElse(allEntities.get(0));
                mark(caster, target, 2f);
                //particle trail
                if (caster.level() instanceof ServerLevel server) {
                    Vec3 from=caster.getEyePosition();
                    Vec3 to = target.getEyePosition();
                    for(double lerp =0;lerp<1;lerp+=0.1) {
                        Vec3 lerped = from.lerp(to, lerp);
                        server.sendParticles(ParticleTypes.SNOWFLAKE, lerped.x(), lerped.y(), lerped.z(), 3, 0, 0, 0, 0.5f);
                    }
                }
                return true;
            }
            return ret;
        }else stats.setState(STATE.ACTIVE);
        return false;
    }

    @Override
    public boolean markTick(@Nullable LivingEntity caster, LivingEntity target, SkillData sd) {
        //add freezing
        if (sd.isCondition())
            target.setTicksFrozen(200);
        return markTickDown(sd);
    }

    @Override
    public @Nullable SkillData onMarked(LivingEntity caster,
                                        LivingEntity target,
                                        SkillData sd,
                                        @Nullable SkillData existing) {
        //no stacking
        if (existing != null) return existing;
        //flagged targets glow
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
        return sd;
    }

    @Override
    public void onEquip(LivingEntity caster) {
        SkillUtils.addAttribute(caster, Attributes.LUCK, luck);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.removeAttribute(caster, Attributes.LUCK, luck);
        super.onUnequip(caster, stats);
    }

}
