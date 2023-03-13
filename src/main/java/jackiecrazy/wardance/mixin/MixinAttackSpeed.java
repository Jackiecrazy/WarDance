package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.api.WarAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinAttackSpeed {

    @Inject(at = @At("RETURN"), method = "createLivingAttributes()Lnet/minecraft/entity/ai/attributes/AttributeModifierMap$MutableAttribute;")
    private static void registerAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cb) {//TODO remove in 1.17
        if(!cb.getReturnValue().hasAttribute(Attributes.FOLLOW_RANGE))
            cb.getReturnValue().add(Attributes.FOLLOW_RANGE, 32);
        cb.getReturnValue().add(Attributes.ATTACK_SPEED, 4).add(Attributes.LUCK, 0).add(WarAttributes.STEALTH.get()).add(WarAttributes.DEFLECTION.get()).add(WarAttributes.ABSORPTION.get()).add(WarAttributes.SHATTER.get()).add(WarAttributes.MAX_SPIRIT.get()).add(WarAttributes.MAX_MIGHT.get()).add(WarAttributes.MAX_POSTURE.get()).add(WarAttributes.POSTURE_REGEN.get()).add(WarAttributes.SPIRIT_REGEN.get()).add(WarAttributes.MIGHT_GEN.get()).add(WarAttributes.BARRIER.get()).add(WarAttributes.BARRIER_COOLDOWN.get());
    }
}