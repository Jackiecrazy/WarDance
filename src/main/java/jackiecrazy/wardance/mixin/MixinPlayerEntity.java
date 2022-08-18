package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.config.GeneralConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {


    protected MixinPlayerEntity(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
    }

    @Redirect(method = "attack",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;walkDist:F", opcode = Opcodes.GETFIELD))
    private float noSweep(PlayerEntity player) {
        if (GeneralConfig.betterSweep)
            return Float.MAX_VALUE;
        return walkDist;
    }
}
