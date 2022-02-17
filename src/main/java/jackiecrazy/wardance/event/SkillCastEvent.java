package jackiecrazy.wardance.event;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class SkillCastEvent extends LivingEvent {
    private final Skill s;
    private final float might;
    private final float spirit;
    private float duration;
    private float arbitrary;
    private boolean flag;

    public SkillCastEvent(LivingEntity entity, Skill skill, float mig, float spi, float dur, boolean fla, float arb) {
        super(entity);
        s = skill;
        duration = dur;
        arbitrary = arb;
        flag = fla;
        might = mig;
        spirit = spi;
    }

    public float getMight() {
        return might;
    }

    public float getSpirit() {
        return spirit;
    }

    public float getDuration() {
        return duration;
    }

    public SkillCastEvent setDuration(float duration) {
        this.duration = duration;
        return this;
    }

    public float getArbitrary() {
        return arbitrary;
    }

    public SkillCastEvent setArbitrary(float arbitrary) {
        this.arbitrary = arbitrary;
        return this;
    }

    public boolean isFlag() {
        return flag;
    }

    public SkillCastEvent setFlag(boolean flag) {
        this.flag = flag;
        return this;
    }

    public Skill getSkill() {
        return s;
    }
}
