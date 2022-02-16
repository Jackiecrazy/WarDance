package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.awt.*;
import java.util.UUID;

public class FrostFang extends WarCry {
    private static final AttributeModifier luck = new AttributeModifier(UUID.fromString("77723885-afb9-4937-9c02-612ee5b6135a"), "frost fang bonus", 2, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier speed = new AttributeModifier(UUID.fromString("07430131-9baa-47b4-a51c-9a6f48d564f4"), "frost fang bonus", 0.4, AttributeModifier.Operation.MULTIPLY_BASE);
    private final Tag<String> tag = makeTag("chant", ProcPoints.melee, ProcPoints.on_being_hurt, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final Tag<String> chant = makeTag(SkillTags.chant, SkillTags.melee, SkillTags.state);

    @Override
    protected void evoke(LivingEntity caster) {
        caster.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(speed);
        caster.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(speed);
        super.evoke(caster);
    }

    @Override
    protected int getDuration(float might) {
        return (int) (might * 40);
    }

    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public void onEquip(LivingEntity caster) {
        caster.getAttribute(Attributes.LUCK).removeModifier(luck.getID());
        caster.getAttribute(Attributes.LUCK).applyPersistentModifier(luck);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.LUCK).removeModifier(luck);
        caster.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(speed);
        super.onUnequip(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.HIGHEST && ((LivingAttackEvent) procPoint).getEntityLiving() == target) {
            target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 60));
            if (CombatUtils.getAwareness(caster, target) == CombatUtils.Awareness.ALERT) {
                target.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 20));
            }
        }
        super.onProc(caster, procPoint, state, stats, target);
    }
}
