package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class ShieldBash extends Skill {
    private final HashSet<String> tag = makeTag("shield", SkillTags.offensive, SkillTags.physical);
    private final HashSet<String> no = makeTag("normalAttack");

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.shield_bash;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    public HashSet<String> getTags() {
        return tag;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent lae && lae.getEntity() == target && DamageUtils.isMeleeAttack(lae.getSource()) && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (lae.getSource() instanceof CombatDamageSource cds) {
                cds.setProcSkillEffects(true);
                cds.setSkillUsed(this);
            }
            final boolean base = isPassive(caster) && state != STATE.COOLING;
            final ItemStack stack = CombatUtils.getAttackingItemStack(lae.getSource());
            if (!WeaponStats.isShield(caster, stack)) return;
            if (stats.getState() == STATE.COOLING && !CasterData.getCap(caster).isTagActive("shield"))
                stats.decrementDuration();
            final boolean otherwise = state == STATE.HOLSTERED;
            if ((base || otherwise) && cast(caster, target, -999)) {
                float attack = CombatUtils.getPostureAtk(caster, target, InteractionHand.MAIN_HAND, 0, stack) * stats.getEffectiveness();
                performEffect(caster, target, attack);
                caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                markUsed(caster);
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)//no need for cooldown because it basically cools down with shield anyway
            setCooldown(caster, prev, 7);
        return boundCast(prev, from, to);
    }

    protected void performEffect(LivingEntity caster, LivingEntity target, float attack) {
        final ICombatCapability cap = CombatData.getCap(caster);
        //SkillUtils.auxAttack(caster, target, new CombatDamageSource("player", caster).setProcNormalEffects(false).setProcAttackEffects(true).setProcSkillEffects(true).setAttackingHand(InteractionHand.OFF_HAND).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setDamageDealer(caster.getMainHandItem()), 0, attack);
    }

    public static class RimPunch extends ShieldBash {

        protected void performEffect(LivingEntity caster, LivingEntity target, float atk) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60));
            CombatUtils.knockBack(target, caster, (float) atk, true, false);
        }
    }

    public static class FootSlam extends ShieldBash {

        protected void performEffect(LivingEntity caster, LivingEntity target, float atk) {
            super.performEffect(caster, target, atk);
            final int time = (int) (atk * 20);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, time * 2));
            target.addEffect(new MobEffectInstance(FootworkEffects.CONFUSION.get(), time * 2));
        }
    }
}
