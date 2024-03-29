package jackiecrazy.wardance.skill.judgment;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class ViralDecay extends Judgment {

    @SubscribeEvent
    public static void virus(LivingDeathEvent e) {
        Marks.getCap(e.getEntity()).getActiveMark(WarSkills.VIRAL_DECAY.get()).ifPresent((a) -> detonate(e.getEntity(), a.getCaster(e.getEntity().level())));
    }

    private static void detonate(LivingEntity target, LivingEntity caster) {
        Marks.getCap(target).removeMark(WarSkills.VIRAL_DECAY.get());
        SkillUtils.createCloud(target.level(), caster, target.getX(), target.getY(), target.getZ(), 3, ParticleTypes.EXPLOSION);
        final List<LivingEntity> list = target.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3), (b) -> !TargetingUtils.isAlly(b, caster));
        for (LivingEntity enemy : list) {
            if (enemy == target) continue;
            Marks.getCap(enemy).mark(new SkillData(WarSkills.VIRAL_DECAY.get(), 6).setArbitraryFloat(1).setCaster(caster));
            enemy.hurt(new CombatDamageSource(caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL), 2);
        }
        target.hurt(new CombatDamageSource(caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true).setDamageTyping(CombatDamageSource.TYPE.TRUE).bypassArmor().bypassMagic(), target.getHealth() / 10);
    }

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target, int stack, SkillData sd) {
        super.performEffect(caster, target, stack, sd);
        if(stack>=3)
            detonate(target, caster);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (target.tickCount % 20 == 0)
            target.hurt(new CombatDamageSource(caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL), sd.getArbitraryFloat()*SkillUtils.getSkillEffectiveness(caster));
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
    }
}
