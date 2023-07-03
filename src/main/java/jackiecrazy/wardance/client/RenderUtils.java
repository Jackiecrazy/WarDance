package jackiecrazy.wardance.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

//@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class RenderUtils {
    static final DecimalFormat formatter = new DecimalFormat("#.#");
    /**
     * @author Vazkii
     */
    public static Entity getEntityLookedAt(Entity e, double finalDistance) {
        Entity foundEntity = null;
        double distance = finalDistance;
        HitResult pos = raycast(e, finalDistance);
        Vec3 positionVector = e.position();

        if (e instanceof Player) positionVector = positionVector.add(0, e.getEyeHeight(e.getPose()), 0);

        if (pos != null) distance = pos.getLocation().distanceTo(positionVector);

        Vec3 lookVector = e.getLookAngle();
        Vec3 reachVector = positionVector.add(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance);

        Entity lookedEntity = null;
        List<Entity> entitiesInBoundingBox = e.getCommandSenderWorld().getEntities(e, e.getBoundingBox().inflate(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance).expandTowards(1F, 1F, 1F));
        double minDistance = distance;

        for (Entity entity : entitiesInBoundingBox) {
            if (entity.isPickable()) {
                AABB collisionBox = entity.getBoundingBoxForCulling();
                Optional<Vec3> interceptPosition = collisionBox.clip(positionVector, reachVector);

                if (collisionBox.contains(positionVector)) {
                    if (0.0D < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = 0.0D;
                    }
                } else if (interceptPosition.isPresent()) {
                    double distanceToEntity = positionVector.distanceTo(interceptPosition.get());

                    if (distanceToEntity < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = distanceToEntity;
                    }
                }
            }

            if (lookedEntity != null && (minDistance < distance || pos == null)) foundEntity = lookedEntity;
        }

        return foundEntity;
    }

    public static HitResult raycast(Entity e, double len) {
        Vec3 vec = new Vec3(e.getX(), e.getY(), e.getZ());
        if (e instanceof Player) vec = vec.add(new Vec3(0, e.getEyeHeight(e.getPose()), 0));

        Vec3 look = e.getLookAngle();
        if (look == null) return null;

        return raycast(vec, look, e, len);
    }

    public static HitResult raycast(Vec3 origin, Vec3 ray, Entity e, double len) {
        Vec3 next = origin.add(ray.normalize().scale(len));
        return e.level.clip(new ClipContext(origin, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, e));
    }
}
