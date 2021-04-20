package jackiecrazy.wardance.event;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;

@Event.HasResult
public class SkillCooldownEvent extends LivingEvent {
    private final float originalCooldown;
    private final Skill casting;
    private float cooldown;

    public SkillCooldownEvent(LivingEntity entity, Skill skill, float cooldown) {
        super(entity);
        originalCooldown = this.cooldown = cooldown;
        casting = skill;
    }

    public float getOriginalCooldown() {
        return originalCooldown;
    }

    public float getCooldown() {
        return cooldown;
    }

    public void setCooldown(float amount) {
        cooldown = amount;
    }

    public Skill getSkill() {return casting;}
}
