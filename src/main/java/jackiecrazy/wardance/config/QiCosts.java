package jackiecrazy.wardance.config;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

public class QiCosts {
    public static final float KICK = 12f;

    public static float translateEnvironment(DamageSource ds) {
        if (ds.is(DamageTypeTags.IS_LIGHTNING)) return 50;
        if (ds.is(DamageTypeTags.DAMAGES_HELMET)) return 30;
        if (ds.is(DamageTypeTags.IS_DROWNING)) return 3;
        return 6;
    }
}
