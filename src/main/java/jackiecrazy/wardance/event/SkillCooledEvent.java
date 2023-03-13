package jackiecrazy.wardance.event;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class SkillCooledEvent extends LivingEvent {
    private final Skill s;

    public SkillCooledEvent(LivingEntity entity, Skill skill) {
        super(entity);
        s = skill;
    }

    public Skill getSkill() {
        return s;
    }
}
