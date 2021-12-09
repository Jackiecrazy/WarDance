package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.EffectUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class FlameDance extends WarCry {
    private static final UUID attackSpeed = UUID.fromString("338a5b6f-46c2-44b6-913f-f15c5e59cd48");
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", ProcPoints.melee, ProcPoints.on_being_hurt, ProcPoints.modify_crit, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList(ProcPoints.melee, ProcPoints.on_parry)));

    @SubscribeEvent
    public static void flames(LivingAttackEvent e) {
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity seme = (LivingEntity) e.getSource().getTrueSource();
            LivingEntity uke = e.getEntityLiving();
            if (CasterData.getCap(seme).isSkillUsable(WarSkills.FLAME_DANCE.get())) {
                EffectUtils.attemptAddPot(uke, EffectUtils.stackPot(uke, new EffectInstance(WarEffects.CORROSION.get(), (int) CombatData.getCap(seme).getCombo(), EffectUtils.getEffectiveLevel(uke, WarEffects.CORROSION.get()) < 4 ? 0 : -1), EffectUtils.StackingMethod.MAXDURATION), false);
            }
        }
    }

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    protected int getDuration(float might) {
        return (int) (might*40);
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public void onCooledDown(LivingEntity caster, float overflow) {
        super.onCooledDown(caster, overflow);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingAttackEvent) {
            stats.setArbitraryFloat(stats.getArbitraryFloat() + 0.34f);
            if (((LivingAttackEvent) procPoint).getEntityLiving() == target && ((LivingAttackEvent) procPoint).getSource() instanceof CombatDamageSource) {
                if(stats.getArbitraryFloat() >= 1&&target.getFireTimer()>0)
                ((CombatDamageSource) ((LivingAttackEvent) procPoint).getSource()).setArmorReductionPercentage(0.5f);
                target.forceFireTicks((int) (CombatData.getCap(caster).getCombo()/2));
            }
        } else if (procPoint instanceof CriticalHitEvent && stats.getArbitraryFloat() >= 1) {
            procPoint.setResult(Event.Result.ALLOW);
            ((CriticalHitEvent) procPoint).setDamageModifier(((CriticalHitEvent) procPoint).getDamageModifier() * 1.5f);
            stats.setArbitraryFloat(0);
        }
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }
}
