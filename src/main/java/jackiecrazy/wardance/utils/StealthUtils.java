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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class StealthUtils {
    public static final StealthData STEALTH = new StealthData("");
    public static HashMap<ResourceLocation, StealthData> stealthMap = new HashMap<>();
    private static long lastUpdate;

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
        if (target.hasEffect(WarEffects.SLEEP.get()) || target.hasEffect(WarEffects.PARALYSIS.get()) || target.hasEffect(WarEffects.PETRIFY.get()))
            a = Awareness.UNAWARE;
        else if (target.hasEffect(WarEffects.DISTRACTION.get()) || target.hasEffect(WarEffects.CONFUSION.get()) || target.getAirSupply() <= 0 || inWeb(target))
            a = Awareness.DISTRACTED;
        else if (target.getLastHurtByMob() == null && (!(target instanceof MobEntity) || ((MobEntity) target).getTarget() == null))
            a = Awareness.UNAWARE;
        else if (target.getLastHurtByMob() != attacker && (!(target instanceof MobEntity) || ((MobEntity) target).getTarget() != attacker))
            a = Awareness.DISTRACTED;
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
            //TODO does this work properly underground?
            if (lastUpdate < world.getGameTime()) {
                world.updateSkyBrightness();
                lastUpdate = world.getGameTime();
            }
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
        private final boolean deaf, nightvision, allSeeing, perceptive, vigil, olfactory, silent;

        public StealthData(String value) {
            allSeeing = value.contains("a");
            deaf = value.contains("d");
            nightvision = value.contains("n");
            olfactory = value.contains("w");
            silent = value.contains("s");
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

        public boolean isWary() {return olfactory;}

        public boolean isSilent() {return silent;}

    }

}
