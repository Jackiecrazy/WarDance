package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.event.FractureEvent;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.event.SweepEvent;
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

import java.awt.*;
import java.util.HashSet;

public class Timberfall extends WarCry {
    private static final Color c=new Color(71, 48, 9);
    private final HashSet<String> tag = makeTag("chant", ProcPoints.melee, ProcPoints.modify_crit, ProcPoints.on_hurt, ProcPoints.attack_might, ProcPoints.on_being_hurt, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> no = none;//.getTagFromContents(new HashSet<>(Collections.emptyList()));

    @Override
    protected int getDuration(float might) {
        return (int) might * 2 - 2;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if(to==STATE.ACTIVE) {
            prev.setState(STATE.ACTIVE);
            return true;
        }
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
        }
        return super.onStateChange(caster, prev, from, to);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return activeTick(stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint.getPhase() != EventPriority.LOWEST) return;
        if (procPoint instanceof LivingAttackEvent lae && DamageUtils.isMeleeAttack(lae.getSource()) && state == STATE.ACTIVE && lae.getEntity() == target) {
            markUsed(caster, true);
        } else if (procPoint instanceof ParryEvent cpe && state == STATE.ACTIVE && cpe.getEntity() == target) {
            cpe.setPostureConsumption(cpe.getPostureConsumption() * 1.4f * SkillUtils.getSkillEffectiveness(caster));
            markUsed(caster, true);
            if (caster.level() instanceof ServerLevel server)
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()).setPos(target.blockPosition()), target.getX(), target.getY(), target.getZ(), 40, target.getBbWidth(), target.getBbHeight() / 2, target.getBbWidth(), 0.5f);
        } else if (procPoint instanceof FractureEvent cpe && state == STATE.ACTIVE && cpe.getEntity() == target) {
            if (!cpe.isCanceled()) cpe.addAmount(1);
        } else if (procPoint instanceof LivingHurtEvent cpe && stats.isCondition() && DamageUtils.isMeleeAttack(cpe.getSource()) && state == STATE.ACTIVE && cpe.getEntity() == target) {
            if (!cpe.isCanceled()) {
                if (cpe.getSource() instanceof CombatDamageSource cds) {
                    cds.setProcSkillEffects(true);
                    cds.setSkillUsed(this);
                }
                cpe.setAmount(cpe.getAmount() * 1.4f * SkillUtils.getSkillEffectiveness(caster));
            }
        } else if (procPoint instanceof SkillCastEvent sce && sce.getSkill()!=this && state == STATE.INACTIVE && sce.getEntity() == caster) {
            cast(caster, 3);
        }else if(procPoint instanceof SweepEvent se&& state==STATE.ACTIVE){
            se.setColor(Color.ORANGE);
        }
        super.onProc(caster, procPoint, state, stats, target);
    }
}
