package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;

public class Afterimage extends IronGuard {
    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, @Nullable LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent) {
            final float cost = ((ParryEvent) procPoint).getPostureConsumption();
            SkillUtils.createCloud(caster.world, caster, caster.getPosX(), caster.getPosY(), caster.getPosZ(), cost, ParticleTypes.LARGE_SMOKE);
            for (LivingEntity e : caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(cost))) {
                if (e.getDistanceSq(caster) > cost * cost) {
                    e.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 100));
                }
            }
            caster.addPotionEffect(new EffectInstance(Effects.INVISIBILITY, 20));
            markUsed(caster);
            ((ParryEvent) procPoint).setPostureConsumption(0);
        }

    }
}
