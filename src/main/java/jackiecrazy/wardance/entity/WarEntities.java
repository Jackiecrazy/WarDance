package jackiecrazy.wardance.entity;

import jackiecrazy.wardance.WarDance;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(WarDance.MODID)
@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class WarEntities {
    @ObjectHolder("thrown_weapon")
    public static EntityType<ThrownWeaponEntity> weapon = null;
    @ObjectHolder("fear")
    public static EntityType<FearEntity> fear = null;

    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        weapon = EntityType.Builder
                .create(ThrownWeaponEntity::new, EntityClassification.MISC)
                .size(0.8F, 0.8F)
                .build("thrown_weapon");
        weapon.setRegistryName(WarDance.MODID, "thrown_weapon");
        fear = EntityType.Builder
                .create(FearEntity::new, EntityClassification.MISC)
                .size(0.5F, 0.5F)
                .build("fear");
        fear.setRegistryName(WarDance.MODID, "fear");
        event.getRegistry().registerAll(
                weapon, fear
        );
    } //registerEntities()


}
