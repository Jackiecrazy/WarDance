package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinSuppressSwapSound extends Entity {

    public MixinSuppressSwapSound(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Redirect(method = "onEquipItem", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isSilent()Z"))
    private boolean alert(LivingEntity instance) {
        if(CombatUtils.suppress)
            return true;
        return isSilent();
    }

}
