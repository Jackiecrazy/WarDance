package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.Entity;
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
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable Entity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if(procPoint instanceof ParryEvent){
            if (!CasterData.getCap(((ParryEvent) procPoint).getAttacker()).isCategoryActive(SkillCategories.heavy_blow))
            CombatData.getCap(target).setHandBind(((ParryEvent) procPoint).getAttackingHand(), 40);
            markUsed(caster);
        }
    }
}
