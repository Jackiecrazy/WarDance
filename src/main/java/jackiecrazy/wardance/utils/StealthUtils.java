package jackiecrazy.wardance.utils;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.StealthConfig;
import jackiecrazy.wardance.event.EntityAwarenessEvent;
import jackiecrazy.wardance.potion.WarEffects;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class StealthUtils {
    public static final StealthData STEALTH = new StealthData("");
    public static HashMap<ResourceLocation, StealthData> stealthMap = new HashMap<>();

    public static void updateMobDetection(List<? extends String> interpretS) {
        stealthMap.clear();
        for (String s : interpretS) {
            try {
                String[] val = s.split(",");
                final ResourceLocation key = new ResourceLocation(val[0]);
                String value = val[1];
                stealthMap.put(key, new StealthData(value.toLowerCase(Locale.ROOT)));
//                String print = val[0]+", ";
//                StealthData sd = stealthMap.get(key);
//                print = print.concat(sd.deaf ? "d" : "");
//                print = print.concat(sd.nightvision ? "n" : "");
//                print = print.concat(sd.illuminati ? "a" : "");
//                print = print.concat(sd.atheist ? "o" : "");
//                print = print.concat(sd.vigil ? "v" : "");
//                System.out.println("\"" + print + "\",");
            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted mob stealth definition " + s + "!");
            }
        }
    }

    public static Awareness getAwareness(LivingEntity attacker, LivingEntity target) {
        if (attacker == null || target == null || attacker == target)
            return Awareness.ALERT;//the cases that don't make sense.
        if (!StealthConfig.stealthSystem || target instanceof PlayerEntity || stealthMap.getOrDefault(target.getType().getRegistryName(), STEALTH).isVigilant())
            return Awareness.ALERT;
        Awareness a = Awareness.ALERT;
        if (target.isPotionActive(WarEffects.SLEEP.get()) || target.isPotionActive(WarEffects.PARALYSIS.get()) || target.isPotionActive(WarEffects.PETRIFY.get()))
            a = Awareness.UNAWARE;
        else if (target.isPotionActive(WarEffects.DISTRACTION.get()) || target.isPotionActive(WarEffects.CONFUSION.get()) || target.getAir() <= 0 || inWeb(target))
            a = Awareness.DISTRACTED;
        else if (target.getRevengeTarget() == null && (!(target instanceof MobEntity) || ((MobEntity) target).getAttackTarget() == null))
            a = Awareness.UNAWARE;
        else if (target.getRevengeTarget() != attacker && (!(target instanceof MobEntity) || ((MobEntity) target).getAttackTarget() != attacker))
            a = Awareness.DISTRACTED;
        EntityAwarenessEvent eae = new EntityAwarenessEvent(target, attacker, a);
        MinecraftForge.EVENT_BUS.post(eae);
        return eae.getAwareness();
    }

    private static boolean inWeb(LivingEntity e) {
        if (!e.world.isAreaLoaded(e.getPosition(), (int) Math.ceil(e.getWidth()))) return false;
        double minX = e.getPosX() - e.getWidth() / 2, minY = e.getPosY() - e.getHeight() / 2, minZ = e.getPosZ() - e.getWidth() / 2;
        double maxX = e.getPosX() + e.getWidth() / 2, maxY = e.getPosY() + e.getHeight() / 2, maxZ = e.getPosZ() + e.getWidth() / 2;
        for (double x = minX; x <= maxX; x++) {
            for (double y = minY; y <= maxY; y++) {
                for (double z = minZ; z <= maxZ; z++) {
                    if (e.world.getBlockState(e.getPosition()).getMaterial().equals(Material.WEB))
                        return true;
                }
            }
        }
        return false;
    }

    public enum Awareness {
        UNAWARE,//cannot be parried, absorbed, shattered, or deflected
        DISTRACTED,//deals extra (posture) damage
        ALERT//normal damage and reduction
    }

    public static class StealthData {
        private final boolean deaf, nightvision, allSeeing, perceptive, vigil, olfactory;

        public StealthData(String value) {
            allSeeing = value.contains("a");
            deaf = value.contains("d");
            nightvision = value.contains("n");
            olfactory = value.contains("o");
            perceptive = value.contains("p");
            vigil = value.contains("v");
        }

        public boolean isDeaf() {
            return deaf;
        }

        public boolean isNightVision() {
            return nightvision;
        }

        public boolean isAllSeeing() {
            return allSeeing;
        }

        public boolean isPerceptive() {
            return perceptive;
        }

        public boolean isVigilant() {
            return vigil;
        }

        public boolean isOlfactory() {return olfactory;}

    }
}
