package jackiecrazy.wardance.api;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;

import javax.annotation.Nullable;

public class CombatDamageSource extends EntityDamageSource {
    public TYPE getDamageTyping() {
        return damageTyping;
    }

    public CombatDamageSource setDamageTyping(TYPE damageTyping) {
        this.damageTyping = damageTyping;
        return this;
    }

    public enum TYPE {
        PHYSICAL,//reduced by absorption, deflection, shatter, and armor
        MAGICAL,//reduced by resist
        TRUE//not reduced by anything
    }

    private ItemStack damageDealer = ItemStack.EMPTY;
    private Hand attackingHand = Hand.MAIN_HAND;
    private Entity proxy = null;
    private Skill skillUsed = null;
    private boolean procAutoEffects = false;
    private boolean procAttackEffects = false;
    private boolean procSkillEffects = false;
    private boolean crit = false;
    private TYPE damageTyping = TYPE.PHYSICAL;

    public CombatDamageSource(String damageTypeIn, @Nullable Entity damageSourceEntityIn) {
        super(damageTypeIn, damageSourceEntityIn);
    }

    public boolean isCrit() {
        return crit;
    }

    public CombatDamageSource setCrit(boolean crit) {
        this.crit = crit;
        return this;
    }

    public ItemStack getDamageDealer() {
        return damageDealer;
    }

    public CombatDamageSource setDamageDealer(ItemStack damageDealer) {
        this.damageDealer = damageDealer;
        return this;
    }

    public Hand getAttackingHand() {
        return attackingHand;
    }

    public CombatDamageSource setAttackingHand(Hand attackingHand) {
        this.attackingHand = attackingHand;
        return this;
    }

    public Entity getProxy() {
        return proxy;
    }

    public CombatDamageSource setProxy(Entity proxy) {
        this.proxy = proxy;
        return this;
    }

    public Skill getSkillUsed() {
        return skillUsed;
    }

    public CombatDamageSource setSkillUsed(Skill skillUsed) {
        this.skillUsed = skillUsed;
        return this;
    }

    public boolean canProcAutoEffects() {
        return procAutoEffects;
    }

    public CombatDamageSource setProcAutoEffects(boolean procAutoEffects) {
        this.procAutoEffects = procAutoEffects;
        return this;
    }

    public boolean canProcAttackEffects() {
        return procAttackEffects;
    }

    public CombatDamageSource setProcAttackEffects(boolean procAttackEffects) {
        this.procAttackEffects = procAttackEffects;
        return this;
    }

    public boolean canProcSkillEffects() {
        return procSkillEffects;
    }

    public CombatDamageSource setProcSkillEffects(boolean procSkillEffects) {
        this.procSkillEffects = procSkillEffects;
        return this;
    }
}
