package jackiecrazy.wardance.config;

/*
how to implement new actions:
- add jumping moveset
    you are considered to be jump attacking if you jumped within the last half second and you have positive y velocity
- play a weapon swing animation from a given location and rotation to another, with the option of calculating collision with terrain or enemies, by summoning a fake entity
    The entity is spawned as a little blob that moves. Track which hand spawned which blob and disallow a new blob until the old one dies, or kill it immediately on swap
    the player is responsible for updating the blob's position from a set of keyframes.
        A keyframe set is a list of vectors that is read from a hashmap json. This is necessary so you can define specific ticks.
        The key is a number that corresponds to the tick when it should be run.
        The entry is 2-3 vectors. The first is orientation, second displacement, and the optional third is weapon rotation.
        The orientation determines the axis of displacement for the displacement. Along this axis displacement takes x y z values.
            at 0 on everything the weapon is essentially in your hand, respecting left/right.
        Weapon rotation is an optional visual spin on how the weapon renders. The entity will always render the tip of the weapon at its center.
        The hashmap is converted into a list by interpolating between points.
    The player maintains a map of strings to entities representing active weapon entities. String and not EnumHand, because flying weapons in the future.
    Each tick the map checks where the next location is supposed to be on the list and makes the entity move there.
    The entity acts as a physics probe to detect collisions. Each tick it draws a line between itself and the player, finds all intersecting mobs, and performs "contact damage"
    The entity also checks for block collisions for environmental effects, such as a hammer making an impact whenever it collides a wall/floor.
    When the probe intersects an entity, it swaps the player's mainhand item into its recorded stack, calls a vanilla attack function, and swaps back.
    Jump codes can skip to specific frames.
    Optionally this can be always active. The player will thus have two floating weapons to send into combat as long as they're in combat mode.


    Sweep rewrite brainstorm:
        each sweep is composed of x motion managers
        each motion manager can be one keyframe, many keyframes, or simply marked as transition
        In any case they have a duration, lerp speed, and a smoothing function.
        transition motion managers can execute actions at the start or end.
        each keyframe needs a referent entity (which doesn't have to be the player!) and can also execute actions
            -when the smoothing function reaches or finishes them.
            -when hitting an entity or block.
                This directly overwrites an onHitBlock/Entity field in flyingweapon with its respective action.
                If this field is not defined the previous hitBlock/Entity are kept.
        while executing the weapon holds a list of string tags and interprets them.
            skip would have the weapon immediately skip to the next move and remove this tag.
            cancel will clear the entire attack queue.
            retargetEntity/Block would have the weapon clear its hitlist and remove this tag.
            noClipBlock would skip block collision checks, same noClipEntity.
- syncing entity motions by tethering one to another
    easy, use the tether capability
- mark time periods as having dodge, guard, parry, or invulnerable frames
    to do this, extend dodge/guard/parry to a list of durations and on-success effects. Maybe map them with UUID
    Base dodge/guard/parry push default implementations, then actions can push custom ones onto the maintained stack
    This should ideally be merged with tick procs and skill marks for a centralized expression.
    In this case dodge/guard/parry actions will carry a respective entity flag, and on success the entire list of effects is queried for special effects.
- add velocity, potentially performing actions in midair or on landing
    you already did this
- perform actions on all entities that pass a filter in an area. The area can take the shape of a cone, a vertical cleave, a line with optional radius, or a circle around a given midpoint.

- callbacks in each timed action that listen to dodge or damage actions
- summon entities with data, velocity, and list of actions to execute. Aside from vanilla entities, this also includes a fake weapon entity that can take the form of any item as well as a generic invisible dummy entity.
- play a particle or a sequence of particles at a given location, optionally with velocity
- play a sound at a given loudness and pitch
- apply potion effects
- a condition for each timer action that activates during a certain time window
- loop through a series of actions when the attack input is held down
- simulate right clicking with the weapon, for weapons that originally use right clicks like bows
 */