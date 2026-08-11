package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Momentum extends HeavyBlow {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void forceCriT(CriticalHitEvent procPoint) {
        if (!procPoint.getEntity().isEffectiveAi() || !(procPoint.getTarget() instanceof LivingEntity)) return;
        final ISkillCapability cap = CasterData.getCap(procPoint.getEntity());
        if (cap.getEquippedSkillsAndStyle().contains(WarSkills.MOMENTUM.get())) {
            cap.getSkillData(WarSkills.MOMENTUM.get()).ifPresent(stats -> {
                if (stats.getArbitraryFloat() == 0) {
                    procPoint.setResult(Event.Result.ALLOW);
                    procPoint.setDamageModifier(procPoint.getDamageModifier() + 0.005f * SkillUtils.getSkillEffectiveness(procPoint.getEntity()) * power(StylishData.getCap(procPoint.getEntity()).getCombo(), 2));
                } else procPoint.setResult(Event.Result.DENY);
            });
        }
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if(procPoint instanceof PlayInteractionEvent.Interaction p){
            stats.setArbitraryFloat(stats.getArbitraryFloat()+1);
            int combo = (int) (stats.getArbitraryFloat() + 1);
            combo %= 7 - (int)StylishData.getCap(caster).getCombo();
            stats.setArbitraryFloat(combo);
        }
        if (procPoint instanceof MeleePostureEvent.Defense && ((MeleePostureEvent.Defense) procPoint).getDefendingHand() != null && procPoint.getPhase() == EventPriority.HIGHEST && ((MeleePostureEvent.Defense) procPoint).getAttacker() == caster) {
            ((MeleePostureEvent.Defense) procPoint).setPostureConsumption(((MeleePostureEvent.Defense) procPoint).getPostureConsumption() + 0.01f * SkillUtils.getSkillEffectiveness(caster) * power(StylishData.getCap(caster).getCombo(), 2));
        }
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return true;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.INACTIVE);
        return false;
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
    }

    private static float power(float base, int to) {
        float fin = 1;
        while (to > 0) {
            fin *= base;
            to--;
        }
        return fin;
    }
}
