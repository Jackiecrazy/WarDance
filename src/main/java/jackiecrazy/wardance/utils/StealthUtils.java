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
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class StealthUtils {
    public static final StealthData STEALTH = new StealthData("");
    public static HashMap<ResourceLocation, StealthData> stealthMap = new HashMap<>();
    public static HashMap<SoundEvent, Integer> soundMap = new HashMap<>();

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

    public static void updateSound(List<? extends String> interpretS) {
        soundMap.clear();
        for (String s : interpretS) {
            try {
                String[] val = s.split(",");
                if (s.startsWith("*")) {
                    String contain = val[0].substring(1);
                    for (SoundEvent se : ForgeRegistries.SOUND_EVENTS) {
                        if (se.getRegistryName().toString().contains(contain))
                            soundMap.put(se, Integer.parseInt(val[1].trim()));
                    }
                } else {
                    final ResourceLocation key = new ResourceLocation(val[0]);
                    if (ForgeRegistries.SOUND_EVENTS.getValue(key) != null) {
                        Integer value = Integer.parseInt(val[1].trim());
                        soundMap.put(ForgeRegistries.SOUND_EVENTS.getValue(key), value);
                    }
                }
            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted sound definition " + s + "!");
            }
        }
    }

    public static Awareness getAwareness(LivingEntity attacker, LivingEntity target) {
        if (target == null || attacker == target)
            return Awareness.ALERT;//the cases that don't make sense.
        //players are alert because being jumped with 2.5x daggers feel bad, and obviously nothing else applies if stealth is turned off
        if (!StealthConfig.stealthSystem || target instanceof PlayerEntity)
            return Awareness.ALERT;
        StealthData sd = stealthMap.getOrDefault(target.getType().getRegistryName(), STEALTH);
        Awareness a = Awareness.ALERT;
        //sleep, paralysis, and petrify take highest priority
        if (target.hasEffect(WarEffects.SLEEP.get()) || target.hasEffect(WarEffects.PARALYSIS.get()) || target.hasEffect(WarEffects.PETRIFY.get()))
            a = Awareness.UNAWARE;
            //idle and not vigilant
        else if (!sd.isVigilant() && target.getLastHurtByMob() == null && (!(target instanceof MobEntity) || ((MobEntity) target).getTarget() == null))
            a = Awareness.UNAWARE;
            //distraction, confusion, and choking take top priority in inferior tier
        else if (target.hasEffect(WarEffects.DISTRACTION.get()) || target.hasEffect(WarEffects.CONFUSION.get()) || target.getAirSupply() <= 0)
            a = Awareness.DISTRACTED;
            //looking around for you, but cannot see
        else if (attacker != null && attacker.isInvisible() && !sd.isObservant())
            a = Awareness.DISTRACTED;
            //webbed and not a spider
        else if (inWeb(target) && !sd.isCheliceric())
            a = Awareness.DISTRACTED;
            //hurt by something else
        else if (!sd.isMindful() && target.getLastHurtByMob() != attacker && (!(target instanceof MobEntity) || ((MobEntity) target).getTarget() != attacker))
            a = Awareness.DISTRACTED;
        //event for more compat
        EntityAwarenessEvent eae = new EntityAwarenessEvent(target, attacker, a);
        MinecraftForge.EVENT_BUS.post(eae);
        return eae.getAwareness();
    }

    private static boolean inWeb(LivingEntity e) {
        if (!e.level.isAreaLoaded(e.blockPosition(), (int) Math.ceil(e.getBbWidth()))) return false;
        double minX = e.getX() - e.getBbWidth() / 2, minY = e.getY() - e.getBbHeight() / 2, minZ = e.getZ() - e.getBbWidth() / 2;
        double maxX = e.getX() + e.getBbWidth() / 2, maxY = e.getY() + e.getBbHeight() / 2, maxZ = e.getZ() + e.getBbWidth() / 2;
        for (double x = minX; x <= maxX; x++) {
            for (double y = minY; y <= maxY; y++) {
                for (double z = minZ; z <= maxZ; z++) {
                    if (e.level.getBlockState(e.blockPosition()).getMaterial().equals(Material.WEB))
                        return true;
                }
            }
        }
        return false;
    }

    public static int getActualLightLevel(World world, BlockPos pos) {
        int i = 0;
        if (world.dimensionType().hasSkyLight()) {
            world.updateSkyBrightness();
            int dark = world.getSkyDarken();
            i = world.getBrightness(LightType.SKY, pos) - dark;
        }

        i = MathHelper.clamp(Math.max(world.getBrightness(LightType.BLOCK, pos), i), 0, 15);
        return i;
    }

    public enum Awareness {
        UNAWARE,//cannot be parried, absorbed, shattered, or deflected
        DISTRACTED,//deals extra (posture) damage
        ALERT//normal damage and reduction
    }

    public static class StealthData {
        private final boolean allSeeing, blind, cheliceric, deaf, eyeless, heatSeeking, lazy, mindful, nightvision, observant, perceptive, skeptical, quiet, vigil, wary;

        public StealthData(String value) {
            allSeeing = value.contains("a");
            blind = value.contains("b");
            cheliceric = value.contains("c");
            deaf = value.contains("d");
            eyeless = value.contains("e");
            heatSeeking = value.contains("h");
            lazy = value.contains("l");
            mindful = value.contains("m");
            nightvision = value.contains("n");
            observant = value.contains("o");
            perceptive = value.contains("p");
            skeptical = value.contains("s");
            quiet = value.contains("s");
            vigil = value.contains("v");
            wary = value.contains("w");
        }

        public boolean isBlind() {
            return blind;
        }

        public boolean isCheliceric() {
            return cheliceric;
        }

        public boolean isEyeless() {
            return eyeless;
        }

        public boolean isLazy() {
            return lazy;
        }

        public boolean isMindful() {
            return mindful;
        }

        public boolean isSkeptical() {
            return skeptical;
        }

        public boolean isVigilant() {
            return vigil;
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

        public boolean isObservant() {
            return observant;
        }

        public boolean isPerceptive() {
            return perceptive;
        }

        public boolean isWary() {return wary;}

        public boolean isQuiet() {return quiet;}

        public boolean isHeatSeeking() {
            return heatSeeking;
        }
    }

}
