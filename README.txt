Project: War Dance is Taoism Lite, adding the mechanics without the weapons.
    Posture: a stat that quantifies how steady the player is on his feet. Stamina and poise rolled into one if you play souls-likes, but actually heavily inspired by Sekiro and DMC.
- Max posture is calculated by (width*height*5*(1+armor/20)), so a player starts with 10 posture and can gain up to double by wearing diamond armor.
- Being attacked consumes posture regardless of parry. This makes parrying unequivocably the best option, since you’ll be taking posture damage either way.
- To avoid ravagers instantly staggering everything else, posture consumption is hard capped at 1/4 of your max each time. Overflow posture damage will be converted into knockback.
- Posture recovers quickly when not attacking, but has a 1s cooldown after being consumed. At full HP, standing still naked, you heal 20 posture per second.
- Armor will decrease regeneration speed down to 0.7x. This offers an anti-tank option.
- Moving slows posture regen down by 7% per block per second, so regeneration is roughly 70% while walking and 60% while sprinting.
- Regeneration also is multiplied by your current health percentage.
- Posture will not regenerate while rolling or attacking; chanting does not affect it.
- This means that while weight plays a factor (49%), the real deal-breaker is health. Defeating an enemy requires wearing down their health to make staggering easier.
- At 0 posture the entity is knocked back and staggered, taking 1.5x damage and losing 5 armor. The entity cannot perform any action during this time. The stagger ends after 0.5s(+%missing hp*3) or after taking physical damage thrice. Posture is instantly filled after the stagger to prevent stun-locking.
Combo: continuous attacks will improve your combat efficacy... just don’t get interrupted. Heavily inspired by DMC, Scourgebringer and other action games.
- Combo is gained by a buildup bar, taking about 5 normal attacks to gain a tier.
- The bar increases from any attack, whether they were parried or not.
- The bar also only has a 5-second grace period before clearing itself.
- Idle parries will not increase the combo counter, but will reset its grace period.
- There are nine levels of combo, taking a total of 45 normal attacks.
- Combo is halved upon taking a full-damage hit, and the bar is cleared.
- Physical skills can fill the bar faster, encouraging their use.
Might: by remaining in combat, one can gradually unlock more and more moves.
- Might is gained at a flat rate every attack.
- Every hit in your combo will increase its generation multiplier by 0.1, up to double.
- After not being used for half a minute, might will decrease slowly, healing fatigue, burnout, and wounding, in that order. This is just for insomnia runs.
Spirit: suspiciously remniscent of a stamina bar.
- Spirit obeys the same rules as posture, sans armor increasing its cap.
- Taking damage will pause spirit regen for 2 seconds.
Idle parry:
- By holding a “weapon” and orienting yourself towards an attack, you can parry it.
- Idle parrying negates damage, but not posture consumption nor attack effects.
- Notably, items have a parry multiplier that is multiplied with the posture damage. This multiplier will additionally multiply the knockback applied to both parties after a parry.
- Shields also parry projectiles, and gain a window on a normal parry, such that the next x attacks for y seconds are free. They’re also generally cheaper posture-wise.
Wounding, Fatigue, and Burnout: can't fight forever, now can you?
- These negative stats are increased by consuming their respective resources: health, posture, and spirit.
- They decrease the caps of their respective resources, and can be cleared by sleeping.
- The intent is to prevent players with absurd regen face-tanking everything.
- The negative stats can be reduced by might decay, for insomnia runs such as Enigmatic Legacy's Ring of Seven Curses
Absorption, Deflection and Shatter: because of course we need to be even more unkillable!
- Absorption reduces incoming damage by a fixed amount. This makes it excellent for defending against small, rapid strikes, and basically useless for big hits. Seen often on light/soft armor.
- Deflection acts as a parry for your two flanks, up to 60 degrees on each side. It costs the default to parry like this, so skilled fighters can angle themselves towards strikes in order to quickly riposte. Plate wearers rejoice!
- Shatter absorbs all damage. Period. However, it's quickly lost and takes time to recover. Shatter completely regenerates after 10 seconds of not taking (shatter) damage. Found on crystalline and ceramic armor.
Dual wielding: weapons in the offhand can be swung.
- Bows and the like will function as per normal.
- The exception to the rule is fists: they can always be swung in the offhand. Boxer builds are now viable.
- An empty offhand confers no benefit without explicit investment in skills etc.
- The intent is to make two-handing, shields, and dual-wielding equally viable.
Damage type: a weapon can have a mix of slashing, blunt, and piercing damage.
- Slashing: dragging a blade across the body so as to leave a wound. Default for swords, being mainly about consistent health damage, large sweeps, and debuffs.
- Blunt: using force to crush and break. Posture damage, control, and raw power.
- Piercing: pushing a sharp tip into body parts to minimize resistance and energy used to attack. Focus on critical damage, attack speed, and defense bypassing.
- Chopping: example mixed damage, 1:1 slashing and blunt. Trades some posture damage and control from blunt for slash’s health damage and debuff (armor break)
- The typing primarily affects skills.
Combat mode: because I hate keybinds, and AcademyCraft is cool.
- Pressing backslash enters combat mode.
- In combat mode, normal key function is overridden with PWD key functions.
- relevant HUDs and your offhand (if it is empty) also only show up in combat mode. The one exception is posture, since it's rather essential to not dying.
Sidesteps and rolls: OP pls nerf
- Double tapping any movement key except forward has you sidestep in that direction.
- You can also sidestep by key+sprint. Both are only available in combat mode.
- The player performs a small jump with large horizontal velocity, ending about 5 blocks from where they begin. The player has step assist during this period of movement. The cooldown is configurable, but defaults to 0.75 seconds (15 ticks).
- If your crosshair is on an entity within 5 blocks, you will sidestep in an arc around it.
- It is intended as a quick way to adjust one’s position around a target or dodge a hit.
- By sneaking when sidestepping you perform a higher-risk and higher-reward roll, ending 3 blocks from where you begin.
- While rolling you are immune to all melee and projectile attacks for 10 ticks, but it also reduces your max posture as you become 0.6x0.6. Failure to escape as you come out of recovery could result in the opponent staggering you very easily.
- Sprint-sneaking will perform a slide forward with the same effect as dodge.
- Sidesteps and rolls are suppressed with Elenai Dodge 2 installed. Might bonus from combo will also apply to feather regeneration, but regeneration will be halted by posture cooldown. Dodges when staggered are changed into rolls.
Spells and skills: hit stuff so you can hit harder!
- Skills must be first set in the skill screen, then brought up in combat mode with the F key as a radial menu. Half the screen (4 slots) is available for each hand to add skills, and two-handing gives 8 skill slots. Combined with 4 spell slots that don’t require weapons, you can have 12 skills equipped at any given time.
- Skills are split into categories depending on their resource consumption for recharge (e.g. might, spirit, time, normal attacks, distance moved, etc). All skills of the same category have their “timers” set to half upon casting one so you can’t continuously chain moves of the same category.
- Skills can be augmented by items/runes that add or modify behavior, see example.
- Instant casts are usually things that act with yourself as center, and are cast just by moving the mouse over the pie slice in the selection. Failure to use effects of a bolstering skill will refund half charge rounded up, without resetting others. These skills are chosen to be packaged into the base API due to their general applicability:

▪ Heavy blow: the next attack in 2 seconds guarantees a crit when dealing damage, and disables the parrying hand if parried. Recharge in 3 attacks, weapon bound.
Shatter: mark hit enemies, causing next 2 attacks to deal 1.5x damage; stagger: deal equivalent posture and 0.8s slow; lunge: range+2 and dash forward; poise: free parries until attack; resolve: double resource regen speed for 2 seconds.
▪ Iron guard: the next parry in 1 second (particles) reflects the posture damage onto attacker. If the attack was ranged, the shot is reflected back at the caster with full force. Cancels out with heavy blow. Recharge in 5 attacks, weapon bound.
Overpower: additionally deduct posture as if attacking with parrying hand; bind: disable attacking hand for melee; backpedal: all posture cost converted to knockback; recovery: reset cooldown of all resources; mikiri: cooldown -1, auto trigger.
▪ Kick: the next attack in 2 seconds has 2 range but deals 6 unblockable posture damage and 1 blockable falling damage. Recharge in 5 attacks, self bound.
Backflip: jump back and gain 30% lost posture; tackle: dashes and kicks first entity collided; iron knee: knockback converted to extra posture damage; trip: fatigue for double the cooldown time, only usable on ground, cooldown -1; overextend: double damage but deal half to self.
▪ Grapple: instantly stagger (they keep their posture when getting back up) by attacking a single enemy within 2 blocks twice uninterrupted within 3 seconds. Recharge in 7 attacks, weapon bound, fists and hand weapons only.
Clinch: bind both hands on first strike; reversal: exchanges posture by percentage; submission: instantly succeeds if opponent has less health; throw: pick up enemy and use as projectile; suplex: consume all your own posture and deal it to the enemy on a multiplier, leaving them behind you.
▪ Shield bash: for 2 seconds, your shield becomes a range 2 blunt weapon that deals posture damage equivalent to shield time. Recharge in 4 parries, weapon bound, shield only.
Rim punch: range increased to 3, nauseates enemy on attack; foot slam: additionally inflicts slowness and distracts enemy; arm lock: parrying with bash active disables attacking hand, cooldown+1; berserk: recharge other hand and gain brief attack speed buff on cast and after attack; overbear: base damage increased by shield time/3 and posture damage doubled, but slow self briefly if enemy isn't staggered by hit.
- Bound casts are chosen, then bound to the attack key in combat mode. Casting them generally requires chanting, but they’re more powerful. As expected, items can have different spells bound to them, or even just have a magic missile as its normal attack. This also enables throwable weapons (such as poison sand or daggers) to be used as melee. There should be an interface for such items. Only two bound skills are included as examples. Chanting can be interrupted by taking damage, using a breath weapon, asphyxiating, eating, drinking, or chanting something else.
▪ Phantom strike: after 0.6s of chanting, launch a shadow of your weapon, dealing a normal attack on impact. Cost 2 might, range 16.
Warp: light gravity, teleport to hit location; crush: triple size and damage but half speed and range, clips and pierces; hamstring: slow target by 40% for 2 seconds; vengeance: ricochet up to 5 times between enemies; trap: only fires when someone crosses its LoS in 10 blocks.
▪ Fighting spirit: passive available until active cast ends, then can only be recharged by sleeping or after 2 minutes have elapsed. Genies from some game.
Name
 Active
 Passive
Boulder Brace
 Refill posture and regen unconditionally for 10 seconds
 Halved posture cooldown time
Wind Scar
 Gain 20 charges and infinite range, every extra block reach consumes a charge
 +1 range
Winter Wolf
 Stop projectiles and maintain passive for 15 seconds, attacks additionally fatigue target
 Speed slowly increases to +40% until attack, slow target equivalently
Flame Dance
 Every third hit deals extra damage and give you a free parry (independent of iron guard) for 15 seconds
 +10% attack speed for every bar of combo acquired
Timberfall
 Guarantee crit and add 2+10% damage for 6 hits
 30% of attack damage added to posture
