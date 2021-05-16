package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
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
            if (!CasterData.getCap(((ParryEvent) procPoint).getAttacker()).isSkillActive(WarSkills.HEAVY_BLOW.get())) {
                ((ParryEvent) procPoint).setPostureConsumption(0);
                CombatData.getCap(((ParryEvent) procPoint).getAttacker()).consumePosture(((ParryEvent) procPoint).getPostureConsumption());
                CombatData.getCap(((ParryEvent) procPoint).getAttacker()).consumePosture(CombatUtils.getPostureAtk(caster, target, ((ParryEvent) procPoint).getDefendingHand(), ((ParryEvent) procPoint).getPostureConsumption(), ((ParryEvent) procPoint).getDefendingStack()));
            }
            markUsed(caster);
        }
        if (procPoint instanceof ProjectileParryEvent) {
            float extra = CombatUtils.getPostureAtk(caster, target, ((ProjectileParryEvent) procPoint).getDefendingHand(), ((ProjectileParryEvent) procPoint).getPostureConsumption(), ((ProjectileParryEvent) procPoint).getDefendingStack());
            ((ProjectileParryEvent) procPoint).setReturnVec(((ProjectileParryEvent) procPoint).getProjectile().getMotion().inverse().scale(extra));
            ((ProjectileParryEvent) procPoint).setPostureConsumption(0);
        }
    }
}
