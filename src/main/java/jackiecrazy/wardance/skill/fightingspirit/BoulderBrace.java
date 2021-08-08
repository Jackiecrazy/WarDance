package jackiecrazy.wardance.skill.fightingspirit;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;

import java.awt.*;

public class BoulderBrace extends WarCry {
    @Override
    protected void evoke(LivingEntity caster) {
        CombatData.getCap(caster).setPosture(CombatData.getCap(caster).getMaxPosture());
    }

    @Override
    public boolean equippedTick(LivingEntity caster, STATE state) {
        CombatData.getCap(caster).decrementPostureGrace(1);
        return super.equippedTick(caster, state);
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        CombatData.getCap(caster).addPosture(0.05f);
        return super.activeTick(caster, d);
    }

    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }
}
