package jackiecrazy.wardance.api;

import jackiecrazy.footwork.api.ITetherAnchor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface IDrag extends ITetherAnchor {
    @Override
    default boolean shouldRepel() {
        return true;
    }

    @Override
    default boolean stopMotion() {
        return true;
    }

    @Override
    default boolean cappedForce() {
        return true;
    }

    @Override
    default boolean fuzzyTargeting() {
        return false;
    }

    //drag simply moves a "target" to a "point" with a "force".
    //this point can be a weapon's location, or a specific position in the world, or even a vec3 dynamically resolved by motion frames.
    //but for the sake of my sanity, I will say that drag is always instigated by an entity, which is almost always the flying weapon.
    //in order for this to work with a flying weapon, motion frames need to be altered with a partial tick parameter.
    // The partial tick parameter determines what frame the weapon is playing. It can increase by 1 per tick or less if dragging.
    // this allows us to tune dragging animations. If you drag a heavy mob, it is possible that the animation's time will run out (and thus not drag the mob the entire way).
    double getDragStrength();

    int getDragDuration();

    default boolean shouldDrag() {
        return getDragDuration() > 0;
    }

    @Override
    default void updateTetheringVelocity() {
        if (shouldDrag())
            ITetherAnchor.super.updateTetheringVelocity();
    }

}
