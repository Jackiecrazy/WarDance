package jackiecrazy.wardance.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.CombatTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface ClientAccessors {
    @Invoker
    void callStartUseItem();
}
