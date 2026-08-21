package jackiecrazy.wardance.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessors {
    @Invoker
    void callBlockUsingShield(LivingEntity p_21200_);

    @Accessor
    void setUseItemRemaining(int useItemRemaining);

    @Invoker
    void callDropFromLootTable(DamageSource p_21021_, boolean p_21022_);

    @Invoker
    void callDropCustomDeathLoot(DamageSource p_21018_, int p_21019_, boolean p_21020_);

    @Invoker
    void callDropExperience();

    @Invoker
    void callDropEquipment();

    @Invoker
    void callDropAllDeathLoot(DamageSource p_21192_);
}
