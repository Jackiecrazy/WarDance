package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class LowSweep extends Kick {
    @Override
    public boolean onCast(LivingEntity caster) {
        CombatData.getCap(caster).setForcedSweep(60);
        return super.onCast(caster);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        super.onEffectEnd(caster, stats);
        CombatData.getCap(caster).setForcedSweep(-1);
    }

    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }
}
