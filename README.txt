Project: War Dance is Taoism Lite, adding the mechanics without the weapons.
Miscellaneous:
- Sweep attacks cannot be executed without sweeping edge, but will hit everything in an area for full damage. The default angle increment for each extra level of sweeping edge is 40 degrees.

Posture: a stat that quantifies how steady the player is on his feet. Stamina and poise rolled into one if you play souls-likes, but actually heavily inspired by Sekiro and DMC.
- Max posture is calculated by (width*height*5), so a player starts with 10 posture.
- Being attacked consumes posture regardless of parry. This makes parrying unequivocally the best option, since you’ll be taking posture damage either way.
- Posture recovers quickly when not attacking, but has a cooldown after being consumed. At full HP, standing still, you regenerate 5 posture per second.
- Armor items may add max posture. Regeneration is a flat number, so it will take you longer to "fully" recover.
- Moving slows posture regen down by 50%, 70% if sprinting.
- Regeneration also is multiplied by your current health percentage.
- If your attack cooldown is less than 100%, you gain an equivalent regeneration penalty
- Dodging resets your posture cooldown.
- This means that while actions play a role, the real deal-breaker is health. Defeating an enemy requires wearing down their health to make staggering easier.
- At 0 posture the entity is knocked back and staggered, gaining 1 fracture, binding both hands and slowing movement. The stagger ends after 1.5 seconds or taking damage once. During this time posture regenerates rapidly and cannot be depleted until it reaches max again.
- Accumulating an attribute-defined quantity of fracture marks instead exposes the entity for 5 seconds. In this state the entity cannot do anything, has all armor negated, and the next hit on it deals an extra 10% of its max health in damage.
- Fracture marks are cleared after exposure. Killing any entity that fractured you will also remove their respective fractures, as well as any ownerless fractures.

Might: charge up for a massive blow.
- Might is gained at a flat rate every attack, with slower weapons granting dramatically more, charging a bar that can be filled up to twice.
- After remaining out of combat for 10 seconds, might will relatively quickly drain to 0.
- Might is consumed in whole bars for specific skills.

Rank: continuous attacks will improve your combat efficacy. Heavily inspired by DMC, Scourgebringer and other action games.
- Rank is gained by a buildup bar that goes up along with your might.
- There are seven levels of rank, each rank provides a subtle increase in movement and attack speed that are briefly halved if you take damage.
- S ranks and above also provide a looting bonus to reward fun(tm) farms.

Spirit: suspiciously remniscent of a stamina bar.
- Spirit is a bar of icons on your HUD that are consumed in discrete quantities to perform certain skills.
- Spirit regenerates at a rate of 1 icon every 2 seconds.
- Consuming spirit or taking damage will pause spirit regen for 2 seconds.
- Spirit will not regenerate when chanting.

Idle Parry:
- By holding a “weapon” and orienting yourself towards an attack, you can parry it.
- Idle parrying negates damage, but not posture consumption nor attack effects.
- Notably, items have a parry multiplier that is multiplied with the posture damage. This multiplier will additionally multiply the knockback applied to both parties after a parry.
- Shields also parry projectiles, and can reduce posture damage from all sources via an attribute.

Armor Stats:
- Amor can have many different attributes that slightly modify the aforementioned mechanics. The overall change on feel in gameplay will be outlined below:
- light armor increases posture regeneration speed/reduces cooldown, so you must manage your own fracture and run away frequently, but can easily come back to continue harassing your enemies. They have an "evasion" stat that negates damage once in a long while to help them survive.
- medium armor increases posture healed (distinct from regeneration speed), which encourages using in-combat recovery and certain reckless tactics like suplex. They also have some evasion to facilitate taking a few hits.
- heavy armor greatly increases max posture and fracture marks, which allows you to tank. Though you have a higher initial pool of not dying, your relative ability to recover it is poor, so you must seek to overpower your opponents before the "fatigue" kicks in.

Dual wielding: weapons in the offhand can be swung.
- Bows and the like will function as per normal.
- The exception to the rule is fists: they can always be swung in the offhand. Boxer builds are now viable.
- An empty offhand confers no benefit without explicit investment in skills etc.
- The intent is to make two-handing, shields, and dual-wielding equally viable.

Combat mode: because I hate keybinds, and AcademyCraft is cool.
- Pressing shift+R enters combat mode.
- In combat mode, normal key function is overridden with PWD key functions.
- relevant HUDs and your offhand (if it is empty) also only show up in combat mode. The one exception is posture, since it's rather essential to not dying.

Mod compat:
- Elenai Dodge 2: feather regeneration will be halted by posture cooldown. Dodges are disabled when staggered or exposed.

Spells and skills: hit stuff so you can hit harder!
- Skills must be first set in the skill screen, then brought up in combat mode with the R key as a radial menu. You have 5 active slots, 5 passive slots, and 1 "style" slot.
- When you first enter you are greeted with a screen with a single slot dead center for your stance/art/style. The center info screen gives a welcome message and basic instructions on how to pick a style, and the styles are listed on the left
- After you pick a style, the slot moves into the top right corner and the skill screen comes out. You can pick another style by clicking that slot, but picking another style will clear currently selected skills.
- Each skill has a color, and each style only supports up to a certain number of colors.
- Above the skill selection scroll bar is a list of filter options. You may filter by color or revert to default, which shows all equippable skills. The colors that you currently have are arranged at the front, and unusable colors are grayed out.
- Each color tab has extra tooltips on mousing over that show their names and short descriptions:
    red: dominance. You are mighty. Crush your foes.
    green: resolution. Never give, never fall. Be as steady as a wall.
    gray: subterfuge. The deadliest strike is the one unseen.
    orange: fervor. Implacable, unstoppable. Death rains down in a thousand cuts.
    cyan: perception. See all, reach all. None can escape your grasp.
    violet: decay. All shall be dust.
- Chanting can be interrupted by taking damage, using a breath weapon, asphyxiating, eating, drinking, or chanting something else.
