package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.HashSet;
import java.util.UUID;

public class WindScar extends WarCry {
    private static final AttributeModifier reach = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "wind scar bonus", 1, AttributeModifier.Operation.ADDITION);
    private final HashSet<String> tag = makeTag("chant", ProcPoints.on_being_hurt, ProcPoints.melee, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> no = makeTag((("sweep")));

    @Override
    public void onEquip(LivingEntity caster) {
        SkillUtils.addAttribute(caster, ForgeMod.ATTACK_RANGE.get(), reach);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.removeAttribute(caster, ForgeMod.ATTACK_RANGE.get(), reach);
        super.onUnequip(caster, stats);
    }

    protected boolean cast(LivingEntity caster, float duration) {
        return cast(caster, null, duration, false, CombatData.getCap(caster).getMight());
    }

    @Override
    protected int getDuration(float might) {
        return (int) (3 * might);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingHurtEvent hurt && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.ACTIVE && hurt.getEntity() == target) {
            double dist = Math.sqrt(GeneralUtils.getDistSqCompensated(caster, target));
            if (hurt.getSource() instanceof CombatDamageSource cds)
                cds.setArmorReductionPercentage((float) (dist * 0.15f));
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return super.onStateChange(caster, prev, from, to);
    }
}
