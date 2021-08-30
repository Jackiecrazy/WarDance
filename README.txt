Project: War Dance is Taoism Lite, adding the mechanics without the weapons.
Miscellaneous:
- Sweep attacks cannot be executed without sweeping edge, but will hit everything in an area for full damage. The default angle increment for each extra level of sweeping edge is 40 degrees.
- Reach distance will influence both attack reach and sweep reach.
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
- Spirit will not regenerate when chanting.
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
- Chopping: example mixed damage, 1: 1 slashing and blunt. Trades some posture damage and control from blunt for slash’s health damage and debuff (armor break)
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
Stealth: Sneak 100
- Mobs are unaware if they are not in combat and haven't noticed you, distracted if they are attacking something else, and alert otherwise.
- Distracted enemies take more damage, and unaware enemies additionally cannot parry, deflect, absorb, or shatter an incoming attack. Weapons have a stabbing multiplier that increases stabbing damage. Attacking an unaware enemy dispels your invisibility, if any.
- Mob detection range is full in their 120 degree frontal sector and 0.2x elsewhere.
- The vision range is 60 degrees vertically, because living things are adapted to survey a flat area, and to encourage using vertical space.
- Light level will decrease detection range by up to 50% at 0 light and increase by up to 10% at 15 light. Mobs with night vision do not receive a bonus or malus.
- Armor adds detection range by 18 degrees per protection point. This overwrites the fixed mob detection range if it is higher, so leather doesn't impede stealth and full diamond nullifies the vision malus.
- Sprinting adds 10% to detection radius; breaking LoS with the target will reduce detection radius by 60%.
- Mobs will investigate sound when unaware; this will distract them. Making sounds such as taking damage from fall and causing explosions will draw enemies in an area to you, but you can also shoot arrows to distract mobs for stealth missions.
- If you are about to be discovered from out of LoS, the luck of you and your opponent will be compared to give you a chance of negating discovery.
Spells and skills: hit stuff so you can hit harder!
- Skills must be first set in the skill screen, then brought up in combat mode with the F key as a radial menu. You have 12 slots in total, period. Since you can only do 12 things at any given time, the people who can switch sets mid-fight are going to have less slots per weapon.
- Skills can be augmented by items/runes that add or modify behavior, see example. You can equip multiple variations of the same skill simultaneously.
- Skills are split into categories depending on their resource consumption for recharge (e.g. might, spirit, time, normal attacks, distance moved, etc).
- Chanting can be interrupted by taking damage, using a breath weapon, asphyxiating, eating, drinking, or chanting something else.

- Heavy Blow: The next attack in 2 seconds launches a vicious strike, guaranteeing a critical hit. If the target parries the blow, their parrying hand will be disabled for 1.5 seconds. Recharges after 3 attacks, costs 1 might and 2 spirit. Offensive bonuses are not applied if the target has Iron Guard active.
Vital Strike: This attack's crit damage is increased by 40%.
Silencer: Passive. When launching an unaware attack, force a critical, bind both hands, and silence the target for 3 seconds. A silenced target cannot call for help or cast skills that require chanting. Cooldown lengthened to 5 hits, automatically refreshes if you kill a silenced enemy.
Poise: Duration increased to 3 seconds, parries are free until this skill expires or an attack is launched.
Stagger: Adds up to 1.3 seconds of slowness and 5 posture damage depending on how slow your weapon is.
Vault: +2 range for the attack and dash backwards slightly.
Shatter: Bind time and crit damage multiplier scales with combo.
- Iron Guard: Brace yourself for impact, causing the next melee parry in 1.5 seconds (no time restriction with manual parry) to reflect all posture damage back onto the attacker. Recharge after 5 parries, costs 1 spirit. Offensive bonuses are not applied if the melee attacker had Heavy Blow active.
Return to Sender: If the attack was ranged, the projectile reflected back at its shooter with full force.
Backpedal: Instead of reflecting posture damage, all posture damage taken is converted to knockback.
Bind: The attacker's attacking hand will be bound for 2 seconds on parry.
Mikiri: Iron guard automatically triggers, its cooldown is increased to 6 parries.
Overpower: Additionally consume posture from the attacker as if attacking with the parrying weapon.
Recovery: Your posture and spirit cooldowns are reset upon parry.
- Coup de Grace: When toggled, all damage dealt to targets with less than 20% health (including wounding) will be converted to posture. An attack on a staggered target under 20% will instantly kill. Cast when active to end this skill. Recharge after 3 skill casts.
Glory Kill: The killing blow will grant 2 might.
Rupture: Absorb half the target's spirit and allow the other half to violently escape in a large explosion. The size of the explosion increases with the target's max posture, and its damage increases with the target's spirit. [stats] Explosion radius: max posture/2 Explosion damage: spirit *2
Reinvigorate: Kills will additionally heal 1 wounding, 1 fatigue, and 1 burnout.
Reaping: Activate to gain omnidirectional sweep. This attack cannot be parried, targets below ((5% max health)+(weapon damage)*2) will take true damage, and this skill instantly refreshes if a target dies in this manner. Cooldown +3.
Frenzy: All physical skills instantly cool down on a kill.
Danse Macabre: The percentage at which an entity is eligible for an instant kill scales with your combo, up to 40%.
- Kick: The next attack in 2 seconds only has a range of 3 blocks, but deals 4 unblockable posture damage. Recharge after 4 attacks, costs 3 spirit.
Roundhouse: Additionally deals 3 points of falling block damage.
Trip: Usable only when both you and your target are grounded. Reduce target posture regeneration by 60% for an amount of time equal to double its posture cooldown. Cooldown -1.
Tackle: +2 range on kick and dash forward.
Low Sweep: Kicks sweep a 90 degree area in front of you.
Iron Knee: All knockback from the attack is converted into additional posture damage.
Backflip: Use the reaction force to leap back and recover 30% lost posture, adds 0.4 combo.
- Grapple: Landing two unarmed strikes on the same target in 3 seconds after casting this skill will grab and throw them to the ground for 8 true posture damage. Recharge after 7 attacks, costs 2 spirit.
Throw: Dealt posture damage increased to 11.
Suplex: Grab the target and brutally slam them behind you, dealing double of your current posture as true posture damage to the target, but also consuming almost all of your own posture.
Submission: Grapple triggers on first strike and has its cooldown reduced by 2 if you have more armor than your target.
Reversal: A successful grapple instead swaps the posture of the attacker and target by percentage.
Clinch: The hand performing the first unarmed strike will bind the same hand on the target. If both hands are unarmed, both of the target's hands will be simultaneously bound.
- Shield Bash: In the next 2 seconds, your shield will behave as a range 3 blunt weapon once, dealing additional posture damage equivalent to its shield time. Recharge in 4 parries, costs 2 spirit.
Determination: Upon cast, ready your equipped shields if they are currently bound.
Rim Punch: Smash the shield's rim into the target, nauseating them for 2 seconds and inflicting additional knockback.
Overbear: Put all your weight into the attack, dealing extra damage equal to double your shield time and doubling posture damage. If this does not stagger the target, slows you for 2.5 seconds.
Foot Slam: Slam the shield into the enemy's foot, slowing and distracting them for 3 seconds.
Berserk: Upon cast and successful attack with shield, recharge unbound hands and increase your attack speed by 30% for 2.5 seconds.
Arm Lock: When active, parrying will bind the attacker's attacking hands for 1.5 seconds.
- Meditate/War Cry: The passive will function until the skill enters cooldown. If cast at full posture, no might, and full spirit, this skill will simulate a full night's sleep after five seconds of not taking damage, during which time you are immobile; otherwise, you gain a series of buffs. Recharge after 5 minutes or sleeping in a bed.
Rejuvenate: Passive: +50% healing. Active: gain the buffs of a golden apple.
Wind Scar: Passive: +1 block reach. Active: gain omnidirectional sweeping for 10 seconds.
Timberfall: Passive: deal additional posture damage equal to 5x the might generated on attack.  Active: guarantee crit and +40% dealt melee damage for 10 seconds.
Frost Fang: Passive: +2 luck.  Active: for 20 seconds, gain 40% speed and slow enemies for 3 seconds on hit; if they are alert, they are additionally blinded for 1 second.
Flame Dance: Passive: attack speed increases by 0.1 for every level of combo.  Active: for 20 seconds, every third hit will crit for an additional 1.5x damage and cause your next parry to be free.
Boulder Brace: Passive: halved posture cooldown time.  Active: restore all posture; for 10 seconds, regenerate 1 posture per second regardless of cooldown.
- Feint: become immune to feint until distracted or unaware towards caster. Recharge in 3 attacks, costs 4 spirit.
Followup: After the feint, your attacking hand instantly recharges.
Upper Hand: Recover an equivalent amount of posture.
Smirking Shadow: Teleport 2 blocks behind the target after the attack.
Scorpion Sting: This attack will trigger effects that apply on damage if it is not parried.
Last Surprise: The target is distracted for 3 seconds.
Capricious Strike: No affliction, skill cooldown lengthened to 6 attacks.
- Matthew Effect: The strong grow stronger, and the weak grow weaker.
Crown Champion: Gain 5% extra attack damage per level of might.
Vengeful Might: Gain 50% of damage received as might, your attacker will be highlighted for 5 seconds.
Prideful Might: Might gain is tripled, but all might is lost when receiving damage. After gaining 3 levels of might, your shatter will instantly cool down.
Hidden Might: Gain 0.5 might from an unaware attack. The distance at which you are detected is reduced by 30%; for every level of might you gain, 3% of this detection is converted to speed.
Elemental Might: Attacking a target causes it to take 1 more damage from non-physical sources for the next 5 seconds; gain 1 might when an entity in 5 blocks dies from non-physical damage.
- Execution: Monsters have floor(ln(max health)-1) lives. For instance, a zombie (20 health) has 1 life, an enderman (40 health) has 2, and the Wither (300 health) has 3. Players always have 3 lives. Requires 8 or more might to activate. Consumes 1 might per second, converts all dealt melee damage to posture, and empties your might to take one life's worth of health from a staggered target, ending their stagger instantly. This skill ends if you reach 0 might.
Endless Might: Only requires 5 might to activate.
Onslaught: No might drain over time, but might decay has no cooldown. Your variation of heavy blow (if one is equipped) will continuously be cooled and cast, and all skills will refund their spirit costs on cast. Execution ends when you are under 1 might.
Master's Lesson: Cannot deal damage to staggered targets. Stagger is lengthened to 10 seconds, but stagger count is reduced to 1.
Flare: take 2 lives in damage, then cause the target to regenerate 1.4 lives over 5 seconds.
Lichtenberg Scar: Accumulates damage dealt during this period as charge. Executing a target fully drains spirit to summon lightning above all targets in a small radius. Radius: 2(+charge/5), Lightning damage: 5+(charge*spirit remaining), distributed over all targets
Crowd Pleaser: After taking a life, allies within 10 blocks gain buffs for half the duration of the life's worth in health. For every skill cast during execution, every existing buff increases in potency, and an additional buff is applied. Speed is applied first, then luck, strength, regeneration, and finally resistance (capped at resistance III).
- Memento Mori: Tonight, we dine in hell.
Bloodlust: For every 1% health lost (including wounding), you gain 1% extra might and 0.5% extra damage.
Rapid Clotting: Gain (lost health percentage*20) armor.
Panic: When taking damage, create a smoke cloud that blinds all enemies in 10 blocks whose distance to you exceeds (current health percentage*10) blocks.
Death Denial: Upon taking fatal damage, become immune to all damage and healing for 5 seconds. Recharges after sleeping.
Saving Throw: Additionally gain 0.1 luck per 1% lost health.
Pound of Flesh: Active skill, consumes all your spirit. When attacking, consume 5% max health to deal 5% of the target's max health, or 10% of the target's max posture if the attack was parried. Lasts until (2*spirit) seconds have elapsed or your spirit fully regenerates.
- Descend: Can only be cast when standing on the ground. Until you touch the ground again, your next attack will additionally deal (fall distance*2) posture damage; performing said attack will negate fall damage. Costs 4 spirit and recharges after 6 attacks, with distracted attacks counting as 2 and unaware attacks counting as 4 attacks.
Drop Bear: Additionally deals (fall distance) falling block damage.
Phantom Dive: Can be cast while airborne. Every 7 blocks fallen upgrades the attack's ambush tier (alert->distracted->unaware); every upgrade beyond unaware paralyzes the target for for 3 seconds.
Lights Out: If the target is staggered by this attack, it will be blinded and stagger time will be lengthened to 10 seconds, but stagger count will be set to 1.
Shockwave: No effects on attack. Landing will create a shockwave that throws all entities within 5 blocks upwards slightly, deals (fall distance/2) posture damage and 1 falling block damage, and slows their posture regeneration by 40% for 2 seconds after posture cooldown has elapsed, negating fall damage.
Titanfall: Consumes all of your own posture and deal it as additional damage to the target, increased by 4% per armor point you have.
- Hex: In the next 3 seconds, your next attack will deal no damage but afflict an effect if it is not parried. Costs 4 spirit and cools down in 15 seconds.
Curse of Misfortune: The target's luck is reduced by 2 for 10 seconds.
Itchy Curse: Afflict the target with paresthesia. Paresthesia lasts 3 seconds, but only ticks down if the target is not moving. The target is treated as distracted, and will be immobilized with both its hands bound every 3 seconds.
Snakebite: Inflict poison II for 10 seconds, taking the higher value for both duration and potency if the target already has poison active, and renders it incurable by milk. The target cannot heal as long as the poison effect remains active.
Unravel: This effect cannot be parried. Violently remove one level from every potion effect curable by milk on the target, creating an explosion. Explosion size: average duration of effects/5, capped at 10 blocks. Explosion damage: +4*(former effect level) per effect dispelled, +2*(effect level) for every effect not dispelled.
Black Mark: Saps the target of its vitality. For 10 seconds, any creature that deals damage to the target will heal for 1/3 the damage dealt and drain either 2 posture or 3 spirit, whichever the attacker has less of, recovering half that amount.
Stiffen: Converts absorption, deflection, and 30% armor into an equivalent amount of shatter.
- Morale: Regain spirit when performing certain combat actions.
Back and Forth: Regain (1/attack speed) spirit when parrying or landing a critical hit.
Archer's Paradox: Regain 1 spirit when your projectile hits a target or when you attack a non-alert target. This ability has a 1.5 second cooldown between triggers.
Apathy: You have 4 base spirit, your spirit instantly regenerates after cooldown, you are immune to burnout. Burnout accumulated will be applied if you remove this passive.
Natural Sprinter: You are very dangerous at short distances. Your max spirit is doubled, but its regeneration speed is reduced to a third; you recover 3 spirit on a kill.
Speed Demon: Halve spirit cooldown on a dodge and regain spirit on attack based on speed. The faster you are compared to your target, the more spirit is regenerated, capped at 1.5.
Lady Luck: When casting a skill there is a (1+luck)/(5+luck) chance (to a minimum of 20%) to instantly refund the spirit cost of the skill. This chance stacks if it fails to trigger.
