package jackiecrazy.wardance.potion;

import jackiecrazy.wardance.utils.EffectUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;

import javax.annotation.Nullable;

public class TerrorEffect extends WarEffect{
    TerrorEffect() {
        super(EffectType.HARMFUL, 0xfcfc00);
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entityLivingBaseIn, int amplifier, double health) {
        if(source instanceof LivingEntity)
        EffectUtils.causeFear(entityLivingBaseIn, (LivingEntity) source, amplifier);
        else EffectUtils.causeFear(entityLivingBaseIn, entityLivingBaseIn.getKillCredit(), amplifier*20);
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity l, int amplifier) {
        l.removeEffect(this);
        EffectUtils.causeFear(l, l.getKillCredit(), amplifier*20);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
