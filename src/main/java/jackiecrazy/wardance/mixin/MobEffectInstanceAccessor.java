package jackiecrazy.wardance.mixin;

import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.effect.MobEffectInstance.class)
public interface MobEffectInstanceAccessor {
    @Accessor
    void setDuration(int duration);
}
