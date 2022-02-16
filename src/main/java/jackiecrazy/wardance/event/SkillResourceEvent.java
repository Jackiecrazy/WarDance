package jackiecrazy.wardance.event;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class SkillResourceEvent extends LivingEvent {
    private final Skill s;
    private float might, spirit;

    public SkillResourceEvent(LivingEntity entity, Skill skill) {
        super(entity);
        s = skill;
        might = skill.mightConsumption(entity);
        spirit = skill.spiritConsumption(entity);
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
}
