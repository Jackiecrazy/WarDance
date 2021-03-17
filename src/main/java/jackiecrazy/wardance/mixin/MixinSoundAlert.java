package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class MixinSoundAlert {
    @Inject(method = "playSound", at = @At("TAIL"))
    private void alert(PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, CallbackInfo ci) {
        if (player != null && player.isSneaking()) return;
        if (category == SoundCategory.PLAYERS || category == SoundCategory.NEUTRAL)
            for (CreatureEntity c : ((ServerWorld) (Object) this).getEntitiesWithinAABB(CreatureEntity.class, new AxisAlignedBB(x, y, z, x, y, z).grow(volume * CombatConfig.blockPerVolume))) {
                if (CombatUtils.getAwareness(null, c) == CombatUtils.AWARENESS.UNAWARE)
                    c.getNavigator().tryMoveToXYZ(x, y, z, c.getAIMoveSpeed());
            }
    }

    @Inject(method = "playMovingSound", at = @At("TAIL"))
    private void alertMoving(PlayerEntity player, Entity entityIn, SoundEvent eventIn, SoundCategory category, float volume, float pitch, CallbackInfo ci) {
        if (player != null && player.isSneaking()) return;
        double x = entityIn.getPosX(), y = entityIn.getPosY(), z = entityIn.getPosZ();
        if (category == SoundCategory.PLAYERS || category == SoundCategory.NEUTRAL)
            for (CreatureEntity c : ((ServerWorld) (Object) this).getEntitiesWithinAABB(CreatureEntity.class, new AxisAlignedBB(x, y, z, x, y, z).grow(volume * CombatConfig.blockPerVolume))) {
                if (CombatUtils.getAwareness(null, c) == CombatUtils.AWARENESS.UNAWARE)
                    c.getNavigator().tryMoveToEntityLiving(entityIn, c.getAIMoveSpeed());
            }
    }

}
