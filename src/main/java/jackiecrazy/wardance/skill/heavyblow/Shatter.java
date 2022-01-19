package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.EffectUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Shatter extends HeavyBlow {
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, Entity target) {
        int buff = EffectUtils.getEffectiveLevel(target, Effects.MINING_FATIGUE);
        if (procPoint instanceof ParryEvent && stats.isCondition() && ((ParryEvent) procPoint).getDefendingHand() != null && ((ParryEvent) procPoint).getAttacker() == caster) {
            if (CasterData.getCap(target).isCategoryActive(SkillCategories.iron_guard)) return;
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getDefendingHand(), 30 + (20 * buff));
            target.removePotionEffect(Effects.MINING_FATIGUE);
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent) {
            if(((CriticalHitEvent) procPoint).isVanillaCritical()) {
                stats.flagCondition(true);
                //((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier() * buff);
                markUsed(caster);
            }else{
                EffectUtils.attemptAddPot(target, EffectUtils.stackPot(target, new EffectInstance(Effects.MINING_FATIGUE, (int) (CombatData.getCap(caster).getCombo()*10)), EffectUtils.StackingMethod.MAXDURATION), false);
            }
        }
    }
}
