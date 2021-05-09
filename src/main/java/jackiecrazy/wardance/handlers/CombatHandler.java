package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.api.ICombatManipulator;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class CombatHandler {

    public static boolean downingHit = false;

    @SubscribeEvent
    public static void projectileParry(final ProjectileImpactEvent e) {
        if (e.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY && e.getRayTraceResult() instanceof EntityRayTraceResult && ((EntityRayTraceResult) e.getRayTraceResult()).getEntity() instanceof LivingEntity) {
            LivingEntity uke = (LivingEntity) ((EntityRayTraceResult) e.getRayTraceResult()).getEntity();
            if (uke.isActiveItemStackBlocking()) return;
            if (CombatUtils.getAwareness(null, uke) != CombatUtils.AWARENESS.ALERT) {
                return;
            }
            if (MovementUtils.hasInvFrames(uke)) e.setCanceled(true);
            if (e.getEntity() instanceof AbstractArrowEntity && ((AbstractArrowEntity) e.getEntity()).getPierceLevel() > 0)
                return;
            float consume = CombatConfig.posturePerProjectile;
            ICombatCapability ukeCap = CombatData.getCap(uke);
            boolean free = ukeCap.getShieldTime() > 0;
            ItemStack defend = null;
            Hand h = null;
            if (CombatUtils.isShield(uke, uke.getHeldItemOffhand()) && CombatUtils.canParry(uke, uke.getHeldItemOffhand())) {
                defend = uke.getHeldItemOffhand();
                h = Hand.OFF_HAND;
            } else if (CombatUtils.isShield(uke, uke.getHeldItemMainhand()) && CombatUtils.canParry(uke, uke.getHeldItemMainhand())) {
                defend = uke.getHeldItemMainhand();
                h = Hand.MAIN_HAND;
            }
            Entity projectile = e.getEntity();
            ProjectileParryEvent pe = new ProjectileParryEvent(uke, projectile, h, defend, free ? 0 : consume, projectile.getMotion().mul(-0.2, -0.2, -0.2));
            MinecraftForge.EVENT_BUS.post(pe);
            boolean sneaking = (!(uke instanceof PlayerEntity) || CombatConfig.sneakParry == 0 || (uke.isSneaking() && EntityHandler.lastSneak.get((PlayerEntity) uke) < uke.ticksExisted + CombatConfig.sneakParry));
            if (pe.getResult() == Event.Result.ALLOW || (defend != null && GeneralUtils.isFacingEntity(uke, projectile, 120) && sneaking && ukeCap.doConsumePosture(pe.getPostureConsumption()) && pe.getResult() == Event.Result.DEFAULT)) {
                e.setCanceled(true);
//                if (projectile instanceof ProjectileEntity)
//                    ((ProjectileEntity) projectile).setShooter(uke);//makes drowned tridents and skeleton arrows collectable, which is honestly silly
                if (!free) {
                    Tuple<Integer, Integer> stat = CombatUtils.getShieldStats(defend);
                    ukeCap.setShieldTime(stat.getA());
                    ukeCap.setShieldCount(stat.getB());
                }
                uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), free ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN : SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                projectile.setMotion(pe.getReturnVec().x, pe.getReturnVec().y, pe.getReturnVec().z);
                if (projectile instanceof ProjectileEntity) {
                    double power = pe.getReturnVec().x / pe.getReturnVec().normalize().x;
                    ((ProjectileEntity) projectile).shoot(pe.getReturnVec().x, pe.getReturnVec().y, pe.getReturnVec().z, (float) power, 0);
                }
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

    @SubscribeEvent(priority = EventPriority.LOWEST)//because compat with BHT...
    public static void parry(final LivingAttackEvent e) {
        if (!e.getEntityLiving().world.isRemote && e.getSource() != null && CombatUtils.isPhysicalAttack(e.getSource())) {
            LivingEntity uke = e.getEntityLiving();
            if (MovementUtils.hasInvFrames(uke)) e.setCanceled(true);
            ICombatCapability ukeCap = CombatData.getCap(uke);
            ItemStack attack = CombatUtils.getAttackingItemStack(e.getSource());
            if (CombatUtils.isMeleeAttack(e.getSource()) && e.getSource().getTrueSource() instanceof LivingEntity && attack != null && e.getAmount() > 0) {
                LivingEntity seme = (LivingEntity) e.getSource().getTrueSource();
                seme.removePotionEffect(Effects.INVISIBILITY);
                ICombatCapability semeCap = CombatData.getCap(seme);
                ukeCap.update();
                semeCap.update();
                Hand h = semeCap.isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND;
                //hand bound or staggered, no attack
                if (semeCap.getStaggerTime() > 0 || semeCap.getHandBind(h) > 0) {
                    e.setCanceled(true);
                    return;
                }
                //add stats if it's the first attack this tick and cooldown is sufficient
                if (seme.getLastAttackedEntityTime() != seme.ticksExisted) {//first hit of a potential sweep attack
                    semeCap.addCombo(0.2f);
                    float might = ((semeCap.getCachedCooldown() * semeCap.getCachedCooldown() * CombatUtils.getCooldownPeriod(seme, Hand.MAIN_HAND) * CombatUtils.getCooldownPeriod(seme, Hand.MAIN_HAND)) / 781.25f * (1 + (semeCap.getCombo() / 10f)));
                    float weakness = 1;
                    if (seme.isPotionActive(Effects.WEAKNESS))
                        for (int foo = 0; foo < seme.getActivePotionEffect(Effects.WEAKNESS).getAmplifier() + 1; foo++) {
                            weakness *= CombatConfig.weakness;
                        }
                    semeCap.addMight(might * weakness);
                    semeCap.consumePosture(0);
                }
                boolean canParry = GeneralUtils.isFacingEntity(uke, seme, 120) && (!(uke instanceof PlayerEntity) || CombatConfig.sneakParry == 0 || (uke.isSneaking() && EntityHandler.lastSneak.get((PlayerEntity) uke) < uke.ticksExisted + CombatConfig.sneakParry));//why does everyone want this feature...
                boolean useDeflect = (uke instanceof PlayerEntity || WarDance.rand.nextFloat() < CombatConfig.mobDeflectChance) && GeneralUtils.isFacingEntity(uke, seme, 120 + 2 * (int) GeneralUtils.getAttributeValueSafe(uke, WarAttributes.DEFLECTION.get())) && !canParry;
                //staggered, no parry
                if (ukeCap.getStaggerTime() > 0) {
                    downingHit = false;
                    return;
                }
                //blocking, reset posture cooldown without resetting combo cooldown, bypass parry
                if (uke.isActiveItemStackBlocking()) {
                    ukeCap.consumePosture(0);
                    return;
                }
                //parry code start, grab attack multiplier
                float atkMult = CombatUtils.getPostureAtk(seme, h, e.getAmount(), attack);
                downingHit = true;
                //stabby bonus
                CombatUtils.AWARENESS awareness = CombatUtils.getAwareness(seme, uke);
                atkMult *= CombatUtils.getDamageMultiplier(awareness, attack);
                //crit bonus
                if (e.getSource() instanceof CombatDamageSource && ((CombatDamageSource) e.getSource()).isCrit())
                    atkMult *= ((CombatDamageSource) e.getSource()).getCritDamage();
                //grab defending stack
                ItemStack defend = null;
                Hand parryHand = null;
                if (canParry) {
                    float posMod = 1337;
                    if (CombatUtils.canParry(uke, uke.getHeldItemOffhand())) {
                        defend = uke.getHeldItemOffhand();
                        posMod = CombatUtils.getPostureDef(uke, uke.getHeldItemOffhand());
                        parryHand = Hand.OFF_HAND;
                    }
                    if (CombatUtils.canParry(uke, uke.getHeldItemMainhand()) && CombatUtils.getPostureDef(uke, uke.getHeldItemMainhand()) < posMod) {
                        defend = uke.getHeldItemMainhand();
                        parryHand = Hand.MAIN_HAND;
                    }
                }
                float defMult = CombatUtils.getPostureDef(uke, defend);
                float finalPostureConsumption = atkMult * defMult;
                ParryEvent pe = new ParryEvent(uke, seme, h, attack, parryHand, defend, finalPostureConsumption);
                MinecraftForge.EVENT_BUS.post(pe);
                if (ukeCap.getStaggerTime() == 0) {
                    //overflow posture
                    float knockback = ukeCap.consumePosture(pe.getPostureConsumption());
                    CombatUtils.knockBack(uke, seme, Math.min(1.5f, knockback / (20f * ukeCap.getMaxPosture())), true, false);
                    //no parries if stabby
                    if (CombatConfig.ignore && awareness == CombatUtils.AWARENESS.UNAWARE) return;
                    if (pe.getResult() == Event.Result.ALLOW || (((canParry && defend != null) || useDeflect) && pe.getResult() == Event.Result.DEFAULT)) {
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
                        if (CombatUtils.isShield(uke, defend)) {
                            if (attack.getItem().canDisableShield(attack, defend, uke, seme)) {
                                //shield is disabled
                                if (uke instanceof PlayerEntity) {
                                    ((PlayerEntity) uke).getCooldownTracker().setCooldown(defend.getItem(), 60);
                                    uke.world.setEntityState(uke, (byte) 30);
                                }
                                ukeCap.setHandBind(uke.getHeldItemOffhand() == defend ? Hand.OFF_HAND : Hand.MAIN_HAND, 60);
                                disshield = true;
                            } else if (ukeCap.getShieldTime() == 0) {
                                Tuple<Integer, Integer> stat = CombatUtils.getShieldStats(defend);
                                ukeCap.setShieldTime(stat.getA());
                                ukeCap.setShieldCount(stat.getB());
                            } else {
                                ukeCap.decrementShieldCount(1);
                            }
                        }
                        uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), disshield ? SoundEvents.ITEM_SHIELD_BLOCK : SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                        //reset cooldown
                        if (defMult != 0)//shield time
                            CombatUtils.setHandCooldown(uke, uke.getHeldItemOffhand() == defend ? Hand.OFF_HAND : Hand.MAIN_HAND, 0f, true);
                        if (defend.getItem() instanceof ICombatManipulator) {
                            ((ICombatManipulator) defend.getItem()).onParry(seme, uke, defend, e.getAmount());
                        }
                        Hand other = uke.getHeldItemMainhand() == defend ? Hand.OFF_HAND : Hand.MAIN_HAND;
                        if (uke.getHeldItem(other).getItem() instanceof ICombatManipulator) {
                            ((ICombatManipulator) uke.getHeldItem(other).getItem()).onOtherHandParry(seme, uke, uke.getHeldItem(other), e.getAmount());
                        }
                    }
                }
//                if (!(seme instanceof PlayerEntity))
//                    CombatUtils.setHandCooldown(seme, Hand.MAIN_HAND, 0, false);
            }
            //shatter, at the rock bottom of the attack event, saving your protected butt.
            if (!uke.isActiveItemStackBlocking() && !e.isCanceled()) {
                if (CombatUtils.isPhysicalAttack(e.getSource()) && CombatUtils.getAwareness(e.getSource().getImmediateSource() instanceof LivingEntity ? (LivingEntity) e.getSource().getImmediateSource() : null, uke) != CombatUtils.AWARENESS.UNAWARE) {
                    if (e.getAmount() < CombatData.getCap(uke).getShatter()) {
                        e.setCanceled(true);
                        CombatData.getCap(uke).consumeShatter(e.getAmount());
                        uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
                    }
                    //otherwise the rest of the damage goes through and is handled later down the line anyway
                }
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void knockKnockWhosThere(LivingKnockBackEvent e) {
        if (!downingHit && CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0) {
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
        LivingEntity kek = null;
        DamageSource ds = e.getSource();
        if (ds.getImmediateSource() instanceof LivingEntity) {
            kek = (LivingEntity) ds.getImmediateSource();
        }
        ICombatCapability cap = CombatData.getCap(uke);
        cap.setCombo((float) (Math.floor(cap.getCombo()) / (CombatUtils.isMeleeAttack(e.getSource()) ? 2d : 1)));
        if (ds.getTrueSource() instanceof LivingEntity) {
            LivingEntity seme = ((LivingEntity) ds.getTrueSource());
            double luckDiff = WarDance.rand.nextFloat() * (GeneralUtils.getAttributeValueSafe(seme, Attributes.LUCK)) - WarDance.rand.nextFloat() * (GeneralUtils.getAttributeValueSafe(uke, Attributes.LUCK));
            e.setAmount(e.getAmount() + (float) luckDiff * CombatConfig.luck);
        }
        CombatUtils.AWARENESS awareness = CombatUtils.getAwareness(kek, uke);
        if (awareness != CombatUtils.AWARENESS.ALERT) {
            e.setAmount((float) (e.getAmount() * CombatUtils.getDamageMultiplier(awareness, CombatUtils.getAttackingItemStack(ds))));
        }
        if (cap.getStaggerTime() > 0 && !downingHit) {
            e.setAmount(e.getAmount() * CombatConfig.staggerDamage);
            //fatality!
            if (ds.getTrueSource() instanceof LivingEntity) {
                LivingEntity seme = ((LivingEntity) ds.getTrueSource());
                if (seme.world instanceof ServerWorld) {
                    ((ServerWorld) seme.world).spawnParticle(ParticleTypes.ANGRY_VILLAGER, uke.getPosX(), uke.getPosY(), uke.getPosZ(), 5, uke.getWidth(), uke.getHeight(), uke.getWidth(), 0.5f);
                }
                seme.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.ENTITY_GENERIC_BIG_FALL, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            }
        } else if (CombatUtils.isPhysicalAttack(e.getSource()) && awareness != CombatUtils.AWARENESS.UNAWARE) {
            float temp = e.getAmount();
            e.setAmount(cap.consumeShatter(e.getAmount()));
            if (e.getAmount() > 0 && temp != e.getAmount()) {//shattered
                uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tanky(LivingDamageEvent e) {
        if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() == 0 && CombatUtils.isPhysicalAttack(e.getSource())) {
            if (e.getSource().getTrueSource() instanceof LivingEntity && CombatUtils.getAwareness((LivingEntity) e.getSource().getTrueSource(), e.getEntityLiving()) == CombatUtils.AWARENESS.UNAWARE)
                return;
            float amount = e.getAmount();
            //absorption
            amount -= GeneralUtils.getAttributeValueSafe(e.getEntityLiving(), WarAttributes.ABSORPTION.get());
            e.setAmount(amount);
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void udedlol(LivingDamageEvent e) {
        WarDance.LOGGER.debug("damage from " + e.getSource() + " finalized with amount " + e.getAmount());
        if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0 && !downingHit) {
            CombatData.getCap(e.getEntityLiving()).decrementStaggerCount(1);
        }
        if (e.getAmount() > e.getEntityLiving().getHealth() + e.getEntityLiving().getAbsorptionAmount()) {
            //are we gonna die? Well, I don't really care either way. Begone, drain!
            ICombatCapability icc = CombatData.getCap(e.getEntityLiving());
            icc.setFatigue(0);
            icc.setWounding(0);
            icc.setBurnout(0);
        } else if (CombatConfig.woundWL == CombatConfig.woundList.contains(e.getSource().getDamageType()))//returns true if whitelist and included, or if blacklist and excluded
            //u hurt lol
            CombatData.getCap(e.getEntityLiving()).addWounding(e.getAmount() * CombatConfig.wound);

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
