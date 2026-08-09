package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class Trample extends Kick {
    @Override
    public void onProc(LivingEntity caster,
                       Event procPoint,
                       STATE state,
                       SkillData stats,
                       @Nullable LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    public boolean fakeMark(LivingEntity caster, LivingEntity target, SkillData stats) {
        return CombatData.getCap(target).getPosture() < 16;
    }

    @Override
    protected void additionally(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (CombatData.getCap(target).getPosture() < 4) {
            if (CombatData.getCap(target).getMaxPosture() < 16)
                completeChallenge(caster);
            CombatData.getCap(target).consumePosture(caster, 6, ICombatCapability.BreachLevel.STUN);
        }

    }
}
