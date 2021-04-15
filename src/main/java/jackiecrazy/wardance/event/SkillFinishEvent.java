package jackiecrazy.wardance.event;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public class SkillFinishEvent extends LivingEvent {
    private final Skill s;
    private final boolean yes;

    public SkillFinishEvent(LivingEntity entity, Skill skill, boolean success) {
        super(entity);
        s = skill;
        yes = success;
    }

    public Skill getSkill() {
        return s;
    }

    public boolean isSuccessful() {return yes;}
}
