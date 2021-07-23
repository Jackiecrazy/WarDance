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
    private static void registerAttributes(CallbackInfoReturnable<AttributeModifierMap.MutableAttribute> cb) {
        cb.getReturnValue().createMutableAttribute(Attributes.ATTACK_SPEED, 4).createMutableAttribute(Attributes.LUCK, 0).createMutableAttribute(WarAttributes.STEALTH.get()).createMutableAttribute(WarAttributes.DEFLECTION.get()).createMutableAttribute(WarAttributes.ABSORPTION.get()).createMutableAttribute(WarAttributes.SHATTER.get());
    }
}