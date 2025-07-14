package jackiecrazy.wardance.event;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class SkillCastEvent extends LivingEvent {
    private final Skill s;
    private final int spirit;
    private final double oeffectiveness;
    private final LivingEntity target;
    private float duration;
    private float arbitrary;
    private double effectiveness;
    private boolean flag;

    public SkillCastEvent(LivingEntity entity, LivingEntity t, Skill skill, double effectiveness, int spi, float dur, boolean fla, float arb) {
        super(entity);
        s = skill;
        oeffectiveness = this.effectiveness = effectiveness;
        duration = dur;
        arbitrary = arb;
        flag = fla;
        spirit = spi;
        target = t;
    }

    public double getEffectiveness() {
        return effectiveness;
    }

    public SkillCastEvent setEffectiveness(double effectiveness) {
        this.effectiveness = effectiveness;
        return this;
    }

    public double getOriginalEffectiveness() {
        return oeffectiveness;
    }

    public int getSpirit() {
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

    public LivingEntity getTarget() {
        return target;
    }
}
