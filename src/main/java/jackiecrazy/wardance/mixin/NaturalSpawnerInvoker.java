package jackiecrazy.wardance.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NaturalSpawner.class)
public interface NaturalSpawnerInvoker {

    @Invoker("canSpawnMobAt")
    static boolean canSpawnMobAt(
            ServerLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            MobCategory category,
            MobSpawnSettings.SpawnerData data,
            BlockPos pos
    ) {
        throw new AssertionError();
    }
}