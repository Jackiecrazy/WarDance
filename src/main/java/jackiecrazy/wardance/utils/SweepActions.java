package jackiecrazy.wardance.utils;

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
import java.util.Locale;

public class SweepActions {
    public static final SweepInfo DEFAULT_FAN = new SweepInfo(SweepInfo.SWEEPTYPE.CONE, 30, 30);
    public static final SweepInfo DEFAULT_CLEAVE = new SweepInfo(SweepInfo.SWEEPTYPE.CLEAVE, 30, 30);
    public static final SweepInfo DEFAULT_NONE = new SweepInfo(SweepInfo.SWEEPTYPE.NONE, 0, 0);
    private static final SweepInfo DEFAULT_IMPACT = new SweepInfo(SweepInfo.SWEEPTYPE.IMPACT, 1, 1.5);
    private static final SweepInfo DEFAULT_LINE = new SweepInfo(SweepInfo.SWEEPTYPE.LINE, 1, 1.5);
    private static final SweepInfo DEFAULT_CIRCLE = new SweepInfo(SweepInfo.SWEEPTYPE.CIRCLE, 1, 1.5);
    public static Gson GSON = new GsonBuilder().registerTypeAdapter(SweepInfo.class, new SweepAdapter()).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

    public static class SweepInfo {
        public static final SweepInfo NOTHING = new SweepInfo(SWEEPTYPE.NONE, 0, 0);

        private HitInfo hitInfo = new HitInfo();
        private double sweep_base = 0;
        private double sweep_scale = 0;
        private SWEEPTYPE sweep = SWEEPTYPE.NONE;
        private double range_multiplier = 1;

        private SweepInfo(SWEEPTYPE t, double b, double s) {
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
            return obj instanceof SweepInfo other && other.sweep == sweep && other.sweep_scale == sweep_scale && other.getHitInfo().damage_scale == getHitInfo().damage_scale && other.getHitInfo().posture_scale == getHitInfo().posture_scale && other.getHitInfo().crit_damage == getHitInfo().crit_damage && other.getHitInfo().crit == getHitInfo().crit && other.getHitInfo().knockback == getHitInfo().knockback && other.getHitInfo().hit_other_command.equals(getHitInfo().hit_other_command) && other.getHitInfo().hit_self_command.equals(getHitInfo().hit_self_command) && other.getHitInfo().damage_other_command.equals(getHitInfo().damage_other_command) && other.getHitInfo().damage_self_command.equals(getHitInfo().damage_self_command) && other.sweep_base == sweep_base;
        }

        public SweepInfo clone() {
            SweepInfo ret = new SweepInfo(sweep, sweep_base, sweep_scale);
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

        public SweepInfo finisherCopy() {
            SweepInfo ret = clone();
            ret.getHitInfo().breach = true;
            ret.getHitInfo().crit = true;
            ret.getHitInfo().damage_scale = 1;
            ret.getHitInfo().crit_damage = 2;
            return ret;
        }

        public SweepInfo preFinishCopy() {
            SweepInfo ret = clone();
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

    public static class HitInfo {
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

    public static class SweepAdapter implements JsonDeserializer<SweepInfo> {
        @Override
        public SweepInfo deserialize(JsonElement json,
                                     Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) return null;
            JsonObject sub = json.getAsJsonObject();
            SweepInfo sweep = new SweepInfo(SweepInfo.SWEEPTYPE.NONE, 0, 0);
            if (sub.has("sweep"))
                sweep.sweep = SweepInfo.SWEEPTYPE.valueOf(sub.get("sweep").getAsString().toUpperCase(Locale.ROOT));
            if (!sub.has("sweep_base")) if (sweep.sweep == SweepInfo.SWEEPTYPE.CONE) {
                sweep.sweep_base = 30;
            } else {
                sweep.sweep_base = 1;
            }
            else sweep.sweep_base = sub.get("sweep_base").getAsDouble();
            if (!sub.has("sweep_scale")) if (sweep.sweep == SweepInfo.SWEEPTYPE.CONE) {
                sweep.sweep_scale = 30;
            } else {
                sweep.sweep_scale = 1.5;
            }
            else sweep.sweep_scale = sub.get("sweep_scale").getAsDouble();

            if (sub.has("damage_scale")) {
                sweep.hitInfo = GSON.fromJson(sub, HitInfo.class);
            } else sweep.hitInfo = GSON.fromJson(sub.get("hitbox_info"), HitInfo.class);
//                sweep.hitInfo.damage_scale = sub.get("damage_scale").getAsDouble();
//            if (sub.has("posture_scale")) sweep.hitInfo.posture_scale = sub.get("posture_scale").getAsDouble();
//            if (sub.has("knockback")) sweep.hitInfo.knockback = sub.get("knockback").getAsDouble();
//            if (sub.has("crit")) sweep.hitInfo.crit = sub.get("crit").getAsBoolean();
//            if (sub.has("crit_damage")) sweep.hitInfo.crit_damage = sub.get("crit_damage").getAsDouble();
//            if (sub.has("hit_self_command")) sweep.hitInfo.hit_self_command = sub.get("hit_self_command").getAsString();
//            if (sub.has("hit_other_command"))
//                sweep.hitInfo.hit_other_command = sub.get("hit_other_command").getAsString();
//            if (sub.has("damage_self_command"))
//                sweep.hitInfo.damage_self_command = sub.get("damage_self_command").getAsString();
//            if (sub.has("damage_other_command"))
//                sweep.hitInfo.damage_other_command = sub.get("damage_other_command").getAsString();
            return sweep;
        }
    }
}
