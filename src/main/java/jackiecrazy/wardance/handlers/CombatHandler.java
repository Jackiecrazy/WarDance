package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.ICombatManipulator;
import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.capability.ICombatCapability;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
        if (e.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY && e.getRayTraceResult().hitInfo instanceof LivingEntity) {
            LivingEntity uke = (LivingEntity) e.getRayTraceResult().hitInfo;
            float consume = CombatConfig.posturePerProjectile;
            if ((CombatUtils.isShield(uke, uke.getHeldItemMainhand()) || CombatUtils.isShield(uke, uke.getHeldItemOffhand()) && CombatData.getCap(uke).consumePosture(consume))) {
                Vector3d look = uke.getLookVec();
                e.setCanceled(true);
                e.getEntity().setVelocity(look.x, look.y, look.z);
            }
        }
    }

    @SubscribeEvent
    public static void parry(final LivingAttackEvent e) {
        if (!e.getEntityLiving().world.isRemote && e.getSource() != null && CombatUtils.isMeleeAttack(e.getSource())) {
            LivingEntity uke = e.getEntityLiving();
            ICombatCapability ukeCap = CombatData.getCap(uke);
            ItemStack attack = CombatUtils.getAttackingItemStack(e.getSource());
            if (e.getSource().getTrueSource() instanceof LivingEntity && attack != null) {
                LivingEntity seme = (LivingEntity) e.getSource().getTrueSource();
                ICombatCapability semeCap = CombatData.getCap(seme);
                ukeCap.update();
                semeCap.update();
                if (semeCap.getStaggerTime() > 0 || semeCap.getHandBind(semeCap.isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND) > 0) {
                    e.setCanceled(true);
                    return;
                }
                if (ukeCap.getStaggerTime() > 0) {
                    ukeCap.decrementStaggerCount(1);
                    return; //also cancels posture consumption, so you keep regenerating
                }
                Hand h=semeCap.isOffhandAttack()?Hand.OFF_HAND:Hand.MAIN_HAND;
                float atkMult = CombatUtils.getPostureAtk(seme, h, e.getAmount(), attack);
                ItemStack defend = CombatUtils.getDefendingItemStack(uke);
                float defMult = CombatUtils.getPostureDef(uke, defend);
                if (ukeCap.consumePosture(atkMult * defMult) && defend != null) {
                    e.setCanceled(true);
                    //knockback based on posture consumed
                    CombatUtils.knockBack(seme, uke, Math.min(1.5f, atkMult * defMult * 2f / semeCap.getMaxPosture()), true, false);
                    CombatUtils.knockBack(uke, seme, Math.min(1.5f, atkMult * defMult * 2f / ukeCap.getMaxPosture()), true, false);
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
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void knockKnockWhosThere(LivingKnockBackEvent e) {
        if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0) {
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
            //fatality!
            DamageSource ds = e.getSource();
            if (ds.getTrueSource() != null && ds.getTrueSource() instanceof LivingEntity) {
                LivingEntity seme = ((LivingEntity) ds.getTrueSource());
                if (seme.world instanceof ServerWorld) {
                    ((ServerWorld) seme.world).spawnParticle(ParticleTypes.ANGRY_VILLAGER, uke.getPosX(), uke.getPosY(), uke.getPosZ(), 5, uke.getWidth(), uke.getHeight(), uke.getWidth(), 0.5f);
                }
                seme.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), SoundEvents.ENTITY_GENERIC_BIG_FALL, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            }
        }
    }

    @SubscribeEvent
    public static void offhandAttack(final PlayerInteractEvent.EntityInteract e) {
        if (CombatUtils.isWeapon(e.getPlayer(), e.getPlayer().getHeldItemOffhand()) && (e.getPlayer().swingingHand != Hand.OFF_HAND || !e.getPlayer().isSwingInProgress)) {
            CombatData.getCap(e.getPlayer()).setOffhandAttack(true);
            e.getPlayer().swing(Hand.OFF_HAND, true);
            e.getPlayer().attackTargetEntityWithCurrentItem(e.getTarget());
            CombatData.getCap(e.getPlayer()).setOffhandAttack(false);
        }
    }
}
