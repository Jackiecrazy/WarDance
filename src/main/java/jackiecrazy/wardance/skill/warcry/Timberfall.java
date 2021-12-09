package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.AttackMightEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Timberfall extends WarCry {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", ProcPoints.melee, ProcPoints.modify_crit, ProcPoints.on_hurt, ProcPoints.attack_might, ProcPoints.on_being_hurt, ProcPoints.recharge_time, ProcPoints.recharge_sleep)));
    private final Tag<String> no = Tag.getEmptyTag();//.getTagFromContents(new HashSet<>(Collections.emptyList()));

    @SubscribeEvent
    public static void timberfall(AttackMightEvent e) {
        //FIXME due to no countdown, meditate broken
        //TODO remove meditate; sneak middle click on block underneath you in combat mode and no drain to start meditating. Pick a stat to restore, then over 5 seconds the stat is reset as screen goes to white, and finally gain a series of buffs depending on what is nearby
        //TODO change saving throw

        if (e.getAttacker() != null &&CasterData.getCap(e.getAttacker()).isSkillUsable(WarSkills.TIMBERFALL.get())) {
            CombatData.getCap(e.getEntityLiving()).consumePosture(e.getQuantity() * 5);
        }
    }

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        super.onEffectEnd(caster, stats);
    }

    @Override
    public void onCooledDown(LivingEntity caster, float overflow) {
        super.onCooledDown(caster, overflow);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof CriticalHitEvent) {
            procPoint.setResult(Event.Result.ALLOW);
            ((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier()*1.4f);
            stats.decrementDuration();
        }
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }

    @Override
    public Color getColor() {
        return Color.orange;
    }

    @Override
    protected int getDuration(float might) {
        return 5+(int)might;
    }
}
