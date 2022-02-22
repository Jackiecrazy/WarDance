package jackiecrazy.wardance.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetingUtils {

    public static boolean isAlly(Entity entity, Entity of) {
        if (entity == null || of == null) return false;
        if (of == entity) return true;
        if (entity instanceof TameableEntity && of instanceof LivingEntity && ((TameableEntity) entity).isOwner((LivingEntity) of))
            return true;
        if (of instanceof TameableEntity && entity instanceof LivingEntity && ((TameableEntity) of).isOwner((LivingEntity) entity))
            return true;
        if (entity.isOnSameTeam(of)) return true;
        if (entity instanceof PlayerEntity && of instanceof PlayerEntity && entity.getServer() != null && entity.getServer().isPVPEnabled())
            return true;
        return false;
    }

    public static boolean isHostile(Entity entity, Entity to) {
        if (entity == null || to == null) return false;
        if (isAlly(entity, to)) return false;
        if (entity instanceof LivingEntity) {
            if (((LivingEntity) entity).getRevengeTarget() != null) {
                LivingEntity revenge = ((LivingEntity) entity).getRevengeTarget();
                if (isAlly(revenge, to)) return true;
            }
            if (entity instanceof MobEntity && ((MobEntity) entity).getAttackTarget() != null) {
                LivingEntity attack = ((MobEntity) entity).getAttackTarget();
                return isAlly(attack, to);
            }
        }
        return false;
    }
}
