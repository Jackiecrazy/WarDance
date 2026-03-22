package jackiecrazy.wardance.capability.aerial;

import net.minecraft.core.Direction;

public interface IAerialMode {
    public enum WallState{
        NONE(false, false),//always sticks
        STICKY(false, false),
        WALL_SLIDE(false, true),
        WALL_JUMP(false, false),//cannot stick to the previous wall
        CLING(true, true),
        CEILING_CLING(true, false);


        public final boolean noGravity;
        public final boolean wall;
        WallState(boolean antigrav, boolean wall) {
            noGravity=antigrav;
            this.wall=wall;
        }
    }
    /*
    Attacking within a few ticks of jumping counts as a launching attack and sets you to aerial mode. Alternatively, double tap space in the air to change into aerial mode.
Aerial mode gives slight slow fall. You cannot fall attack in air mode, but you can do the 'aerial' sweep.
Different weapons will specialize in different aerial velocity modifiers; many will charge to their target if one is not in range, some will bounce back after attacking, most will continue to apply some upward velocity to you and their targets with normal attacks. The general guideline for attacks is jump to launch, normal to charge and juggle, sneak to knockback (either enemy or yourself), and sprint to slam the target back down.
You can jump and dodge in the air up to 3 consecutive times.
Attacking or kicking an enemy will deal extra posture damage depending on your max posture and reset the limit.
Dodge into a wall to start running on it. While running on the wall you are always considered to be in the launching state for sweeps. Jump to leap off back into aerial mode, resetting your jump and dodge limit.
In aerial mode, you can either double sneak or use a slamming attack to leave it.
variant 1: if sneak to charge attack is implemented (instead of having a sneak sweep set, sneak enters guard mode and charges a large finisher attack that can parry), jumping at the end of a sweep executes the launching attack. I still don't know if disabling attacks when sneaking is a good idea, so this is just here as an offshoot idea.
variant 2: aerial dodges and jumps could cost spirit. In this case aerial attacks will also refill spirit.
variant 3: split finishers away from normal move states, instead finishers come out when you fulfill certain conditions. In this case entering or leaving aerial mode could be a condition.


basic:
double jump, attribute for extra jumps. Jumps cost spirit, reset on grounding or wall contact
some kind of auto-step/climb that preserves velocity including on walls
wall running by dodging into a wall. control by camera aim
Coyote time and auto-sideways step assist to account for breaks and ledges.
Prevent from leaving collided surfaces unless there is another collided surface.
jump to leap off early, sneak to stop and cling (drains spirit?)
near surface: hit a block with an exposed top face near head level to hang on for free. Hanging continues along the same y level along unbroken line of blocks (outcrops ok), sneak to drop, jump to mantle up
roof cling: jump when within half a block of the ceiling to stick
     */
    default boolean isAerialMode(){
        return getEffectiveSpeed()<1;
    }
    void alterGravity(int ticks, double speed);
    void tick();
    void resetSpeed();
    double getEffectiveSpeed();
    int getTimeRemaining();
    WallState getState();
    boolean setState(WallState state);
    Direction getWallDir();  // Facing normal.
    void setWallDir(Direction dir);
    boolean enforcedNoOff();
    void noOffFor(int ticks);
}
