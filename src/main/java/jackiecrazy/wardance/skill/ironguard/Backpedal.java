package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;

import java.awt.*;

public class Backpedal extends IronGuard {
    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    protected void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (!caster.isOnGround() && state != STATE.COOLING) {
            float str = -Math.min(procPoint.getPostureConsumption() / 2, 2);
            caster.setMotion(caster.getMotion().add(caster.getPositionVec().subtractReverse(target.getPositionVec()).normalize().scale(str)));
            caster.velocityChanged = true;
            procPoint.setPostureConsumption(0);
            markUsed(caster);
        }
    }
}
