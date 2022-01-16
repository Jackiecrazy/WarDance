package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class MixinSuppressSwapSound extends LivingEntity {

    protected MixinSuppressSwapSound(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Redirect(method = "setItemStackToSlot", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;playEquipSound(Lnet/minecraft/item/ItemStack;)V"))
    private void alert(PlayerEntity instance, ItemStack itemStack) {
        if(!CombatUtils.suppress)
            this.playEquipSound(itemStack);
    }

}
