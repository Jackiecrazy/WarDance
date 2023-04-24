package jackiecrazy.wardance.skill.styles.four;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.entity.FakeExplosion;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

public class UnstableSpirit extends SkillStyle {

    public UnstableSpirit() {
        super(4);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof SkillCastEvent sre && sre.getPhase() == EventPriority.HIGHEST) {
            DamageSource ds=new CombatDamageSource("explosion", caster).setProcSkillEffects(true).setSkillUsed(this).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setKnockbackPercentage(0).setPostureDamage(3).setExplosion();
            FakeExplosion.explode(caster.level, null, caster.getX(), caster.getY(), caster.getZ(), 3, true, ds, 3);
        }
    }
}
