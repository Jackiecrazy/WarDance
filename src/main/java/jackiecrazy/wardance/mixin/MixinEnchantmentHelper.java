package jackiecrazy.wardance.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {
    @Redirect(method = "applyThornEnchantments(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "applyEnchantmentModifier"))
    private static void checkOffhand(){
        //EnchantmentHelper.applyEnchantmentModifier(v, user.getHeldItemMainhand());
    }

}
