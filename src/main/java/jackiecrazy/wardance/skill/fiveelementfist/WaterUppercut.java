package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.event.EntityAwarenessEvent;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.Animation;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaterUppercut extends FiveElementFist {
    private static final List<MotionFrame> SWEEP = List.of(
            new MotionFrame(new Vec3(0.0F, -0.8F, 0.3F),
                            new Vec3(0.0F, 0.0F, 1.0F))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON).setHit(new HitInfo(2, 1, 1, false, false, 1).withKBDir(new Vec3(0,2,0)))),
            new MotionFrame(new Vec3(0.0F, 0.0F, 1.0),
                            new Vec3(0.0F, 0.0F, 1.0F))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)),
            new MotionFrame(new Vec3(0.0F, 0.7F, 1.0F),
                            new Vec3(0.0F, 0.0F, 1.0F)));
    private static final MotionManager MANAGER = new MotionManagers.DefinitionMM(new MotionGroup(SWEEP, EasingFunctionEnum.IN_SINE, 5));
    public static final WeaponInteractions.InteractionGroup GROUP = new Animation().setAction(MANAGER).asGroup().withSwingEffect(new HitEffects().setVelocity(new Vec3(0, 0.7, 0)));

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof EntityAwarenessEvent.Attack e && procPoint.getPhase() == EventPriority.HIGHEST && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND) && e.getAttacker() == caster) {
            e.setAwareness(e.getOriginalAwareness() == StealthUtils.Awareness.ALERT ? StealthUtils.Awareness.DISTRACTED : StealthUtils.Awareness.UNAWARE);
        }
        if (procPoint instanceof LivingHurtEvent lhe && procPoint.getPhase() == EventPriority.HIGHEST && lhe.getEntity() != caster && CombatUtils.isUnarmed(caster, InteractionHand.MAIN_HAND)) {
            mark(caster, target, 1);
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    @Override
    WeaponInteractions.InteractionGroup getSweep() {
        return GROUP;
    }

    @Override
    WeaponStats.AttackState toReplace() {
        return WeaponStats.AttackState.AERIAL;
    }
}
