package jackiecrazy.wardance.capability.stylish;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import net.minecraft.world.entity.LivingEntity;

public class StyleDataOverride extends StylishData {
    public StyleDataOverride(LivingEntity e){
        super(new StylishCapability(e));
    }
}
