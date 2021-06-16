package jackiecrazy.wardance.skill.fightingspirit;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoint;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
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
        else if (procPoint instanceof LivingHurtEvent && CombatUtils.isMeleeAttack(((LivingHurtEvent) procPoint).getSource())) {
            ((LivingHurtEvent) procPoint).setAmount(((LivingHurtEvent) procPoint).getAmount() * 1.4f);
        }
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }

    @Override
    public Color getColor() {
        return Color.orange;
    }

    @SubscribeEvent
    public static void timberfall(ParryEvent e) {
        if (e.getAttacker() != null && CasterData.getCap(e.getAttacker()).isSkillUsable(WarSkills.TIMBERFALL.get())) {
            LivingEntity seme = e.getAttacker();
            ICombatCapability semeCap = CombatData.getCap(seme);
            e.setPostureConsumption(e.getPostureConsumption() + ((semeCap.getCachedCooldown() * semeCap.getCachedCooldown() * CombatUtils.getCooldownPeriod(seme, Hand.MAIN_HAND) * CombatUtils.getCooldownPeriod(seme, Hand.MAIN_HAND)) / 156.25f));
        }
    }
}
