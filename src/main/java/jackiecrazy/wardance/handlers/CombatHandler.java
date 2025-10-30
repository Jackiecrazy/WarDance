package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.capability.timeslow.TimeSlowData;
import jackiecrazy.footwork.capability.weaponry.CombatManipulator;
import jackiecrazy.footwork.event.DamageKnockbackEvent;
import jackiecrazy.footwork.event.MeleeKnockbackEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.config.*;
import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.event.ProjectileDefendEvent;
import jackiecrazy.wardance.mixin.ProjectileImpactMixin;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class CombatHandler {

    private static final UUID uuid = UUID.fromString("98c361c7-de32-4f40-b129-d7752bac3712");
    private static final UUID uuid2 = UUID.fromString("98c361c8-de32-4f40-b129-d7752bac3722");

    @SubscribeEvent
    public static void mohistWhy(AttackEntityEvent e) {
        float cd = e.getEntity().getAttackStrengthScale(0.5f);
        CombatData.getCap(e.getEntity()).tickProc("swing", cd);
    }

    /**
     * bound entities cannot use that specific hand
     */
    @SubscribeEvent
    public static void mudamudamuda(LivingEntityUseItemEvent e) {
        InteractionHand h = e.getEntity().getMainHandItem() == e.getItem() ? InteractionHand.MAIN_HAND : e.getEntity().getOffhandItem() == e.getItem() ? InteractionHand.OFF_HAND : null;
        if (h != null && CombatData.getCap(e.getEntity()).getHandBind(h) > 0) {
            if (e.isCancelable())
                e.setCanceled(true);
            e.setDuration(-1);
        }
    }

    @SubscribeEvent
    public static void swapItemFreshness(LivingEquipmentChangeEvent e) {
        if (e.getSlot() == EquipmentSlot.MAINHAND || e.getSlot() == EquipmentSlot.OFFHAND)
            StylishData.getCap(e.getEntity()).addCombo(0.1f, "swap");
    }

    @SubscribeEvent
    public static void projectileParry(final ProjectileImpactEvent e) {
        Entity projectile = e.getEntity();
        if (e.getRayTraceResult().getType() == HitResult.Type.ENTITY && e.getRayTraceResult() instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity uke) {
            //stealth shot
            if (StealthUtils.INSTANCE.getAwareness(null, uke) != StealthUtils.Awareness.ALERT) {
                return;
            }

            //iframes
            if (CombatData.getCap(uke).isIframe()) {
                e.setCanceled(true);
                return;
            }

            //nothing applies if you shot it
            if (projectile instanceof Projectile pro && pro.getOwner() instanceof LivingEntity shooter) {
                //don't defend against yourself
                if (shooter == uke) return;
            }

            //dodged
            if (CombatData.getCap(uke).isDodging()) {
                e.setCanceled(true);//.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                CombatUtils.onSuccessfulDodge(uke, projectile);
                return;
            }

            //add ranged combo and finisher
            if (projectile instanceof Projectile pro && pro.getOwner() instanceof LivingEntity shooter) {
                StylishData.getCap(shooter).addCombo(0.2f, "projectile");
                StylishData.getCap(shooter).processAttack(false);
            }
            //defer to vanilla, no longer correct as new blocking directly alters isBlocking
            //if (uke.isBlocking()) return;
            //refuse to handle piercing arrows to prevent oddity
            if (e.getEntity() instanceof AbstractArrow aa && aa.getPierceLevel() > 0) {
                return;
            }
            //hard no go
            if (!PermissionData.getCap(uke).canParry()) {
                return;
            }
            float consume = CombatConfig.posturePerProjectile;
            ICombatCapability ukeCap = CombatData.getCap(uke);

            //find defending hands
            ItemStack defend = null;
            InteractionHand defendingHand = null;
            float defMult = 1;
            //find the preferred range defend tool
            boolean offChip = CombatUtils.canBlock(uke, e.getEntity(), uke.getOffhandItem(), consume);
            boolean mainChip = CombatUtils.canBlock(uke, e.getEntity(), uke.getMainHandItem(), consume);
            float offDefMult = CombatUtils.getPostureDef(null, uke, uke.getOffhandItem(), consume);
            float mainDefMult = CombatUtils.getPostureDef(null, uke, uke.getMainHandItem(), consume);
            if (offChip) {
                defend = uke.getOffhandItem();
                defendingHand = InteractionHand.OFF_HAND;
                defMult = offDefMult;
            }
            //this makes blocking prioritize offhand
            if (mainChip && (!offChip || mainDefMult < defMult)) {
                defend = uke.getMainHandItem();
                defendingHand = InteractionHand.MAIN_HAND;
                defMult = mainDefMult;
            }

            //mobs cannot parry so we resolve parry first
            ProjectileDefendEvent.Parry pe1 = new ProjectileDefendEvent.Parry(uke, projectile, defendingHand == null ? InteractionHand.OFF_HAND : defendingHand, defend == null ? ItemStack.EMPTY : defend, defMult);
            MinecraftForge.EVENT_BUS.post(pe1);

            //successful
            if (pe1.getResult() == Event.Result.ALLOW || (ukeCap.isParrying() && pe1.getResult() == Event.Result.DEFAULT)) {
                CombatUtils.onSuccessfulParry(uke, projectile, defendingHand, defend, pe1.getPostureConsumption());
                handleProjectileDefense(e, pe1, defend, projectile, uke);
                return;
            }

            //only for mobs
            boolean inBlockArea = GeneralUtils.isFacingEntity(uke, projectile, 90, 140);
            boolean force = false;

            //mob natural blocking
            MobSpecs.MobInfo stats = MobSpecs.getMobInfo(uke);
            if (stats != null) {
                if (stats.isShield() && WarDance.rand.nextFloat() < stats.getBlockChance()) {
                    if (stats.getBlockMult() < 0) {//cannot parry
                        defend = null;
                        defMult = (float) -stats.getBlockMult();
                    } else if (stats.isOmnidirectional() || inBlockArea) {
                        if (!inBlockArea) {
                            defendingHand = InteractionHand.OFF_HAND;
                        }
                        defend = ItemStack.EMPTY;
                        defMult = (float) Math.min(stats.getBlockMult(), defMult);
                        force = true;
                    }
                }
            }

            //block event
            ProjectileDefendEvent.Block pe2 = new ProjectileDefendEvent.Block(uke, projectile, defendingHand, defend, defMult);
            if (force)
                pe2.setResult(Event.Result.ALLOW);
            MinecraftForge.EVENT_BUS.post(pe2);

            //successful
            if (pe2.getResult() == Event.Result.ALLOW || (defend != null && pe2.getResult() == Event.Result.DEFAULT && ukeCap.isBlocking())) {
                CombatUtils.onSuccessfulBlock(uke, projectile, defendingHand, defend, pe2.getPostureConsumption());
                handleProjectileDefense(e, pe2, defend, projectile, uke);
            }

        }
    }

    private static void handleProjectileDefense(ProjectileImpactEvent e,
                                                ProjectileDefendEvent pe,
                                                ItemStack defend,
                                                Entity projectile,
                                                LivingEntity uke) {
        e.setCanceled(true);//.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
        ICombatCapability ukeCap = CombatData.getCap(uke);
        ukeCap.consumePosture(null, pe.getPostureConsumption(), false, 1);//fixme
        //do not change shooter! It makes drowned tridents and skeleton arrows collectable, which is honestly silly
        uke.level().playSound(null, uke.getX(), uke.getY(), uke.getZ(), SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
        if (pe.doesTrigger()) {
            if (uke.isEffectiveAi()) {
                //I am not proud of this.
                Marker dummy = new Marker(EntityType.MARKER, uke.level());
                dummy.teleportTo(projectile.getX(), projectile.getY(), projectile.getZ());
                uke.level().addFreshEntity(dummy);
                if (projectile instanceof Projectile) {
                    HitResult rtr = new EntityHitResult(dummy);
                    ((ProjectileImpactMixin) projectile).callOnHit(rtr);
                }
                dummy.discard();
            }
        } else if (pe.getReturnVec() != null) {
            projectile.setDeltaMovement(pe.getReturnVec().x, pe.getReturnVec().y, pe.getReturnVec().z);
            if (projectile instanceof Projectile) {
                double power = pe.getReturnVec().x / pe.getReturnVec().normalize().x;
                ((Projectile) projectile).shoot(pe.getReturnVec().x, pe.getReturnVec().y, pe.getReturnVec().z, (float) power, 0);
            }
        } else projectile.remove(Entity.RemovalReason.KILLED);
        CombatUtils.knockBack(uke, projectile, 0.01f, true, false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancel(final LivingAttackEvent e) {
        if (GeneralConfig.debug && !e.getEntity().level().isClientSide) {
            WarDance.LOGGER.debug("attack from " + e.getSource() + " started with amount " + e.getAmount());
        }
        if (!e.getEntity().level().isClientSide && e.getSource() != null && DamageUtils.isPhysicalAttack(e.getSource())) {
            LivingEntity uke = e.getEntity();
            ICombatCapability ukeCap = CombatData.getCap(uke);

            //iframing and knocked down people are immune to damage
            if (ukeCap.isIframe() || ukeCap.isKnockdown()) {
                e.setCanceled(true);
                return;
            }

            //dodged!
            if (ukeCap.isDodging()) {
                CombatUtils.onSuccessfulDodge(uke, e.getSource().getDirectEntity());
                e.setCanceled(true);
                return;
            }

            ItemStack attack = CombatUtils.getAttackingItemStack(e.getSource());
            if (DamageUtils.isMeleeAttack(e.getSource()) && e.getSource().getEntity() instanceof LivingEntity && attack != null && e.getAmount() > 0) {
                LivingEntity seme = (LivingEntity) e.getSource().getEntity();
                ICombatCapability semeCap = CombatData.getCap(seme);
                //update values first? Is this necessary?
                //ukeCap.serverTick();
                //semeCap.serverTick();
                InteractionHand h = semeCap.isOffhandAttack() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                //hand bound or staggered, no attack
                if (semeCap.isStunned() || semeCap.getHandBind(InteractionHand.MAIN_HAND) > 0) {
                    e.setCanceled(true);
                    return;
                }
                //footwork code for combat manipulation
                if (seme.getMainHandItem().getCapability(CombatManipulator.CAP).resolve().isPresent()) {
                    e.setCanceled(seme.getMainHandItem().getCapability(CombatManipulator.CAP).resolve().get().canAttack(e.getSource(), seme, uke, seme.getMainHandItem(), e.getAmount()));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)//because compat with Better Hurt Timer...
    public static void parry(final LivingAttackEvent e) {
        if (GeneralConfig.debug && !e.getEntity().level().isClientSide) {
            WarDance.LOGGER.debug("attack source " + e.getSource() + " sent to hurt check with amount " + e.getAmount());
        }
        //if physical attack with source
        if (!e.getEntity().level().isClientSide && e.getSource() != null && DamageUtils.isPhysicalAttack(e.getSource())) {
            LivingEntity uke = e.getEntity();

            if (CombatData.getCap(uke).isDodging() || CombatData.getCap(uke).isIframe()) {
                //iframe cancel, this should never happen as it's already handled above
                e.setCanceled(true);
                return;
            }
            ICombatCapability ukeCap = CombatData.getCap(uke);
            ItemStack attack = CombatUtils.getAttackingItemStack(e.getSource());
            //melee attack from an entity source over 0
            if (DamageUtils.isMeleeAttack(e.getSource()) && e.getSource().getEntity() instanceof LivingEntity seme && attack != null && e.getAmount() > 0) {

                if (seme.getType().getDescriptionId().equals("entity.evilcraft.vengeance_spirit")) {
                    //makes the world lag plus how do you parry a ghost
                    return;
                }
                ICombatCapability semeCap = CombatData.getCap(seme);
                InteractionHand attackingHand = InteractionHand.MAIN_HAND;//semeCap.isOffhandAttack() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

                //hand bound or staggered, no attack
                if (semeCap.isStunned() || semeCap.getHandBind(attackingHand) > 0) {
                    e.setCanceled(true);
                    return;
                }

                //handle capability and any on-hit effects, todo revamp to action based system
                seme.getMainHandItem().getCapability(CombatManipulator.CAP).ifPresent((i) -> i.attackStart(e.getSource(), seme, uke, seme.getMainHandItem(), e.getAmount()));
                final WeaponStats.SweepInfo sweepInfo = WeaponStats.getSweepInfo(seme.getMainHandItem(), CombatUtils.getSweepState(seme));
                sweepInfo.performCommand(seme, true, false);
                sweepInfo.performCommand(uke, false, false);
                if (e.getSource() instanceof CombatDamageSource cds && WeaponStats.lookupStats(seme.getMainHandItem()) != null) {
                    cds.setKnockbackPercentage((float) sweepInfo.getKnockback());
                    cds.setCrit(sweepInfo.isCrit());
                    cds.setCritDamage((float) sweepInfo.getCritDamage());
                }

                //add stats if it's the first attack this tick and cooldown is sufficient
                if (!semeCap.alreadyProc("attack") && !semeCap.alreadyProc("oncePerSweep")) {//first hit of a sweep attack this tick, add combo based on state
                    //semeCap.addRank(0.1f);
                    StylishData.getCap(seme).processAttack(true);
                    StylishData.getCap(seme).addCombo(0.2f, semeCap.isOffhandAttack() + CombatUtils.getSweepState(seme).name());
                    semeCap.tickProc("attack");
                }

                //blocking, no longer useful due to me directly interfacing with block
//                if (uke.isBlocking()) {
//                    ukeCap.consumePosture(0);
//                    return;
//                }

                //stunned, add extra finisher points
                if (ukeCap.isStunned()) {
                    //add extra finisher charge to attacker
                    if (!semeCap.alreadyProc("stunTrigger")) {
                        StylishData.getCap(seme).addTriggerBar(1);
                        semeCap.tickProc("stunTrigger");
                    }
                }

                //posture consumption code start, grab attack multiplier
                float atkMult = CombatUtils.getPostureAtk(seme, seme, attackingHand, e.getSource(), e.getAmount(), attack);
                //store atkMult at this stage for event
                float original = atkMult;
                //stabby bonus
                StealthUtils.Awareness awareness = StealthUtils.INSTANCE.getAwareness(seme, uke);
                //whether the attack can stun someone at 0 posture
                boolean canBreach = true;
                //crit bonus
                if (e.getSource() instanceof CombatDamageSource cds) {
                    if (cds.isCrit()) atkMult *= cds.getCritDamage();
                    canBreach = cds.canBreach();
                }
                canBreach |= sweepInfo.canBreach();
                canBreach |= semeCap.alreadyProc("canBreach");

                MeleePostureEvent.Pre pe = new MeleePostureEvent.Pre(uke, seme, attackingHand, attack, atkMult, original, e.getSource(), e.getAmount(), canBreach);
                MinecraftForge.EVENT_BUS.post(pe);
                //strictly speaking the "original" that the defender receives starts here, so update the "original" value
                atkMult = original = pe.getPostureConsumption();
                canBreach = pe.canBreach();

                //it's a trap! no parrying backstabs
                if (awareness == StealthUtils.Awareness.UNAWARE) {
                    ukeCap.consumePosture(seme, pe.getPostureConsumption(), pe.canBreach(), 0);
                    return;
                }

                //not only can mobs not defend in time slow, the attacker gets a steve time extension
                if (TimeSlowData.getCap(uke).getEffectiveSpeed() < 1) {
                    ukeCap.consumePosture(seme, pe.getPostureConsumption(), pe.canBreach(), 0);
                    //CombatUtils.triggerSteveTime(seme, (int) (TimeSlowData.getCap(uke).getTimeRemaining() * 1.5));
                    return;
                }

                //find defending hands
                ItemStack defend = null;
                InteractionHand defendingHand = null;
                float defMult = 1;

                //find the preferred defend tool
                boolean offChip = CombatUtils.canBlock(uke, seme, uke.getOffhandItem(), attack, atkMult);
                boolean mainChip = CombatUtils.canBlock(uke, seme, uke.getMainHandItem(), attack, atkMult);
                float offDefMult = CombatUtils.getPostureDef(seme, uke, uke.getOffhandItem(), atkMult);
                float mainDefMult = CombatUtils.getPostureDef(seme, uke, uke.getMainHandItem(), atkMult);
                if (offChip) {
                    defend = uke.getOffhandItem();
                    defendingHand = InteractionHand.OFF_HAND;
                    defMult = offDefMult;
                }
                //this makes blocking prioritize offhand
                if (mainChip && (!offChip || mainDefMult < defMult)) {
                    defend = uke.getMainHandItem();
                    defendingHand = InteractionHand.MAIN_HAND;
                    defMult = mainDefMult;
                }

                //players block if they are... blocking
                boolean defenderMaybeBlocking = uke instanceof Player && uke.isBlocking();
                //mobs can only guard, by being in the right angle
                boolean defenderMaybeGuarding = GeneralUtils.isFacingEntity(uke, seme, 90, 140);

                //special mob blocking overrides
                MobSpecs.MobInfo stats = MobSpecs.getMobInfo(uke);
                if (!ukeCap.isStunned() && atkMult >= 0 && awareness != StealthUtils.Awareness.UNAWARE && stats != null) {
                    if (WarDance.rand.nextFloat() < stats.getBlockChance()) {
                        if (stats.getBlockMult() < 0) {//cannot parry
                            defend = null;
                            defenderMaybeBlocking = false;
                            defMult = (float) -stats.getBlockMult();
                        } else if (stats.isOmnidirectional() || defenderMaybeBlocking) {
                            if (defMult > stats.getBlockMult()) {
                                if (!defenderMaybeBlocking) {
                                    defendingHand = CombatUtils.getCooledAttackStrength(uke, InteractionHand.MAIN_HAND, 0.5f) > CombatUtils.getCooledAttackStrength(uke, InteractionHand.OFF_HAND, 0.5f) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                                }
                                defend = ItemStack.EMPTY;
                                defMult = (float) Math.min(stats.getBlockMult(), defMult);
                                defenderMaybeBlocking = true;
                            }
                        }
                    }
                }

                //accounting for negative posture damage, used to mark an item as ignoring parries
                float finalPostureConsumption = Math.abs(atkMult * defMult);

                //updating this quickly, it's basically the above without crit and stab multipliers, which were necessary for calculating canParry so they couldn't be eliminated cleanly...
                float originalPostureConsumption = Math.abs(original * defMult);

                //begin parry resolution
                MeleePostureEvent.Defense.Parry pe1 = new MeleePostureEvent.Defense.Parry(uke, seme, ukeCap.isParrying(), attackingHand, attack, defendingHand, defend, finalPostureConsumption, originalPostureConsumption, e.getSource(), e.getAmount(), canBreach);
                MinecraftForge.EVENT_BUS.post(pe1);

                //success!
                if (pe1.success()) {
                    e.setCanceled(true);
                    WarDance.LOGGER.debug("successfully parried!");
                    CombatUtils.onSuccessfulParry(uke, seme, defendingHand, defend, pe1.getPostureConsumption());
                    return;
                }

                //begin block resolution
                MeleePostureEvent.Defense.Block pe2 = new MeleePostureEvent.Defense.Block(uke, seme, (defenderMaybeBlocking && defend != null), attackingHand, attack, defendingHand, defend, finalPostureConsumption, originalPostureConsumption, e.getSource(), e.getAmount(), canBreach);
                MinecraftForge.EVENT_BUS.post(pe2);

                //success!
                if (pe2.success() && ukeCap.consumePosture(seme, pe2.getPostureConsumption(), pe2.canBreach(), Math.max(0, 1 - defMult / 2)) == 0) {//todo config rally value
                    e.setCanceled(true);
                    WarDance.LOGGER.debug("successfully blocked!");
                    CombatUtils.onSuccessfulBlock(uke, seme, defendingHand, defend, pe2.getPostureConsumption());
                    //do not cancel the event. It technically succeeded but will be blocked by vanilla functions. I just mark the right item to keep processing.
                    return;
                }

                //last chance, idle guard resolution
                MeleePostureEvent.Defense.Guard pe3 = new MeleePostureEvent.Defense.Guard(uke, seme, (defenderMaybeGuarding && uke instanceof Player && defend != null), attackingHand, attack, defendingHand, defend, finalPostureConsumption, originalPostureConsumption, e.getSource(), e.getAmount(), canBreach);
                MinecraftForge.EVENT_BUS.post(pe3);

                //success!
                if (pe3.success() && ukeCap.consumePosture(seme, pe3.getPostureConsumption(), pe3.canBreach(), 0) == 0) {
                    e.setCanceled(true);
                    WarDance.LOGGER.debug("successfully idle guarded!");
                    CombatUtils.onIdleGuard(uke, seme, defendingHand, defend, pe3.getPostureConsumption());
                    return;
                }

                //failed everything, use the original damage and reset rally
                WarDance.LOGGER.debug("failed everything! " + defenderMaybeBlocking + " " + defend);
                if (!pe2.success())
                    ukeCap.consumePosture(seme, pe.getPostureConsumption(), pe.canBreach(), 0f);
                //internally enforced hand bind to bypass slimes
                //added to world check to bypass goety lichdom weirdness
                //removed for sanity
//                if (!(seme instanceof Player) && uke.isAddedToWorld()) {
//                    semeCap.setHandBind(attackingHand, CombatUtils.getCooldownPeriod(seme, attackingHand) + 1);
//                }
            }
        } else {
            //parry nukes and the earth
            if (e.getSource().is(DamageTypeTags.IS_FALL) || e.getSource().is(DamageTypeTags.IS_EXPLOSION) || e.getSource().is(DamageTypeTags.IS_LIGHTNING)) {
                MeleePostureEvent.Environment pe1 = new MeleePostureEvent.Environment(e.getEntity(), CombatData.getCap(e.getEntity()).isParrying(), e.getAmount(), e.getSource(), e.getAmount(), true);
                MinecraftForge.EVENT_BUS.post(pe1);
                if (pe1.success())
                    CombatUtils.onSuccessfulParry(e.getEntity(), null, null, null, pe1.getPostureConsumption());
            }
            //handle nonphysical cases of combat damage docking posture, this can never breach
            if (e.getSource() instanceof CombatDamageSource cds && cds.getPostureDamage() > 0) {
                CombatData.getCap(e.getEntity()).consumePosture(cds.getEntity() instanceof LivingEntity elb ? elb : null, cds.getPostureDamage(), cds.canBreach(), 0.5f);//todo conversion percentages
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void critHooks(CriticalHitEvent e) {
        if (!e.getEntity().level().isClientSide && e.getTarget() instanceof LivingEntity uke) {
            LivingEntity seme = e.getEntity();
            if (seme.getMainHandItem().getCapability(CombatManipulator.CAP).isPresent()) {
                e.setResult(seme.getMainHandItem().getCapability(CombatManipulator.CAP).resolve().get().critCheck(seme, uke, seme.getMainHandItem(), e.getOldDamageModifier(), e.isVanillaCritical()));
                e.setDamageModifier(seme.getMainHandItem().getCapability(CombatManipulator.CAP).resolve().get().critDamage(seme, uke, seme.getMainHandItem()));
            }
            if (WeaponStats.isWeapon(seme, seme.getMainHandItem())) {
                final WeaponStats.SweepInfo info = WeaponStats.getSweepInfo(seme.getMainHandItem(), CombatUtils.getSweepState(seme));
                e.setResult(info.isCrit() ? Event.Result.ALLOW : Event.Result.DENY);
                e.setDamageModifier((float) info.getCritDamage());
            }
        }
    }

    @SubscribeEvent
    public static void knockbackHooks(MeleeKnockbackEvent e) {
        if (!e.getEntity().level().isClientSide) {
            LivingEntity uke = e.getEntity();
            LivingEntity seme = e.getAttacker();
            seme.getMainHandItem().getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onKnockingBack(seme, uke, seme.getMainHandItem(), e.getOriginalStrength()));
            uke.getMainHandItem().getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onBeingKnockedBack(seme, uke, seme.getMainHandItem(), e.getOriginalStrength()));
            uke.getOffhandItem().getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onBeingKnockedBack(seme, uke, seme.getOffhandItem(), e.getOriginalStrength()));

        }
    }

    @SubscribeEvent
    public static void otherKnockbackHooks(DamageKnockbackEvent e) {
        if (e.getDamageSource() instanceof CombatDamageSource) {
            CombatDamageSource cds = (CombatDamageSource) e.getDamageSource();
            e.setStrength(e.getStrength() * cds.getKnockbackPercentage());
        }
        if (e.getStrength() < 0 && e.getDamageSource().getEntity() instanceof LivingEntity from) {
            //not handled by LivingEntity, so we have to do it ourselves
            LivingEntity to = e.getEntity();
            Vec3 distVec = to.position().add(0, to.getBbHeight() / 2, 0).vectorTo(from.position().add(0, from.getBbHeight() / 2, 0)).multiply(1, 0.5, 1).normalize();
            CombatUtils.knockBack(e.getEntity(), (float) e.getStrength(), distVec.x, distVec.y, distVec.z, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void knockKnockWhosThere(LivingKnockBackEvent e) {
        final LivingEntity entity = e.getEntity();
        ICombatCapability cap = CombatData.getCap(entity);

        //the latter three shouldn't make it here, but just to be safe
        if (cap.isStunned() || cap.isIframe() || cap.isParrying() || cap.isDodging()) {
            e.setCanceled(true);
            return;
        }
        //since knockback is ignored when mounted, it becomes extra posture instead
        if (entity.getVehicle() != null) {
            int divisor = 1;
            for (Entity ride = entity; ride != null && ride.getVehicle() != null; ride = ride.getVehicle()) {
                divisor++;
            }
            for (Entity ride = entity; ride != null && ride.getVehicle() != null; ride = ride.getVehicle()) {
                if (ride instanceof LivingEntity)
                    CombatData.getCap((LivingEntity) ride).consumePosture(e.getStrength() / divisor);
            }
        }
        e.setStrength(e.getStrength() * CombatConfig.kbNerf);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pain(LivingHurtEvent e) {
        if (GeneralConfig.debug && !e.getEntity().level().isClientSide) {
            WarDance.LOGGER.debug("damage from " + e.getSource() + " received with amount " + e.getAmount());
        }
        LivingEntity uke = e.getEntity();
        LivingEntity seme = null;
        DamageSource ds = e.getSource();
        if (ds.getDirectEntity() instanceof LivingEntity direct) {
            seme = direct;
        }
        //stuff used to exist here, moved to footwork

        ICombatCapability cap = CombatData.getCap(uke);
        // combo reduces damage
        e.setAmount(e.getAmount() / StylishData.getCap(uke).getCombo());
        StylishData.getCap(uke).resetCombo();
        StealthUtils.Awareness awareness = StealthUtils.INSTANCE.getAwareness(seme, uke);

        //weapon on hit effects
        if (ds.getEntity() instanceof LivingEntity trueSource) {
            final WeaponStats.SweepInfo sweepInfo = WeaponStats.getSweepInfo(trueSource.getMainHandItem(), CombatUtils.getSweepState(trueSource));
            sweepInfo.performCommand(trueSource, true, true);
            sweepInfo.performCommand(uke, false, true);
            double luckDiff = WarDance.rand.nextFloat() * (GeneralUtils.getAttributeValueSafe(trueSource, Attributes.LUCK)) - WarDance.rand.nextFloat() * (GeneralUtils.getAttributeValueSafe(uke, Attributes.LUCK));
            e.setAmount(e.getAmount() + (float) luckDiff * GeneralConfig.luck);

            //dock rally for being melee hit
            //cap.setRally(cap.getRally() / 2);
        }
        if (GeneralConfig.debug && !e.getEntity().level().isClientSide) {
            WarDance.LOGGER.debug("luck has been resolved, damage is now " + e.getAmount());
        }

        if (DamageUtils.isPhysicalAttack(ds)) {
            if (cap.isStunned()) {
                //this should never make it here, but just to be safe
                if (cap.isKnockdown()) e.setCanceled(true);
                else e.setAmount(e.getAmount() * CombatConfig.stunDamage);
            } else {
                //multiply damage by normal config amount
                e.setAmount(e.getAmount() * CombatConfig.normalDamage);
            }
        }
        if (GeneralConfig.debug && !e.getEntity().level().isClientSide) {
            WarDance.LOGGER.debug("darktide and config has been resolved, damage is now " + e.getAmount());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tanky(LivingDamageEvent e) {
        if (GeneralConfig.debug && !e.getEntity().level().isClientSide)
            WarDance.LOGGER.debug("damage from " + e.getSource() + " recalculated to " + e.getAmount());
        final LivingEntity uke = e.getEntity();
        //no food!
        ItemStack active = uke.getItemInHand(uke.getUsedItemHand());
        if (DamageUtils.isPhysicalAttack(e.getSource()) && CombatConfig.foodCool >= 0 && (active.getUseAnimation() == UseAnim.EAT || active.getItem().getUseAnimation(active) == UseAnim.DRINK) && uke.isUsingItem()) {
            uke.stopUsingItem();
            if (uke instanceof Player && CombatConfig.foodCool > 0) {
                ((Player) uke).getCooldowns().addCooldown(active.getItem(), CombatConfig.foodCool);
            }
        }
        //stuff used to exist here, moved to footwork
//        if (CombatData.getCap(uke).getStunTime() == 0 && CombatUtils.isPhysicalAttack(e.getSource())) {
//            if (e.getSource().getEntity() instanceof LivingEntity && StealthUtils.INSTANCE.getAwareness((LivingEntity) e.getSource().getEntity(), uke) == StealthUtils.Awareness.UNAWARE)
//                return;
//            float amount = e.getAmount();
//            //absorption
//            amount -= GeneralUtils.getAttributeValueSafe(uke, FootworkAttributes.ABSORPTION.get());
//            e.setAmount(Math.max(0, amount));
//        }
        if (e.getAmount() < 0) e.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void udedlol(LivingDamageEvent e) {
        if (GeneralConfig.debug && !e.isCanceled() && !e.getEntity().level().isClientSide)
            WarDance.LOGGER.debug("damage from " + e.getSource() + " finalized with amount " + e.getAmount());
        if (!Float.isFinite(e.getAmount()))//what
            e.setAmount(0);
        final ICombatCapability cap = CombatData.getCap(e.getEntity());

        if (DamageUtils.isPhysicalAttack(e.getSource())) {
            float darktide = e.getAmount() / 2;
            darktide *= cap.getPosturePercentage();
            //temporary, darktide
            if (cap.getMaxPosture() > 0) {
                e.setAmount(e.getAmount() - darktide);
                cap.consumePosture(e.getSource().getEntity() instanceof LivingEntity attack ? attack : null,
                                   darktide, false);
            }
        }

        //fall damage deducts posture
        if (e.getSource().is(DamageTypeTags.IS_FALL) || e.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            cap.consumePosture(null, e.getAmount(), false, 0);
        }

        //finalize knockdown, ugly fix to prevent the knocking hit from being skipped
        if (cap.alreadyProc("knockdown")) {
            cap.knockdown(e.getEntity(), (int) cap.getProc("knockdown"));
            //temporary, players lose 30% health on knockdown todo use deathblow resistance
            if (e.getEntity() instanceof Player p) {
                e.setAmount(p.getMaxHealth() * 0.3f);
            }else{
                e.setAmount(e.getAmount()+e.getEntity().getMaxHealth()/10f);
                CombatUtils.knockBack(e.getEntity(), e.getSource().getEntity(), 0.7f, true, true);
            }
            cap.tickProc("knockdown", 1);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void diepotato(LivingDeathEvent e) {
        LivingEntity elb = e.getEntity();
        CombatData.getCap(elb).setHandBind(InteractionHand.MAIN_HAND, 0);
        CombatData.getCap(elb).setHandBind(InteractionHand.OFF_HAND, 0);
        if (e.getSource().getEntity() instanceof LivingEntity killer) {
            StylishData.getCap(killer).addCombo(0.3f, "kill");
        }
    }

    @SubscribeEvent
    public static void noHealOnKnockdown(LivingHealEvent e) {
        LivingEntity elb = e.getEntity();
        if (CombatData.getCap(elb).isStunned())
            e.setCanceled(true);
    }
}
