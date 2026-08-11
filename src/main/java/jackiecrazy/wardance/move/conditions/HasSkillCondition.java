package jackiecrazy.wardance.move.conditions;

import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.argument.entity.CasterEntityArgument;
import jackiecrazy.footwork.move.condition.Condition;
import jackiecrazy.footwork.move.utils.ArgumentContext;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class HasSkillCondition extends Condition {
    private Argument<ResourceLocation> skill;
    private transient Skill me;
    private Skill.STATE state = null;
    private Argument<Entity> reference= CasterEntityArgument.INSTANCE;

    @Override
    public Boolean resolve(ArgumentContext argumentContext) {
        if (this.me == null) {
            this.me = WarSkills.SUPPLIER.get().getValue(this.skill.resolve(argumentContext));
        }

        if (this.me != null) {
            Entity ent = this.reference.resolve(argumentContext);
            if (ent instanceof LivingEntity e) {
                if (state != null)
                    return CasterData.getCap(e).isSkillEquipped(me) && CasterData.getCap(e).getSkillState(me) == state;
                return CasterData.getCap(e).isSkillEquipped(me);
            }
        }

        return false;
    }
}
