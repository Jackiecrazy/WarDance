package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.AttackMightEvent;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Timberfall extends WarCry {
    private final HashSet<String> tag = makeTag("chant", ProcPoints.melee, ProcPoints.modify_crit, ProcPoints.on_hurt, ProcPoints.attack_might, ProcPoints.on_being_hurt, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> no = none;//.getTagFromContents(new HashSet<>(Collections.emptyList()));

    @SubscribeEvent
    public static void timberfall(AttackMightEvent e) {
        if (e.getAttacker() != null && CasterData.getCap(e.getAttacker()).isSkillUsable(WarSkills.TIMBERFALL.get())) {
            CombatData.getCap(e.getEntity()).consumePosture(e.getQuantity() * 5);
        }
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof CriticalHitEvent && state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.HIGHEST && ((CriticalHitEvent) procPoint).getEntity() == caster) {
            procPoint.setResult(Event.Result.ALLOW);
            ((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier() * 1.4f);
            stats.decrementDuration();
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    protected int getDuration(float might) {
        return (int) might*2-2;
    }
}
