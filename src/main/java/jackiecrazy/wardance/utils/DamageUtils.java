package jackiecrazy.wardance.utils;

import jackiecrazy.footwork.api.CombatDamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;

public class DamageUtils {
    public static boolean isMeleeAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            return ((CombatDamageSource) s).canProcAutoEffects();
        }
        return s.getEntity() != null && s.getEntity() == s.getDirectEntity() && !s.is(DamageTypeTags.IS_EXPLOSION) && !s.is(DamageTypeTags.IS_PROJECTILE);//!s.isFire() && !s.isMagic() &&
    }

    public static boolean isSkillAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            return ((CombatDamageSource) s).getSkillUsed() != null;
        }
        return false;
    }

    public static boolean isPhysicalAttack(DamageSource s) {
        if (s instanceof CombatDamageSource cds) {
            return cds.getDamageTyping() == CombatDamageSource.TYPE.PHYSICAL;
        }
        return !s.is(DamageTypeTags.IS_EXPLOSION) && !s.is(DamageTypeTags.IS_FIRE) && !s.is(DamageTypeTags.WITCH_RESISTANT_TO) && !s.is(DamageTypeTags.BYPASSES_ARMOR);
    }

    public static boolean isTrueDamage(DamageSource s) {
        if (s instanceof CombatDamageSource cds) {
            return cds.getDamageTyping() == CombatDamageSource.TYPE.TRUE;
        }
        return !s.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || (!s.is(DamageTypeTags.BYPASSES_EFFECTS) && !s.is(DamageTypeTags.BYPASSES_ARMOR));
    }

    public static boolean isCrit(CriticalHitEvent e) {
        return e.getResult() == Event.Result.ALLOW || (e.getResult() == Event.Result.DEFAULT && e.isVanillaCritical());
    }
}
