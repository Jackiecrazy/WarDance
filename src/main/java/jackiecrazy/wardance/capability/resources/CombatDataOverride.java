package jackiecrazy.wardance.capability.resources;

import jackiecrazy.footwork.capability.resources.CombatData;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;

public class CombatDataOverride extends CombatData {
    public CombatDataOverride(LivingEntity e){
        super(LazyOptional.of(()->new CombatCapability(e)));
    }
}
