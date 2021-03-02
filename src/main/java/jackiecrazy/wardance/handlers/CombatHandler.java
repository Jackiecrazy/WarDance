package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
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
            if (MovementUtils.hasInvFrames(uke)) e.setCanceled(true);
            float consume = CombatConfig.posturePerProjectile;
            ICombatCapability ukeCap = CombatData.getCap(uke);
            boolean free = ukeCap.getShieldTime() > 0;
            ItemStack defend = CombatUtils.getDefendingItemStack(uke, true);
            if (defend != null && GeneralUtils.isFacingEntity(uke, e.getEntity(), 120) && ukeCap.doConsumePosture(free ? 0 : consume)) {
                e.setCanceled(true);
                if (!free) {
                    Tuple<Integer, Integer> stat = CombatUtils.getShieldStats(defend);
                    ukeCap.setShieldTime(stat.getA());
                    ukeCap.setShieldCount(stat.getB());
                } else {
                    ukeCap.decrementShieldCount(1);
                }
                uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), free ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN : SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                Vector3d look = e.getEntity().getMotion().mul(-1, -1, -1).normalize();
                e.getEntity().setVelocity(look.x, look.y, look.z);
                return;
            }
            //deflection
            if (GeneralUtils.isFacingEntity(uke, e.getEntity(), 120 + 2 * (int) uke.getAttributeValue(WarAttributes.DEFLECTION.get())) && !GeneralUtils.isFacingEntity(uke, e.getEntity(), 120) && ukeCap.doConsumePosture(consume * CombatConfig.deflectionPenalty)) {
                e.setCanceled(true);
                uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                Vector3d look = e.getEntity().getMotion().mul(-1, -1, -1).normalize();
                e.getEntity().setVelocity(look.x, look.y, look.z);
                return;
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
                Hand h = semeCap.isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND;
                if (semeCap.getStaggerTime() > 0 || semeCap.getHandBind(h) > 0) {
                    e.setCanceled(true);
                    return;
                }
                if (ukeCap.getStaggerTime() > 0) {
                    ukeCap.decrementStaggerCount(1);
                    downingHit = false;
                    return;
                }
                float atkMult = CombatUtils.getPostureAtk(seme, h, e.getAmount(), attack);
                ItemStack defend = CombatUtils.getDefendingItemStack(uke, false);
                float defMult = CombatUtils.getPostureDef(uke, defend);
                downingHit = true;
                float kb = ukeCap.consumePosture(atkMult * defMult);
                if (ukeCap.getStaggerTime() == 0) {
                    CombatUtils.knockBack(uke, seme, Math.min(1.5f, (atkMult * Math.max(defMult, 0.5f) * 3f + kb / 20f) / ukeCap.getMaxPosture()), true, false);
                    if (GeneralUtils.isFacingEntity(uke, seme, 120) && defend != null) {
                        e.setCanceled(true);
                        downingHit = false;
                        ukeCap.addCombo(0);
                        //knockback based on posture consumed
                        CombatUtils.knockBack(seme, uke, Math.min(1.5f, atkMult * Math.max(defMult, 0.5f) * 3f / semeCap.getMaxPosture()), true, false);
                        //shield disabling
                        boolean disshield = false;
                        if (CombatUtils.isShield(uke, defend)) {
                            if (attack.getItem().canDisableShield(attack, defend, uke, seme)) {
                                //shield is disabled
                                if (uke instanceof PlayerEntity) {
                                    ((PlayerEntity) uke).getCooldownTracker().setCooldown(defend.getItem(), 60);
                                    uke.world.setEntityState(uke, (byte) 30);
                                }
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
                    CombatUtils.setHandCooldown(seme, h, 0, false);
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
        ICombatCapability cap = CombatData.getCap(uke);
        cap.setCombo((float) (Math.floor(cap.getCombo()) / 2d));
        if (cap.getStaggerTime() > 0) {
            e.setAmount(e.getAmount() * 1.5f);
            //fatality!
            DamageSource ds = e.getSource();
            if (ds.getTrueSource() != null && ds.getTrueSource() instanceof LivingEntity) {
                LivingEntity seme = ((LivingEntity) ds.getTrueSource());
                if (seme.world instanceof ServerWorld) {
                    ((ServerWorld) seme.world).spawnParticle(ParticleTypes.ANGRY_VILLAGER, uke.getPosX(), uke.getPosY(), uke.getPosZ(), 5, uke.getWidth(), uke.getHeight(), uke.getWidth(), 0.5f);
                }
                seme.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.ENTITY_GENERIC_BIG_FALL, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            }
        } else if (CombatUtils.isPhysicalAttack(e.getSource())) {
            e.setAmount(cap.consumeShatter(e.getAmount()));
        }
        cap.setWounding(cap.getWounding() + e.getAmount() * CombatConfig.wound);
    }

    @SubscribeEvent
    public static void tanky(LivingDamageEvent e) {
        if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() == 0 && CombatUtils.isPhysicalAttack(e.getSource())) {
            float amount = e.getAmount();
            //absorption
            amount -= e.getEntityLiving().getAttributeValue(WarAttributes.ABSORPTION.get());
            e.setAmount(amount);
        }
    }

    @SubscribeEvent
    public static void offhandAttack(final PlayerInteractEvent.EntityInteract e) {
        if ((CombatUtils.isWeapon(e.getPlayer(), e.getPlayer().getHeldItemOffhand()) || (e.getPlayer().getHeldItemOffhand().isEmpty() && CombatData.getCap(e.getPlayer()).isCombatMode())) && (e.getPlayer().swingingHand != Hand.OFF_HAND || !e.getPlayer().isSwingInProgress)) {
            CombatData.getCap(e.getPlayer()).setOffhandAttack(true);
            e.getPlayer().swing(Hand.OFF_HAND, true);
            e.getPlayer().attackTargetEntityWithCurrentItem(e.getTarget());
            CombatData.getCap(e.getPlayer()).setOffhandAttack(false);
        }
    }
}
