package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;

public class Recovery extends IronGuard {
    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, @Nullable LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getPostureConsumption() > 0) {
            CombatData.getCap(caster).addPosture(((ParryEvent) procPoint).getPostureConsumption());
            markUsed(caster);
        }
        ICombatCapability icc = CombatData.getCap(caster);
        icc.setSpiritGrace(0);
    }
}
