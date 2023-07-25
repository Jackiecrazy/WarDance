package jackiecrazy.wardance.utils;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {
    public static void playSweepParticle(LivingEntity e, Vec3 vec, int angle, double horDistScale, double vertFromFoot) {
        final float rotated = e.getYRot() + angle;
        double d0 = horDistScale * -Mth.sin(rotated * ((float) Math.PI / 180F));
        double d1 = horDistScale * Mth.cos(rotated * ((float) Math.PI / 180F));
        if (e.level instanceof ServerLevel) {
            ((ServerLevel) e.level).sendParticles(ParticleTypes.SWEEP_ATTACK, vec.x + d0, vec.y + vertFromFoot, vec.z + d1, 0, d0, 0.0D, d1+Math.PI, 0.0D);
        }

    }
    public static void playSweepParticle(LivingEntity e, int angle, double horDistScale, double vertFromFoot) {
        playSweepParticle(e, e.position(), angle, horDistScale, vertFromFoot);
    }
}
