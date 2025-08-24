package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.config.GeneralConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.units.qual.A;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends LivingEntity {


    protected MixinPlayerEntity(EntityType<? extends LivingEntity> p_i48577_1_, Level p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
    }

    @Redirect(method = "attack",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;walkDist:F", opcode = Opcodes.GETFIELD))
    private float noSweep(Player player) {
        if (GeneralConfig.betterSweep)
            return Float.MAX_VALUE;
        return walkDist;
    }
}
