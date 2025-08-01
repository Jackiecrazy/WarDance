package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    public MixinLivingEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Redirect(method = "onEquipItem", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isSilent()Z"))
    private boolean alert(LivingEntity instance) {
        if (CombatUtils.suppressChangeFunctions)
            return true;
        return isSilent();
    }

    @Inject(method = "isDamageSourceBlocked", at = @At(value = "RETURN"), cancellable = true)
    private void secretlyBlocking(DamageSource ds, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity le = (LivingEntity) (Object) this;
        if (!cir.getReturnValue() && CombatData.getCap(le).isBlocking()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isBlocking", at = @At(value = "RETURN"), cancellable = true)
    private void actuallyBlocking(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity le = (LivingEntity) (Object) this;
        if (!cir.getReturnValue() && CombatData.getCap(le).isBlocking()) {
            cir.setReturnValue(true);
        }
    }

}
