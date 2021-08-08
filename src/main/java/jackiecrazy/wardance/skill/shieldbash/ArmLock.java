package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class ArmLock extends ShieldBash{
    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "melee", "boundCast", "normalAttack", "countdown", SkillTags.on_parry, SkillTags.recharge_parry)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack", SkillTags.on_parry)));
    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if (procPoint instanceof ParryEvent && (CombatUtils.isShield(caster, caster.getHeldItemMainhand()) || CombatUtils.isShield(caster, caster.getHeldItemOffhand()))) {
            CombatData.getCap(((ParryEvent) procPoint).getAttacker()).setHandBind(((ParryEvent) procPoint).getAttackingHand(), 30);
        }
    }
}
