package jackiecrazy.wardance.config.weapon.interactions;

import com.google.gson.*;
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
    public static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(WeaponInteraction.class, new WeaponDeserializer())
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


            .create();

    public static abstract class WeaponInteraction {
        private Vec3 velocity = Vec3.ZERO;
        private boolean set_velocity = false;
        private boolean swingHand = true;
        private HitEffects on_swing=new HitEffects();

        public HitEffects on_swing() {
            return on_swing;
        }

        public WeaponInteraction addOverride(InteractionOverride io){
            getOverrides().add(io);
            return this;
        }

        public List<InteractionOverride> getOverrides() {
            return overrides;
        }

        private List<InteractionOverride> overrides = new ArrayList<>();

        public WeaponInteraction() {
            velocity = Vec3.ZERO;
            set_velocity = false;
            swingHand = true;
        }

        public static WeaponInteraction readFromByte(FriendlyByteBuf f) {
            switch (TYPE.values()[f.readInt()]) {
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

        public Vec3 getVelocity() {
            return velocity;
        }

        public boolean isSetVelocity() {
            return set_velocity;
        }

        public boolean isSwingHand() {
            return swingHand;
        }

        public abstract TYPE getInteractionType();

        public Component getToolTip(ItemStack e, boolean advanced) {
            return Component.translatable("wardance.tooltip.attacks." + getInteractionType().toString().toLowerCase(Locale.ROOT));
        }

        public abstract WeaponInteraction clone();

        public void write(FriendlyByteBuf f) {
            f.writeInt(getInteractionType().ordinal());
            f.writeVector3f(velocity.toVector3f());
            f.writeBoolean(set_velocity);
            f.writeBoolean(swingHand);
        }

        public WeaponInteraction read(FriendlyByteBuf f) {
            velocity = new Vec3(f.readVector3f());
            set_velocity = f.readBoolean();
            swingHand = f.readBoolean();
            return this;
        }

        public HitInfo getHitInfo() {
            return SweepAttack.DEFAULT_NONE.getHitInfo();
        }

        public enum TYPE {
            SWEEP,
            USE,
            THROW,
            ANIMATE
        }
    }

    public record InteractionOverride(Condition condition, WeaponInteraction override) {
    }

    public static class WeaponDeserializer implements JsonDeserializer<WeaponInteraction> {
        @Override
        public WeaponInteraction deserialize(JsonElement json,
                                             Type typeOfT,
                                             JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) return null;
            JsonObject baseObj = json.getAsJsonObject();
            //extract partial overrides first
            JsonElement overObj = baseObj.remove("overrides");
            WeaponInteraction ret = asSweepAttack(baseObj);
            if (baseObj.has("type")) {
                //others go in here
                String type = baseObj.get("type").getAsString();
                if (type.toLowerCase(Locale.ROOT).equals("use")) ret = asUse(baseObj);
                if (type.toLowerCase(Locale.ROOT).equals("animation")) ret = asAnimation(baseObj);
                if (type.toLowerCase(Locale.ROOT).equals("throw")) ret = asThrow(baseObj);
            }
            if (overObj != null && overObj.isJsonArray()) {
                JsonArray overrides = overObj.getAsJsonArray();
                for (JsonElement override : overrides.asList()) {
                    if (override.isJsonObject()) {
                        JsonObject obj = override.getAsJsonObject();
                        if (obj.has("override") && obj.has("condition")) {
                            Condition c = ActionJsonAdapters.gson.fromJson(obj.get("condition"), Condition.class);
                            final JsonObject merged = JsonUtils.deepMerge(baseObj, obj.get("override").getAsJsonObject());
                            WeaponInteraction wi = GSON.fromJson(merged, WeaponInteraction.class);
                            ret.overrides.add(new InteractionOverride(c, wi));
                        }
                    }
                }
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
            if (sub.has("actions")) {
                anim.actions = GSON.fromJson(sub.get("actions"), ArrayList.class);
            } else {
                List<MotionManager> added = new ArrayList<>();
                added.add(GSON.fromJson(sub, MotionManager.class));
                anim.actions = added;
            }
            return anim;
        }
    }
}
