package jackiecrazy.wardance.capability.flyingweapon;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.joml.Vector4d;

public class FlyingWeaponCapability implements IFlyingWeapon {
    private static final MotionManager[] idleFrame = {
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 0), Vec3.ZERO, new Vector4d(0, 0, 1, 0)), 20),
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 0), Vec3.ZERO, new Vector4d(0, 0, 1, 0)), 20)
    };
    private static final MotionManager[] blockingFrame = {
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 1), Vec3.ZERO, new Vector4d(1, 1, 0, 0)), 20),
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 1), Vec3.ZERO, new Vector4d(-1, 1, 0, 0)), 20)
    };
    private static final Vec3[] idleOffset={
            new Vec3(1, 0, 0.5),
            new Vec3(-1, 0, 0.5)
    };
    private static final Vec3[] blockOffset={
            new Vec3(0, 0, 1),
            new Vec3(0, 0, 0.7)
    };

    Player player;
    FlyingWeaponEntity main, off;
    boolean mainSwap, offSwap;

    public FlyingWeaponCapability() {

    }

    public FlyingWeaponCapability(Player bind) {
        player = bind;
    }


    @Override
    public FlyingWeaponEntity getWeapon(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? main : off;
    }

    @Override
    public void scheduleAction(InteractionHand hand, MotionManager mm, WeaponStats.SweepInfo info, double range, int totalTime) {
        final boolean isMain = hand == InteractionHand.MAIN_HAND;
        boolean scheduleLock = isMain ? mainSwap : offSwap;
        FlyingWeaponEntity fwe = getWeapon(hand);
        if (!scheduleLock && fwe != null) {
            fwe.queuePath(mm, 0, totalTime-mm.getDuration());
            fwe.setAttackRange((float) range);
            fwe.setIdlePose(idleFrame[isMain ? 0 : 1]);
            fwe.setUniversalOffset(idleOffset[isMain ? 0 : 1]);
            fwe.setIncorporeal(false);
            fwe.setSweepState(CombatUtils.getSweepState(player));
            fwe.setInfo(info);
        }
    }

    @Override
    public void tick() {
        //reset weapons if they're dead
        if (main != null && main.isRemoved()) main = null;
        if (off != null && off.isRemoved()) off = null;

        //not in combat mode, dismiss weapons
        if (!StylishData.getCap(player).isCombatMode()||player.isDeadOrDying()) {
            if (main != null) main.remove(Entity.RemovalReason.DISCARDED);
            if (off != null) off.remove(Entity.RemovalReason.DISCARDED);
            main = off = null;
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            boolean isMain = hand == InteractionHand.MAIN_HAND;
            if (!StylishData.getCap(player).isCombatMode()) ;
            else if (getWeapon(hand) == null) {
                //make new weapons
                //create a flying weapon
                FlyingWeaponEntity fwe = new FlyingWeaponEntity(WarEntities.WEAPON.get(), player.level());
                updateWeapon(fwe, hand);
                if (isMain) {
                    main = fwe;
                } else {
                    fwe.setUniversalOffset(new Vec3(-1, 0, 0));
                    off = fwe;
                }
                if (!player.level().isClientSide())
                    player.level().addFreshEntity(fwe);
            } else {
                //check the old weapons to see if they need to be replaced
                FlyingWeaponEntity fwe = getWeapon(hand);
                if (!fwe.getHeldItem().equals(player.getItemInHand(hand))) {
                    //flag the weapons for replacement
                    if (isMain) mainSwap = true;
                    else offSwap = true;
                }
                //if weapons are idle
                if (fwe.isIdle()) {
                    //and the flag is set, they are replaced and the flag is reset
                    if (isMain ? mainSwap : offSwap) {
                        updateWeapon(fwe, hand);
                        if (isMain) mainSwap = false;
                        else offSwap = false;
                    }
                    //if the player is blocking, change position
                    if (player.isBlocking()) {
                        fwe.setIdlePose(blockingFrame[isMain ? 0 : 1]);
                        fwe.setUniversalOffset(blockOffset[isMain ? 0 : 1]);
                    }
                    else{
                        fwe.setIdlePose(idleFrame[isMain ? 0 : 1]);
                        fwe.setUniversalOffset(idleOffset[isMain ? 0 : 1]);
                    }
                }
            }
        }
    }

    private void updateWeapon(FlyingWeaponEntity fwe, InteractionHand hand) {
        if (fwe.isRemoved()) {
            fwe = new FlyingWeaponEntity(WarEntities.WEAPON.get(), player.level());
            player.level().addFreshEntity(fwe);
        }
        fwe.setHeldItem(player.getItemInHand(hand));
        fwe.setOwner(player);
        fwe.setPosRaw(player.xo, player.yo, player.zo);
        fwe.setAttackRange((float) GeneralUtils.getAttributeValueHandSensitive(player, ForgeMod.ENTITY_REACH.get(), hand));
    }
}
