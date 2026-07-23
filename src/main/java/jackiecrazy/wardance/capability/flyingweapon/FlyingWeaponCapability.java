package jackiecrazy.wardance.capability.flyingweapon;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionGroup;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import jackiecrazy.wardance.entity.GrappleEntity;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.UpdateFlyingWeaponPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector4d;

import java.util.List;
import java.util.Optional;

public class FlyingWeaponCapability implements IFlyingWeapon {
    private static final MotionManager BLOCKYEET = new MotionManagers.DefinitionMM(new MotionGroup(List.of(new MotionFrame(new Vec3(0, 1, 0.4), new Vec3(0, 1, 0)), new MotionFrame(new Vec3(0, 1, -0.4), new Vec3(0, 1, 0))), EasingFunctionEnum.IN_SINE, 10));
    private static final MotionManager BLOCKHOLD = new MotionManagers.DefinitionMM(new MotionGroup(List.of(new MotionFrame(new Vec3(0, 1, 0.4), new Vec3(0, 1, 0))), EasingFunctionEnum.LINEAR, 10));
    private static final MotionManager[] idleFrame = {
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 0), new Vec3(0, -0.4, 0), new Vector4d(0, 1, 0, 0)), 5),
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 0), new Vec3(0, -0.4, 0), new Vector4d(0, 1, 0, 0)), 5)
    };
    private static final MotionManager[] blockingFrame = {
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 1), Vec3.ZERO, new Vector4d(0, 1, 0, 90)), 5),
            new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, -1, 1), Vec3.ZERO, new Vector4d(0, 1, 0, -90)), 5)
    };
    private static final Vec3[] idleOffset = {
            new Vec3(0.5, 0, 0.5), new Vec3(-0.5, 0, 0.5)
    };
    private static final Vec3[] blockOffset = {
            new Vec3(0, 0, 1), new Vec3(0, 0, 0.7)
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
    public Optional<FlyingWeaponEntity> getWeapon(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? Optional.ofNullable(main) : Optional.ofNullable(off);
    }

    @Override
    public ThrownWeaponEntity getHeldBlock() {
        return held;
    }

    @Override
    public void setHeldBlock(ThrownWeaponEntity sb) {
        if (held != null) held.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        held = sb;
        if (!sb.level().isClientSide) {
            MotionManager mm = new MotionManagers.DefinitionMM(
                    new MotionGroup(
                            List.of(
                                    new MotionFrame(new Vec3(0, 1, 0.4), new Vec3(0, 1, 0), sb.getIdlePose().getStartFrame().renderOrientation()),
                                    new MotionFrame(new Vec3(0, 1, -0.4), new Vec3(0, 1, 0), sb.getIdlePose().getStartFrame().renderOrientation())),
                            EasingFunctionEnum.IN_SINE, 10));
            sb.queuePath(mm);

            //sb.queuePath(BLOCKHOLD);
        }
        sync();
    }

    @Override
    public GrappleEntity getGrapple() {
        return grapple;
    }

    @Override
    public void launchGrapple(Vec3 to) {
        if (!player.level().isClientSide()) {
            if (getGrapple() != null) {
                getGrapple().remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
            }
            GrappleEntity grapple = new GrappleEntity(WarEntities.GRAPPLE.get(), player.level());
            grapple.setOwner(player);
            grapple.setInteractionRange(1);
            grapple.setPosRaw(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
            grapple.setDeltaMovement(to.subtract(grapple.position()).normalize().scale(3));
            this.grapple = grapple;
            player.level().addFreshEntity(grapple);
            sync();
        }
    }

    @Override
    public void scheduleAction(InteractionHand hand, MotionManager mm, boolean overwrite) {
        //set attack range from manager, then temporarily set the rest to override whatever sweep the player should have grabbed
        //no idea how this should be stored on the player. Since it's used in the span of a single function, maybe a global is fine?
        final boolean isMain = hand == InteractionHand.MAIN_HAND;
        boolean scheduleLock = isMain ? mainSwap : offSwap;
        getWeapon(hand).ifPresent(fwe->{
            try {
                if (!scheduleLock) {
                    if (mm.getStartFrame() == null)
                        fwe.clearPath();
                    else if (overwrite && !fwe.isIdle()) {
                        fwe.invalidateWhenDone();
                        respawnWeapon(hand);
                        fwe = getWeapon(hand).get();
                    }
                    fwe.queuePath(mm, 0, 0);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private boolean weaponValid(InteractionHand hand) {
        if (!StylishData.getCap(player).isCombatMode() || player.isDeadOrDying()) return false;
        if (!WeaponStats.isCombatItem(player, hand)) return false;
        if (getWeapon(hand).isPresent() && getWeapon(hand).get().getHeldItem() != player.getItemInHand(hand)) return false;
        return true;
    }

    @Override
    public CompoundTag write() {
        CompoundTag t = new CompoundTag();
        getWeapon(InteractionHand.MAIN_HAND).ifPresent(fwe-> t.putInt("mainID", fwe.getId()));
        getWeapon(InteractionHand.OFF_HAND).ifPresent(fwe-> t.putInt("offID", fwe.getId()));
        if (getHeldBlock() != null)
            t.putInt("quiverID", getHeldBlock().getId());
        if (getGrapple() != null)
            t.putInt("grappleID", getGrapple().getId());
        return t;
    }

    @Override
    public void read(Level l, CompoundTag t) {
        Entity e = l.getEntity(t.getInt("mainID"));
        if (e instanceof FlyingWeaponEntity f)
            main = f;
        else main = null;
        e = l.getEntity(t.getInt("offID"));
        if (e instanceof FlyingWeaponEntity f)
            off = f;
        else off = null;
        e = l.getEntity(t.getInt("quiverID"));
        if (e instanceof ThrownWeaponEntity f)
            setHeldBlock(f);
        else held = null;
        e = l.getEntity(t.getInt("grappleID"));
        if (e instanceof GrappleEntity f)
            grapple = f;
        else grapple = null;
    }

    @Override
    public void tick() {
        //reset weapons if they're dead
        if (main != null && main.isRemoved()) main = null;
        if (off != null && off.isRemoved()) off = null;
        if (grapple != null && grapple.isRemoved()) grapple = null;

        //not in combat mode, dismiss weapons
        /*if(!weaponValid(InteractionHand.MAIN_HAND)){
            if(main!=null) main.remove(Entity.RemovalReason.DISCARDED);
            main=null;
        }
        if(!weaponValid(InteractionHand.OFF_HAND)){
            if(off!=null) off.remove(Entity.RemovalReason.DISCARDED);
            off=null;
        }*/
        //auto yeet block
        if (getHeldBlock() != null && getHeldBlock().isIdle()) {
            Vec3 dest = player.getEyePosition().add(player.getLookAngle().scale(32));
            yeet(null, dest, 2);
        }
        if (!StylishData.getCap(player).isCombatMode() || player.isDeadOrDying()) {
            if (main != null) main.invalidateWhenDone();
            if (off != null) off.invalidateWhenDone();
            main = off = null;
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            boolean isMain = hand == InteractionHand.MAIN_HAND;
            if (!StylishData.getCap(player).isCombatMode()) ;//do nothing
            else if (getWeapon(hand).isEmpty()) {
                //make new weapons
                //create a flying weapon
                //fixme doesn't work on relog?
                respawnWeapon(hand);
            } else {
                //check the old weapons to see if they need to be replaced
                FlyingItemEntity fwe = getWeapon(hand).get();
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
                        fwe.setEffect();
                    }
                    //if the player is blocking, change position
                    else if (player.isDiscrete()) {
                        //fwe.setIdlePose(blockingFrame[isMain ? 0 : 1]);
                        fwe.setUniversalOffset(blockOffset[isMain ? 0 : 1]);
                    } else {
                        //fwe.setEffect();
                        //fwe.setIdlePose(idleFrame[isMain ? 0 : 1]);
                        fwe.setUniversalOffset(idleOffset[isMain ? 0 : 1]);
                    }
                }
            }
        }
    }

    private void respawnWeapon(InteractionHand hand) {
        FlyingWeaponEntity fwe = new FlyingWeaponEntity(WarEntities.WEAPON.get(), player.level());
        updateWeapon(fwe, hand);
        if (hand == InteractionHand.MAIN_HAND) {
            main = fwe;
        } else {
            fwe.setUniversalOffset(new Vec3(-1, 0, 0));
            off = fwe;
        }
        if (!player.level().isClientSide()) player.level().addFreshEntity(fwe);
        sync();
    }

    private void sync() {
        if (player instanceof ServerPlayer sp && !sp.level().isClientSide) {
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateFlyingWeaponPacket(sp));
        }
    }

    @Override
    public void setRender(InteractionHand hand, FlyingWeaponEffect... effects) {
        getWeapon(hand).ifPresent(fwe->{
            if (fwe.isIdle()) fwe.setEffect(effects);
        });

    }

    @Override
    public ThrownWeaponEntity yeet(InteractionHand hand, Vec3 pos, double strength) {
        if (hand == null) {
            if (getHeldBlock() != null) {
                ThrownWeaponEntity gbe = held;
                getHeldBlock().yeet(pos, 2);
                held = null;
                StylishData.getCap(player).addCombo(0.08f, "wardance.combo.blockyeet");
                sync();
                return gbe;
            }
            return null;
        }
        StylishData.getCap(player).addCombo(0.1f, "wardance.combo.throw");
        ThrownWeaponEntity fwe = new ThrownWeaponEntity(WarEntities.THROWN_WEAPON.get(), player.level());
        getWeapon(hand).ifPresent(oldFW->{
            oldFW.clearPath();
            oldFW.unDrag(true);
            fwe.inheritDrag(oldFW);
        });
        final ItemStack held = player.getItemInHand(hand);
        fwe.setHeldItem(held.copyWithCount(1));
        fwe.setOwner(player);
        Vec3 look=player.getLookAngle().reverse();
        fwe.setPosRaw(player.getX()+look.x, player.getEyeY()+look.y, player.getZ()+look.z);
        fwe.setState(FlyingItemEntity.STATE.THROW_NATURAL);

        fwe.yeet(pos, strength);
        fwe.setInteractionRange(1f);
        //fwe is not added to level here! This is so Throw can transform it again
        sync();
        return fwe;
    }


    private void updateWeapon(FlyingItemEntity fwe, InteractionHand hand) {
        //fwe.remove(Entity.RemovalReason.DISCARDED);
        if (!fwe.isIdle() && fwe instanceof FlyingWeaponEntity f) {
            //it's still doing something, let it finish
            f.invalidateWhenDone();
            respawnWeapon(hand);
            fwe = getWeapon(hand).get();
        }
        if (fwe.isRemoved()) {
            respawnWeapon(hand);
            fwe = getWeapon(hand).get();
        }
        final ItemStack stack = player.getItemInHand(hand);
        final WeaponStats.WeaponInfo info = WeaponStats.lookupStats(stack);
        if (info != null) fwe.setIdlePose(info.idle_frame(hand == InteractionHand.OFF_HAND));
        else fwe.setIdlePose(WeaponStats.DEFAULTMELEE.idle_frame(hand == InteractionHand.OFF_HAND));
        fwe.setHeldItem(stack);
        fwe.setOwner(player);
        fwe.setFlipRender(hand == InteractionHand.OFF_HAND);
        fwe.setPosRaw(player.xo, player.yo, player.zo);
        fwe.setInteractionRange((float) GeneralUtils.getAttributeValueHandSensitive(player, ForgeMod.ENTITY_REACH.get(), hand));
    }

    @Override
    public void forceRefreshWeapon(InteractionHand hand) {
        getWeapon(hand).ifPresent(fwe->{
            if (fwe.isRemoved() || fwe.fading()) {
                respawnWeapon(hand);
                fwe = getWeapon(hand).get();
            }
            updateWeapon(fwe, hand);
            if (hand == InteractionHand.MAIN_HAND) mainSwap = false;
            else offSwap = false;
        });

    }
}
