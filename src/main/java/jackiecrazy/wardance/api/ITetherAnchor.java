package jackiecrazy.wardance.api;

import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public interface ITetherAnchor {
    /**
     * updates the tether wielder's velocity
     */
    default void updateTetheringVelocity() {
        if (getTetheringEntity() != null) {
            Vector3d offset = getTetheredOffset();
            Entity toBeMoved = getTetheringEntity();
            Entity moveTowards = getTetheredEntity();
            if (toBeMoved != null) {
                double distsq = 0;
                Vector3d point = null;
                if (offset != null) {
                    distsq = GeneralUtils.getDistSqCompensated(toBeMoved, offset);
                    point = offset;
                    if (moveTowards != null) {
                        distsq = toBeMoved.getDistanceSq(moveTowards);
                        point = moveTowards.getPositionVec().add(offset);
                    }
                }
                //update the entity's relative position to the point
                //if the distance is below tether length, do nothing
                //if the distance is above tether length, apply centripetal force to the point
                if (getTetherLength() * getTetherLength() < distsq && point != null) {
                    toBeMoved.addVelocity((point.x - toBeMoved.getPosX()) * 0.05, (point.y - toBeMoved.getPosY()) * 0.05, (point.z - toBeMoved.getPosZ()) * 0.05);
                }
                if (shouldRepel() && getTetherLength() * getTetherLength() < distsq && point != null) {
                    toBeMoved.addVelocity((point.x - toBeMoved.getPosX()) * -0.05, (point.y - toBeMoved.getPosY()) * -0.05, (point.z - toBeMoved.getPosZ()) * -0.05);
                }
                if (getTetherLength() == 0 && moveTowards != null) {//special case to help with catching up to entities
                    //System.out.println(target.getDistanceSq(e));
                    //if(NeedyLittleThings.getDistSqCompensated(moveTowards, toBeMoved)>8){
                    toBeMoved.setPosition(moveTowards.getPosX(), moveTowards.getPosY(), moveTowards.getPosZ());
                    //}
                    Vector3d vec = moveTowards.getMotion();
                    toBeMoved.setMotion(moveTowards.getMotion());
                    if (!moveTowards.isOnGround())
                        toBeMoved.setVelocity(vec.x, moveTowards.isOnGround() ? 0 : vec.y, vec.z);
                }//else e.motionZ=e.motionX=e.motionY=0;
                toBeMoved.velocityChanged = true;
            }
        }
    }

    Entity getTetheringEntity();

    void setTetheringEntity(Entity to);

    @Nullable
    Vector3d getTetheredOffset();

    @Nullable
    Entity getTetheredEntity();

    void setTetheredEntity(Entity to);

    double getTetherLength();

    default boolean shouldRepel() {
        return false;
    }
}
