package jackiecrazy.wardance.advancement;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;

public class SkillPredicate {
    public static final SkillPredicate ANY = new SkillPredicate(null);
    private final MinMaxBounds.Doubles duration;
    private final MinMaxBounds.Doubles maxDuration;
    private final MinMaxBounds.Doubles effectiveness;
    private final MinMaxBounds.Doubles stack;
    private final Skill skill;

    public SkillPredicate(Skill s) {
        this.duration = MinMaxBounds.Doubles.ANY;
        this.maxDuration = MinMaxBounds.Doubles.ANY;
        this.effectiveness = MinMaxBounds.Doubles.ANY;
        this.stack = MinMaxBounds.Doubles.ANY;
        skill = s;
    }

    public SkillPredicate(MinMaxBounds.Doubles duration, MinMaxBounds.Doubles maxDuration, MinMaxBounds.Doubles effectiveness, MinMaxBounds.Doubles stack, Skill skill) {
        this.duration = duration;
        this.maxDuration = maxDuration;
        this.effectiveness = effectiveness;
        this.stack = stack;
        this.skill = skill;
    }

    public static SkillPredicate fromJson(@Nullable JsonElement obj) {
        if (obj != null && !obj.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(obj, "skilldata");
            MinMaxBounds.Doubles dur = MinMaxBounds.Doubles.fromJson(jsonobject.get("duration"));
            MinMaxBounds.Doubles mdur = MinMaxBounds.Doubles.fromJson(jsonobject.get("max_duration"));
            MinMaxBounds.Doubles eff = MinMaxBounds.Doubles.fromJson(jsonobject.get("effectiveness"));
            MinMaxBounds.Doubles sta = MinMaxBounds.Doubles.fromJson(jsonobject.get("stack"));
            Skill s = Skill.getSkill(new ResourceLocation(GsonHelper.getAsString(jsonobject, "skill")));
            return new SkillPredicate(dur, mdur, eff, sta, s);
        } else {
            return ANY;
        }
    }

    public boolean matches(Skill s, double dur, double maxDur, double eff, double stack) {
        if (this == ANY) {
            return false;
        } else if (!this.duration.matches(dur)) {
            return false;
        } else if (skill != s) {
            return false;
        } else if (!this.maxDuration.matches(maxDur)) {
            return false;
        } else if (!this.effectiveness.matches(eff)) {
            return false;
        } else if (!this.stack.matches(stack)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean matches(SkillData sd) {
        return matches(sd.getSkill(), sd.getDuration(), sd.getMaxDuration(), sd.getEffectiveness(), sd.getArbitraryFloat());
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("duration", duration.serializeToJson());
            jsonobject.add("max_duration", maxDuration.serializeToJson());
            jsonobject.add("effectiveness", effectiveness.serializeToJson());
            jsonobject.add("stack", stack.serializeToJson());
            jsonobject.addProperty("skill", skill.getRegistryName().toString());

            return jsonobject;
        }
    }
}