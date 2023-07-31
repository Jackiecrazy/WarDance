package jackiecrazy.wardance.entity;

import jackiecrazy.wardance.WarDance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WarEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WarDance.MODID);

    public static final RegistryObject<EntityType<ThrownWeaponEntity>> THROWN_WEAPON = ENTITIES.register("thrown_weapon", () -> EntityType.Builder
            .of(ThrownWeaponEntity::new, MobCategory.MISC)
            .sized(0.8F, 0.8F)
            .build("thrown_weapon"));

    public static final RegistryObject<EntityType<FearEntity>> FEAR = ENTITIES.register("fear", () -> EntityType.Builder
            .of(FearEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .build("fear"));

}
