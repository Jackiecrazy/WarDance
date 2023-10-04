package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.ConsumePostureEvent;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.HashSet;

public class Timberfall extends WarCry {
    private final HashSet<String> tag = makeTag("chant", ProcPoints.melee, ProcPoints.modify_crit, ProcPoints.on_hurt, ProcPoints.attack_might, ProcPoints.on_being_hurt, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> no = none;//.getTagFromContents(new HashSet<>(Collections.emptyList()));

    @Override
    protected int getDuration(float might) {
        return (int) might * 2 - 2;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return activeTick(stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint.getPhase() != EventPriority.LOWEST) return;
        if (procPoint instanceof LivingAttackEvent lae && DamageUtils.isMeleeAttack(lae.getSource()) && state == STATE.ACTIVE && lae.getEntity() == target) {
            markUsed(caster);
        } else if (procPoint instanceof ParryEvent cpe && state == STATE.ACTIVE && cpe.getEntity() == target) {
            cpe.setPostureConsumption(cpe.getPostureConsumption() * 1.4f * SkillUtils.getSkillEffectiveness(caster));
            if (caster.level instanceof ServerLevel server)
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()).setPos(target.blockPosition()), target.getX(), target.getY(), target.getZ(), (int) stats.getArbitraryFloat() * 20, target.getBbWidth(), target.getBbHeight() / 2, target.getBbWidth(), 0.5f);

        } else if (procPoint instanceof ConsumePostureEvent cpe && state == STATE.ACTIVE && cpe.getAbove() <= 0 && cpe.getEntity() == target && cpe.getAmount() > CombatData.getCap(target).getPosture()) {
            if (!cpe.isCanceled()) CombatData.getCap(target).addFracture(caster, 1);
        } else if (procPoint instanceof LivingHurtEvent cpe && DamageUtils.isMeleeAttack(cpe.getSource()) && state == STATE.ACTIVE && cpe.getEntity() == target) {
            if (!cpe.isCanceled()) {
                if (cpe.getSource() instanceof CombatDamageSource cds) {
                    cds.setProcSkillEffects(true);
                    cds.setSkillUsed(this);
                }
                cpe.setAmount(cpe.getAmount() * 1.4f * SkillUtils.getSkillEffectiveness(caster));
            }
        } else if (procPoint instanceof SkillCastEvent sce && state == STATE.INACTIVE && sce.getEntity() == caster) {
            activate(caster, 3);
        }
        super.onProc(caster, procPoint, state, stats, target);
    }
}
