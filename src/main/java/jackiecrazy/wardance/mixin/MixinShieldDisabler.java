package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.SkillTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IForgeItemStack.class)
public class MixinShieldDisabler {
    @Inject(method = "canDisableShield", at = @At("HEAD"), cancellable = true)
    private void halt(ItemStack shield, LivingEntity entity, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (CasterData.getCap(attacker).isTagActive(SkillTags.disable_shield)) cir.setReturnValue(true);
    }
}
