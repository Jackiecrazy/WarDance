package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;

public class ReturnToSender extends IronGuard {
    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof final ProjectileParryEvent ppe && procPoint.getPhase() == EventPriority.HIGHEST && ppe.getReturnVec() != null && ppe.getEntity() == caster&& cast(caster)) {
            ppe.setReturnVec(ppe.getProjectile().getDeltaMovement().reverse());
            ppe.setPostureConsumption(ppe.getPostureConsumption() * 2f / SkillUtils.getSkillEffectiveness(caster));
            markUsed(caster);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.INACTIVE);
        return false;
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return false;
    }

    @Override
    protected void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target, STATE state) {

    }
}
