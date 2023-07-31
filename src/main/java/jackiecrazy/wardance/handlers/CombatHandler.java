package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.weaponry.CombatManipulator;
import jackiecrazy.footwork.event.DamageKnockbackEvent;
import jackiecrazy.footwork.event.MeleeKnockbackEvent;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.config.StealthConfig;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.mixin.ProjectileImpactMixin;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class CombatHandler {

    private static final UUID uuid = UUID.fromString("98c361c7-de32-4f40-b129-d7752bac3712");
    private static final UUID uuid2 = UUID.fromString("98c361c8-de32-4f40-b129-d7752bac3722");
    public static boolean downingHit = false;

    @SubscribeEvent
    public static void mohistWhy(AttackEntityEvent e) {
        CombatData.getCap(e.getEntity()).setCachedCooldown(e.getEntity().getAttackStrengthScale(0.5f));
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
    public static void projectileParry(final ProjectileImpactEvent e) {
        Entity projectile = e.getEntity();
        //add might with internal timer
        //0.01 per tick, up to 0.3 per shot from last attack
        if (projectile instanceof Projectile && ((Projectile) projectile).getOwner() instanceof LivingEntity) {
            final LivingEntity shooter = (LivingEntity) ((Projectile) projectile).getOwner();
            CombatData.getCap(shooter).addRangedMight(e.getRayTraceResult().getType() == HitResult.Type.ENTITY);
        }
        if (e.getRayTraceResult().getType() == HitResult.Type.ENTITY && e.getRayTraceResult() instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity uke) {
            if (StealthUtils.INSTANCE.getAwareness(null, uke) != StealthUtils.Awareness.ALERT) {
                return;
            }
            //dodged
            if (MovementUtils.hasInvFrames(uke)) e.setCanceled(true);
            //defer to vanilla
            if (uke.isBlocking()) return;
            //refuse to handle piercing arrows to prevent oddity
            if (e.getEntity() instanceof AbstractArrow) {
                if (((AbstractArrow) e.getEntity()).getPierceLevel() > 0)
                    return;
            }
            float consume = CombatConfig.posturePerProjectile;
            ICombatCapability ukeCap = CombatData.getCap(uke);
            //manual parry toggle
            // why does everyone want this feature...
            boolean failManualParry = CombatConfig.parryTime > 0 && (ukeCap.getParryingTick() > uke.tickCount || ukeCap.getParryingTick() < uke.tickCount - CombatConfig.parryTime);
            failManualParry |= CombatConfig.parryTime < 0 && ukeCap.getParryingTick() == -1;
            failManualParry &= uke instanceof Player;
            ItemStack defend = null;
            InteractionHand h = null;
            float defMult = 0;
            if (CombatUtils.isShield(uke, uke.getOffhandItem()) && CombatUtils.canParry(uke, e.getEntity(), uke.getOffhandItem(), 0)) {
                defend = uke.getOffhandItem();
                defMult = CombatUtils.getPostureDef(null, uke, defend, 0);
                h = InteractionHand.OFF_HAND;
            } else if (CombatUtils.isShield(uke, uke.getMainHandItem()) && CombatUtils.canParry(uke, e.getEntity(), uke.getMainHandItem(), 0)) {
                defend = uke.getMainHandItem();
                defMult = CombatUtils.getPostureDef(null, uke, defend, 0);
                h = InteractionHand.MAIN_HAND;
            }
            StealthUtils.Awareness a = StealthUtils.Awareness.ALERT;
            if (projectile instanceof Projectile && ((Projectile) projectile).getOwner() instanceof LivingEntity) {
                //don't parry yourself
                if (((Projectile) projectile).getOwner() == uke) return;
                a = StealthUtils.INSTANCE.getAwareness((LivingEntity) ((Projectile) projectile).getOwner(), uke);
            }
            boolean canParry = GeneralUtils.isFacingEntity(uke, projectile, 90);
            boolean force = false;
            if (a != StealthUtils.Awareness.UNAWARE && CombatUtils.parryMap.containsKey(GeneralUtils.getResourceLocationFromEntity(uke))) {
                CombatUtils.MobInfo stats = CombatUtils.parryMap.get(GeneralUtils.getResourceLocationFromEntity(uke));
                if (stats.shield && WarDance.rand.nextFloat() < stats.chance) {
                    if (stats.mult < 0) {//cannot parry
                        defend = null;
                        canParry = false;
                        defMult = (float) -stats.mult;
                    } else if (stats.omnidirectional || canParry) {
                        if (!canParry) {
                            h = CombatUtils.getCooledAttackStrength(uke, InteractionHand.MAIN_HAND, 0.5f) > CombatUtils.getCooledAttackStrength(uke, InteractionHand.OFF_HAND, 0.5f) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                        }
                        defend = ItemStack.EMPTY;
                        defMult = (float) Math.min(stats.mult, defMult);
                        canParry = true;
                        force = true;
                    }
                }
            }
            ProjectileParryEvent pe = new ProjectileParryEvent(uke, projectile, h, defend, defMult);
            if (failManualParry)
                pe.setResult(Event.Result.DENY);
            if (force)
                pe.setResult(Event.Result.ALLOW);
            MinecraftForge.EVENT_BUS.post(pe);
            if (pe.getResult() == Event.Result.ALLOW || (defend != null && canParry && pe.getResult() == Event.Result.DEFAULT)) {
                e.setCanceled(true);
                //do not change shooter! It makes drowned tridents and skeleton arrows collectable, which is honestly silly
                uke.level.playSound(null, uke.getX(), uke.getY(), uke.getZ(), SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                if (pe.doesTrigger()) {
                    if (uke.isEffectiveAi()) {
                        //I am not proud of this.
                        Marker dummy = new Marker(EntityType.MARKER, uke.level);
                        dummy.teleportTo(projectile.getX(), projectile.getY(), projectile.getZ());
                        uke.level.addFreshEntity(dummy);
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
                return;
            }
            //deflection
//            if ((uke instanceof Player || WarDance.rand.nextFloat() > CombatConfig.mobDeflectChance) && GeneralUtils.isFacingEntity(uke, projectile, 120 + 2 * (int) GeneralUtils.getAttributeValueSafe(uke, FootworkAttributes.DEFLECTION.get())) && !canParry && ukeCap.doConsumePosture(consume)) {
//                e.setCanceled(true);
//                uke.level.playSound(null, uke.getX(), uke.getY(), uke.getZ(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
//                if (pe.getReturnVec() != null) {
//                    projectile.setDeltaMovement(pe.getReturnVec().x, pe.getReturnVec().y, pe.getReturnVec().z);
//                    if (projectile instanceof Projectile) {
//                        double power = pe.getReturnVec().x / pe.getReturnVec().normalize().x;
//                        ((Projectile) projectile).shoot(pe.getReturnVec().x, pe.getReturnVec().y, pe.getReturnVec().z, (float) power, 0);
//                    }
//                } else projectile.remove(Entity.RemovalReason.KILLED);
//            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancel(final LivingAttackEvent e) {
        if (GeneralConfig.debug) {
            WarDance.LOGGER.debug("attack from " + e.getSource() + " started with amount " + e.getAmount());
        }
        if (!e.getEntity().level.isClientSide && e.getSource() != null && DamageUtils.isPhysicalAttack(e.getSource())) {
            LivingEntity uke = e.getEntity();
            if (MovementUtils.hasInvFrames(uke)) {
                e.setCanceled(true);
            }
            ICombatCapability ukeCap = CombatData.getCap(uke);
            ItemStack attack = CombatUtils.getAttackingItemStack(e.getSource());
            if (DamageUtils.isMeleeAttack(e.getSource()) && e.getSource().getEntity() instanceof LivingEntity && attack != null && e.getAmount() > 0) {
                LivingEntity seme = (LivingEntity) e.getSource().getEntity();
                ICombatCapability semeCap = CombatData.getCap(seme);
                ukeCap.serverTick();
                semeCap.serverTick();
                InteractionHand h = InteractionHand.MAIN_HAND;
                //hand bound or staggered, no attack
                if (semeCap.isVulnerable() || semeCap.getHandBind(h) > 0) {
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

    @SubscribeEvent(priority = EventPriority.LOWEST)//because compat with BHT...
    public static void parry(final LivingAttackEvent e) {
        if (GeneralConfig.debug) {
            WarDance.LOGGER.debug("attack source " + e.getSource() + " sent to hurt check with amount " + e.getAmount());
        }
        //if physical attack with source
        if (!e.getEntity().level.isClientSide && e.getSource() != null && DamageUtils.isPhysicalAttack(e.getSource())) {
            LivingEntity uke = e.getEntity();
            if (MovementUtils.hasInvFrames(uke)) {
                //iframe cancel
                e.setCanceled(true);
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
                InteractionHand attackingHand = InteractionHand.MAIN_HAND;
                //hand bound or staggered, no attack
                if (semeCap.isVulnerable() || semeCap.getHandBind(attackingHand) > 0) {
                    e.setCanceled(true);
                    return;
                }
                boolean sweeping = false;
                //capability handler
                seme.getMainHandItem().getCapability(CombatManipulator.CAP).ifPresent((i) -> i.attackStart(e.getSource(), seme, uke, seme.getMainHandItem(), e.getAmount()));
                //add stats if it's the first attack this tick and cooldown is sufficient
                if (semeCap.getSweepTick() != seme.tickCount) {//first hit of a potential sweep attack
                    //semeCap.addRank(0.1f);
                    float might = CombatUtils.getAttackMight(seme, uke);
                    semeCap.addMight(might);
                    semeCap.setSweepTick(seme.tickCount);
                } else {//hitting twice in a sweep attack, disqualified from parry refund
                    sweeping = true;
                }
                //blocking, reset posture cooldown without resetting combo cooldown, bypass parry
                if (uke.isBlocking()) {
                    ukeCap.consumePosture(0);
                    return;
                }
                //manual parry toggle
                // why does everyone want this feature...
                boolean failManualParry = CombatConfig.parryTime > 0 && (ukeCap.getParryingTick() > uke.tickCount || ukeCap.getParryingTick() < uke.tickCount - CombatConfig.parryTime);
                failManualParry |= CombatConfig.parryTime < 0 && ukeCap.getParryingTick() == -1;
                failManualParry &= uke instanceof Player;
                boolean canParry = GeneralUtils.isFacingEntity(uke, seme, 90);
                //boolean useDeflect = (uke instanceof Player || WarDance.rand.nextFloat() < CombatConfig.mobDeflectChance) && GeneralUtils.isFacingEntity(uke, seme, 120 + 2 * (int) GeneralUtils.getAttributeValueSafe(uke, FootworkAttributes.DEFLECTION.get())) && !GeneralUtils.isFacingEntity(uke, seme, 120) && !canParry;
                //staggered, no parry
                if (ukeCap.isVulnerable()) {
                    downingHit = false;
                    return;
                }
                //parry code start, grab attack multiplier
                float atkMult = CombatUtils.getPostureAtk(seme, seme, attackingHand, e.getAmount(), attack);
                //store atkMult at this stage for event
                float original = atkMult;
                downingHit = true;
                //stabby bonus
                StealthUtils.Awareness awareness = StealthUtils.INSTANCE.getAwareness(seme, uke);
                //crit bonus
                if (e.getSource() instanceof CombatDamageSource && ((CombatDamageSource) e.getSource()).isCrit())
                    atkMult *= ((CombatDamageSource) e.getSource()).getCritDamage();
                //grab defending stack
                ItemStack defend = null;
                InteractionHand parryHand = null;
                if (canParry) {
                    float posMod = 1337;
                    boolean isShield = false;
                    if (CombatUtils.canParry(uke, seme, uke.getOffhandItem(), attack, atkMult)) {
                        defend = uke.getOffhandItem();
                        posMod = CombatUtils.getPostureDef(seme, uke, uke.getOffhandItem(), e.getAmount());
                        isShield = CombatUtils.isShield(uke, uke.getOffhandItem());
                        parryHand = InteractionHand.OFF_HAND;
                    }
                    if (!isShield && CombatUtils.canParry(uke, seme, uke.getMainHandItem(), attack, atkMult) && CombatUtils.getPostureDef(seme, uke, uke.getMainHandItem(), e.getAmount()) < posMod) {
                        defend = uke.getMainHandItem();
                        parryHand = InteractionHand.MAIN_HAND;
                    }
                }
                float defMult = CombatUtils.getPostureDef(seme, uke, defend, e.getAmount());
                //special mob parry overrides
                if (!ukeCap.isVulnerable() && atkMult >= 0 && awareness != StealthUtils.Awareness.UNAWARE && CombatUtils.parryMap.containsKey(GeneralUtils.getResourceLocationFromEntity(uke))) {
                    CombatUtils.MobInfo stats = CombatUtils.parryMap.get(GeneralUtils.getResourceLocationFromEntity(uke));
                    if (WarDance.rand.nextFloat() < stats.chance) {
                        if (stats.mult < 0) {//cannot parry
                            defend = null;
                            canParry = false;
                            defMult = (float) -stats.mult;
                        } else if (stats.omnidirectional || canParry) {
                            if (defMult > stats.mult) {
                                if (!canParry) {
                                    parryHand = CombatUtils.getCooledAttackStrength(uke, InteractionHand.MAIN_HAND, 0.5f) > CombatUtils.getCooledAttackStrength(uke, InteractionHand.OFF_HAND, 0.5f) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                                }
                                defend = ItemStack.EMPTY;
                                defMult = (float) Math.min(stats.mult, defMult);
                                canParry = true;
                            }
                        }
                    }
                }
                //accounting for negative posture damage, used to mark an item as ignoring parries
                float finalPostureConsumption = Math.abs(atkMult * defMult);
                //updating this quickly, it's basically the above without crit and stab multipliers, which were necessary for calculating canParry so they couldn't be eliminated cleanly...
                float originalPostureConsumption = Math.abs(original * defMult);
                ParryEvent pe = new ParryEvent(uke, seme, (canParry && defend != null), attackingHand, attack, parryHand, defend, finalPostureConsumption, originalPostureConsumption, e.getAmount());
                if (failManualParry)
                    pe.setResult(Event.Result.DENY);
                MinecraftForge.EVENT_BUS.post(pe);
                if (pe.isCanceled()) {
                    e.setCanceled(true);
                    return;
                }
                if (ukeCap.getStunTime() == 0) {
                    //overflow posture
                    float consumption = pe.getPostureConsumption();
                    float knockback = ukeCap.consumePosture(seme, consumption);
                    //no parries if stabby
                    if (StealthConfig.ignore && awareness == StealthUtils.Awareness.UNAWARE) return;
                    if (pe.canParry()) {
                        e.setCanceled(true);
                        downingHit = false;
                        ukeCap.addRank(0);
//                        if (useDeflect) {
//                            //deflect
//                            uke.level.playSound(null, uke.getX(), uke.getY(), uke.getZ(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
//                            return;
//                        }
                        //shield disabling
                        boolean disshield = false;
                        parryHand = uke.getOffhandItem() == defend ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                        //barrier has already been handled. Subsequent binding and cooldown are handled by the capability.
                        if (CombatUtils.isShield(uke, defend)) {
                            //Tuple<Integer, Float> stat = CombatUtils.getShieldStats(defend);
                            if (attack.canDisableShield(defend, uke, seme)) {
                                //shield is disabled
                                if (uke instanceof Player) {
                                    ((Player) uke).getCooldowns().addCooldown(defend.getItem(), 100);
                                } else ukeCap.setHandBind(parryHand, 100);
                                disshield = true;
                            }
                        }
                        //knockback based on posture consumed
                        double kb = Math.sqrt(atkMult) - 0.18 - (1 / Math.max(defMult, 0.1)); //this will return negative if the defmult is greater, and positive if the atkmult is greater. Larger abs val=larger difference
                        //sigmoid curve again!
                        kb = 1d / (1d + Math.exp(-kb));//this is the knockback to be applied to the defender
                        CombatUtils.knockBack(uke, seme, Math.min(uke instanceof Player ? 1.6f : 1.3f, 0.2f + (pe.getPostureConsumption() + knockback) * (float) kb * (uke instanceof Player ? 2f : 1f) / ukeCap.getMaxPosture()), true, false);
                        kb = 1 - kb;
                        CombatUtils.knockBack(seme, uke, Math.min(uke instanceof Player ? 1.6f : 1.3f, 0.1f + pe.getPostureConsumption() * (float) kb * (seme instanceof Player ? 1.7f : 1f) / semeCap.getMaxPosture()), true, false);
                        uke.level.playSound(null, uke.getX(), uke.getY(), uke.getZ(), disshield ? SoundEvents.SHIELD_BLOCK : SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                        //reset cooldown
//                        if (defMult != 0) {//shield time
//                            int ticks = (int) ((consumption + 1) * 5);//(posture consumption+1)*5 ticks of cooldown
//                            float cd = CombatUtils.getCooldownPeriod(uke, parryHand);//attack cooldown ticks
//                            if (cd > ticks)//if attack speed is lower, refund partial cooldown
//                                CombatUtils.setHandCooldownDirect(uke, parryHand, ticks, true);
//                            else//otherwise bind hand
//                                ukeCap.setHandBind(parryHand, (ticks - (int) cd));
//                        }
                        if (sweeping) {
                            CombatUtils.setHandCooldown(seme, attackingHand, 0, true);
                        } else CombatUtils.setHandCooldown(seme, attackingHand, (float) (1 - kb), true);
                        //sword on sword is 1.4, sword on shield is 1.12
                        if (defend != null) {
                            ItemStack finalDefend = defend;
                            defend.getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onParry(seme, uke, finalDefend, e.getAmount()));
                            InteractionHand other = uke.getMainHandItem() == defend ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                            ItemStack finalDefend1 = uke.getItemInHand(other);
                            finalDefend1.getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onOtherHandParry(seme, uke, finalDefend1, e.getAmount()));
                        }
                    }
                }
                //internally enforced hand bind to bypass slimes
                if (!(seme instanceof Player)) {
                    semeCap.setHandBind(attackingHand, CombatUtils.getCooldownPeriod(seme, attackingHand) + 7);
                }
            }
            //evade, at the rock bottom of the attack event, saving your protected butt.
            if (!uke.isBlocking() && !e.isCanceled()) {
                if (DamageUtils.isPhysicalAttack(e.getSource()) && StealthUtils.INSTANCE.getAwareness(e.getSource().getDirectEntity() instanceof LivingEntity ? (LivingEntity) e.getSource().getDirectEntity() : null, uke) != StealthUtils.Awareness.UNAWARE) {
                    if (CombatData.getCap(uke).consumeEvade()) {
                        e.setCanceled(true);
                        uke.level.playSound(null, uke.getX(), uke.getY(), uke.getZ(), SoundEvents.IRON_DOOR_OPEN, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
                    }
                    //otherwise the rest of the damage goes through and is handled later down the line anyway
                }
            }
        }

    }

    @SubscribeEvent
    public static void critHooks(CriticalHitEvent e) {
        if (!e.getEntity().level.isClientSide && e.getTarget() instanceof LivingEntity uke) {
            LivingEntity seme = e.getEntity();
            if (seme.getMainHandItem().getCapability(CombatManipulator.CAP).isPresent()) {
                e.setResult(seme.getMainHandItem().getCapability(CombatManipulator.CAP).resolve().get().critCheck(seme, uke, seme.getMainHandItem(), e.getOldDamageModifier(), e.isVanillaCritical()));
                e.setDamageModifier(seme.getMainHandItem().getCapability(CombatManipulator.CAP).resolve().get().critDamage(seme, uke, seme.getMainHandItem()));
            }
        }
    }

    @SubscribeEvent
    public static void knockbackHooks(MeleeKnockbackEvent e) {
        if (!e.getEntity().level.isClientSide) {
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
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void knockKnockWhosThere(LivingKnockBackEvent e) {
        final LivingEntity entity = e.getEntity();
        if (!CombatData.getCap(entity).isStaggeringStrike() && CombatData.getCap(entity).getStunTime() > 0) {
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
        if (GeneralConfig.debug) {
            WarDance.LOGGER.debug("damage from " + e.getSource() + " received with amount " + e.getAmount());
        }
        LivingEntity uke = e.getEntity();
        LivingEntity kek = null;
        DamageSource ds = e.getSource();
        if (ds.getDirectEntity() instanceof LivingEntity) {
            kek = (LivingEntity) ds.getDirectEntity();
        }
        //stuff used to exist here, moved to footwork
        ICombatCapability cap = CombatData.getCap(uke);
        cap.setSpiritGrace(ResourceConfig.spiritCD);
        cap.setAdrenalineCooldown(CombatConfig.adrenaline);
        SubtleBonusHandler.update = true;
        StealthUtils.Awareness awareness = StealthUtils.INSTANCE.getAwareness(kek, uke);
        if (ds.getEntity() instanceof LivingEntity seme) {
            if (DamageUtils.isPhysicalAttack(e.getSource())) {
                cap.setMightGrace(0);
            }
            double luckDiff = WarDance.rand.nextFloat() * (GeneralUtils.getAttributeValueSafe(seme, Attributes.LUCK)) - WarDance.rand.nextFloat() * (GeneralUtils.getAttributeValueSafe(uke, Attributes.LUCK));
            e.setAmount(e.getAmount() + (float) luckDiff * GeneralConfig.luck);
        }
        if (DamageUtils.isPhysicalAttack(ds)) {
            if ((cap.isVulnerable()) && !cap.isStaggeringStrike()) {
                //stagger tests for melee damage
                if (cap.isExposed()) {
                    e.setAmount(e.getAmount() * CombatConfig.exposeDamage);
                    if (DamageUtils.isMeleeAttack(ds)) {
                        //expose, add 10% max health damage
                        e.setAmount(e.getAmount() + uke.getMaxHealth() * 0.1f);
                        e.getSource().bypassArmor().bypassEnchantments().bypassMagic();
                        //fatality!
                        if (ds.getEntity() instanceof LivingEntity seme) {
                            if (seme.level instanceof ServerLevel) {
                                ((ServerLevel) seme.level).sendParticles(ParticleTypes.ANGRY_VILLAGER, uke.getX(), uke.getY(), uke.getZ(), 5, uke.getBbWidth(), uke.getBbHeight(), uke.getBbWidth(), 0.5f);
                            }
                            seme.level.playSound(null, uke.getX(), uke.getY(), uke.getZ(), SoundEvents.GENERIC_BIG_FALL, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
                        }
                    }
                }
                //knockdown damage multiplier
                else if (cap.isKnockedDown()) e.setAmount(e.getAmount() * CombatConfig.knockdownDamage);
                    //stun damage multiplier
                else if (cap.isStunned()) e.setAmount(e.getAmount() * CombatConfig.stunDamage);
            } else {
                //unfatality!
                e.setAmount(e.getAmount() * CombatConfig.normalDamage);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void paint(LivingHurtEvent e) {
        if (GeneralConfig.debug)
            WarDance.LOGGER.debug((e.isCanceled() ? "canceled " : "") + "damage from " + e.getSource() + " sent for armor calculations with amount " + e.getAmount());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tanky(LivingDamageEvent e) {
        if (GeneralConfig.debug)
            WarDance.LOGGER.debug("damage from " + e.getSource() + " recalculated to " + e.getAmount());
        final LivingEntity uke = e.getEntity();
        //no food!
        ItemStack active = uke.getItemInHand(uke.getUsedItemHand());
        if (DamageUtils.isPhysicalAttack(e.getSource()) && CombatConfig.foodCool >= 0 && (active.getItem().getUseAnimation(active) == UseAnim.EAT || active.getItem().getUseAnimation(active) == UseAnim.DRINK) && uke.isUsingItem()) {
            uke.releaseUsingItem();
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
        if (e.getAmount() <= 0) e.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void udedlol(LivingDamageEvent e) {
        if (GeneralConfig.debug)
            WarDance.LOGGER.debug("damage from " + e.getSource() + " finalized with amount " + e.getAmount());
        //"halo mode"
        if (GeneralConfig.test) {
            //master chief!
            if (!CombatData.getCap(e.getEntity()).isVulnerable()) {
                CombatData.getCap(e.getEntity()).consumePosture(e.getAmount());
                e.setCanceled(true);
            }
        }
        if (!Float.isFinite(e.getAmount()))//what
            e.setAmount(0);
        final ICombatCapability cap = CombatData.getCap(e.getEntity());
        if (e.getSource().isFall()) {
            //nom posture
            cap.consumePosture(e.getAmount());
        }
        if (!e.isCanceled() && DamageUtils.isMeleeAttack(e.getSource()) && !cap.isStaggeringStrike()) {
            cap.updateDefenselessStatus();
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void diepotato(LivingDeathEvent e) {
        LivingEntity elb = e.getEntity();
        LivingEntity killer = elb.getCombatTracker().getKiller();
        if (killer != null) {
            CombatData.getCap(killer).addRank(0.2f);
        }
    }
}
