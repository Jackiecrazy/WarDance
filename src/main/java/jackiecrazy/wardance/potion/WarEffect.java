package jackiecrazy.wardance.potion;

import jackiecrazy.wardance.capability.resources.CombatData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.world.server.ServerWorld;

class WarEffect extends Effect {
    WarEffect(EffectType typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);
    }

    @Override
    public void performEffect(LivingEntity l, int amplifier) {
        if (this == WarEffects.RESTORATION.get()) {
            CombatData.getCap(l).addWounding(-amplifier);
        }
        if (this == WarEffects.REENERGIZATION.get()) {
            CombatData.getCap(l).addBurnout(-amplifier);
        }
        if (this == WarEffects.REFRESHMENT.get()) {
            CombatData.getCap(l).addFatigue(-amplifier);
        }
        if (this == WarEffects.FEAR.get() && l.world instanceof ServerWorld) {
            ((ServerWorld) l.world).spawnParticle(ParticleTypes.DRIPPING_WATER, l.getPosX(), l.getPosY() + l.getHeight() / 2, l.getPosZ(), 5, l.getWidth() / 4, l.getHeight() / 4, l.getWidth() / 4, 0.5f);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 20 == 1;
    }
}
