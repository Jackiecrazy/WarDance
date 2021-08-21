package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.weaponry.CombatManipulator;
import jackiecrazy.wardance.capability.weaponry.ICombatItemCapability;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.config.StealthConfig;
import jackiecrazy.wardance.event.MeleeKnockbackEvent;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.server.ServerWorld;
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
        CombatData.getCap(e.getPlayer()).setCachedCooldown(e.getPlayer().getCooledAttackStrength(0.5f));
    }

    @SubscribeEvent
    public static void projectileParry(final ProjectileImpactEvent e) {
        if (e.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY && e.getRayTraceResult() instanceof EntityRayTraceResult && ((EntityRayTraceResult) e.getRayTraceResult()).getEntity() instanceof LivingEntity) {
            LivingEntity uke = (LivingEntity) ((EntityRayTraceResult) e.getRayTraceResult()).getEntity();
            if (uke.isActiveItemStackBlocking()) return;
            if (CombatUtils.getAwareness(null, uke) != CombatUtils.Awareness.ALERT) {
                return;
            }
            if (MovementUtils.hasInvFrames(uke)) e.setCanceled(true);
            if (e.getEntity() instanceof AbstractArrowEntity && ((AbstractArrowEntity) e.getEntity()).getPierceLevel() > 0)
                return;
            float consume = CombatConfig.posturePerProjectile;
            ICombatCapability ukeCap = CombatData.getCap(uke);
            final boolean failManualParry = CombatConfig.sneakParry > 0 && (ukeCap.getParryingTick() > uke.ticksExisted || ukeCap.getParryingTick() < uke.ticksExisted - CombatConfig.sneakParry);
            final boolean toggleParry = CombatConfig.sneakParry < 0 && ukeCap.getParryingTick() == -1;
            if (uke instanceof PlayerEntity && (failManualParry || toggleParry)) {
                //manual parry toggle
                // why does everyone want this feature...
                return;
            }
            boolean free = ukeCap.getShieldTime() > 0;
            ItemStack defend = null;
            Hand h = null;
            if (CombatUtils.isShield(uke, uke.getHeldItemOffhand()) && CombatUtils.canParry(uke, e.getEntity(), uke.getHeldItemOffhand(), 0)) {
                defend = uke.getHeldItemOffhand();
                h = Hand.OFF_HAND;
            } else if (CombatUtils.isShield(uke, uke.getHeldItemMainhand()) && CombatUtils.canParry(uke, e.getEntity(), uke.getHeldItemMainhand(), 0)) {
                defend = uke.getHeldItemMainhand();
                h = Hand.MAIN_HAND;
            }
            Entity projectile = e.getEntity();
            ProjectileParryEvent pe = new ProjectileParryEvent(uke, projectile, h, defend, free ? 0 : consume, projectile.getMotion().normalize().scale(-1));
            MinecraftForge.EVENT_BUS.post(pe);
            if (pe.getResult() == Event.Result.ALLOW || (defend != null && GeneralUtils.isFacingEntity(uke, projectile, 120) && ukeCap.doConsumePosture(pe.getPostureConsumption()) && pe.getResult() == Event.Result.DEFAULT)) {
                //System.out.println("the target has parried!");
                e.setCanceled(true);
//                if (projectile instanceof ProjectileEntity)
//                    ((ProjectileEntity) projectile).setShooter(uke);//makes drowned tridents and skeleton arrows collectable, which is honestly silly
                if (!free) {
                    Tuple<Integer, Integer> stat = CombatUtils.getShieldStats(defend);
                    ukeCap.setShieldTime(stat.getA());
                    ukeCap.setShieldCount(stat.getB());
                } else {
                    ukeCap.decrementShieldCount(1);
                    if (ukeCap.getShieldCount() <= 0) {
                        ukeCap.setHandBind(h, ukeCap.getShieldTime());
                    }
                }
                uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), free ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN : SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                projectile.setMotion(pe.getReturnVec().x, pe.getReturnVec().y, pe.getReturnVec().z);
                if (projectile instanceof ProjectileEntity) {
                    double power = pe.getReturnVec().x / pe.getReturnVec().normalize().x;
                    ((ProjectileEntity) projectile).shoot(pe.getReturnVec().x, pe.getReturnVec().y, pe.getReturnVec().z, (float) power, 0);
                }
                CombatUtils.knockBack(uke, projectile, 0.01f, true, false);
                return;
            }
            //deflection
            if ((uke instanceof PlayerEntity || WarDance.rand.nextFloat() > CombatConfig.mobDeflectChance) && GeneralUtils.isFacingEntity(uke, projectile, 120 + 2 * (int) GeneralUtils.getAttributeValueSafe(uke, WarAttributes.DEFLECTION.get())) && !GeneralUtils.isFacingEntity(uke, projectile, 120) && ukeCap.doConsumePosture(consume)) {
                e.setCanceled(true);
                uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                projectile.setMotion(pe.getReturnVec().x, pe.getReturnVec().y, pe.getReturnVec().z);
                if (projectile instanceof ProjectileEntity) {
                    double power = pe.getReturnVec().x / pe.getReturnVec().normalize().x;
                    ((ProjectileEntity) projectile).shoot(pe.getReturnVec().x, pe.getReturnVec().y, pe.getReturnVec().z, (float) power, 0);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancel(final LivingAttackEvent e) {
        if (!e.getEntityLiving().world.isRemote && e.getSource() != null && CombatUtils.isPhysicalAttack(e.getSource())) {
            LivingEntity uke = e.getEntityLiving();
            if (MovementUtils.hasInvFrames(uke)) {
                //System.out.println("the target has inv frames.");
                e.setCanceled(true);
            }
            ICombatCapability ukeCap = CombatData.getCap(uke);
            ItemStack attack = CombatUtils.getAttackingItemStack(e.getSource());
            if (CombatUtils.isMeleeAttack(e.getSource()) && e.getSource().getTrueSource() instanceof LivingEntity && attack != null && e.getAmount() > 0) {
                LivingEntity seme = (LivingEntity) e.getSource().getTrueSource();
                ICombatCapability semeCap = CombatData.getCap(seme);
                ukeCap.serverTick();
                semeCap.serverTick();
                Hand h = semeCap.isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND;
                //hand bound or staggered, no attack
                if (semeCap.getStaggerTime() > 0 || semeCap.getHandBind(h) > 0) {
                    //System.out.println("the attacker's hands are bound.");
                    e.setCanceled(true);
                    return;
                }
                if (seme.getHeldItemMainhand().getCapability(CombatManipulator.CAP).isPresent()) {
                    //System.out.println("the attacker's weapon is a capable weapon.");
                    e.setCanceled(seme.getHeldItemMainhand().getCapability(CombatManipulator.CAP).resolve().get().canAttack(e.getSource(), seme, uke, seme.getHeldItemMainhand(), e.getAmount()));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)//because compat with BHT...
    public static void parry(final LivingAttackEvent e) {
        if (!e.getEntityLiving().world.isRemote && e.getSource() != null && CombatUtils.isPhysicalAttack(e.getSource())) {
            LivingEntity uke = e.getEntityLiving();
            if (MovementUtils.hasInvFrames(uke)) {
                //System.out.println("the target has inv-frames.");
                e.setCanceled(true);
            }
            ICombatCapability ukeCap = CombatData.getCap(uke);
            ItemStack attack = CombatUtils.getAttackingItemStack(e.getSource());
            if (CombatUtils.isMeleeAttack(e.getSource()) && e.getSource().getTrueSource() instanceof LivingEntity && attack != null && e.getAmount() > 0) {
                LivingEntity seme = (LivingEntity) e.getSource().getTrueSource();
                seme.removePotionEffect(Effects.INVISIBILITY);
                ICombatCapability semeCap = CombatData.getCap(seme);
                Hand attackingHand = semeCap.isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND;
                //hand bound or staggered, no attack
                if (semeCap.getStaggerTime() > 0 || semeCap.getHandBind(attackingHand) > 0) {
                    //System.out.println("the attacker is staggered or bound.");
                    e.setCanceled(true);
                    return;
                }
                //capability handler
                seme.getHeldItemMainhand().getCapability(CombatManipulator.CAP).ifPresent((i) -> i.attackStart(e.getSource(), seme, uke, seme.getHeldItemMainhand(), e.getAmount()));
                //add stats if it's the first attack this tick and cooldown is sufficient
                if (seme.getLastAttackedEntityTime() != seme.ticksExisted) {//first hit of a potential sweep attack
                    semeCap.addCombo(0.2f);
                    float might = CombatUtils.getAttackMight(seme, uke);
                    semeCap.addMight(might);
                }
                //blocking, reset posture cooldown without resetting combo cooldown, bypass parry
                if (uke.isActiveItemStackBlocking()) {
                    ukeCap.consumePosture(0);
                    return;
                }
                final boolean failManualParry = CombatConfig.sneakParry > 0 && (ukeCap.getParryingTick() > uke.ticksExisted || ukeCap.getParryingTick() < uke.ticksExisted - CombatConfig.sneakParry);
                final boolean toggleParry = CombatConfig.sneakParry < 0 && ukeCap.getParryingTick() == -1;
                if (uke instanceof PlayerEntity && (failManualParry || toggleParry)) {
                    //manual parry toggle
                    // why does everyone want this feature...
                    return;
                }
                boolean canParry = GeneralUtils.isFacingEntity(uke, seme, 120);
                boolean useDeflect = (uke instanceof PlayerEntity || WarDance.rand.nextFloat() < CombatConfig.mobDeflectChance) && GeneralUtils.isFacingEntity(uke, seme, 120 + 2 * (int) GeneralUtils.getAttributeValueSafe(uke, WarAttributes.DEFLECTION.get())) && !GeneralUtils.isFacingEntity(uke, seme, 120) && !canParry;
                //staggered, no parry
                if (ukeCap.getStaggerTime() > 0) {
                    downingHit = false;
                    return;
                }
                //parry code start, grab attack multiplier
                float atkMult = CombatUtils.getPostureAtk(seme, seme, attackingHand, e.getAmount(), attack);
                downingHit = true;
                //stabby bonus
                CombatUtils.Awareness awareness = CombatUtils.getAwareness(seme, uke);
                atkMult *= CombatUtils.getDamageMultiplier(awareness, attack);
                //crit bonus
                if (e.getSource() instanceof CombatDamageSource && ((CombatDamageSource) e.getSource()).isCrit())
                    atkMult *= ((CombatDamageSource) e.getSource()).getCritDamage();
                //grab defending stack
                ItemStack defend = null;
                Hand parryHand = null;
                if (canParry) {
                    float posMod = 1337;
                    boolean isShield = false;
                    if (CombatUtils.canParry(uke, seme, uke.getHeldItemOffhand(), e.getAmount())) {
                        defend = uke.getHeldItemOffhand();
                        posMod = CombatUtils.getPostureDef(seme, uke, uke.getHeldItemOffhand(), e.getAmount());
                        isShield = CombatUtils.isShield(uke, uke.getHeldItemOffhand());
                        parryHand = Hand.OFF_HAND;
                    }
                    if (!isShield && CombatUtils.canParry(uke, seme, uke.getHeldItemMainhand(), e.getAmount()) && CombatUtils.getPostureDef(seme, uke, uke.getHeldItemMainhand(), e.getAmount()) < posMod) {
                        defend = uke.getHeldItemMainhand();
                        parryHand = Hand.MAIN_HAND;
                    }
                }
                float defMult = CombatUtils.getPostureDef(seme, uke, defend, e.getAmount());
                if (CombatUtils.parryMap.containsKey(GeneralUtils.getResourceLocationFromEntity(uke))) {
                    Tuple<Float, Float> stats = CombatUtils.parryMap.get(GeneralUtils.getResourceLocationFromEntity(uke));
                    defMult = stats.getA();
                    canParry = WarDance.rand.nextFloat() < stats.getB();
                    if (canParry) {
                        parryHand = CombatUtils.getCooledAttackStrength(uke, Hand.MAIN_HAND, 0.5f) > CombatUtils.getCooledAttackStrength(uke, Hand.OFF_HAND, 0.5f) ? Hand.MAIN_HAND : Hand.OFF_HAND;
                    }
                }
                float finalPostureConsumption = atkMult * defMult;
                ParryEvent pe = new ParryEvent(uke, seme, ((canParry && defend != null) || useDeflect), attackingHand, attack, parryHand, defend, finalPostureConsumption, e.getAmount());
                MinecraftForge.EVENT_BUS.post(pe);
                if (pe.isCanceled()) {
                    //System.out.println("parry has been canceled with the attack.");
                    e.setCanceled(true);
                    return;
                }
                if (ukeCap.getStaggerTime() == 0) {
                    //overflow posture
                    float knockback = ukeCap.consumePosture(seme, pe.getPostureConsumption());
                    CombatUtils.knockBack(uke, seme, Math.min(1.5f, knockback / (20f * ukeCap.getMaxPosture())), true, false);
                    //no parries if stabby
                    if (StealthConfig.ignore && awareness == CombatUtils.Awareness.UNAWARE) return;
                    if (pe.canParry()) {
                        e.setCanceled(true);
                        downingHit = false;
                        ukeCap.addCombo(0);
                        //knockback based on posture consumed
                        CombatUtils.knockBack(uke, seme, Math.min(1f, pe.getPostureConsumption() * 3 / ukeCap.getMaxPosture()), true, false);
                        CombatUtils.knockBack(seme, uke, Math.min(1f, pe.getPostureConsumption() * 3 / semeCap.getMaxPosture()), true, false);
                        if (defend == null) {
                            //deflect
                            uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                            return;
                        }
                        //shield disabling
                        boolean disshield = false;
                        parryHand = uke.getHeldItemOffhand() == defend ? Hand.OFF_HAND : Hand.MAIN_HAND;
                        if (CombatUtils.isShield(uke, defend)) {
                            if (CasterData.getCap(seme).isTagActive("disableShield") || attack.getItem().canDisableShield(attack, defend, uke, seme)) {
                                //shield is disabled
                                if (uke instanceof PlayerEntity) {
                                    ukeCap.setHandBind(parryHand, 60);
                                }
                                ukeCap.setHandBind(parryHand, 60);
                                disshield = true;
                            } else if (ukeCap.getShieldTime() == 0) {
                                Tuple<Integer, Integer> stat = CombatUtils.getShieldStats(defend);
                                ukeCap.setShieldTime(stat.getA());
                                ukeCap.setShieldCount(stat.getB());
                            } else {
                                ukeCap.decrementShieldCount(1);
                                if (ukeCap.getShieldCount() <= 0) {
                                    ukeCap.setHandBind(parryHand, ukeCap.getShieldTime());
                                }
                            }
                        }
                        uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), disshield ? SoundEvents.ITEM_SHIELD_BLOCK : SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                        //reset cooldown
                        if (defMult != 0) {//shield time
                            float cd = CombatUtils.getCooldownPeriod(uke, parryHand);//attack cooldown ticks
                            cd = (cd - pe.getPostureConsumption()) / cd;//5*(1+posture damage) cooldown
                            if (cd > 0)
                                CombatUtils.setHandCooldown(uke, parryHand, cd, true);
                            else
                                ukeCap.setHandBind(parryHand, (int) -cd);
                        }
                        //sword on sword is 1.4, sword on shield is 1.12
                        //normal distribution?
                        ItemStack finalDefend = defend;
                        defend.getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onParry(seme, uke, finalDefend, e.getAmount()));
                        Hand other = uke.getHeldItemMainhand() == defend ? Hand.OFF_HAND : Hand.MAIN_HAND;
                        ItemStack finalDefend1 = uke.getHeldItem(other);
                        defend.getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onOtherHandParry(seme, uke, finalDefend1, e.getAmount()));
                    }
                }
//                if (!(seme instanceof PlayerEntity))
//                    CombatUtils.setHandCooldown(seme, Hand.MAIN_HAND, 0, false);
            }//else System.out.println("the attack is not a melee attack, or the damage dealt was 0.");
            //shatter, at the rock bottom of the attack event, saving your protected butt.
            if (!uke.isActiveItemStackBlocking() && !e.isCanceled()) {
                if (CombatUtils.isPhysicalAttack(e.getSource()) && CombatUtils.getAwareness(e.getSource().getImmediateSource() instanceof LivingEntity ? (LivingEntity) e.getSource().getImmediateSource() : null, uke) != CombatUtils.Awareness.UNAWARE) {
                    if (CombatData.getCap(uke).consumeShatter(e.getAmount())) {
                        e.setCanceled(true);
                        uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
                    }
                    //otherwise the rest of the damage goes through and is handled later down the line anyway
                }
            }
        }

    }

    @SubscribeEvent
    public static void critHooks(CriticalHitEvent e) {
        if (!e.getEntityLiving().world.isRemote) {
            LivingEntity uke = e.getEntityLiving();
            LivingEntity seme = e.getPlayer();
            if (seme.getHeldItemMainhand().getCapability(CombatManipulator.CAP).isPresent()) {
                e.setResult(seme.getHeldItemMainhand().getCapability(CombatManipulator.CAP).resolve().get().critCheck(seme, uke, seme.getHeldItemMainhand(), e.getOldDamageModifier(), e.isVanillaCritical()));
                e.setDamageModifier(seme.getHeldItemMainhand().getCapability(CombatManipulator.CAP).resolve().get().critDamage(seme, uke, seme.getHeldItemMainhand()));
            }
        }
    }

    @SubscribeEvent
    public static void knockbackHooks(MeleeKnockbackEvent e) {
        if (!e.getEntityLiving().world.isRemote) {
            LivingEntity uke = e.getEntityLiving();
            LivingEntity seme = e.getAttacker();
            seme.getHeldItemMainhand().getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onKnockingBack(seme, uke, seme.getHeldItemMainhand(), e.getOriginalStrength()));
            uke.getHeldItemMainhand().getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onBeingKnockedBack(seme, uke, seme.getHeldItemMainhand(), e.getOriginalStrength()));
            uke.getHeldItemOffhand().getCapability(CombatManipulator.CAP).ifPresent((i) -> i.onBeingKnockedBack(seme, uke, seme.getHeldItemOffhand(), e.getOriginalStrength()));

        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void knockKnockWhosThere(LivingKnockBackEvent e) {
        if (!CombatData.getCap(e.getEntityLiving()).isFirstStaggerStrike() && CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0) {
            e.setCanceled(true);
            return;
        }
        //since knockback is ignored when mounted, it becomes extra posture instead
        if (e.getEntityLiving().getRidingEntity() != null) {
            int divisor = 1;
            for (Entity ride = e.getEntityLiving(); ride != null && ride.getRidingEntity() != null; ride = ride.getRidingEntity()) {
                divisor++;
            }
            for (Entity ride = e.getEntityLiving(); ride != null && ride.getRidingEntity() != null; ride = ride.getRidingEntity()) {
                if (ride instanceof LivingEntity)
                    CombatData.getCap((LivingEntity) ride).consumePosture(e.getStrength() / divisor);
            }
        }
        e.setStrength(e.getStrength() * CombatConfig.kbNerf);
        //if (e.getStrength() == 0) e.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pain(LivingHurtEvent e) {
        WarDance.LOGGER.debug("damage from " + e.getSource() + " received with amount " + e.getAmount());
        LivingEntity uke = e.getEntityLiving();
        uke.removePotionEffect(WarEffects.DISTRACTION.get());
        uke.removePotionEffect(WarEffects.SLEEP.get());
        LivingEntity kek = null;
        DamageSource ds = e.getSource();
        if (uke.isPotionActive(WarEffects.VULNERABLE.get()) && !CombatUtils.isPhysicalAttack(ds))
            e.setAmount(e.getAmount() + uke.getActivePotionEffect(WarEffects.VULNERABLE.get()).getAmplifier() + 1);
        if (ds.getImmediateSource() instanceof LivingEntity) {
            kek = (LivingEntity) ds.getImmediateSource();
        }
        uke.getAttribute(Attributes.ARMOR).removeModifier(uuid);
        uke.getAttribute(Attributes.ARMOR).removeModifier(uuid2);
        if (ds instanceof CombatDamageSource) {
            float mult = ((CombatDamageSource) ds).getArmorReductionPercentage() - 1;
            if (mult != 0) {
                AttributeModifier armor = new AttributeModifier(uuid2, "temporary armor multiplier", mult, AttributeModifier.Operation.MULTIPLY_TOTAL);
                uke.getAttribute(Attributes.ARMOR).applyNonPersistentModifier(armor);
            }
        }
        ItemStack ukemain = uke.getHeldItemMainhand();
        ItemStack ukeoff = uke.getHeldItemOffhand();
        if (ukemain.getCapability(CombatManipulator.CAP).isPresent()) {
            ICombatItemCapability icic = ukemain.getCapability(CombatManipulator.CAP).resolve().get();
            e.setAmount(icic.onBeingHurt(e.getSource(), uke, ukemain, e.getAmount()));
        }
        if (ukeoff.getCapability(CombatManipulator.CAP).isPresent()) {
            ICombatItemCapability icic = ukeoff.getCapability(CombatManipulator.CAP).resolve().get();
            e.setAmount(icic.onBeingHurt(e.getSource(), uke, ukeoff, e.getAmount()));
        }
        ICombatCapability cap = CombatData.getCap(uke);
        cap.setSpiritGrace(ResourceConfig.spiritCD);
        CombatUtils.Awareness awareness = CombatUtils.getAwareness(kek, uke);
        if (ds.getTrueSource() instanceof LivingEntity) {
            LivingEntity seme = ((LivingEntity) ds.getTrueSource());
            if (seme.getHeldItemMainhand().getCapability(CombatManipulator.CAP).isPresent()) {
                ICombatItemCapability icic = seme.getHeldItemMainhand().getCapability(CombatManipulator.CAP).resolve().get();
                e.setAmount(icic.hurtStart(e.getSource(), seme, uke, seme.getHeldItemMainhand(), e.getAmount()) * icic.damageMultiplier(seme, uke, seme.getHeldItemMainhand()));
                AttributeModifier armor = new AttributeModifier(uuid, "temporary armor removal", -icic.armorIgnoreAmount(e.getSource(), seme, uke, seme.getHeldItemMainhand(), e.getAmount()), AttributeModifier.Operation.ADDITION);
                uke.getAttribute(Attributes.ARMOR).applyNonPersistentModifier(armor);
            }
            if (CombatUtils.isMeleeAttack(e.getSource())) {
                if (awareness != CombatUtils.Awareness.ALERT) {
                    e.setAmount((float) (e.getAmount() * CombatUtils.getDamageMultiplier(awareness, CombatUtils.getAttackingItemStack(ds))));
                }
                cap.setCombo((float) (Math.floor(cap.getCombo()) / 2d));
            }
            double luckDiff = WarDance.rand.nextFloat() * (GeneralUtils.getAttributeValueSafe(seme, Attributes.LUCK)) - WarDance.rand.nextFloat() * (GeneralUtils.getAttributeValueSafe(uke, Attributes.LUCK));
            e.setAmount(e.getAmount() + (float) luckDiff * GeneralConfig.luck);
        }
        if (cap.getStaggerTime() > 0 && !cap.isFirstStaggerStrike()) {
            e.setAmount(e.getAmount() * CombatConfig.staggerDamage);
            //fatality!
            if (ds.getTrueSource() instanceof LivingEntity) {
                LivingEntity seme = ((LivingEntity) ds.getTrueSource());
                if (seme.world instanceof ServerWorld) {
                    ((ServerWorld) seme.world).spawnParticle(ParticleTypes.ANGRY_VILLAGER, uke.getPosX(), uke.getPosY(), uke.getPosZ(), 5, uke.getWidth(), uke.getHeight(), uke.getWidth(), 0.5f);
                }
                seme.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.ENTITY_GENERIC_BIG_FALL, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tanky(LivingDamageEvent e) {
        final LivingEntity uke = e.getEntityLiving();
        uke.getAttribute(Attributes.ARMOR).removeModifier(uuid);
        uke.getAttribute(Attributes.ARMOR).removeModifier(uuid2);
        //no food!
        ItemStack active = uke.getHeldItem(uke.getActiveHand());
        if (CombatUtils.isPhysicalAttack(e.getSource()) && CombatConfig.foodCool >= 0 && (active.getItem().getUseAction(active) == UseAction.EAT || active.getItem().getUseAction(active) == UseAction.DRINK) && uke.isHandActive()) {
            uke.stopActiveHand();
            if (uke instanceof PlayerEntity && CombatConfig.foodCool > 0) {
                ((PlayerEntity) uke).getCooldownTracker().setCooldown(active.getItem(), CombatConfig.foodCool);
            }
        }
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity seme = ((LivingEntity) e.getSource().getTrueSource());
            if (CombatUtils.isMeleeAttack(e.getSource())) {
                ItemStack sememain = seme.getHeldItemMainhand();
                if (sememain.getCapability(CombatManipulator.CAP).isPresent()) {
                    ICombatItemCapability icic = sememain.getCapability(CombatManipulator.CAP).resolve().get();
                    e.setAmount(icic.damageStart(e.getSource(), seme, uke, sememain, e.getAmount()));
                }
                ItemStack ukemain = uke.getHeldItemMainhand();
                ItemStack ukeoff = uke.getHeldItemOffhand();
                if (ukemain.getCapability(CombatManipulator.CAP).isPresent()) {
                    ICombatItemCapability icic = ukemain.getCapability(CombatManipulator.CAP).resolve().get();
                    e.setAmount(icic.onBeingDamaged(e.getSource(), uke, ukemain, e.getAmount()));
                }
                if (ukeoff.getCapability(CombatManipulator.CAP).isPresent()) {
                    ICombatItemCapability icic = ukeoff.getCapability(CombatManipulator.CAP).resolve().get();
                    e.setAmount(icic.onBeingDamaged(e.getSource(), uke, ukeoff, e.getAmount()));
                }
            }
        }
        if (CombatData.getCap(uke).getStaggerTime() == 0 && CombatUtils.isPhysicalAttack(e.getSource())) {
            if (e.getSource().getTrueSource() instanceof LivingEntity && CombatUtils.getAwareness((LivingEntity) e.getSource().getTrueSource(), uke) == CombatUtils.Awareness.UNAWARE)
                return;
            float amount = e.getAmount();
            //absorption
            amount -= GeneralUtils.getAttributeValueSafe(uke, WarAttributes.ABSORPTION.get());
            e.setAmount(Math.max(0, amount));
        }
        if (e.getAmount() <= 0) e.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void udedlol(LivingDamageEvent e) {
        WarDance.LOGGER.debug("damage from " + e.getSource() + " finalized with amount " + e.getAmount());
        final ICombatCapability cap = CombatData.getCap(e.getEntityLiving());
        if (cap.getStaggerTime() > 0 && !cap.isFirstStaggerStrike()) {
            cap.decrementStaggerCount(1);
        }
        if (e.getAmount() > e.getEntityLiving().getHealth() + e.getEntityLiving().getAbsorptionAmount()) {
            //are we gonna die? Well, I don't really care either way. Begone, drain!
            cap.setFatigue(0);
            cap.setWounding(0);
            cap.setBurnout(0);
        } else if (e.getAmount() > 0 && ResourceConfig.woundWL == ResourceConfig.woundList.contains(e.getSource().getDamageType()))//returns true if whitelist and included, or if blacklist and excluded
            //u hurt lol
            cap.addWounding(e.getAmount() * ResourceConfig.wound);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void adMortemInimicus(final LivingDeathEvent e) {
        //you dead yet? Well, I don't really care either way. Begone, drain!
        ICombatCapability icc = CombatData.getCap(e.getEntityLiving());
        icc.setFatigue(0);
        icc.setWounding(0);
        icc.setBurnout(0);
    }
}
