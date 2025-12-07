package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class MixinEmptyHands {
    @Shadow
    @Final
    public Player player;

    @Shadow
    @Final
    public NonNullList<ItemStack> items;

    @Shadow
    public static boolean isHotbarSlot(int p_36046_) {
        return false;
    }

    @Inject(method = "getFreeSlot", at = @At(value = "RETURN"), cancellable = true)
    private void notTheHotbar(CallbackInfoReturnable<Integer> cir) {
        //todo in the future picking up weapons shouldn't count here
        if (StylishData.getCap(player).isCombatMode() && isHotbarSlot(cir.getReturnValue()) && !CombatUtils.allowCombatHotbarPickup) {
            cir.setReturnValue(-1);
            for (int i = 9; i < items.size(); ++i) {
                if (items.get(i).isEmpty()) {
                    cir.setReturnValue(i);
                    break;
                }
            }
        }
    }
}
