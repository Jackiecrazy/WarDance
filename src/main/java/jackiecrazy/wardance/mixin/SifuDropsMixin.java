package jackiecrazy.wardance.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface SifuDropsMixin {
    @Invoker
    public void callDropAllDeathLoot(DamageSource p_21192_);
}
