package jackiecrazy.wardance.capability.resources;

import jackiecrazy.footwork.capability.resources.CombatData;
import net.minecraft.world.entity.LivingEntity;

public class CombatDataOverride extends CombatData {
    public CombatDataOverride(LivingEntity e){
        super(new CombatCapability(e));
    }
}
