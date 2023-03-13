package jackiecrazy.wardance.entity;

import jackiecrazy.wardance.WarDance;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(WarDance.MODID)
@Mod.EventBusSubscriber(modid = WarDance.MODID, bus= Mod.EventBusSubscriber.Bus.MOD)
public class WarEntities {
    @ObjectHolder("thrown_weapon")
    public static EntityType<ThrownWeaponEntity> weapon = null;
    @ObjectHolder("fear")
    public static EntityType<FearEntity> fear = null;

    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        weapon = EntityType.Builder
                .of(ThrownWeaponEntity::new, MobCategory.MISC)
                .sized(0.8F, 0.8F)
                .build("thrown_weapon");
        weapon.setRegistryName(WarDance.MODID, "thrown_weapon");
        fear = EntityType.Builder
                .of(FearEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .build("fear");
        fear.setRegistryName(WarDance.MODID, "fear");
        event.getRegistry().registerAll(
                weapon, fear
        );
    } //registerEntities()


}
