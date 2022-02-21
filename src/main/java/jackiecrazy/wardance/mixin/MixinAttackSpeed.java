package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.event.DamageKnockbackEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinAttackSpeed {

    DamageSource tempDS = null;

    @Inject(at = @At("RETURN"), method = "registerAttributes()Lnet/minecraft/entity/ai/attributes/AttributeModifierMap$MutableAttribute;")
    private static void registerAttributes(CallbackInfoReturnable<AttributeModifierMap.MutableAttribute> cb) {//TODO remove in 1.17
        if(!cb.getReturnValue().hasAttribute(Attributes.FOLLOW_RANGE))
            cb.getReturnValue().createMutableAttribute(Attributes.FOLLOW_RANGE, 32);
        cb.getReturnValue().createMutableAttribute(Attributes.ATTACK_SPEED, 4).createMutableAttribute(Attributes.LUCK, 0).createMutableAttribute(WarAttributes.STEALTH.get()).createMutableAttribute(WarAttributes.DEFLECTION.get()).createMutableAttribute(WarAttributes.ABSORPTION.get()).createMutableAttribute(WarAttributes.SHATTER.get()).createMutableAttribute(WarAttributes.MAX_SPIRIT.get()).createMutableAttribute(WarAttributes.MAX_MIGHT.get()).createMutableAttribute(WarAttributes.MAX_POSTURE.get()).createMutableAttribute(WarAttributes.POSTURE_REGEN.get()).createMutableAttribute(WarAttributes.SPIRIT_REGEN.get()).createMutableAttribute(WarAttributes.MIGHT_GEN.get());
    }

    @Inject(method = "attackEntityFrom",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 0, target = "Lnet/minecraft/entity/LivingEntity;applyKnockback(FDD)V"))
    private void mark(DamageSource ds, float amnt, CallbackInfoReturnable<Boolean> cir) {
        tempDS = ds;
    }

    @Redirect(method = "attackEntityFrom",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/entity/LivingEntity;applyKnockback(FDD)V"))
    private void change(LivingEntity livingEntity, float strength, double ratioX, double ratioZ) {
        DamageKnockbackEvent mke = new DamageKnockbackEvent(livingEntity, tempDS, strength, ratioX, ratioZ);
        MinecraftForge.EVENT_BUS.post(mke);
        livingEntity.applyKnockback(mke.getStrength(), mke.getRatioX(), mke.getRatioZ());
        tempDS = null;
    }
}