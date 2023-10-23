package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.HashSet;
import java.util.UUID;

public class FrostFang extends WarCry {
    private static final AttributeModifier luck = new AttributeModifier(UUID.fromString("77723885-afb9-4937-9c02-612ee5b6135a"), "frost fang bonus", 2, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier speed = new AttributeModifier(UUID.fromString("07430131-9baa-47b4-a51c-9a6f48d564f4"), "frost fang bonus", 0.4, AttributeModifier.Operation.MULTIPLY_BASE);
    private final HashSet<String> tag = makeTag("chant", ProcPoints.melee, ProcPoints.on_being_hurt, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> chant = makeTag(SkillTags.chant, SkillTags.melee, SkillTags.state);

    @Override
    protected int getDuration(float might) {
        return (int) (might * 20);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint.getPhase() != EventPriority.LOWEST) return;
        if (procPoint instanceof LivingAttackEvent lae && state == STATE.INACTIVE && lae.getEntity() != caster) {
            activate(caster, 2, stats.getArbitraryFloat());
            stats.setMaxDuration(2);
        } else if (procPoint instanceof ParryEvent cpe && state == STATE.ACTIVE && cpe.getEntity() != caster) {
            cpe.setPostureConsumption(cpe.getPostureConsumption() * (1 + stats.getArbitraryFloat() / 10));
            if (caster.level() instanceof ServerLevel server)
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()).setPos(target.blockPosition()), target.getX(), target.getY(), target.getZ(), (int) stats.getArbitraryFloat() * 20, target.getBbWidth(), target.getBbHeight() / 2, target.getBbWidth(), 0.5f);

        } else if (procPoint instanceof LivingHurtEvent cpe && state == STATE.ACTIVE && cpe.getEntity() != caster) {
            if (!cpe.isCanceled()) {
                if (cpe.getSource() instanceof CombatDamageSource cds) {
                    cds.setProcSkillEffects(true);
                }
                cpe.setAmount(cpe.getAmount() * (1 + stats.getArbitraryFloat() / 10));
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            prev.setDuration(2);
            prev.setArbitraryFloat(0);
            prev.setMaxDuration(0);
        }
        return instantCast(prev, from, to);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.INACTIVE) {

            stats.decrementDuration(0.05f);
            if (stats.getDuration() <= 0) {
                stats.setArbitraryFloat(Math.min(5f * SkillUtils.getSkillEffectiveness(caster), stats.getArbitraryFloat() + 1f * SkillUtils.getSkillEffectiveness(caster)));
                stats.setDuration(2);
                stats.setMaxDuration(0);
                stats.markDirty();
            }
        }
        return activeTick(stats);
    }

    @Override
    public void onEquip(LivingEntity caster) {
        SkillUtils.addAttribute(caster, Attributes.LUCK, luck);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.removeAttribute(caster, Attributes.LUCK, luck);
        super.onUnequip(caster, stats);
    }

    @Override
    public boolean displaysInactive(LivingEntity caster, SkillData stats) {
        return true;
    }
}
