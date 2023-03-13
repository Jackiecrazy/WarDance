package jackiecrazy.wardance.skill.warcry;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;

import jackiecrazy.wardance.skill.Skill.STATE;

public class BoulderBrace extends WarCry {
    @Override
    protected void evoke(LivingEntity caster) {
        super.evoke(caster);
        CombatData.getCap(caster).setPosture(CombatData.getCap(caster).getMaxPosture());
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
