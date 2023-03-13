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
    private float might, spirit;

    public SkillResourceEvent(LivingEntity entity, @Nullable LivingEntity target, Skill skill) {
        super(entity);
        s = skill;
        might = skill.mightConsumption(entity);
        spirit = skill.spiritConsumption(entity);
        targ = target;
    }

    public float getMight() {
        return might;
    }

    public SkillResourceEvent setMight(float might) {
        this.might = might;
        return this;
    }

    public float getSpirit() {
        return spirit;
    }

    public SkillResourceEvent setSpirit(float spirit) {
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
