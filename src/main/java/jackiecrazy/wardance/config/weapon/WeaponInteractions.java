package jackiecrazy.wardance.config.weapon;

import com.google.gson.*;
import jackiecrazy.wardance.client.RenderUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.lang.reflect.Type;

public class WeaponInteractions {
    public static final SweepAttack DEFAULT_FAN = new SweepAttack(SweepAttack.SWEEPTYPE.CONE, 30, 30);
    public static final SweepAttack DEFAULT_CLEAVE = new SweepAttack(SweepAttack.SWEEPTYPE.CLEAVE, 30, 30);
    public static final SweepAttack DEFAULT_NONE = new SweepAttack(SweepAttack.SWEEPTYPE.NONE, 0, 0);
    private static final SweepAttack DEFAULT_IMPACT = new SweepAttack(SweepAttack.SWEEPTYPE.IMPACT, 1, 1.5);
    private static final SweepAttack DEFAULT_LINE = new SweepAttack(SweepAttack.SWEEPTYPE.LINE, 1, 1.5);
    private static final SweepAttack DEFAULT_CIRCLE = new SweepAttack(SweepAttack.SWEEPTYPE.CIRCLE, 1, 1.5);
    public static Gson GSON = new GsonBuilder().registerTypeAdapter(SweepAttack.class, new SweepAdapter()).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

    public static abstract class WeaponInteraction {
        private TYPE t;

        public WeaponInteraction(TYPE e) {
            t = e;
        }

        public TYPE getInteractionType() {
            return t;
        }

        public abstract Component getToolTip(ItemStack e, boolean advanced);

        public abstract WeaponInteraction clone();

        public abstract void write(FriendlyByteBuf f);

        public abstract void read(FriendlyByteBuf f);

        public HitInfo getHitInfo() {
            return DEFAULT_NONE.getHitInfo();
        }

        public enum TYPE {
            SWEEP,
            USE,
            THROW,
            ANIMATE
        }
    }

    public static class SweepAttack extends WeaponInteraction {
        public static final SweepAttack NOTHING = new SweepAttack(SWEEPTYPE.NONE, 0, 0);

        private HitInfo hitInfo = new HitInfo();
        private double sweep_base = 0;
        private double sweep_scale = 0;
        private SWEEPTYPE sweep = SWEEPTYPE.NONE;
        private double range_multiplier = 1;

        private SweepAttack(SWEEPTYPE t, double b, double s) {
            super(TYPE.SWEEP);
            sweep = t;
            sweep_base = b;
            sweep_scale = s;
        }

        private static ChatFormatting getColorFromValue(double a) {
            if (a > 1) return ChatFormatting.GREEN;
            else if (a < 0) return ChatFormatting.YELLOW;
            else return ChatFormatting.RED;
        }

        public Component getToolTip(ItemStack e, boolean advanced) {
            String advance = "";
            double finalized = sweep_base + (sweep_scale * e.getEnchantmentLevel(Enchantments.SWEEPING_EDGE));
            MutableComponent sweepTip = Component.translatable("wardance.tooltip.sweep." + sweep, Component.literal(String.valueOf(finalized)).withStyle(ChatFormatting.AQUA));
            //grab different tooltips if and only if they are different
            double damage = getHitInfo().damage_scale;
            double posture = getHitInfo().posture_scale;
            if (getHitInfo().knockback != NOTHING.getHitInfo().knockback) {
                MutableComponent cp = Component.literal(RenderUtils.formatter.format(getHitInfo().knockback) + "x");
                if (!advanced) {
                    if (getHitInfo().knockback > 1) cp = Component.translatable("wardance.tooltip.more");
                    else if (getHitInfo().knockback < 0) cp = Component.translatable("wardance.tooltip.negative");
                    else cp = Component.translatable("wardance.tooltip.less");
                }
                sweepTip.append(Component.translatable("wardance.tooltip.sweep.knockback", cp.withStyle(getColorFromValue(getHitInfo().knockback))));
            }
            if (getHitInfo().crit) {
                sweepTip.append(Component.translatable("wardance.tooltip.sweep.crit").withStyle(ChatFormatting.GOLD));
                damage *= getHitInfo().crit_damage;
                posture *= getHitInfo().crit_damage;
            }
            if (damage != 1)
                sweepTip.append(Component.translatable("wardance.tooltip.sweep.damage" + advance, Component.literal(RenderUtils.formatter.format(damage * 100) + "%").withStyle(getColorFromValue(damage))));
            if (posture != 1)
                sweepTip.append(Component.translatable("wardance.tooltip.sweep.posture" + advance, Component.literal(RenderUtils.formatter.format(posture * 100) + "%").withStyle(getColorFromValue(posture))));
            if (!getHitInfo().hit_self_command.isEmpty() || !getHitInfo().hit_other_command.isEmpty() || !getHitInfo().damage_other_command.isEmpty() || !getHitInfo().damage_self_command.isEmpty()) {
                if (advanced) {
                    if (!getHitInfo().hit_self_command.isEmpty()) {
                        sweepTip.append(Component.literal("\n"));
                        sweepTip.append(Component.translatable("wardance.tooltip.sweep.command.self_hit", Component.literal(getHitInfo().hit_self_command).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                    if (!getHitInfo().hit_other_command.isEmpty()) {
                        sweepTip.append(Component.literal("\n"));
                        sweepTip.append(Component.translatable("wardance.tooltip.sweep.command.other_hit", Component.literal(getHitInfo().hit_other_command).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                    if (!getHitInfo().damage_self_command.isEmpty()) {
                        sweepTip.append(Component.literal("\n"));
                        sweepTip.append(Component.translatable("wardance.tooltip.sweep.command.self_damage", Component.literal(getHitInfo().damage_self_command).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                    if (!getHitInfo().damage_other_command.isEmpty()) {
                        sweepTip.append(Component.literal("\n"));
                        sweepTip.append(Component.translatable("wardance.tooltip.sweep.command.other_damage", Component.literal(getHitInfo().damage_other_command).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                } else
                    sweepTip.append(Component.translatable("wardance.tooltip.sweep.command").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            sweepTip = sweepTip.withStyle(ChatFormatting.WHITE);
            return sweepTip;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SweepAttack other && other.sweep == sweep && other.sweep_scale == sweep_scale && other.getHitInfo().damage_scale == getHitInfo().damage_scale && other.getHitInfo().posture_scale == getHitInfo().posture_scale && other.getHitInfo().crit_damage == getHitInfo().crit_damage && other.getHitInfo().crit == getHitInfo().crit && other.getHitInfo().knockback == getHitInfo().knockback && other.getHitInfo().hit_other_command.equals(getHitInfo().hit_other_command) && other.getHitInfo().hit_self_command.equals(getHitInfo().hit_self_command) && other.getHitInfo().damage_other_command.equals(getHitInfo().damage_other_command) && other.getHitInfo().damage_self_command.equals(getHitInfo().damage_self_command) && other.sweep_base == sweep_base;
        }

        public SweepAttack clone() {
            SweepAttack ret = new SweepAttack(sweep, sweep_base, sweep_scale);
            getHitInfo().copyTo(ret.getHitInfo());
            return ret;
        }

        public double getBase() {
            return sweep_base;
        }

        public double getScaling() {
            return sweep_scale;
        }

        public SWEEPTYPE getType() {
            return sweep;
        }

        public void write(FriendlyByteBuf f) {
            f.writeInt(sweep.ordinal());
            f.writeDouble(sweep_base);
            f.writeDouble(sweep_scale);
            getHitInfo().write(f);
        }

        public void read(FriendlyByteBuf f) {
            sweep = SWEEPTYPE.values()[f.readInt()];
            sweep_base = f.readDouble();
            sweep_scale = f.readDouble();
            getHitInfo().read(f);
        }

        public SweepAttack finisherCopy(boolean breach) {
            SweepAttack ret = clone();
            ret.getHitInfo().breach = breach;
            ret.getHitInfo().crit = true;
            ret.getHitInfo().damage_scale = 1;
            ret.getHitInfo().crit_damage = 2;
            return ret;
        }

        public SweepAttack preFinishCopy() {
            SweepAttack ret = clone();
            ret.getHitInfo().damage_scale = 0.3f;
            return ret;
        }

        public double getRangeMult() {
            return range_multiplier;
        }

        public HitInfo getHitInfo() {
            return hitInfo;
        }

        public enum SWEEPTYPE {
            NONE, CONE,//horizontal fan area in front of the entity up to max range, base and scale add angle
            CLEAVE,//cone but vertical
            LINE,//1 block wide line up to max range, base and scale add to thickness
            IMPACT,//splash at point of impact or furthest distance if no mob aimed, base and scale add radius
            CIRCLE//splash with entity as center, ignores range, base and scale add radius
        }
    }

    //uhh
    public static class Use extends WeaponInteraction {
        private int startTime = 0;
        private boolean continuous = true;

        public Use() {
            super(TYPE.USE);
        }

        @Override
        public Component getToolTip(ItemStack e, boolean advanced) {
            return Component.translatable("key.use");
        }

        public Use clone() {
            Use ret = new Use();
            ret.startTime = startTime;
            ret.continuous = continuous;
            return ret;
        }

        public int getStartTime() {
            return startTime;
        }

        public boolean isContinuous() {
            return continuous;
        }

        public void write(FriendlyByteBuf f) {
            f.writeInt(startTime);
            f.writeBoolean(continuous);
        }

        public void read(FriendlyByteBuf f) {
            startTime = f.readInt();
            continuous = f.readBoolean();
        }
    }

    public static class HitInfo {
        public static final HitInfo THROWN = new HitInfo(0, 1, 1, true, false, 1);
        public static final HitInfo BREACH = new HitInfo(0, 1, 1, true, true, 2);
        //general effects:
        // knockback scaling (negative supported),
        // (posture) damage scaling,
        // force crit,
        // crit damage
        //where tf do I even store this for weapon entities and sweeps? Separately?
        private double knockback = 1;
        private double damage_scale = 1;
        private double posture_scale = 1;
        private boolean crit = false;
        private boolean breach = false;
        private double crit_damage = 1.5;
        private String hit_self_command = "";
        private String hit_other_command = "";
        private String damage_self_command = "";
        private String damage_other_command = "";

        public HitInfo() {
        }

        public HitInfo(double knockback,
                       double damage_scale,
                       double posture_scale,
                       boolean crit,
                       boolean breach,
                       double crit_damage) {
            this.knockback = knockback;
            this.damage_scale = damage_scale;
            this.posture_scale = posture_scale;
            this.crit = crit;
            this.breach = breach;
            this.crit_damage = crit_damage;
        }

        public HitInfo copyTo(HitInfo ret) {
            ret.knockback = knockback;
            ret.damage_scale = damage_scale;
            ret.posture_scale = posture_scale;
            ret.crit = crit;
            ret.crit_damage = crit_damage;
            ret.hit_self_command = hit_self_command;
            ret.hit_other_command = hit_other_command;
            ret.damage_self_command = damage_self_command;
            ret.damage_other_command = damage_other_command;
            return ret;
        }

        public void write(FriendlyByteBuf f) {
            f.writeDouble(knockback);
            f.writeDouble(damage_scale);
            f.writeDouble(posture_scale);
            f.writeBoolean(crit);
            f.writeDouble(crit_damage);
            f.writeUtf(hit_self_command);
            f.writeUtf(hit_other_command);
            f.writeUtf(damage_self_command);
            f.writeUtf(damage_other_command);
        }

        public void read(FriendlyByteBuf f) {
            knockback = f.readDouble();
            damage_scale = f.readDouble();
            posture_scale = f.readDouble();
            crit = f.readBoolean();
            crit_damage = f.readDouble();
            hit_self_command = f.readUtf();
            hit_other_command = f.readUtf();
            damage_self_command = f.readUtf();
            damage_other_command = f.readUtf();
        }

        public boolean performCommand(LivingEntity by, boolean self, boolean damage) {
            String command = self ? (damage ? damage_self_command : hit_self_command) : (damage ? damage_other_command : hit_other_command);
            Level level = by.level();
            if (!level.isClientSide) {
                MinecraftServer minecraftserver = level.getServer();
                if (!StringUtil.isNullOrEmpty(command)) {
                    try {
                        CommandSourceStack commandsourcestack = new CommandSourceStack(by, by.position(), by.getRotationVector(), level instanceof ServerLevel s ? s : null, 3, by.getName().getString(), by.getDisplayName(), level.getServer(), by).withSuppressedOutput();
                        minecraftserver.getCommands().performPrefixedCommand(commandsourcestack, command);
                    } catch (Throwable ignored) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        public boolean canBreach() {
            return breach;
        }

        public double getKnockback() {
            return knockback;
        }

        public double getDamageScale() {
            return damage_scale;
        }

        public double getPostureScale() {
            return posture_scale;
        }

        public boolean isCrit() {
            return crit;
        }

        public double getCritDamage() {
            return crit_damage;
        }
    }

    public static class SweepAdapter implements JsonDeserializer<WeaponInteraction> {
        @Override
        public WeaponInteraction deserialize(JsonElement json,
                                             Type typeOfT,
                                             JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) return null;
            JsonObject sub = json.getAsJsonObject();
            if (sub.has("type")) {
                //others go in here
                String type = sub.get("type").getAsString();
                if (type.equals("use")) return asUse(sub);
            }
            return asSweepAttack(sub);
        }

        private Use asUse(JsonObject sub) {
            return GSON.fromJson(sub, Use.class);
        }

        private SweepAttack asSweepAttack(JsonObject sub) {
            SweepAttack sweep = GSON.fromJson(sub, SweepAttack.class);
//            if (sub.has("sweep"))
//                sweep.sweep = SweepAttack.SWEEPTYPE.valueOf(sub.get("sweep").getAsString().toUpperCase(Locale.ROOT));
//            if (!sub.has("sweep_base")) if (sweep.sweep == SweepAttack.SWEEPTYPE.CONE) {
//                sweep.sweep_base = 30;
//            } else {
//                sweep.sweep_base = 1;
//            }
//            else sweep.sweep_base = sub.get("sweep_base").getAsDouble();
//            if (!sub.has("sweep_scale")) if (sweep.sweep == SweepAttack.SWEEPTYPE.CONE) {
//                sweep.sweep_scale = 30;
//            } else {
//                sweep.sweep_scale = 1.5;
//            }
//            else sweep.sweep_scale = sub.get("sweep_scale").getAsDouble();

            if (sub.has("hitbox_info")) {
                sweep.hitInfo = GSON.fromJson(sub.get("hitbox_info"), HitInfo.class);
            } else sweep.hitInfo = GSON.fromJson(sub, HitInfo.class);
            return sweep;
        }
    }
}
