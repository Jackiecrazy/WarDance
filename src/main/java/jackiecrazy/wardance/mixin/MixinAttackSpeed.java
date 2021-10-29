package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.api.WarAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinAttackSpeed {

    @Inject(at = @At("RETURN"), method = "registerAttributes()Lnet/minecraft/entity/ai/attributes/AttributeModifierMap$MutableAttribute;")
    private static void registerAttributes(CallbackInfoReturnable<AttributeModifierMap.MutableAttribute> cb) {//TODO remove in 1.17
        cb.getReturnValue().createMutableAttribute(Attributes.ATTACK_SPEED, 4).createMutableAttribute(Attributes.LUCK, 0).createMutableAttribute(WarAttributes.STEALTH.get()).createMutableAttribute(WarAttributes.DEFLECTION.get()).createMutableAttribute(WarAttributes.ABSORPTION.get()).createMutableAttribute(WarAttributes.SHATTER.get()).createMutableAttribute(WarAttributes.MAX_SPIRIT.get()).createMutableAttribute(WarAttributes.MAX_MIGHT.get()).createMutableAttribute(WarAttributes.MAX_POSTURE.get()).createMutableAttribute(WarAttributes.POSTURE_REGEN.get()).createMutableAttribute(WarAttributes.SPIRIT_REGEN.get()).createMutableAttribute(WarAttributes.MIGHT_GEN.get());
    }
}