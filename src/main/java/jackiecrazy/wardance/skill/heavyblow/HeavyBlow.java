package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;

public class HeavyBlow extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoints.disable_shield, ProcPoints.melee, ProcPoints.on_hurt, "boundCast", ProcPoints.normal_attack, ProcPoints.modify_crit, ProcPoints.recharge_normal, ProcPoints.on_being_parried)));
    private final Tag<String> tags = makeTag(SkillTags.physical, SkillTags.forced_crit, SkillTags.passive, SkillTags.offensive, SkillTags.disable_shield);

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return offensive;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.heavy_blow;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        return true;
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
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 3);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent && stats.isCondition() && ((ParryEvent) procPoint).getDefendingHand() != null && ((ParryEvent) procPoint).getAttacker() == caster) {
            if (CasterData.getCap(target).isCategoryActive(SkillCategories.iron_guard)) return;
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getDefendingHand(), 30);
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent && ((CriticalHitEvent) procPoint).isVanillaCritical() && CombatData.getCap(caster).consumeMight(mightConsumption(caster))) {
            stats.flagCondition(true);
            if (this == WarSkills.VITAL_STRIKE.get())
                ((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier() * 1.4f);
            markUsed(caster);
        }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, STATE state) {
        if (state == STATE.INACTIVE) {
            activate(caster, 40);
            return true;
        }
        return super.equippedTick(caster, state);
    }
}
