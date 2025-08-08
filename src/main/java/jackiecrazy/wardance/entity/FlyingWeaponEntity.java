package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.client.particle.ScalingParticleType;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class FlyingWeaponEntity extends FlyingItemEntity {
    private final List<Entity> alreadyHit = new ArrayList<>();
    private int internalIdleTimer = 0;
    private WeaponStats.SweepInfo cacheInfo;

    public FlyingWeaponEntity(EntityType<? extends FlyingItemEntity> type,
                              Level level) {
        //keep hitframes separate and logged here.
        //keep defense frames here?
        super(type, level);
        setShouldRender(FlyingWeaponEffect.BIG_SHADOW, false);
    }

    @Nullable
    public WeaponStats.SweepInfo getInfo() {
        return cacheInfo;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide ) {
            if(isIdle()) {
                internalIdleTimer++;
                if (internalIdleTimer > 120 || getOwner() == null)//reasonably sure the player doesn't need it anymore
                    remove(RemovalReason.DISCARDED);
            }else internalIdleTimer=0;
        }
//        if (level() instanceof ServerLevel s&&!isIdle()&&tickCount%3==0) {
//            Vec3 vec= getPosition(0);
//            s.sendParticles(
//                    new ScalingParticleType(FootworkParticles.LINE.get(), 1, 1, 30, Color.WHITE),
//                    vec.x, vec.y, vec.z, 0, getXRot(), getYRot(), 0, 1.0D);
//            vec= getPosition(1);
//            s.sendParticles(
//                    new ScalingParticleType(FootworkParticles.LINE.get(), 1, 1, 30, Color.BLACK),
//                    vec.x, vec.y, vec.z, 0, getXRot(), getYRot(), 0, 1.0D);
//            vec= getPosition(0).add(getViewVector(0).scale(getInteractionRange()));
//            s.sendParticles(
//                    new ScalingParticleType(FootworkParticles.LINE.get(), 1, 1, 30, Color.GREEN),
//                    vec.x, vec.y, vec.z, 0, getXRot(), getYRot(), 0, 1.0D);
//            vec= getPosition(1).add(getViewVector(1).scale(getInteractionRange()));
//            s.sendParticles(
//                    new ScalingParticleType(FootworkParticles.LINE.get(), 1, 1, 30, Color.BLUE),
//                    vec.x, vec.y, vec.z, 0, getXRot(), getYRot(), 0, 1.0D);
//        }
    }

    @Override
    protected void onHitEntity(List<Entity> targets) {
        //don't do any of this on the client because that's not good:tm:
        if (level().isClientSide()) return;
        //normal hits skip hit calculation
        if (getInfo() == null) return;
        targets = targets.stream().filter(tg -> tg != owner &&
                !alreadyHit.contains(tg) &&
                !TargetingUtils.isAlly(tg, owner) &&
                !tg.isInvulnerable()).toList();
        LivingEntity e = getOwner();
        int ticks = e.attackStrengthTicker;
        ItemStack main = e.getMainHandItem();
        try {
            CombatUtils.quickSwap(e, getHeldItem());
            WeaponStats.tmp_info = getInfo();
            for (Entity target : targets) {
                e.attackStrengthTicker = 99999;
                //temporary pin code
                if (target instanceof LivingEntity elb)
                    CombatData.getCap(elb).pin(20);
                if (!alreadyHit.isEmpty())
                    CombatData.getCap(e).tickProc("oncePerSweep");
                CombatData.getCap(e).tickProc("noFinisherCharge");
                CombatData.getCap(e).tickProc("sweepStateOverride", getInfo().getType().ordinal());
                target.invulnerableTime = 0;
                GeneralUtils.attack(e, target);
                alreadyHit.add(target);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            CombatUtils.quickSwap(e, main);
            e.attackStrengthTicker = ticks;
            WeaponStats.tmp_info = null;
        }
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        //todo impact sweep, store the sweep type and on hit stats to overwrite on combathandler
    }

    @Override
    protected void updateClientData() {
        if (transitioning())
            trailHistory.clear();
        renderLag=0;
        super.updateClientData();
    }

    @Override
    protected boolean updateMotionTargets(boolean forceskip) {
        //true if a new move started
        boolean ret = super.updateMotionTargets(forceskip);
        if (ret) {
            alreadyHit.clear();
            internalIdleTimer = 0;
        }
        MotionManager motion = moveQueue.peek();
        if (motion instanceof WeaponMotionManager wmm) {
            setInteractionRange((float) wmm.range());
            setTransitioning(false);
            cacheInfo = wmm.info();
            if (getInfo() == null) {
                //special case, do not render big weapon
                setShouldRender(FlyingWeaponEffect.TRAIL, true);
                setShouldRender(FlyingWeaponEffect.AFTERIMAGE, false);
                setShouldRender(FlyingWeaponEffect.BIG_SHADOW, false);
                setShouldRender(FlyingWeaponEffect.WEAPON, true);
            } else {
                //attacking, on a finisher
                setShouldRender(FlyingWeaponEffect.TRAIL, true);
                setShouldRender(FlyingWeaponEffect.AFTERIMAGE, false);
                setShouldRender(FlyingWeaponEffect.WEAPON, false);
                setShouldRender(FlyingWeaponEffect.BIG_SHADOW, true);
            }
        } else setTransitioning(true);

        if (transitioning()) {
            //not attacking
            setShouldRender(FlyingWeaponEffect.TRAIL, false);
            setShouldRender(FlyingWeaponEffect.BIG_SHADOW, false);
            setShouldRender(FlyingWeaponEffect.WEAPON, true);
        }
        if (!isIdle()) {
            //setUniversalOffset(Vec3.ZERO);
        } else if (getOwner() != null) {
            setInteractionRange((float) getOwner().getAttributeValue(ForgeMod.ENTITY_REACH.get()));
        }
        return ret;
    }
}
