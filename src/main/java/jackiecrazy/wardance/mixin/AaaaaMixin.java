package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.handlers.EntityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(NaturalSpawner.class)
public class AaaaaMixin {

    // -------------------------------------------------------------------------
    // 1. Main position / rule validation (before the entity is even created)
    // -------------------------------------------------------------------------
    @Inject(
            method = "isValidSpawnPostitionForType",
            at = @At("RETURN"),
            cancellable = false,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void onIsValidSpawnPositionForType(
            ServerLevel level,
            MobCategory category,
            StructureManager structureManager,
            ChunkGenerator generator,
            MobSpawnSettings.SpawnerData spawnerData,
            BlockPos.MutableBlockPos pos,
            double nearestPlayerDistanceSqr,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (cir.getReturnValue()) {
            return; // success – nothing to log
        }

        EntityType<?> type = spawnerData.type;
        if(type.getCategory()!=MobCategory.MONSTER)return;
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        String mobId = id != null ? id.toString() : type.toString();

        // Re-evaluate the same conditions in order so we can report the exact failure
        String reason = diagnosePositionFailure(level, category, structureManager, generator,
                                                spawnerData, pos, nearestPlayerDistanceSqr);
        EntityHandler.LAST_TICK_SPAWN_FAIL.compute(id, (k,v)->v==null?1:v+1);

        WarDance.LOGGER.debug("[NaturalSpawnFail] {} ({}) failed at {} – reason: {}", mobId, category.getName(), pos.immutable(), reason);
    }

    // -------------------------------------------------------------------------
    // 2. Final per-mob validation (after the Mob instance exists)
    // -------------------------------------------------------------------------
    @Inject(
            method = "isValidPositionForMob",
            at = @At("RETURN")
    )
    private static void onIsValidPositionForMob(
            ServerLevel level,
            Mob mob,
            double nearestPlayerDistanceSqr,
            CallbackInfoReturnable<Boolean> cir
    ) {
        EntityType<?> type = mob.getType();
        if (cir.getReturnValue()) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
            EntityHandler.HISTORICAL_SPAWN_SUCCESS.compute(id, (k,v)->v==null?1:v+1);
            return;
        }

        MobCategory category = type.getCategory();
        if(!category.equals(MobCategory.MONSTER))return;
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        EntityHandler.LAST_TICK_SPAWN_FAIL.compute(id, (k,v)->v==null?1:v+1);
        String mobId = id != null ? id.toString() : type.toString();

        String reason = diagnoseMobFailure(level, mob, nearestPlayerDistanceSqr);

        WarDance.LOGGER.debug("[NaturalSpawnFail] {} ({}) failed final check at {} – reason: {}",
                    mobId, category.getName(), mob.blockPosition(), reason);
    }

    // -------------------------------------------------------------------------
    // Diagnosis helpers (mirror vanilla logic)
    // -------------------------------------------------------------------------
    @Unique
    private static String diagnosePositionFailure(
            ServerLevel level,
            MobCategory category,
            StructureManager structureManager,
            ChunkGenerator generator,
            MobSpawnSettings.SpawnerData data,
            BlockPos.MutableBlockPos pos,
            double distSqr
    ) {
        EntityType<?> type = data.type;

        if (type.getCategory() == MobCategory.MISC) {
            return "category is MISC";
        }
        if (!type.canSpawnFarFromPlayer()
                && distSqr > category.getDespawnDistance() * (double) category.getDespawnDistance()) {
            return "too far from player (despawn distance) and cannot spawn far from player";
        }
        if (!type.canSummon()) {
            return "entityType.canSummon() == false";
        }
        // canSpawnMobAt – biome / structure list match
        if (!NaturalSpawnerInvoker.canSpawnMobAt(level, structureManager, generator, category, data, pos)) {
            return "canSpawnMobAt failed (not in biome/structure spawn list for this category)";
        }

        SpawnPlacements.Type placement = SpawnPlacements.getPlacementType(type);
        if (!NaturalSpawner.isSpawnPositionOk(placement, level, pos, type)) {
            return "isSpawnPositionOk failed for "+placement;
        }
        if (!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, level.random)) {
            return "SpawnPlacements.checkSpawnRules failed (light, biome, custom rules, etc.)";
        }
        if (!level.noCollision(type.getAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) {
            return "collision box intersects solid blocks or entities";
        }

        return "unknown (logic changed?)";
    }

    @Unique
    private static String diagnoseMobFailure(ServerLevel level, Mob mob, double distSqr) {
        EntityType<?> type = mob.getType();
        double despawn = type.getCategory().getDespawnDistance();

        if (distSqr > despawn * despawn && mob.removeWhenFarAway(distSqr)) {
            return "too far from player and removeWhenFarAway returned true";
        }
        if (!mob.checkSpawnRules(level, MobSpawnType.NATURAL)) {
            return "mob.checkSpawnRules failed";
        }
        if (!mob.checkSpawnObstruction(level)) {
            return "mob.checkSpawnObstruction failed (space / obstruction)";
        }
        return "unknown";
    }
}