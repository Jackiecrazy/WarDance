package jackiecrazy.wardance.config.weapon.interactions;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.action.timer.TimerAction;
import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.argument.ResourceEnums;
import jackiecrazy.footwork.move.argument.number.FixedNumberArgument;
import jackiecrazy.footwork.move.argument.number.NumberArgument;
import jackiecrazy.footwork.move.argument.resourcelocation.ResourceLocationArgument;
import jackiecrazy.footwork.move.argument.vector.VectorArgument;
import jackiecrazy.footwork.move.condition.Condition;
import jackiecrazy.footwork.move.condition.ConsumeResourceCondition;
import jackiecrazy.footwork.move.filter.Filter;
import jackiecrazy.footwork.move.motionframe.HitEffects;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.utils.ActionJsonAdapters;
import jackiecrazy.footwork.utils.JsonAdapters;
import jackiecrazy.footwork.utils.JsonUtils;
import jackiecrazy.wardance.WarDance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

public class WeaponInteractions {
    public static final ConsumeResourceCondition BREACH_CONDITION = new ConsumeResourceCondition(ResourceEnums.ResourceFormat.PERCENTAGE, ResourceEnums.TYPE.SPIRIT, new FixedNumberArgument(1));

    public static final Gson NAIVE = (new GsonBuilder())
            .registerTypeAdapter(Class.class, new ActionJsonAdapters.ClassAdapter())
            .registerTypeAdapter(Supplier.class, new ActionJsonAdapters.SupplierAdapter())
            .registerTypeAdapter(Action.class, new ActionJsonAdapters.ActionAdapter())
            .registerTypeAdapter(TimerAction.class, new ActionJsonAdapters.ActionAdapter())
            .registerTypeAdapter(Argument.class, new ActionJsonAdapters.ArgumentAdapter())
            .registerTypeAdapter(NumberArgument.class, new ActionJsonAdapters.NumberAdapter())
            .registerTypeAdapter(VectorArgument.class, new ActionJsonAdapters.VectorAdapter())
            .registerTypeAdapter(ResourceLocationArgument.class, new ActionJsonAdapters.ResourceAdapter())
            .registerTypeAdapter(Condition.class, new ActionJsonAdapters.ConditionAdapter())
            .registerTypeAdapter(Filter.class, new ActionJsonAdapters.FilterAdapter())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(CompoundTag.class, new ActionJsonAdapters.NBTAdapter())
            .registerTypeAdapter(WeaponInteraction.class, new InteractionDeserializer())
            .registerTypeAdapter(Vec3.class, new JsonAdapters.Vec3TypeAdapter()).setPrettyPrinting().create();
    public static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(WeaponInteraction.class, new InteractionDeserializer())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(Vec3.class, new JsonAdapters.Vec3TypeAdapter())
            .registerTypeAdapter(MotionFrame.class, new JsonAdapters.MotionFrameAdapter())
            .registerTypeAdapter(MotionManager.class, new JsonAdapters.MotionManagerDeserializer())
            .registerTypeAdapter(HitInfo.class, new JsonAdapters.HitInfoAdapter())
            .registerTypeAdapter(Class.class, new ActionJsonAdapters.ClassAdapter())
            .registerTypeAdapter(Supplier.class, new ActionJsonAdapters.SupplierAdapter())
            .registerTypeAdapter(Action.class, new ActionJsonAdapters.ActionAdapter())
            .registerTypeAdapter(TimerAction.class, new ActionJsonAdapters.ActionAdapter())
            .registerTypeAdapter(Argument.class, new ActionJsonAdapters.ArgumentAdapter<>())
            .registerTypeAdapter(NumberArgument.class, new ActionJsonAdapters.NumberAdapter())
            .registerTypeAdapter(VectorArgument.class, new ActionJsonAdapters.VectorAdapter())
            .registerTypeAdapter(ResourceLocationArgument.class, new ActionJsonAdapters.ResourceAdapter())
            .registerTypeAdapter(Condition.class, new ActionJsonAdapters.ConditionAdapter())
            .registerTypeAdapter(Filter.class, new ActionJsonAdapters.FilterAdapter())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(CompoundTag.class, new ActionJsonAdapters.NBTAdapter())
            .registerTypeAdapter(InteractionGroup.class, new GroupDeserializer())
            .setPrettyPrinting()
            .create();

    public static class InteractionGroup {
        private List<WeaponInteraction> interactions = new ArrayList<>();
        private Vec3 velocity = Vec3.ZERO;
        private boolean set_velocity = false;
        private boolean swing_hand = true;
        private HitEffects on_swing = new HitEffects();
        private String description;
        private transient Component desc;
        private List<InteractionOverride> overrides = new ArrayList<>();
        private transient Map<WeaponInteraction.InteractionType, WeaponInteraction> bakedTypes = null;
        private double minimum_cooldown = 0.9;
        private double cooldown_refund = 0;
        private Vec3 left_hand_offset = new Vec3(-0.5, 0, 0.5);
        private Vec3 right_hand_offset = new Vec3(0.5, 0, 0.5);
        private boolean debug = false;

        public InteractionGroup() {
        }

        public Vec3 left_hand_offset() {
            return left_hand_offset;
        }

        public Vec3 right_hand_offset() {
            return right_hand_offset;
        }

        public double getCooldownRefund() {
            return cooldown_refund;
        }

        public double getMinimumCooldown() {
            return minimum_cooldown;
        }

        public void write(FriendlyByteBuf f) {
            f.writeVector3f(velocity.toVector3f());
            f.writeBoolean(set_velocity);
            f.writeBoolean(swing_hand);
            f.writeComponent(desc);
            f.writeCollection(interactions, (a, b) -> {
                b.write(a);
            });
        }

        public InteractionGroup read(FriendlyByteBuf f) {
            velocity = new Vec3(f.readVector3f());
            set_velocity = f.readBoolean();
            swing_hand = f.readBoolean();
            desc = f.readComponent();
            interactions = f.readList(WeaponInteraction::readFromByte);
            return this;
        }

        public Vec3 getVelocity() {
            return velocity;
        }

        public boolean isSetVelocity() {
            return set_velocity;
        }

        public boolean isSwingHand() {
            return swing_hand;
        }

        public String description() {
            return description;
        }

        public HitEffects on_swing() {
            return on_swing;
        }

        public InteractionGroup setDescription(String description) {
            this.description = description;
            desc = Component.translatable(description);
            return this;
        }

        public Component getToolTip(ItemStack e, boolean advanced) {
            return desc;
        }

        public InteractionGroup addOverride(InteractionOverride io) {
            getOverrides().add(io);
            return this;
        }

        public List<InteractionOverride> getOverrides() {
            return overrides;
        }

        public boolean hasInteractionType(WeaponInteraction.InteractionType interactionType) {
            //bake types for faster lookup
            if (bakedTypes == null) {
                bakedTypes = new HashMap<>();
                for (WeaponInteraction wi : interactions) {
                    bakedTypes.putIfAbsent(wi.getInteractionType(), wi);
                }
            }
            return bakedTypes.containsKey(interactionType);
        }

        public WeaponInteraction getInteractionOfType(WeaponInteraction.InteractionType interactionType) {
            //bake types for faster lookup
            if (bakedTypes == null) {
                bakedTypes = new HashMap<>();
                for (WeaponInteraction wi : interactions) {
                    bakedTypes.putIfAbsent(wi.getInteractionType(), wi);
                }
            }
            return bakedTypes.get(interactionType);
        }

        public List<WeaponInteraction> getInteractions() {
            return interactions;
        }

        public InteractionGroup setInteractions(List<WeaponInteraction> interactions) {
            this.interactions = ImmutableList.copyOf(interactions);
            return this;
        }
    }

    public static abstract class WeaponInteraction {
        public WeaponInteraction() {

        }

        public static WeaponInteraction readFromByte(FriendlyByteBuf f) {
            switch (InteractionType.values()[f.readInt()]) {
                case SWEEP -> {
                    return SweepAttack.NOTHING.clone().read(f);
                }
                case USE -> {
                    return new Use().read(f);
                }
                case ANIMATE -> {
                    return new Animation().read(f);
                }
                case THROW -> {
                    return new Throw().read(f);
                }
            }
            return SweepAttack.NOTHING.clone();
        }

        public abstract InteractionType getInteractionType();

        public abstract WeaponInteraction clone();

        public void write(FriendlyByteBuf f) {
            f.writeInt(getInteractionType().ordinal());
        }

        public WeaponInteraction read(FriendlyByteBuf f) {
            return this;
        }

        public HitInfo getHitInfo() {
            return SweepAttack.DEFAULT_NONE.interactions.get(0).getHitInfo();
        }

        public InteractionGroup asGroup() {
            InteractionGroup ret = new InteractionGroup();
            ret.setInteractions(List.of(this));
            return ret;
        }

        public enum InteractionType {
            SWEEP, USE, THROW, ANIMATE
        }
    }

    public record InteractionOverride(Condition condition, InteractionGroup override) {
        //fixme anim overrides do not inherit added trail effects (which are weapon and trail by default, where did I define this???)
    }

    public static class GroupDeserializer implements JsonDeserializer<InteractionGroup> {

        @Override
        public InteractionGroup deserialize(JsonElement json,
                                            Type typeOfT,
                                            JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                //could be either a full fledged def or just a single interaction, possibly containing overrides
                //extract partial overrides first
                final JsonObject baseObj = JsonUtils.parseSyntacticSugar(json).getAsJsonObject();
                JsonElement overObj = baseObj.remove("overrides");
                InteractionGroup ret = NAIVE.fromJson(baseObj, InteractionGroup.class);
                if (ret.getInteractions().isEmpty()) {
                    ret.setInteractions(List.of(GSON.fromJson(json, WeaponInteraction.class)));
                }
                if (overObj != null && overObj.isJsonArray()) {
                    JsonArray overrides = overObj.getAsJsonArray();
                    for (JsonElement override : overrides.asList()) {
                        if (override.isJsonObject()) {
                            JsonObject obj = override.getAsJsonObject();
                            if (obj.has("override") && obj.has("condition")) {
                                Condition c = ActionJsonAdapters.gson.fromJson(obj.get("condition"), Condition.class);
                                final JsonObject merged = JsonUtils.deepMerge(baseObj, obj.get("override").getAsJsonObject());
                                InteractionGroup wi = GSON.fromJson(merged, InteractionGroup.class);
                                ret.overrides.add(new InteractionOverride(c, wi));
                            }
                        }
                    }
                }

                if (ret.debug) {
                    WarDance.LOGGER.info("DEBUG - interaction group deserialized into");
                    WarDance.LOGGER.info(GSON.toJson(ret));
                }
                return ret;
            }
            if (json.isJsonArray()) {
                InteractionGroup ret=new InteractionGroup();
                //a simple list of interactions with no override, tooltip, or velocity. I'm not sure why you would want this.
                List<WeaponInteraction> list = context.deserialize(json, new TypeToken<ArrayList<WeaponInteraction>>() {}.getType());
                ret.setInteractions(list);
                return ret;
            }
            InteractionGroup ret = ActionJsonAdapters.gson.fromJson(json, InteractionGroup.class);
            return ret;
        }
    }

    public static class InteractionDeserializer implements JsonDeserializer<WeaponInteraction> {
        @Override
        public WeaponInteraction deserialize(JsonElement json,
                                             Type typeOfT,
                                             JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) return null;
            JsonObject baseObj = json.getAsJsonObject();
            WeaponInteraction ret = asSweepAttack(baseObj);
            if (baseObj.has("type")) {
                //others go in here
                String type = baseObj.get("type").getAsString();
                if (type.toLowerCase(Locale.ROOT).equals("use")) ret = asUse(baseObj);
                if (type.toLowerCase(Locale.ROOT).equals("animation")) ret = asAnimation(baseObj);
                if (type.toLowerCase(Locale.ROOT).equals("throw")) ret = asThrow(baseObj);
            }
            return ret;
        }

        private Use asUse(JsonObject sub) {
            return GSON.fromJson(sub, Use.class);
        }

        private Throw asThrow(JsonObject sub) {
            return GSON.fromJson(sub, Throw.class);
        }

        private SweepAttack asSweepAttack(JsonObject sub) {
            SweepAttack sweep = GSON.fromJson(sub, SweepAttack.class);

            if (sub.has("attack_info")) {
                sweep.attack_info = GSON.fromJson(sub.get("attack_info"), HitInfo.class);
            } else sweep.attack_info = GSON.fromJson(sub, HitInfo.class);
            return sweep;
        }

        private Animation asAnimation(JsonObject sub) {
            Animation anim = GSON.fromJson(sub, Animation.class);
            if (sub.has("animations")) {
                if (anim.animations.isEmpty())
                    anim.animations = GSON.fromJson(sub.get("animations"), new TypeToken<List<MotionManager>>() {
                    }.getType());
            } else {
                List<MotionManager> added = new ArrayList<>();
                added.add(GSON.fromJson(sub, MotionManager.class));
                anim.animations = added;
            }
            return anim;
        }
    }
}
