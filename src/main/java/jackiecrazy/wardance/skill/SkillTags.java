package jackiecrazy.wardance.skill;

public class SkillTags {
    public static final String normal_attack = "normalAttack";
    public static final String on_hurt = "beforeArmor";
    public static final String on_damage = "afterArmor";
    public static final String on_being_hurt = "onHurt";
    public static final String on_being_damaged = "onDamage";
    public static final String change_parry_result = "manipulateParry";
    public static final String change_might = "manipulateMight";
    public static final String change_spirit = "manipulateSpirit";
    public static final String on_being_parried = "onBeingParried";
    public static final String on_projectile_impact = "onProjectileImpact";
    public static final String on_parry = "onParry";
    public static final String on_projectile_parry = "onProjectileParry";
    public static final String modify_crit = "modifyCrit";
    public static final String melee = "melee";
    public static final String recharge_normal = "rechargeWithAttack";
    public static final String recharge_parry = "rechargeWithParry";
    public static final String recharge_time = "rechargeWithTime";
    public static final String recharge_cast = "rechargeWithCast";
    /**
     * skills tagged with this will automatically refresh on sleep. It's still possible to add an independent cooldown.
     */
    public static final String recharge_sleep = "rechargeWithSleep";
    public static final String countdown = "countdown";
    public static final String knockback = "knockback";
    public static final String state = "state";

}
