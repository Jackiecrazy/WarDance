package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    public MixinLivingEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean injected(DamageSource instance, TagKey<DamageType> type) {
        if (type == DamageTypeTags.NO_IMPACT && instance.getEntity() == null) {
            return false;
        }
        return instance.is(type);
    }

    @Shadow
    public abstract void indicateDamage(double p_270514_, double p_270826_);

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
        //my block is omnidirectional
        LivingEntity le = (LivingEntity) (Object) this;
        if (!cir.getReturnValue() && !ds.is(DamageTypeTags.BYPASSES_SHIELD) && CombatData.getCap(le).isBlocking()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isBlocking", at = @At(value = "RETURN"), cancellable = true)
    private void actuallyBlocking(CallbackInfoReturnable<Boolean> cir) {
        //mixin for compat
        LivingEntity le = (LivingEntity) (Object) this;
        if (!cir.getReturnValue() && CombatData.getCap(le).isBlocking()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hurt", at = @At(value = "RETURN"), cancellable = false)
    private void combatModeOverride(DamageSource ds, float amnt, CallbackInfoReturnable<Boolean> cir) {
        //cancel damage shake by inputting invalid values and relying on moar mixins
        if (ds.getEntity() == null && !ds.is(DamageTypeTags.IS_EXPLOSION) && !ds.is(DamageTypeTags.IS_FALL) && cir.getReturnValue()) {
            indicateDamage(-99999, -99999);
        }
    }

}
