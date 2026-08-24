package jackiecrazy.wardance.skill.fiveelementfist;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.Animation;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class WoodenJab extends FiveElementFist {
    private static final List<MotionFrame> SWEEP = List.of(
            new MotionFrame(new Vec3(0.0F, 0.0F, 1.0F),
                            new Vec3(0.0F, 0.0F, 0.2F))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON).setHit(new HitInfo(1.5, 1, 1, false, false, 1))),
            new MotionFrame(new Vec3(0.0F, 0.0F, 1.0),
                            new Vec3(0.0F, 0.0F, 1.0F)));
    private static final MotionManager MANAGER = new MotionManagers.DefinitionMM(new MotionGroup(SWEEP, EasingFunctionEnum.IN_SINE, 5));
    public static final WeaponInteractions.InteractionGroup GROUP = new Animation().setAction(MANAGER).asGroup();

    private static final UUID u = UUID.fromString("1896391d-0d6c-4a3e-a4b5-5e3c9d573b80");

    @Override
    WeaponInteractions.InteractionGroup getSweep() {
        return GROUP;
    }

    @Override
    WeaponStats.AttackState toReplace() {
        return WeaponStats.AttackState.STANDING;
    }

    @Override
    protected void doAttack(LivingEntity caster, LivingEntity target) {
        StylishData.getCap(caster).addCombo(0.1f, this.getRegistryName().toString());
    }
}
