package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class FlyingWeaponEntity extends FlyingItemEntity {
    private final List<Entity> alreadyHit = new ArrayList<>();
    private int internalIdleTimer = 0;
    private WeaponStats.SWEEPSTATE sweepstate;
    private WeaponStats.SweepInfo info;

    public FlyingWeaponEntity(EntityType<? extends FlyingItemEntity> type,
                              Level level) {
        super(type, level);
    }

    public WeaponStats.SweepInfo getInfo() {
        return info;
    }

    public void setInfo(WeaponStats.SweepInfo info) {
        this.info = info;
    }

    public WeaponStats.SWEEPSTATE getSweepState() {
        return sweepstate;
    }

    public void setSweepState(WeaponStats.SWEEPSTATE sweepstate) {
        this.sweepstate = sweepstate;
    }

    @Override
    public void tick() {
        super.tick();
        internalIdleTimer++;
        if (internalIdleTimer > 1200)//reasonably sure the player doesn't need it anymore
            remove(RemovalReason.DISCARDED);
    }

    @Override
    protected void onHitEntity(List<Entity> targets) {
        targets = targets.stream().filter(tg -> tg != owner &&
                !alreadyHit.contains(tg) &&
                !TargetingUtils.isAlly(tg, owner) &&
                !tg.isInvulnerable()).toList();
        LivingEntity e = getOwner();
        int ticks = e.attackStrengthTicker;
        ItemStack main = e.getMainHandItem();
        try {
            CombatUtils.quickSwap(e, getHeldItem());
            for (Entity target : targets) {
                e.attackStrengthTicker = 99999;
                if (!alreadyHit.isEmpty())
                    CombatData.getCap(e).tickProc("oncePerSweep");
                CombatData.getCap(e).tickProc("sweepStateOverride", sweepstate.ordinal());
                GeneralUtils.attack(e, target);
                alreadyHit.add(target);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            CombatUtils.quickSwap(e, main);
            e.attackStrengthTicker = ticks;
        }
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        //todo impact sweep, store the sweep type and on hit stats to overwrite on combathandler
    }

    @Override
    protected void updateMotionTargets(boolean forceskip) {
        super.updateMotionTargets(forceskip);
        alreadyHit.clear();
        internalIdleTimer = 0;
    }
}
