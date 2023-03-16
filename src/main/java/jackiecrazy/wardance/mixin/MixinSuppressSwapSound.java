package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinSuppressSwapSound extends Entity {

    public MixinSuppressSwapSound(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }
    @Shadow
    protected abstract void playEquipSound(ItemStack p_217042_);

    @Redirect(method = "onEquipItem", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;playEquipSound(Lnet/minecraft/world/item/ItemStack;)V"))
    private void alert(LivingEntity instance, ItemStack itemStack) {
        if(!CombatUtils.suppress)
            (this).playEquipSound(itemStack);
    }

}
