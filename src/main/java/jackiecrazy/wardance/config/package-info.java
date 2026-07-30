package jackiecrazy.wardance.config;

/*
    survivor: nuked
    tabula rasa: exactly the same, restrictions lifted
    boulder brace: still and rolling boulder, working it out
while still: lose half move speed. Gain increased rally regen, rally on block, knockback resist, darktide power. Lose on any move.
while sprinting: slowly gains move speed. After 1.5 seconds start to deal posture damage and knockback any mob you are about to hit.
on swing: cancel horizontal velocity and regain still stats. If sprinting or falling at high speed, create an enfeebling and slowing cloud of dust.
    wind scar: constant erratic movement and ranged pressure
every movement or combat action generates wind pressure.
Normal attacks do not but mark targets. Projectiles and throws generate slightly more.
At 10 wind pressure, automatically consume all to launch (combo) wind blades. Wind blades track targets, favoring marked ones, and pierce through them repeatedly
(during adrenaline burst throw wind blades repeatedly)
    flame dance: relentless fire. I can either think of burning away impurities and carbonizing fuel, sticking to the enemy like a glob of magma, or... phoenixes? Repeated death's door style lol
reduces attack knockback. your body radiates heat. Anyone in your attack radius will slowly gain a stacking penalty to armor/some other things.
At 100% penalty they will start to take burn damage whenever they are attacked or periodically, scaling with their armor.
Heat dissipates rapidly when they leave your radius.
rapid attacks, swapping weapons, casting skills, and in general keeping rhythm will produce bursts of heat that increase heat more than usual.
This burst will ignite (or burn away fire immunity), disarm, or
    timberfall: delayed tree falling
After skill cast, create a tree nearby. the tree slowly grows in size and damage
Trees count as terrain for sabaton smash etc.
Dealing damage to the tree or slamming a mob onto it causes it to fall over and deal extra (breaching?) damage
    frost fang: patience, enter, burst damage, retreat
doubled damage on first melee strike, sticks a mark on the target that transfers half damage received to internal instead
the mark goes down by 3 per second. +1 if you're not in range, +2 for distracted, +3 for unaware
randomly as the mark ticks down it could ding. Attacks within ~2 seconds of the ding gain increased damage and ticks the mark down
    pestilent edge
    doppelsoldner
    gold rush
    demon hunter
    unyielding spirit
    walk of dionysus
    blood tax
    unstable spirit
gunlance's heat gauge?
    gambler's whimsy
    sifu

afterimage
apathy
archers_paradox
lockdown
backflip
backpedal
berserk
bloodlust
confidence
crown_champion
cursed_palms
curse_of_echoes
curse_of_misfortune
danse_macabre
decapitate
earthen_sweep
fatal_cadence
fiery_lunge
flurry
followup
foot_slam
frenzy
gangrene
iron_chop
itchy_curse
lady_luck
leverage
mikiri
momentum
montante
natural_sprinter
necrosis
overbear
overpower
petrify
phantom_dive
pound_of_flesh
prideful_might
pummel
rapid_clotting
reaping
bite_the_dust
return_to_sender
rim_punch
sabaton_smash
selfish_mascot
shadowless_kick
shadow_dive
shield_crush
silencer
smirking_shadow
spirit_resonance
stagger
static_discharge
submission
suplex
tackle
throw
tornado
trample
unravel
taunt
vengeful_might
viral_decay
vital_strike
water_uppercut
wooden_jab
wrestle
lunge
pounce
wind_shot
countershot
ripper

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