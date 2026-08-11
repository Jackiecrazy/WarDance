package jackiecrazy.wardance.move.conditions;

import jackiecrazy.footwork.move.condition.ConditionRegistry;
import jackiecrazy.footwork.move.condition.ConditionType;
import jackiecrazy.wardance.WarDance;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class WarConditionsRegistry {
    public static final DeferredRegister<ConditionType> CONDITIONS = DeferredRegister.create(ConditionRegistry.REGISTRY_NAME, WarDance.MODID);

    public static final RegistryObject<ConditionType> HAS_SKILL = CONDITIONS.register("has_skill", () -> new ConditionType(HasSkillCondition.class));

}
