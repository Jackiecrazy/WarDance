package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

public class Backpedal extends IronGuard {
    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, @Nullable LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent) {
            ((ParryEvent) procPoint).setPostureConsumption(0);
            caster.setMotion(caster.getMotion().add(caster.getPositionVec().subtractReverse(target.getPositionVec()).scale(0.1 * ((ParryEvent) procPoint).getPostureConsumption())));
            caster.velocityChanged = true;
        }
        if (procPoint instanceof ProjectileParryEvent) {
            ((ProjectileParryEvent) procPoint).setPostureConsumption(0);
            caster.setMotion(caster.getMotion().add(caster.getPositionVec().subtractReverse(target.getPositionVec()).scale(0.1 * ((ParryEvent) procPoint).getPostureConsumption())));
            caster.velocityChanged = true;
        }
    }
}
