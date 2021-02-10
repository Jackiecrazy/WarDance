package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.ICombatManipulator;
import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.capability.ICombatCapability;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
            float consume = 0.5f;//TODO config variable consumption
            if ((CombatUtils.isShield(uke, uke.getHeldItemMainhand()) || CombatUtils.isShield(uke, uke.getHeldItemOffhand()) && CombatData.getCap(uke).consumePosture(consume))) {
                Vector3d look = uke.getLookVec();
                e.setCanceled(true);
                e.getEntity().setVelocity(look.x, look.y, look.z);
            }
        }
    }

    @SubscribeEvent
    public static void parry(final LivingAttackEvent e) {
        if (e.getSource() != null && CombatUtils.isMeleeAttack(e.getSource())) {
            LivingEntity uke = e.getEntityLiving();
            ICombatCapability ukeCap = CombatData.getCap(uke);
            ItemStack attack = CombatUtils.getAttackingItemStack(e.getSource());
            if (e.getSource().getTrueSource() instanceof LivingEntity && attack != null) {
                if (ukeCap.getStaggerTime() > 0) return;
                LivingEntity seme = (LivingEntity) e.getSource().getTrueSource();
                float atkMult = CombatUtils.getPostureAtk(seme, e.getAmount(), attack);
                float defMult = 1f;//TODO config variable consumption
                ItemStack defend = CombatUtils.getDefendingItemStack(uke);
                if (ukeCap.consumePosture(atkMult * defMult) && defend != null) {
                    e.setCanceled(true);
                    //knockback based on posture consumed
                    ICombatCapability semeCap = CombatData.getCap(seme);
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
                        } else if (ukeCap.getShieldTime() != 0) {
                            ukeCap.setShieldTime(30);//TODO shield threshold config
                        }
                    }
                    uke.world.playSound(null, uke.getPosX(), uke.getPosY(), uke.getPosZ(), disshield ? SoundEvents.ITEM_SHIELD_BLOCK : SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, (1 - (ukeCap.getPosture() / ukeCap.getMaxPosture())) + WarDance.rand.nextFloat() * 0.5f);
                    //reset cooldown
                    CombatUtils.setHandCooldown(uke, uke.getHeldItemOffhand() == defend ? Hand.OFF_HAND : Hand.MAIN_HAND, 0.5f);
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

    //by config option, will also replace the idiotic chance to resist knock with ratio resist. Somewhat intrusive.
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
    public static void offhandAttack(final PlayerInteractEvent.EntityInteract e) {
        if (CombatUtils.isWeapon(e.getPlayer(), e.getPlayer().getHeldItemOffhand())) {
            CombatData.getCap(e.getPlayer()).setOffhandAttack(true);
            e.getPlayer().attackTargetEntityWithCurrentItem(e.getEntity());
            CombatData.getCap(e.getPlayer()).setOffhandAttack(true);
        }
    }
}
