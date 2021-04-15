package jackiecrazy.wardance.event;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class SkillCastEvent extends LivingEvent {
    private final Skill s;
    public SkillCastEvent(LivingEntity entity, Skill skill) {
        super(entity);
        s=skill;
    }
    public Skill getSkill(){
        return s;
    }
}
