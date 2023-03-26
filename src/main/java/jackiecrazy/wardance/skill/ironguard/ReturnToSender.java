package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;

public class ReturnToSender extends IronGuard {
    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof ProjectileParryEvent && procPoint.getPhase() == EventPriority.HIGHEST && ((ProjectileParryEvent) procPoint).getReturnVec() != null && ((ProjectileParryEvent) procPoint).getEntity() == caster&& cast(caster)) {
            ((ProjectileParryEvent) procPoint).setReturnVec(((ProjectileParryEvent) procPoint).getProjectile().getDeltaMovement().reverse());
            ((ProjectileParryEvent) procPoint).setPostureConsumption(((ProjectileParryEvent) procPoint).getPostureConsumption() * 1.5f);
            markUsed(caster);
        }
    }

    @Override
    protected void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target, STATE state) {

    }
}
