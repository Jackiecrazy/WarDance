package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;

public class Overpower extends IronGuard {
    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, @Nullable LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent) {
            if (!CasterData.getCap(((ParryEvent) procPoint).getAttacker()).isCategoryActive(SkillCategories.heavy_blow)) {
                CombatData.getCap(((ParryEvent) procPoint).getAttacker()).consumePosture(caster, ((ParryEvent) procPoint).getPostureConsumption());
                CombatData.getCap(((ParryEvent) procPoint).getAttacker()).consumePosture(caster, CombatUtils.getPostureAtk(caster, target, ((ParryEvent) procPoint).getDefendingHand(), ((ParryEvent) procPoint).getAttackDamage(), ((ParryEvent) procPoint).getDefendingStack()));
                ((ParryEvent) procPoint).setPostureConsumption(0);
            }
            markUsed(caster);
        }
    }
}
