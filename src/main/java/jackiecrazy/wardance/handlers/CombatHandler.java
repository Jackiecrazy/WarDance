package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.api.ICombatManipulator;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.capability.ICombatCapability;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class CombatHandler {

    @SubscribeEvent
    public static void projectileParry(final ProjectileImpactEvent e) {
        if (e.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY && e.getRayTraceResult() instanceof EntityRayTraceResult && ((EntityRayTraceResult) e.getRayTraceResult()).getEntity() instanceof LivingEntity) {
            LivingEntity uke = (LivingEntity) ((EntityRayTraceResult) e.getRayTraceResult()).getEntity();
            if (uke.isActiveItemStackBlocking()) return;
            if (CombatUtils.getAwareness(null, uke) != CombatUtils.AWARENESS.ALERT) {
                return;
            }
            if (MovementUtils.hasInvFrames(uke)) e.setCanceled(true);
            float consume = CombatConfig.posturePerProjectile;
            ICombatCapability ukeCap = CombatData.getCap(uke);
            boolean free = ukeCap.getShieldTime() > 0;
            ItemStack defend = null;
            if (CombatUtils.isShield(uke, uke.getHeldItemOffhand()) && CombatUtils.canParry(uke, uke.getHeldItemOffhand()))
                defend = uke.getHeldItemOffhand();
            else if (CombatUtils.isShield(uke, uke.getHeldItemMainhand()) && CombatUtils.canParry(uke, uke.getHeldItemMainhand()))
                defend = uke.getHeldItemMainhand();
            Entity projectile = e.getEntity();
            if (defend != null && GeneralUtils.isFacingEntity(uke, projectile, 120) && ukeCap.doConsumePosture(free ? 0 : consume)) {
                e.setCanceled(true);
                if (projectile instanceof ProjectileEntity)
                    ((ProjectileEntity) projectile).setShooter(uke);
                if (!free) {
                    Tuple<Integer, Integer> stat = CombatUtils.getShieldStats(defend);
                    ukeCap.setShieldTime(stat.getA());
                    ukeCap.setShieldCount(stat.getB());
                }
                uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), free ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN : SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                Vector3d look = uke.getLookVec().mul(0.2, 0.2, 0.2);
                projectile.setMotion(look.x, look.y, look.z);
                return;
            }
            //deflection
            if ((uke instanceof PlayerEntity || WarDance.rand.nextFloat() > CombatConfig.mobDeflectChance) && GeneralUtils.isFacingEntity(uke, projectile, 120 + 2 * (int) GeneralUtils.getAttributeValueSafe(uke, WarAttributes.DEFLECTION.get())) && !GeneralUtils.isFacingEntity(uke, projectile, 120) && ukeCap.doConsumePosture(consume)) {
                e.setCanceled(true);
                uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                Vector3d look = projectile.getMotion().mul(-0.2, -0.2, -0.2);
                projectile.setMotion(look.x, look.y, look.z);
            }
        }
    }

    private static boolean downingHit = false;

    @SubscribeEvent
    public static void parry(final LivingAttackEvent e) {
        if (!e.getEntityLiving().world.isRemote && e.getSource() != null && CombatUtils.isMeleeAttack(e.getSource())) {
            LivingEntity uke = e.getEntityLiving();
            if (MovementUtils.hasInvFrames(uke)) e.setCanceled(true);
            ICombatCapability ukeCap = CombatData.getCap(uke);
            ItemStack attack = CombatUtils.getAttackingItemStack(e.getSource());
            if (e.getSource().getTrueSource() instanceof LivingEntity && attack != null) {
                LivingEntity seme = (LivingEntity) e.getSource().getTrueSource();
                ICombatCapability semeCap = CombatData.getCap(seme);
                ukeCap.update();
                semeCap.update();
                //add stats if it's the first attack this tick and cooldown is sufficient
                if (seme.getLastAttackedEntityTime() != seme.ticksExisted) {//first hit of a potential sweep attack
                    semeCap.addCombo(0.2f);
                    float might = ((semeCap.getCachedCooldown() * semeCap.getCachedCooldown()) / 781.25f * (1 + (semeCap.getCombo() / 10f)));
                    float weakness = 1;
                    if (seme.isPotionActive(Effects.WEAKNESS))
                        for (int foo = 0; foo < seme.getActivePotionEffect(Effects.WEAKNESS).getAmplifier() + 1; foo++) {
                            weakness *= CombatConfig.weakness;
                        }
                    semeCap.addMight(might * weakness);
                    semeCap.consumePosture(0);
                }
                boolean canParry = GeneralUtils.isFacingEntity(uke, seme, 120);
                boolean useDeflect = (uke instanceof PlayerEntity || WarDance.rand.nextFloat() > CombatConfig.mobDeflectChance) && GeneralUtils.isFacingEntity(uke, seme, 120 + 2 * (int) GeneralUtils.getAttributeValueSafe(uke, WarAttributes.DEFLECTION.get())) && !canParry;
                Hand h = semeCap.isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND;
                //hand bound, no attack
                if (semeCap.getStaggerTime() > 0 || semeCap.getHandBind(h) > 0) {
                    e.setCanceled(true);
                    return;
                }
                //staggered, no parry
                if (ukeCap.getStaggerTime() > 0) {
                    ukeCap.decrementStaggerCount(1);
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
                if (awareness != CombatUtils.AWARENESS.ALERT) {
                    atkMult *= awareness == CombatUtils.AWARENESS.UNAWARE ? CombatConfig.unaware : CombatConfig.distract;
                }
                //crit bonus
                if (e.getSource() instanceof CombatDamageSource && ((CombatDamageSource) e.getSource()).isCrit())
                    atkMult *= 1.5;
                //grab defending stack
                ItemStack defend = null;
                if (canParry) {
                    float posMod = 1337;
                    if (CombatUtils.canParry(uke, uke.getHeldItemOffhand())) {
                        defend = uke.getHeldItemOffhand();
                        posMod = CombatUtils.getPostureDef(uke, uke.getHeldItemOffhand());
                    }
                    if (CombatUtils.canParry(uke, uke.getHeldItemMainhand()) && CombatUtils.getPostureDef(uke, uke.getHeldItemMainhand()) < posMod)
                        defend = uke.getHeldItemMainhand();
                }
                float defMult = CombatUtils.getPostureDef(uke, defend);
                //overflow posture
                float knockback = ukeCap.consumePosture(atkMult * defMult);
                if (ukeCap.getStaggerTime() == 0) {
                    float consume = atkMult * Math.max(defMult, 0.5f) * 3f;
                    CombatUtils.knockBack(uke, seme, Math.min(1.5f, (consume + knockback / 20f) / ukeCap.getMaxPosture()), true, false);
                    //no parries if stabby
                    if (CombatConfig.ignore && awareness == CombatUtils.AWARENESS.UNAWARE) return;
                    if ((canParry && defend != null) || useDeflect) {
                        e.setCanceled(true);
                        downingHit = false;
                        ukeCap.addCombo(0);
                        //knockback based on posture consumed
                        CombatUtils.knockBack(seme, uke, Math.min(1.5f, consume / semeCap.getMaxPosture()), true, false);
                        if (defend == null) {
                            uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundCategory.PLAYERS, 0.75f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
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
                            CombatUtils.setHandCooldown(uke, uke.getHeldItemOffhand() == defend ? Hand.OFF_HAND : Hand.MAIN_HAND, 0.5f, true);
                        if (defend.getItem() instanceof ICombatManipulator) {
                            ((ICombatManipulator) defend.getItem()).onParry(seme, uke, defend, e.getAmount());
                        }
                        Hand other = uke.getHeldItemMainhand() == defend ? Hand.OFF_HAND : Hand.MAIN_HAND;
                        if (uke.getHeldItem(other).getItem() instanceof ICombatManipulator) {
                            ((ICombatManipulator) uke.getHeldItem(other).getItem()).onOtherHandParry(seme, uke, uke.getHeldItem(other), e.getAmount());
                        }
                    }
                }
                if (!(seme instanceof PlayerEntity))
                    CombatUtils.setHandCooldown(seme, Hand.MAIN_HAND, 0, false);
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
        //if (e.getStrength() == 0) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void pain(LivingHurtEvent e) {
        LivingEntity uke = e.getEntityLiving();
        LivingEntity kek = null;
        DamageSource ds = e.getSource();
        if (ds.getImmediateSource() instanceof LivingEntity) {
            kek = (LivingEntity) ds.getImmediateSource();
        }
        ICombatCapability cap = CombatData.getCap(uke);
        cap.setCombo((float) (Math.floor(cap.getCombo()) / 2d));
        CombatUtils.AWARENESS awareness = CombatUtils.getAwareness(kek, uke);
        if (awareness != CombatUtils.AWARENESS.ALERT) {
            e.setAmount(e.getAmount() * (awareness == CombatUtils.AWARENESS.UNAWARE ? CombatConfig.unaware : CombatConfig.distract));
        }
        if (ds.getTrueSource() instanceof LivingEntity) {
            LivingEntity seme = ((LivingEntity) ds.getTrueSource());
            double luckDiff = WarDance.rand.nextFloat() * (GeneralUtils.getAttributeValueSafe(seme, Attributes.LUCK)) - WarDance.rand.nextFloat() * (GeneralUtils.getAttributeValueSafe(uke, Attributes.LUCK));
            e.setAmount(e.getAmount() + (float) luckDiff * CombatConfig.luck);
        }
        if (CombatConfig.woundWL == CombatConfig.woundList.contains(e.getSource().getDamageType()))//returns true if whitelist and included, or if blacklist and excluded
            cap.setWounding(cap.getWounding() + e.getAmount() * CombatConfig.wound);
        if (cap.getStaggerTime() > 0) {
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
                uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
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
        if (e.getAmount() > e.getEntityLiving().getHealth() + e.getEntityLiving().getAbsorptionAmount()) {
            //are we gonna die? Well, I don't really care either way. Begone, drain!
            ICombatCapability icc = CombatData.getCap(e.getEntityLiving());
            icc.setFatigue(0);
            icc.setWounding(0);
            icc.setBurnout(0);
        }
    }

    @SubscribeEvent
    public static void offhandAttack(final PlayerInteractEvent.EntityInteract e) {
        if (!e.getPlayer().world.isRemote && e.getHand() == Hand.OFF_HAND && (CombatUtils.isWeapon(e.getPlayer(), e.getPlayer().getHeldItemOffhand()) || (e.getPlayer().getHeldItemOffhand().isEmpty() && CombatData.getCap(e.getPlayer()).isCombatMode())) && (e.getPlayer().swingingHand != Hand.OFF_HAND || !e.getPlayer().isSwingInProgress)) {
            CombatData.getCap(e.getPlayer()).setOffhandAttack(true);
            int a = CombatData.getCap(e.getPlayer()).getOffhandCooldown();
            CombatUtils.sweep(e.getPlayer(), e.getTarget(), Hand.OFF_HAND, GeneralUtils.getAttributeValueSafe(e.getPlayer(), ForgeMod.REACH_DISTANCE.get()));
            CombatUtils.swapHeldItems(e.getPlayer());
            e.getPlayer().ticksSinceLastSwing = a;
            e.getPlayer().attackTargetEntityWithCurrentItem(e.getTarget());
            e.getPlayer().swing(Hand.OFF_HAND, true);
            CombatUtils.swapHeldItems(e.getPlayer());
            CombatData.getCap(e.getPlayer()).setOffhandAttack(false);
        }
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
