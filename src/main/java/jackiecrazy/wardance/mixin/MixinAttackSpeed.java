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
    DamageKnockbackEvent dke=null;

    @Inject(at = @At("RETURN"), method = "createLivingAttributes()Lnet/minecraft/entity/ai/attributes/AttributeModifierMap$MutableAttribute;")
    private static void registerAttributes(CallbackInfoReturnable<AttributeModifierMap.MutableAttribute> cb) {//TODO remove in 1.17
        if(!cb.getReturnValue().hasAttribute(Attributes.FOLLOW_RANGE))
            cb.getReturnValue().add(Attributes.FOLLOW_RANGE, 32);
        cb.getReturnValue().add(Attributes.ATTACK_SPEED, 4).add(Attributes.LUCK, 0).add(WarAttributes.STEALTH.get()).add(WarAttributes.DEFLECTION.get()).add(WarAttributes.ABSORPTION.get()).add(WarAttributes.SHATTER.get()).add(WarAttributes.MAX_SPIRIT.get()).add(WarAttributes.MAX_MIGHT.get()).add(WarAttributes.MAX_POSTURE.get()).add(WarAttributes.POSTURE_REGEN.get()).add(WarAttributes.SPIRIT_REGEN.get()).add(WarAttributes.MIGHT_GEN.get()).add(WarAttributes.BARRIER.get()).add(WarAttributes.BARRIER_COOLDOWN.get());
    }

    @Inject(method = "hurt",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 0, target = "Lnet/minecraft/entity/LivingEntity;knockback(FDD)V"))
    private void mark(DamageSource ds, float amnt, CallbackInfoReturnable<Boolean> cir) {
        tempDS = ds;
    }

    @Redirect(method = "hurt",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/entity/LivingEntity;knockback(FDD)V"))
    private void change(LivingEntity livingEntity, float strength, double ratioX, double ratioZ) {
        DamageKnockbackEvent mke = new DamageKnockbackEvent(livingEntity, tempDS, strength, ratioX, ratioZ);
        MinecraftForge.EVENT_BUS.post(mke);
        livingEntity.knockback(mke.getStrength(), mke.getRatioX(), mke.getRatioZ());
        tempDS = null;
    }
}