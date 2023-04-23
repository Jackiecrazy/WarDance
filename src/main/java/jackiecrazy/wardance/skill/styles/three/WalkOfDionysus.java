package jackiecrazy.wardance.skill.styles.three;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

public class WalkOfDionysus extends SkillStyle {

    public WalkOfDionysus() {
        super(3);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (!CombatData.getCap(caster).isVulnerable() && CombatData.getCap(caster).consumeMight(1)) {
            CombatData.getCap(caster).knockdown(CombatConfig.knockdownDuration);
            CombatData.getCap(caster).updateDefenselessStatus();
            fall(caster);
        }
        return false;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof StunEvent se && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (se.getEntity() == caster) {
                se.setKnockdown(true);
                fall(caster);
            }
        }
    }

    private void fall(LivingEntity caster) {
        //area damage
        SkillUtils.createCloud(caster.level, caster, caster.getX(), caster.getY(), caster.getZ(), 7, ParticleTypes.LARGE_SMOKE);
        for (LivingEntity entity : caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBoxForCulling().inflate(5), a -> !TargetingUtils.isAlly(a, caster))) {
            CombatData.getCap(entity).consumePosture(caster, 5);
        }
    }
}
