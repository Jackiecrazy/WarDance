package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class MixinSuppressSwapSound extends LivingEntity {

    protected MixinSuppressSwapSound(EntityType<? extends LivingEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Redirect(method = "setItemSlot", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;playEquipSound(Lnet/minecraft/item/ItemStack;)V"))
    private void alert(Player instance, ItemStack itemStack) {
        if(!CombatUtils.suppress)
            this.playEquipSound(itemStack);
    }

}
