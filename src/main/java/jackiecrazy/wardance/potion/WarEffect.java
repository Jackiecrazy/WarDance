package jackiecrazy.wardance.potion;

import jackiecrazy.wardance.capability.resources.CombatData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

class WarEffect extends Effect {
    //this is really pointless, but just in case(tm)
    WarEffect(EffectType typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);
    }

    @Override
    public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
        if (this == WarEffects.RESTORATION.get()) {
            CombatData.getCap(entityLivingBaseIn).addWounding(-amplifier);
        }
        if (this == WarEffects.REENERGIZATION.get()) {
            CombatData.getCap(entityLivingBaseIn).addBurnout(-amplifier);
        }
        if (this == WarEffects.REFRESHMENT.get()) {
            CombatData.getCap(entityLivingBaseIn).addFatigue(-amplifier);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 20 == 1;
    }
}
