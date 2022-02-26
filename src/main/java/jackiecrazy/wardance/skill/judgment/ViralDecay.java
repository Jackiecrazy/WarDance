package jackiecrazy.wardance.skill.judgment;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.SkillUtils;
import jackiecrazy.wardance.utils.TargetingUtils;
import jackiecrazy.wardance.utils.WarColors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class ViralDecay extends Judgment {
    public static final DamageSource VIRUS = (new DamageSource("decay")).setDamageBypassesArmor();

    @SubscribeEvent
    public static void virus(LivingDeathEvent e) {
        Marks.getCap(e.getEntityLiving()).getActiveMark(WarSkills.VIRAL_DECAY.get()).ifPresent((a) -> detonate(e.getEntityLiving(), a.getCaster(e.getEntityLiving().world)));
    }

    private static void detonate(LivingEntity target, LivingEntity caster) {
        target.attackEntityFrom(new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true).setDamageTyping(CombatDamageSource.TYPE.TRUE).setDamageBypassesArmor().setDamageIsAbsolute(), target.getHealth() / 10);
        SkillUtils.createCloud(target.world, caster, target.getPosX(), target.getPosY(), target.getPosZ(), 3, ParticleTypes.EXPLOSION);
        final List<LivingEntity> list = target.world.getLoadedEntitiesWithinAABB(LivingEntity.class, target.getBoundingBox().grow(3), (b) -> !TargetingUtils.isAlly(b, caster));
        for (LivingEntity enemy : list) {
            enemy.attackEntityFrom(VIRUS, 2);
            Marks.getCap(enemy).mark(new SkillData(WarSkills.VIRAL_DECAY.get(), 6).setArbitraryFloat(1).setCaster(caster));
        }
    }

    @Override
    public Color getColor() {
        return WarColors.VIOLET;
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            if (existing.getArbitraryFloat() >= 2) {
                detonate(target, caster);
                return null;
            }
        }
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target, int stack, SkillData sd) {
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (target.ticksExisted % 20 == 0)
            target.attackEntityFrom(VIRUS, sd.getArbitraryFloat() / 2);
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
    }
}
