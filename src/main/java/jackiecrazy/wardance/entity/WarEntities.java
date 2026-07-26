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

    public static final RegistryObject<EntityType<GrappleEntity>> GRAPPLE = ENTITIES.register("grapple", () -> EntityType.Builder
            .of(GrappleEntity::new, MobCategory.MISC)
            .sized(0.4F, 0.4F)
            .build("grapple"));
    public static final RegistryObject<EntityType<ThrownWeaponEntity>> THROWN_WEAPON = ENTITIES.register("thrown_weapon", () -> EntityType.Builder
            .of(ThrownWeaponEntity::new, MobCategory.MISC)
            .sized(0.4F, 0.4F)
            .build("thrown_weapon"));
    public static final RegistryObject<EntityType<GhostBlockEntity>> FLYING_BLOCK = ENTITIES.register("flying_block", () -> EntityType.Builder
            .of(GhostBlockEntity::new, MobCategory.MISC)
            .sized(1F, 1F)
            .setTrackingRange(128)
            .build("flying_block"));

    public static final RegistryObject<EntityType<FearEntity>> FEAR = ENTITIES.register("fear", () -> EntityType.Builder
            .of(FearEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .build("fear"));

    public static final RegistryObject<EntityType<FlyingWeaponEntity>> WEAPON = ENTITIES.register("flying_weapon", () -> EntityType.Builder
            .of(FlyingWeaponEntity::new, MobCategory.MISC)
            .clientTrackingRange(64)
            .sized(0.8F, 0.8F)
            .updateInterval(1)
            .setShouldReceiveVelocityUpdates(true)
            .build("flying_weapon"));

    public static final RegistryObject<EntityType<WindBladeEntity>> WIND_BLADE = ENTITIES.register("wind_blade", () -> EntityType.Builder
            .of(WindBladeEntity::new, MobCategory.MISC)
            .sized(0.4F, 0.4F)
            .build("wind_blade"));
}
