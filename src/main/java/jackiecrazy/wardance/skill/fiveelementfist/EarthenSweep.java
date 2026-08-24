package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.event.ConsumePostureEvent;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.Animation;
import jackiecrazy.wardance.config.weapon.interactions.SweepAttack;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.event.BasicSweepEvent;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EarthenSweep extends FiveElementFist {
    private static final List<MotionFrame> SWEEP = List.of(
            new MotionFrame(new Vec3(1.0F, 0.0F, 0.3F),
                            new Vec3(0.0F, 0.0F, 1.0F))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON).setHit(new HitInfo(0, 1, 2, false, false, 1))),
            new MotionFrame(new Vec3(0.0F, 0.0F, 1.0),
                            new Vec3(0.0F, 0.0F, 1.0F))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)),
            new MotionFrame(new Vec3(-1.0F, 0.0F, 0.3F),
                            new Vec3(0.0F, 0.0F, 1.0F)));
    private static final MotionManager EARTH = new MotionManagers.DefinitionMM(new MotionGroup(SWEEP, EasingFunctionEnum.IN_OUT_SINE, 5));
    public static final WeaponInteractions.InteractionGroup EARTH_SWEEP = new Animation().setAction(EARTH).asGroup();

    @Override
    WeaponInteractions.InteractionGroup getSweep() {
        return EARTH_SWEEP;
    }

    @Override
    WeaponStats.AttackState toReplace() {
        return WeaponStats.AttackState.STANDING;
    }
}
