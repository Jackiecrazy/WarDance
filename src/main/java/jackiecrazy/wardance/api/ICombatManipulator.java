package jackiecrazy.wardance.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.eventbus.api.Event;

public interface ICombatManipulator {
    /**
     * dictates the angle of the sweep area. Vanilla is 1 block flat range, PWD default is (+40 for every level of sweeping edge)
     */
    int sweepArea(LivingEntity attacker, ItemStack item);

    /**
     * called on LivingAttackEvent to determine whether the hit is valid
     */
    boolean canAttack(DamageSource ds, LivingEntity attacker, LivingEntity target, ItemStack item, float orig);

    /**
     * this is called on LivingAttackEvent, before parries. It returns void because LAE doesn't support modifying amounts
     */
    void attackStart(DamageSource ds, LivingEntity attacker, LivingEntity target, ItemStack item, float orig);

    /**
     * called on CriticalHitEvent to determine whether the hit is critical
     */
    Event.Result critCheck(LivingEntity attacker, LivingEntity target, ItemStack item, float crit, boolean vanCrit);

    /**
     * this is called on CriticalHitEvent to determine crit multiplier
     */
    float critDamage(LivingEntity attacker, LivingEntity target, ItemStack item);

    /**
     * this is called on LivingHurt to determine damage multiplier
     */
    float damageMultiplier(LivingEntity attacker, LivingEntity target, ItemStack item);

    /**
     * this is called on LivingKnockBackEvent
     * @return a new knockback if necessary
     */
    float onKnockingBack(LivingEntity attacker, LivingEntity target, ItemStack item, float orig);

    /**
     * this is called on LivingKnockBackEvent
     * @return a new knockback if necessary
     */
    float onBeingKnockedBack(LivingEntity attacker, LivingEntity target, ItemStack item, float orig);

    /**
     * this is called on LivingHurtEvent, before armor reductions
     * @return a new damage if necessary
     */
    float hurtStart(DamageSource ds, LivingEntity attacker, LivingEntity target, ItemStack item, float orig);

    /**
     * this is called on LivingDamageEvent, after armor, absorption, and all other reductions
     * @return a new damage if necessary
     */
    float damageStart(DamageSource ds, LivingEntity attacker, LivingEntity target, ItemStack item, float orig);

    /**
     * this is called on LivingHurtEvent to apply armor down for the particular attack only
     * @return how much armor to ignore
     */
    int armorIgnoreAmount(DamageSource ds, LivingEntity attacker, LivingEntity target, ItemStack item, float orig);

    /**
     * this is called on LivingHurtEvent, after hurtStart, but before downed damage multipliers have been applied
     * @return a new damage if necessary
     */
    float onBeingHurt(DamageSource ds, LivingEntity defender, ItemStack item, float amount);

    /**
     * this is called on LivingDamageEvent, after damageStart
     * @return a new damage if necessary
     */
    float onBeingDamaged(DamageSource ds, LivingEntity defender, ItemStack item, float amount);

    /**
     * this is called during LivingAttackEvent to determine whether this item can parry
     */
    boolean canBlock(LivingEntity defender, Entity attacker, ItemStack item, boolean recharged, float amount);

    /**
     * use this to apply special effects on parrying
     */
    void onParry(LivingEntity attacker, LivingEntity defender, ItemStack item, float amount);

    /**
     * use this to apply special effects on the other hand parrying. It's kind of obscure, but it exists
     */
    void onOtherHandParry(LivingEntity attacker, LivingEntity defender, ItemStack item, float amount);

    float postureMultiplierDefend(Entity attacker, LivingEntity defender, ItemStack item, float amount);

    /**
     * this is a flat value rather than a multiplier with damage. The reasoning is that a sharp sword doesn't actually confer more force.
     * When empty-handed you can get hard-hitting unarmed fighters with damage multipliers, but the damage enchants on weapons are too much.
     */
    float postureDealtBase(LivingEntity attacker, LivingEntity defender, ItemStack item, float amount);
}
