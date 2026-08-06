package jackiecrazy.wardance.event;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;

@Cancelable
public class SkillResourceEvent extends LivingEvent {
    private final Skill s;
    private final LivingEntity targ;
    private int spirit;

    public SkillResourceEvent(LivingEntity entity, @Nullable LivingEntity target, Skill skill) {
        super(entity);
        s = skill;
        spirit = skill.spiritCost(entity);
        targ = target;
    }

    public int getSpirit() {
        return spirit;
    }

    public SkillResourceEvent setSpirit(int spirit) {
        this.spirit = spirit;
        return this;
    }

    public Skill getSkill() {
        return s;
    }

    public LivingEntity getTarget() {
        return targ;
    }
}
