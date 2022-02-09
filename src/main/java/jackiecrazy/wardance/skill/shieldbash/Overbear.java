package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
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
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent) {
            final ItemStack stack = CombatUtils.getAttackingItemStack(((LivingAttackEvent) procPoint).getSource());
            if (CombatUtils.isShield(caster, stack)) {
                performEffect(caster, target);
                caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                final int shieldTime = CombatUtils.getShieldStats(stack).getA();
                CombatData.getCap(target).consumePosture(caster, shieldTime / 10f, 0, true);
                if (CombatData.getCap(target).getStaggerTime() == 0) {
                    CombatData.getCap(caster).setHandBind(CombatData.getCap(caster).isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND, shieldTime);
                    if (caster instanceof PlayerEntity) {
                        ((PlayerEntity) caster).getCooldownTracker().setCooldown(stack.getItem(), shieldTime);
                    }
                }
                markUsed(caster);
            }
        }
        if (procPoint instanceof LivingHurtEvent && (CombatUtils.isShield(caster, CombatUtils.getAttackingItemStack(((LivingHurtEvent) procPoint).getSource())))) {
            Tuple<Integer, Integer> stat = CombatUtils.getShieldStats(CombatUtils.getAttackingItemStack(((LivingHurtEvent) procPoint).getSource()));
            ((LivingHurtEvent) procPoint).setAmount(((LivingHurtEvent) procPoint).getAmount() + stat.getA() / 10f);
            markUsed(caster);
        }
    }

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target) {
        final int shieldTime = CombatUtils.getShieldStats(caster.getHeldItemMainhand()).getA();
        if (CombatData.getCap(target).consumePosture(caster, shieldTime / 8f, 0, true) >= 0) {
            //failed to stagger
            CombatData.getCap(caster).setHandBind(CombatData.getCap(caster).isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND, shieldTime);
            if (caster instanceof PlayerEntity) {
                ((PlayerEntity) caster).getCooldownTracker().setCooldown(caster.getHeldItemMainhand().getItem(), shieldTime);
            }
        }
    }
}
