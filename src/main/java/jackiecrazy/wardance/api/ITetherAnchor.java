package jackiecrazy.wardance.api;

import jackiecrazy.footwork.utils.GeneralUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface ITetherAnchor {
    /**
     * updates the tether wielder's velocity
     */
    default void updateTetheringVelocity() {
        if (getTetheringEntity() != null) {
            Vec3 offset = getTetheredOffset();
            Entity toBeMoved = getTetheringEntity();
            Entity moveTowards = getTetheredEntity();
            if (toBeMoved != null) {
                double distsq = 0;
                Vec3 point = null;
                if (offset != null) {
                    distsq = GeneralUtils.getDistSqCompensated(toBeMoved, offset);
                    point = offset;
                    if (moveTowards != null) {
                        distsq = toBeMoved.distanceToSqr(moveTowards);
                        point = moveTowards.position().add(offset);
                    }
                }
                //update the entity's relative position to the point
                //if the distance is below tether length, do nothing
                //if the distance is above tether length, apply centripetal force to the point
                if (getTetherLength() * getTetherLength() < distsq && point != null) {
                    toBeMoved.push((point.x - toBeMoved.getX()) * 0.05, (point.y - toBeMoved.getY()) * 0.05, (point.z - toBeMoved.getZ()) * 0.05);
                }
                if (shouldRepel() && getTetherLength() * getTetherLength() < distsq && point != null) {
                    toBeMoved.push((point.x - toBeMoved.getX()) * -0.05, (point.y - toBeMoved.getY()) * -0.05, (point.z - toBeMoved.getZ()) * -0.05);
                }
                if (getTetherLength() == 0 && moveTowards != null) {//special case to help with catching up to entities
                    //System.out.println(target.getDistanceSq(e));
                    //if(NeedyLittleThings.getDistSqCompensated(moveTowards, toBeMoved)>8){
                    toBeMoved.setPos(moveTowards.getX(), moveTowards.getY(), moveTowards.getZ());
                    //}
                    Vec3 vec = moveTowards.getDeltaMovement();
                    toBeMoved.setDeltaMovement(moveTowards.getDeltaMovement());
                    if (!moveTowards.isOnGround())
                        toBeMoved.lerpMotion(vec.x, moveTowards.isOnGround() ? 0 : vec.y, vec.z);
                }//else e.motionZ=e.motionX=e.motionY=0;
                toBeMoved.hurtMarked = true;
            }
        }
    }

    Entity getTetheringEntity();

    void setTetheringEntity(Entity to);

    @Nullable
    Vec3 getTetheredOffset();

    @Nullable
    Entity getTetheredEntity();

    void setTetheredEntity(Entity to);

    double getTetherLength();

    default boolean shouldRepel() {
        return false;
    }
}
