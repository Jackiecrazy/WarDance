package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class MixinSweepDurabilitySuppressor {
    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    private void halt(int amount, LivingEntity entityIn, Consumer<LivingEntity> onBroken, CallbackInfo ci) {
        if (WeaponStats.isWeapon(entityIn, (ItemStack) (Object) this) && !GeneralConfig.sweepDurability && CombatData.getCap(entityIn).alreadyProc("durabilityConsumed"))
            ci.cancel();
        CombatData.getCap(entityIn).tickProc("durabilityConsumed");

    }
}
