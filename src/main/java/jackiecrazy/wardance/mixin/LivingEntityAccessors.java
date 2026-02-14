package jackiecrazy.wardance.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessors {
    @Invoker
    void callBlockUsingShield(LivingEntity p_21200_);

    @Accessor
    void setUseItemRemaining(int useItemRemaining);
}
