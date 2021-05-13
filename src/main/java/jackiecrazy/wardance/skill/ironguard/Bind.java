package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;

public class Bind extends IronGuard {
    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, @Nullable LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if(procPoint instanceof ParryEvent){
            if (!CasterData.getCap(((ParryEvent) procPoint).getAttacker()).isSkillActive(WarSkills.HEAVY_BLOW.get()))
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getAttackingHand(), 40);
            markUsed(caster);
        }
    }
}
