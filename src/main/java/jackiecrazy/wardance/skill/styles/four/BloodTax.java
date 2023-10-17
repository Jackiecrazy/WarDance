package jackiecrazy.wardance.skill.styles.four;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.event.SkillResourceEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
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
            caster.setHealth(caster.getHealth() - sre.getSpirit() * 2);
            sre.setSpirit(0);
        }
        if (procPoint instanceof LivingDamageEvent e && !DamageUtils.isTrueDamage(e.getSource()) && e.getPhase() == EventPriority.LOWEST && e.getEntity() == caster && caster != target) {
            //split damage
            double max = (1 - (caster.getHealth() / caster.getMaxHealth())) * SkillUtils.getSkillEffectiveness(caster);
            max = Math.min(max, 1);
            double absorbed = Math.min(e.getAmount() * max, CombatData.getCap(caster).getSpirit());
            e.setAmount((float) (e.getAmount() - absorbed));
            CombatData.getCap(caster).consumeSpirit((float) absorbed);
        }
    }
}
