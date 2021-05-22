package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class Bandage extends Skill {
    private static final AttributeModifier wrap = new AttributeModifier(UUID.fromString("67fe7ef6-a398-4c65-9bb1-42edaa80e7b1"), "bandaging wounds", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "chant", ProcPoint.on_being_hurt, ProcPoint.countdown, ProcPoint.recharge_time, ProcPoint.recharge_sleep)));
    private final Tag<String> no = Tag.getEmptyTag();

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == Bandage.class ? null : WarSkills.IRON_GUARD.get();
    }

    @Override
    public boolean canCast(LivingEntity caster) {
        final ICombatCapability cap = CombatData.getCap(caster);
        return super.canCast(caster) && cap.getMight() == 0 && cap.getPosture() == cap.getMaxPosture() && cap.getSpirit() == cap.getMaxSpirit();
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 100);
        caster.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(wrap);
        return true;
    }

    @Override
    public boolean tick(LivingEntity caster, SkillData d) {
        if (d.getDuration() == 0) {
            CombatData.getCap(caster).setWounding(0);
            CombatData.getCap(caster).setFatigue(0);
            CombatData.getCap(caster).setBurnout(0);
        }
        return super.tick(caster, d);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        caster.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(wrap);
        setCooldown(caster, 6000);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingHurtEvent) {
            markUsed(caster);
        }
    }
}
