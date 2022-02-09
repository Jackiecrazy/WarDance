package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;

import java.awt.*;

public class BoulderBrace extends WarCry {
    @Override
    protected void evoke(LivingEntity caster) {
        CombatData.getCap(caster).setPosture(CombatData.getCap(caster).getMaxPosture());
        super.evoke(caster);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if(stats.getState()==STATE.ACTIVE){
            CombatData.getCap(caster).addPosture(0.05f);
            return activeTick(stats);
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }


}
