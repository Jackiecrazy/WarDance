package jackiecrazy.wardance.skill.fightingspirit;

import jackiecrazy.wardance.skill.ProcPoint;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Timberfall extends FightingSpirit {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", ProcPoint.melee, ProcPoint.modify_crit, ProcPoint.on_hurt, ProcPoint.on_being_hurt, ProcPoint.countdown, ProcPoint.recharge_time, ProcPoint.recharge_sleep)));
    private final Tag<String> no = Tag.getEmptyTag();//.getTagFromContents(new HashSet<>(Collections.emptyList()));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
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
        if (procPoint instanceof CriticalHitEvent)
            procPoint.setResult(Event.Result.ALLOW);
        else if (procPoint instanceof LivingHurtEvent) {
            ((LivingHurtEvent) procPoint).setAmount(((LivingHurtEvent) procPoint).getAmount() * 1.4f);
        }
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }

    @Override
    public Color getColor() {
        return Color.orange;
    }
}
