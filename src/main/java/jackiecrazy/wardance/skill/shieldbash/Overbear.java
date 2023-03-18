package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.skill.ProcPoints;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashSet;

public class Overbear extends ShieldBash {
    private final HashSet<String> tag = makeTag("physical", "melee", "boundCast", "normalAttack", "countdown", ProcPoints.on_hurt, ProcPoints.recharge_parry);

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target) {
//        final float barrier = CombatData.getCap(caster).consumeBarrier(Float.MAX_VALUE)/2;
//        if (CombatData.getCap(target).consumePosture(caster, barrier, 0, true) < 0) {
//            //successful stagger, refund cooldown
//            CombatData.getCap(caster).setBarrierCooldown(CombatData.getCap(caster).getBarrierCooldown() / 2);
//        }else CombatData.getCap(caster).setBarrierCooldown((int) (CombatData.getCap(caster).getBarrierCooldown() * 1.5));
    }
}
