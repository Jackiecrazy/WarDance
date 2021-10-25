package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Overbear extends ShieldBash {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "melee", "boundCast", "normalAttack", "countdown", ProcPoints.on_hurt, ProcPoints.recharge_parry)));

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingAttackEvent && CombatUtils.isShield(caster, CombatUtils.getAttackingItemStack(((LivingAttackEvent) procPoint).getSource()))) {
            performEffect(caster, target);
            caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ITEM_SHIELD_BLOCK , SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f+WarDance.rand.nextFloat() * 0.5f);
            CombatData.getCap(target).consumePosture(caster, CombatUtils.getShieldStats(CombatUtils.getAttackingItemStack(((LivingAttackEvent) procPoint).getSource())).getA() / 10f);
            if(CombatData.getCap(target).getStaggerTime()==0){
                caster.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 50));
            }
            markUsed(caster);
        }
        if (procPoint instanceof LivingHurtEvent && (CombatUtils.isShield(caster, CombatUtils.getAttackingItemStack(((LivingHurtEvent) procPoint).getSource())))) {
            Tuple<Integer, Integer> stat = CombatUtils.getShieldStats(CombatUtils.getAttackingItemStack(((LivingHurtEvent) procPoint).getSource()));
            ((LivingHurtEvent) procPoint).setAmount(((LivingHurtEvent) procPoint).getAmount() + stat.getA() / 10f);
            markUsed(caster);
        }
    }
}
