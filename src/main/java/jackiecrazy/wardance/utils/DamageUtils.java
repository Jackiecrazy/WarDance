package jackiecrazy.wardance.utils;

import jackiecrazy.footwork.api.CombatDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

public class DamageUtils {
    public static boolean isMeleeAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            return ((CombatDamageSource) s).canProcAutoEffects();
        }
        return s.getEntity() != null && s.getEntity() == s.getDirectEntity() && !s.isExplosion() && !s.isProjectile();//!s.isFire() && !s.isMagic() &&
    }
    public static boolean isSkillAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            return ((CombatDamageSource) s).getSkillUsed()!=null;
        }
        return false;
    }

    public static boolean isPhysicalAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            CombatDamageSource cds = (CombatDamageSource) s;
            return cds.getDamageTyping() == CombatDamageSource.TYPE.PHYSICAL;
        }
        return !s.isExplosion() && !s.isFire() && !s.isMagic() && !s.isBypassArmor();
    }

    public static boolean isTrueDamage(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            CombatDamageSource cds = (CombatDamageSource) s;
            return cds.getDamageTyping() == CombatDamageSource.TYPE.TRUE;
        }
        return s.isBypassInvul() || (s.isBypassMagic() && s.isBypassArmor());
    }

    public static boolean isCrit(CriticalHitEvent e) {
        return e.getResult() == Event.Result.ALLOW || (e.getResult() == Event.Result.DEFAULT && e.isVanillaCritical());
    }
}
