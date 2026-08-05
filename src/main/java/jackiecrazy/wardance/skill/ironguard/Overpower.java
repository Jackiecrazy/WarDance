package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.FrameEffects;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
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
    protected static final List<MotionFrame> CHOP = List.of(
            new MotionFrame(new Vec3(0.0F, 1.0F, -1.0F),
                            new Vec3(0.0F, 0.0F, 1.0F))
                    .setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON).setHit(new HitInfo(1,1,1.3,false,true,1))),
            new MotionFrame(new Vec3(0.0F, 1.0F, 0.2),
                            new Vec3(0.0F, 0.0F, 1.0F)),
            new MotionFrame(new Vec3(0.0F, -0.5F, 1.0F),
                            new Vec3(0.0F, 0.0F, 1.0F)));

    @Override
    protected void parry(LivingEntity caster, ConsumePostureEvent procPoint, SkillData stats, LivingEntity target, STATE state) {
        if (state == STATE.COOLING) return;
        if(procPoint instanceof MeleePostureEvent.Block bl&&cast(caster, -999)) {
            FlyingWeaponData.getCap(caster).getWeapon(bl.getDefendingHand()).ifPresent(a->{
                a.queuePath();
            });
            markUsed(caster);
        }
    }
}
