package jackiecrazy.wardance.mixin;

import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CombatTracker.class)
public interface InCombatAccessor {
    @Accessor
    boolean isInCombat();
}
