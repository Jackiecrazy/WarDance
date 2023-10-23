package jackiecrazy.wardance.advancement;

import com.google.gson.JsonObject;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class SkillChallengeTrigger extends SimpleCriterionTrigger<SkillChallengeTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation(WarDance.MODID, "skill_challenge");

    public ResourceLocation getId() {
        return ID;
    }

    @Nonnull
    public TriggerInstance createInstance(JsonObject obj, @Nonnull ContextAwarePredicate entity, DeserializationContext ctx) {
        SkillPredicate skill = SkillPredicate.fromJson(obj.get("skilldata"));
        return new TriggerInstance(entity, skill);
    }

    public void trigger(ServerPlayer player, SkillData sd) {
        this.trigger(player, (pred) -> pred.matches(sd));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final SkillPredicate data;

        public TriggerInstance(ContextAwarePredicate cap, SkillPredicate skilldata) {
            super(SkillChallengeTrigger.ID, cap);
            data = skilldata;
        }

        public boolean matches(SkillData dt) {
            return this.data.matches(dt);
        }

        public static TriggerInstance skill(Skill s) {
            return new TriggerInstance(ContextAwarePredicate.ANY, new SkillPredicate(s));
        }

        @Nonnull
        public JsonObject serializeToJson(@Nonnull SerializationContext obj) {
            JsonObject jsonobject = super.serializeToJson(obj);
            jsonobject.add("skilldata", data.serializeToJson());
            return jsonobject;
        }
    }
}