package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.EffectUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Shatter extends HeavyBlow {
    @SubscribeEvent
    public static void shatter(LivingAttackEvent e) {
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity seme = (LivingEntity) e.getSource().getTrueSource();
            LivingEntity uke = e.getEntityLiving();
            if (CasterData.getCap(seme).isSkillUsable(WarSkills.SHATTER.get()))
                EffectUtils.attemptAddPot(uke, EffectUtils.stackPot(uke, new EffectInstance(Effects.MINING_FATIGUE, (int) CombatData.getCap(seme).getCombo()), EffectUtils.StackingMethod.MAXDURATION), false);
        }
    }

    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        int buff = EffectUtils.getEffectiveLevel(target, Effects.MINING_FATIGUE);
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getDefendingHand() != null && ((ParryEvent) procPoint).getAttacker() == caster) {
            if (CasterData.getCap(target).isSkillActive(WarSkills.IRON_GUARD.get())) return;
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getDefendingHand(), 30 + (20 * buff));
            target.removePotionEffect(Effects.MINING_FATIGUE);
            markUsed(caster);
        } else if (procPoint instanceof CriticalHitEvent) {
            procPoint.setResult(Event.Result.ALLOW);
            //((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier() * buff);
            markUsed(caster);
        }
    }
}
