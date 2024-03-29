package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Wrestle extends Skill {
    private final HashSet<String> unarm = makeTag(SkillTags.offensive, SkillTags.physical, SkillTags.unarmed);
    UUID slow = UUID.fromString("abe24c38-73e3-4551-9ef4-e16e117699c1");

    @SubscribeEvent
    public static void wrestle(LivingDamageEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        LivingEntity target = e.getEntity();
        Entity t = e.getSource().getEntity();
        //black mark stealing health/posture/spirit
        if (e.getAmount() > 0 && DamageUtils.isMeleeAttack(e.getSource()) && t instanceof LivingEntity attacker) {
            Marks.getCap(target).getActiveMark(WarSkills.WRESTLE.get()).ifPresent(a -> {
                a.addArbitraryFloat(-1);
                if (a.getArbitraryFloat() < 0) a.setDuration(-999);
            });
            Marks.getCap(attacker).getActiveMark(WarSkills.WRESTLE.get()).ifPresent(a -> {
                a.addArbitraryFloat(-1);
                if (target == a.getCaster(target.level())) a.addArbitraryFloat(-2);
                if (a.getArbitraryFloat() < 0) a.setDuration(-999);
            });
        }
    }

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.grapple;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
    }

    @Override
    public HashSet<String> getTags() {
        return unarm;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        activeTick(stats);
        if (stats.getState() == STATE.ACTIVE) {
            SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, slow, -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        if (cooldownTick(stats)) {
            return true;
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (caster == null) {
            sd.setDuration(-999);
            return true;
        }
        //immediately end if not unarmed
        if (!CombatUtils.isFullyUnarmed(caster))
            sd.setDuration(-999);
        //boing
        SkillUtils.updateTetheringVelocity(caster, target, 2);
        //effects
        target.addEffect(new MobEffectInstance(FootworkEffects.ENFEEBLE.get(), 10));
        target.addEffect(new MobEffectInstance(FootworkEffects.UNSTEADY.get(), 10));
        sd.decrementDuration(0.05f);
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, slow, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {

    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity e = GeneralUtils.raytraceLiving(caster.level(), caster, 3);
        if (to == STATE.ACTIVE && e != null && CombatUtils.isFullyUnarmed(caster) && cast(caster, e, duration())) {
            if(caster.isInvisible())
                completeChallenge(caster);
            mark(caster, e, duration(), 10 * prev.getEffectiveness());
            prev.setArbitraryFloat(0);
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 10);
            SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, slow, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        return boundCast(prev, from, to);
    }

    @Nullable
    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        SkillUtils.modifyAttribute(target, Attributes.ATTACK_SPEED, slow, -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (caster != null) {
            SkillUtils.modifyAttribute(caster, Attributes.MOVEMENT_SPEED, slow, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
            markUsed(caster);
        }
        SkillUtils.modifyAttribute(target, Attributes.ATTACK_SPEED, slow, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        super.onMarkEnd(caster, target, sd);
    }

    protected int duration() {
        return 200;
    }
}
