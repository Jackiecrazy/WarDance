package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.skill.ProcPoints;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = IForgeItemStack.class, remap = false)
public interface MixinShieldDisabler {

    @Shadow
    ItemStack self();

    /**
     * @reason Mixins don't allow injecting interfaces, so here we are
     *
     * @author Jackiecrazy
     */
    @Overwrite
    default boolean canDisableShield(ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        if (CasterData.getCap(attacker).isTagActive(ProcPoints.disable_shield)) return true;
        if(self().is(WeaponStats.AXE_LIKE)) return true;
        return self().getItem().canDisableShield(self(), shield, entity, attacker);
    }
}
