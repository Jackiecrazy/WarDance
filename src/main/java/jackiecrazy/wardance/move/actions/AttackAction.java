package jackiecrazy.wardance.move.actions;

import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.argument.DamageArgument;
import jackiecrazy.footwork.move.argument.entity.CasterEntityArgument;
import jackiecrazy.footwork.move.argument.entity.TargetEntityArgument;
import jackiecrazy.footwork.move.argument.number.FixedNumberArgument;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.move.utils.ActionContext;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class AttackAction extends Action {

    private Argument<Entity> attacker = CasterEntityArgument.INSTANCE;
    private Argument<Entity> attack_target = TargetEntityArgument.INSTANCE;
    private Argument<Double> strength = FixedNumberArgument.ONE;
    private DamageArgument damage_source;
    private HitInfo info_override;

    private List<Action> on_hit = new ArrayList<>();
    private List<Action> on_kill = new ArrayList<>();

    @Override
    public int perform(ActionContext actionContext) {
        DamageSource baked = null;
        if (damage_source != null) baked = damage_source.resolve(actionContext);
        if (attacker == null || attack_target == null || strength == null) return 0;
        Double str = strength.resolve(actionContext);
        Entity a = attacker.resolve(actionContext), b = attack_target.resolve(actionContext);
        if (!(a instanceof LivingEntity le) || b == null || str == null) return 0;
        WeaponStats.info_override = info_override;
        b.invulnerableTime = 0;
        CombatUtils.setHandCooldown(le, InteractionHand.MAIN_HAND, str.floatValue(), false);
        GeneralUtils.attack(le, b, baked);
        int ret = runActions(actionContext, on_hit);
        if (!actionContext.target().isAlive()) {
            int damageRet = runActions(actionContext, on_kill);
            if (damageRet != 0) ret = damageRet;
        }
        WeaponStats.info_override = null;
        return ret;
    }
}
