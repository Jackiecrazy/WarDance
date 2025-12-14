package jackiecrazy.wardance.capability.flyingweapon;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.entity.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.joml.Vector4d;

public class FlyingWeaponCapability implements IFlyingWeapon {
    private static final MotionManager[] idleFrame = {
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 0), new Vec3(0, -0.4, 0), new Vector4d(0, 1, 0, 0)), 5),
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 0), new Vec3(0, -0.4, 0), new Vector4d(0, 1, 0, 0)), 5)
    };
    private static final MotionManager[] blockingFrame = {
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 1), Vec3.ZERO, new Vector4d(0, 1, 0, 90)), 5),
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 1), Vec3.ZERO, new Vector4d(0, 1, 0, -90)), 5)
    };
    private static final Vec3[] idleOffset = {
            new Vec3(0.5, 0, 0.5),
            new Vec3(-0.5, 0, 0.5)
    };
    private static final Vec3[] blockOffset = {
            new Vec3(0, 0, 1),
            new Vec3(0, 0, 0.7)
    };
    Player player;
    FlyingWeaponEntity main, off;
    ThrownWeaponEntity held;
    GrappleEntity grapple;
    boolean mainSwap, offSwap;
    private FlyingWeaponEffect[] mainFX, offFX;

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
    public ThrownWeaponEntity getHeldBlock() {
        return held;
    }

    @Override
    public void setHeldBlock(ThrownWeaponEntity sb) {
        if(held!=null)
            held.remove(Entity.RemovalReason.DISCARDED);
        held=sb;
    }

    @Override
    public GrappleEntity getGrapple() {
        return grapple;
    }

    @Override
    public void launchGrapple(Vec3 to) {
        if (!player.level().isClientSide()) {
            if(getGrapple()!=null){
                getGrapple().remove(Entity.RemovalReason.DISCARDED);
            }
            GrappleEntity grapple = new GrappleEntity(WarEntities.GRAPPLE.get(), player.level());
            grapple.setOwner(player);
            grapple.setInteractionRange(1);
            grapple.setPosRaw(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
            grapple.setDeltaMovement(to.subtract(grapple.position()).normalize().scale(1.5));
            this.grapple = grapple;
            player.level().addFreshEntity(grapple);
        }
    }

    @Override
    public void scheduleAction(InteractionHand hand,
                               MotionManager mm,
                               WeaponStats.SweepInfo info,
                               double range,
                               int totalTime) {
        //set attack range from manager, then temporarily set the rest to override whatever sweep the player should have grabbed
        //no idea how this should be stored on the player. Since it's used in the span of a single function, maybe a global is fine?
        final boolean isMain = hand == InteractionHand.MAIN_HAND;
        boolean scheduleLock = isMain ? mainSwap : offSwap;
        FlyingItemEntity fwe = getWeapon(hand);
        if (!scheduleLock && fwe != null) {
            //updateWeapon(fwe, hand);
            if (info == null)
                fwe.clearPath();
            fwe.queuePath(new WeaponMotionManager(mm, info, range), 0, 0);
            //fwe.setIdlePose(idleFrame[isMain ? 0 : 1]);
            //fwe.setShouldRender(FlyingWeaponEffect.WEAPON,true);
            //fwe.setUniversalOffset(idleOffset[isMain ? 0 : 1]);
        }
    }

    private boolean weaponValid(InteractionHand hand) {
        if (!StylishData.getCap(player).isCombatMode() || player.isDeadOrDying()) return false;
        if (!WeaponStats.isCombatItem(player, hand)) return false;
        if (getWeapon(hand) != null && getWeapon(hand).getHeldItem() != player.getItemInHand(hand)) return false;
        return true;
    }

    @Override
    public void tick() {
        //reset weapons if they're dead
        if (main != null && main.isRemoved()) main = null;
        if (off != null && off.isRemoved()) off = null;
        if(grapple!=null&&grapple.isRemoved())grapple=null;

        //not in combat mode, dismiss weapons
        /*if(!weaponValid(InteractionHand.MAIN_HAND)){
            if(main!=null) main.remove(Entity.RemovalReason.DISCARDED);
            main=null;
        }
        if(!weaponValid(InteractionHand.OFF_HAND)){
            if(off!=null) off.remove(Entity.RemovalReason.DISCARDED);
            off=null;
        }*/
        if (!StylishData.getCap(player).isCombatMode() || player.isDeadOrDying()) {
            if (main != null) main.remove(Entity.RemovalReason.DISCARDED);
            if (off != null) off.remove(Entity.RemovalReason.DISCARDED);
            main = off = null;
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            boolean isMain = hand == InteractionHand.MAIN_HAND;
            if (!StylishData.getCap(player).isCombatMode()) ;//do nothing
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
                FlyingItemEntity fwe = getWeapon(hand);
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
                    //hidden if hand is bound
                    if (CombatData.getCap(player).getHandBind(hand) > 0) {
                        fwe.setShouldRender();
                    }
                    //if the player is blocking, change position
                    else if (player.isBlocking()) {
                        fwe.setIdlePose(blockingFrame[isMain ? 0 : 1]);
                        fwe.setUniversalOffset(blockOffset[isMain ? 0 : 1]);
                    } else {
                        fwe.setShouldRender();
                        fwe.setIdlePose(idleFrame[isMain ? 0 : 1]);
                        fwe.setUniversalOffset(idleOffset[isMain ? 0 : 1]);
                    }
                }
            }
        }
    }

    @Override
    public void setRender(InteractionHand hand, FlyingWeaponEffect... effects) {
        if (getWeapon(hand).isIdle())
            getWeapon(hand).setShouldRender(effects);
    }

    @Override
    public void yeet(InteractionHand hand, Vec3 pos) {
        if(hand==null&&getHeldBlock()!=null){
            getHeldBlock().yeet(pos);
            held=null;
        }
        if (getWeapon(hand).isIdle()) {
            Level level = getWeapon(hand).level();
            ThrownWeaponEntity fwe = new ThrownWeaponEntity(WarEntities.THROWN_WEAPON.get(), level);
            fwe.setHeldItem(player.getItemInHand(hand).copyWithCount(1));
            fwe.setOwner(player);
            fwe.setPosRaw(player.getX(), player.getEyeY(), player.getZ());
            fwe.setInteractionRange(1);
            fwe.setState(FlyingItemEntity.STATE.THROW_NATURAL);
            fwe.yeet(pos);
            level.addFreshEntity(fwe);
            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
            //release the weapon to create another one
//            if (hand == InteractionHand.MAIN_HAND) main = null;
//            else off = null;
        }
    }


    private void updateWeapon(FlyingItemEntity fwe, InteractionHand hand) {
        if (fwe.isRemoved()) {
            fwe = new FlyingWeaponEntity(WarEntities.WEAPON.get(), player.level());
            player.level().addFreshEntity(fwe);
        }
        fwe.setHeldItem(player.getItemInHand(hand));
        fwe.setOwner(player);
        fwe.setPosRaw(player.xo, player.yo, player.zo);
        fwe.setInteractionRange((float) GeneralUtils.getAttributeValueHandSensitive(player, ForgeMod.ENTITY_REACH.get(), hand));
    }
}
