package jackiecrazy.wardance.skill.styles.four;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.event.SkillResourceEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

public class BloodTax extends SkillStyle {

    public BloodTax() {
        super(4);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof SkillResourceEvent sre && sre.getPhase() == EventPriority.HIGHEST && sre.getSpirit() > 0) {
            caster.hurt(CombatDamageSource.causeSelfDamage(caster), sre.getSpirit()/ SkillUtils.getSkillEffectiveness(caster));
            sre.setSpirit(0);
        }
    }
}
