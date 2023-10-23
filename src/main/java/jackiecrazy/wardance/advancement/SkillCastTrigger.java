package jackiecrazy.wardance.advancement;

import com.google.gson.JsonObject;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

import javax.annotation.Nonnull;

public class SkillCastTrigger extends SimpleCriterionTrigger<SkillCastTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation(WarDance.MODID, "cast_skill");

    public ResourceLocation getId() {
        return ID;
    }

    @Nonnull
    public TriggerInstance createInstance(JsonObject obj, @Nonnull ContextAwarePredicate entity, DeserializationContext ctx) {
        SkillPredicate skill = SkillPredicate.fromJson(obj.get("skilldata"));
        ContextAwarePredicate target = EntityPredicate.fromJson(obj, "target", ctx);
        return new TriggerInstance(entity, target, skill);
    }

    public void trigger(ServerPlayer player, Entity target, SkillData sd) {
        LootContext targ = EntityPredicate.createContext(player, player);
        if (target != null)
            targ = EntityPredicate.createContext(player, target);
        LootContext caster = EntityPredicate.createContext(player, player);
        LootContext finalTarg = targ;
        this.trigger(player, (pred) -> pred.matches(caster, finalTarg, sd));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final SkillPredicate data;
        private final ContextAwarePredicate caster, target;

        public TriggerInstance(ContextAwarePredicate caster, ContextAwarePredicate target, SkillPredicate skilldata) {
            super(SkillCastTrigger.ID, caster);
            this.target = target;
            this.caster = caster;
            data = skilldata;
        }

        public boolean matches(LootContext caster, LootContext target, SkillData dt) {
            if (!this.data.matches(dt)) {
                return false;
            } else {
                return this.caster.matches(caster) && this.target.matches(target);
            }
        }

        @Nonnull
        public JsonObject serializeToJson(@Nonnull SerializationContext obj) {
            JsonObject jsonobject = super.serializeToJson(obj);
            jsonobject.add("damage", this.target.toJson(obj));
            jsonobject.add("entity", this.caster.toJson(obj));
            jsonobject.add("skill", data.serializeToJson());
            return jsonobject;
        }
    }
}