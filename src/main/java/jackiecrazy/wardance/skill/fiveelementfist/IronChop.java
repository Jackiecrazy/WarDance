package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.config.weapon.interactions.Animation;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IronChop extends FiveElementFist {

    private static final List<MotionFrame> SWEEP = List.of(
            new MotionFrame(new Vec3(0.0F, 1.0F, 0.3F),
                            new Vec3(0.0F, 0.0F, 1.0F))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON).setHit(new HitInfo(2, 1, 1, false, false, 1).withKBDir(new Vec3(0,-2,0)))),
            new MotionFrame(new Vec3(0.0F, 0.0F, 1.0),
                            new Vec3(0.0F, 0.0F, 1.0F))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)),
            new MotionFrame(new Vec3(0.0F, -0.2F, 1.0F),
                            new Vec3(0.0F, 0.0F, 1.0F)));
    private static final MotionManager IRON = new MotionManagers.DefinitionMM(new MotionGroup(SWEEP, EasingFunctionEnum.IN_SINE, 5));
    public static final WeaponInteractions.InteractionGroup IRON_CHOP = new Animation().setAction(IRON).asGroup().withSwingEffect(new HitEffects().setVelocity(new Vec3(0, -0.3, 0)));

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof MeleePostureEvent.Block e && e.getEntity() == caster && procPoint.getPhase() == EventPriority.HIGHEST && stats.isCondition()) {
            e.setPostureConsumption(0);
            e.setResult(Event.Result.ALLOW);
            if (caster.level() instanceof ServerLevel s)
                for (int reps = 0; reps < 40; reps++) {
                    Vec3 startAt = caster.position().add((((caster.tickCount+reps) * 5) % caster.getBbWidth()) - caster.getBbWidth() / 2, (((caster.tickCount+reps) * 31) % caster.getBbHeight()), (((caster.tickCount+reps) * 17) % caster.getBbWidth()) - caster.getBbWidth() / 2);
                    Vec3 move = startAt.subtract(caster.position()).normalize();
                    s.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()).setPos(caster.blockPosition()), startAt.x, startAt.y, startAt.z, 0, move.x, move.y, move.z, 1);
                }
            stats.flagCondition(false);
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    WeaponInteractions.InteractionGroup getSweep() {
        return IRON_CHOP;
    }
}
