package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Wrestle extends Skill {
    private final HashSet<String> unarm = makeTag(SkillTags.offensive, SkillTags.physical, SkillTags.unarmed);

    @SubscribeEvent
    public static void wrestle(LivingDamageEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        LivingEntity target = e.getEntity();
        Entity t = e.getSource().getEntity();
        //black mark stealing health/posture/spirit
        if (e.getAmount() > 0 && CombatUtils.isMeleeAttack(e.getSource()) && t instanceof LivingEntity attacker) {
            Marks.getCap(target).getActiveMark(WarSkills.WRESTLE.get()).ifPresent(a -> {
                a.addArbitraryFloat(-1);
                if (a.getArbitraryFloat() < 0) a.setDuration(-999);
            });
            Marks.getCap(attacker).getActiveMark(WarSkills.WRESTLE.get()).ifPresent(a -> {
                a.addArbitraryFloat(-1);
                if (target == a.getCaster(target.level)) a.addArbitraryFloat(-2);
                if (a.getArbitraryFloat() < 0) a.setDuration(-999);
            });
        }
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
    public HashSet<String> getTags(LivingEntity caster) {
        return unarm;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (cooldownTick(stats)) {
            return true;
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration();
        //test for distance
        if (GeneralUtils.getDistSqCompensated(caster, target) > 9) {
            //boing
            target.setDeltaMovement(caster.getDeltaMovement().add(caster.position().vectorTo(target.position()).scale(-0.3)));
            caster.setDeltaMovement(target.getDeltaMovement().add(target.position().vectorTo(caster.position()).scale(-0.3)));
            removeMark(target);
        } else {
            //effects
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60));
            target.addEffect(new MobEffectInstance(FootworkEffects.ENFEEBLE.get(), 60));
            target.addEffect(new MobEffectInstance(FootworkEffects.UNSTEADY.get(), 60));
        }
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {

    }

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.grapple;
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity e = GeneralUtils.raytraceLiving(caster.level, caster, 3);
        if (to == STATE.ACTIVE && e != null && CombatUtils.isUnarmed(caster.getMainHandItem(), caster) && CombatUtils.isUnarmed(caster.getOffhandItem(), caster) && cast(caster, e, -999)) {
            mark(caster, e, duration(), 10);
            prev.setArbitraryFloat(0);
            markUsed(caster);
        }
        if (to == STATE.COOLING)
            setCooldown(caster, prev, 10);
        return boundCast(prev, from, to);
    }

    protected int duration() {
        return 200;
    }
}
