package jackiecrazy.wardance.api;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;

import javax.annotation.Nullable;

public class CombatDamageSource extends EntityDamageSource {
    private ItemStack damageDealer = ItemStack.EMPTY;
    private Hand attackingHand = Hand.MAIN_HAND;
    private Entity proxy = null;
    private Skill skillUsed = null;
    private boolean procNormalEffects = false;
    private boolean procAttackEffects = false;
    private boolean procSkillEffects = false;
    private boolean crit = false;
    private float cdmg = 1.5f;
    private float armor = 1f, knockback=1f;
    private TYPE damageTyping = TYPE.PHYSICAL;

    public static CombatDamageSource causeSelfDamage(LivingEntity to){
        return new CombatDamageSource("self", to);
    }

    public CombatDamageSource(String damageTypeIn, @Nullable Entity damageSourceEntityIn) {
        super(damageTypeIn, damageSourceEntityIn);
    }

    public float getCritDamage() {
        return cdmg;
    }

    public CombatDamageSource setCritDamage(float cdmg) {
        this.cdmg = cdmg;
        return this;
    }

    public TYPE getDamageTyping() {
        return damageTyping;
    }

    public CombatDamageSource setDamageTyping(TYPE damageTyping) {
        this.damageTyping = damageTyping;
        return this;
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

    @Nullable
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
        return procNormalEffects;
    }

    public CombatDamageSource setProcNormalEffects(boolean procNormalEffects) {
        this.procNormalEffects = procNormalEffects;
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

    public float getArmorReductionPercentage(){
        return armor;
    }

    public CombatDamageSource setArmorReductionPercentage(float armorReductionPercentage){
        armor=armorReductionPercentage;
        return this;
    }

    public float getKnockbackPercentage(){
        return knockback;
    }

    public CombatDamageSource setKnockbackPercentage(float perc){
        knockback=perc;
        return this;
    }

    public enum TYPE {
        PHYSICAL,//reduced by absorption, deflection, shatter, and armor
        MAGICAL,//reduced by resist
        TRUE//not reduced by anything
    }
}
