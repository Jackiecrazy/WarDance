package jackiecrazy.wardance.event;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public class CollisionEvent extends EntityEvent {
    private Vec3 projectedPath, collisionPoint;

    public Vec3 getProjectedPath() {
        return projectedPath;
    }

    public Vec3 getCollisionPoint() {
        return collisionPoint;
    }

    public CollisionEvent(Entity entity, Vec3 projectedPath, Vec3 collisionPoint) {
        super(entity);
        this.projectedPath = projectedPath;
        this.collisionPoint = collisionPoint;
    }
}
