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
    public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entityLivingBaseIn, int amplifier, double health) {
        if(source instanceof LivingEntity)
        EffectUtils.causeFear(entityLivingBaseIn, (LivingEntity) source, amplifier);
        else EffectUtils.causeFear(entityLivingBaseIn, entityLivingBaseIn.getAttackingEntity(), amplifier);
    }

    @Override
    public boolean isInstant() {
        return true;
    }
}
