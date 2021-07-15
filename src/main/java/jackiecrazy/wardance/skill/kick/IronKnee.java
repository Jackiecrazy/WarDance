package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.MeleeKnockbackEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class IronKnee extends Kick {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "melee", "boundCast", "normalAttack", "countdown", "knockback", "rechargeWithAttack")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if (procPoint instanceof MeleeKnockbackEvent) {
            CombatData.getCap(target).consumePosture(caster, ((MeleeKnockbackEvent) procPoint).getStrength() * 3);
            ((MeleeKnockbackEvent) procPoint).setStrength(0);
        }
    }
}
