package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ZTargetHandler {
    public static KeyMapping LOCK_ON;
    public static KeyMapping TAB;

    public static List<LivingEntity> list = new ArrayList<>();

    private static final Minecraft mc = Minecraft.getInstance();

    public static void client(FMLClientSetupEvent e) {//TODO register
        LOCK_ON = new KeyMapping("key." + WarDance.MODID + ".lock_on", GLFW.GLFW_KEY_O, "key.categories." + WarDance.MODID);
        TAB = new KeyMapping("key." + WarDance.MODID + ".tab", GLFW.GLFW_KEY_TAB, "key.categories." + WarDance.MODID);
        //ClientRegistry.registerKeyBinding(LOCK_ON);
        //ClientRegistry.registerKeyBinding(TAB);
        MinecraftForge.EVENT_BUS.addListener(ZTargetHandler::handleKeyPress);
    }

    public static boolean lockedOn;
    private static Entity targetted;

    private static void handleKeyPress(TickEvent.RenderTickEvent e) {
        Player player = mc.player;
        if (e.phase == TickEvent.Phase.START && mc.player != null && !mc.isPaused()) {
            tickLockedOn();
            while (LOCK_ON.consumeClick()) {
                if (lockedOn) {
                    leaveLockOn();
                } else {
                    attemptEnterLockOn(player);
                }
            }

            while (TAB.consumeClick()) {
                tabToNextEnemy(player);
            }

            if (targetted != null) {
                Vec3 targetPos = targetted.position();
                Vec3 directionVec = targetPos.subtract(mc.player.position()).normalize();
                double angle = Math.atan2(-directionVec.x, directionVec.z) * 180 / Math.PI;

                float adjustedPrevYaw = mc.player.yRotO;
                if (Math.abs(angle - adjustedPrevYaw) > 180) {
                    if (adjustedPrevYaw > angle) {
                        angle += 360;
                    } else if (adjustedPrevYaw < angle) {
                        angle -= 360;
                    }
                }

                double newDelta = Mth.lerp(e.renderTickTime, adjustedPrevYaw, angle);
                if (newDelta > 180) {
                    newDelta -= 360;
                }
                if (newDelta < -180) {
                    newDelta += 360;
                }
                mc.player.setYRot((float) newDelta);//problematic
            }
        }
    }

    private static void attemptEnterLockOn(Player player) {
        tabToNextEnemy(player);
        if (targetted != null) {
            lockedOn = true;
        }
    }

    private static void tickLockedOn() {
        list.removeIf(livingEntity -> mc.player == null || !livingEntity.isAlive());
        if (targetted != null && !targetted.isAlive()) {
            targetted = null;
            lockedOn = false;
        }
    }

    private static final Predicate<LivingEntity> ENTITY_PREDICATE = entity -> entity.isAlive() && entity.attackable();
    private static final TargetingConditions ENEMY_CONDITION = TargetingConditions.forCombat().range(20.0D).selector(ENTITY_PREDICATE);

    private static int cycle = -1;

    public static Entity findNearby(Player player) {
        List<LivingEntity> entities = player.level.getNearbyEntities(LivingEntity.class, ENEMY_CONDITION, player, player.getBoundingBox().inflate(10.0D, 2.0D, 10.0D));
        if (lockedOn) {
            cycle++;
            for (LivingEntity entity : entities) {
                if (!list.contains(entity)) {
                    list.add(entity);
                    return entity;
                }
            }

            //cycle existing entity
            if (cycle >= list.size()) {
                cycle = 0;
            }
            return list.get(cycle);
        } else {
            if (!entities.isEmpty()) {
                LivingEntity first = entities.get(0);
                list.add(first);
                return entities.get(0);
            } else {
                return null;
            }
        }
    }

    private static void tabToNextEnemy(Player player) {
        targetted = findNearby(player);
    }

    private static void leaveLockOn() {
        targetted = null;
        lockedOn = false;
        list.clear();
    }
}
