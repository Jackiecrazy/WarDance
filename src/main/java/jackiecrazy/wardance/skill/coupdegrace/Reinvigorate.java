package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.wardance.capability.resources.CombatData;
import net.minecraft.entity.LivingEntity;

import java.awt.*;

public class Reinvigorate extends CoupDeGrace {
    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    protected void uponDeath(LivingEntity caster, LivingEntity target, float amount) {
        CombatData.getCap(caster).addFatigue(-amount / 5);
        CombatData.getCap(caster).addWounding(-amount / 5);
        CombatData.getCap(caster).addBurnout(-amount / 5);
    }
}
