package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.event.FractureEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashSet;

public class Overbear extends ShieldBash {
    private final HashSet<String> tag = makeTag("physical", "melee", "boundCast", "normalAttack", "countdown", ProcPoints.on_hurt, ProcPoints.recharge_parry);

    @Override
    protected float performEffect(LivingEntity caster, LivingEntity target, float atk) {
        if (CombatData.getCap(target).getPosture() < atk * 3) {
            //successful stagger
        } else {
            if (caster instanceof Player p) {
                p.getCooldowns().addCooldown(caster.getMainHandItem().getItem(), 100);
            }
            CombatData.getCap(caster).setHandBind(InteractionHand.MAIN_HAND, 100);
        }
        return atk * 3;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (state == STATE.ACTIVE && procPoint instanceof FractureEvent e) {
            e.addAmount(1);
        }
        super.onProc(caster, procPoint, state, stats, target);
    }
}
