package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import jackiecrazy.wardance.event.ConsumePostureEvent;
import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Overpower extends IronGuard {
    private static final List<MotionFrame> CHOP = List.of(
            new MotionFrame(new Vec3(0.0F, 1.0F, -1.0F),
                            new Vec3(0.0F, 0.0F, 1.0F))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)),
            new MotionFrame(new Vec3(0.0F, 1.0F, 0.2),
                            new Vec3(0.0F, 0.0F, 1.0F))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON).setRange(5).setHit(new HitInfo(1,1.5,2,false,true,1))),
            new MotionFrame(new Vec3(0.0F, -0.5F, 1.0F),
                            new Vec3(0.0F, 0.0F, 1.0F)));
    private static final MotionManager RIPOSTE = new MotionManagers.DefinitionMM(new MotionGroup(CHOP, EasingFunctionEnum.IN_SINE, 10));

    @Override
    protected void parry(LivingEntity caster, ConsumePostureEvent procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (state == STATE.COOLING) return;
        if(procPoint instanceof MeleePostureEvent.Defense bl && bl.getAttacker()!=null && bl.success()&&cast(caster, -999)) {
            CombatUtils.triggerSteveTime(caster, 15);
            CombatData.getCap(bl.getAttacker()).consumePosture(caster, bl.getPostureConsumption(), ICombatCapability.BreachLevel.NO);
            bl.setPostureConsumption(0);
            FlyingWeaponData.getCap(caster).scheduleAction(bl.getDefendingHand(), RIPOSTE, true, 8, 0);
            //markUsed(caster);
            bl.setCanceled(true);
        }
    }
}
