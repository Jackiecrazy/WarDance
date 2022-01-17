package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Poise extends HeavyBlow {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "disableShield", ProcPoints.melee, ProcPoints.on_hurt, "boundCast", ProcPoints.normal_attack, ProcPoints.countdown, ProcPoints.modify_crit, ProcPoints.recharge_normal, ProcPoints.on_being_parried, ProcPoints.on_parry)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack", "noCrit")));

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        CombatData.getCap(caster).consumeMight(1);
        CombatData.getCap(caster).consumeSpirit(spiritConsumption(caster));
        activate(caster, 60);
        return true;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent && stats.isCondition() && ((ParryEvent) procPoint).getDefendingHand() != null && ((ParryEvent) procPoint).getAttacker() == caster) {
            if (CasterData.getCap(target).isCategoryActive(SkillCategories.iron_guard)) return;
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getDefendingHand(), 30);
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent && ((CriticalHitEvent) procPoint).isVanillaCritical() && CombatData.getCap(caster).consumeMight(mightConsumption(caster))) {
            stats.flagCondition(true);
            ((CriticalHitEvent) procPoint).setDamageModifier(1 + 0.7f * CombatData.getCap(caster).getPosture() / CombatData.getCap(caster).getMaxPosture());
            CombatData.getCap(caster).setPostureGrace(0);
            markUsed(caster);
        }
    }
}
