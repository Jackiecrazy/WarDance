package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ThrownWeaponEntity extends FlyingWeaponEntity {
    private boolean dormant = false;
    private boolean falling = false;

    public ThrownWeaponEntity(EntityType<? extends FlyingItemEntity> type,
                              Level level) {
        super(type, level);
        setInteractionRange(1);
        setShouldRender(FlyingWeaponEffect.WEAPON);
        setIntangible(false);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    public boolean isReal() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity ent) {
        if (!ent.level().isClientSide && ent instanceof Player p && p.getMainHandItem().isEmpty() && isReal()) {
            return pickup(p);
        }
        return false;
    }

    public boolean elaborateSwap(Player p) {
        if (!WeaponStats.isCombatItem(p, getPickResult()) || p.getMainHandItem().isEmpty()) return pickup(p);
        //push out the offhand into ender chest, move the main hand to the offhand, replace main hand
        p.setItemInHand(InteractionHand.OFF_HAND, p.getEnderChestInventory().addItem(p.getOffhandItem()));
        //displace offhand into inventory
        if (p.getOffhandItem().isEmpty() || p.getInventory().add(-1, p.getOffhandItem().copy())) {
            CombatUtils.swapHeldItems(p);
            p.setItemInHand(InteractionHand.MAIN_HAND, getPickResult());
            this.remove(RemovalReason.KILLED);

            //pickup flourish
            ItemStack held = p.getMainHandItem();
            int ticks = p.attackStrengthTicker;
            try {
                CombatUtils.quickSwap(p, getHeldItem());
                CombatData.getCap(p).tickProc("canBreach");
                FlyingWeaponData.getCap(p).forceRefreshWeapons();
                CombatUtils.sweep(p, null, InteractionHand.MAIN_HAND, WeaponStats.SWEEPTYPE.CIRCLE, 3, 3, 1);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                CombatUtils.quickSwap(p, held);
                p.attackStrengthTicker = ticks;
            }
            p.resetFallDistance();
            return true;
        }
        return false;

    }

    public boolean lessElaborateSwap(Player p) {
        if (!WeaponStats.isCombatItem(p, getPickResult()) || p.getMainHandItem().isEmpty() || p.getOffhandItem().isEmpty())
            return pickup(p);
        //push out the offhand into ender chest, move the main hand to the offhand, replace main hand
        p.setItemInHand(InteractionHand.MAIN_HAND, p.getEnderChestInventory().addItem(p.getMainHandItem()));
        //displace offhand into inventory
        if (p.getMainHandItem().isEmpty() || p.getInventory().add(-1, p.getMainHandItem().copy())) {
            p.setItemInHand(InteractionHand.MAIN_HAND, getPickResult());
            this.remove(RemovalReason.KILLED);

            //pickup flourish
            ItemStack held = p.getMainHandItem();
            int ticks = p.attackStrengthTicker;
            try {
                CombatUtils.quickSwap(p, getHeldItem());
                CombatData.getCap(p).tickProc("canBreach");
                FlyingWeaponData.getCap(p).forceRefreshWeapons();
                CombatUtils.sweep(p, null, InteractionHand.MAIN_HAND, WeaponStats.SWEEPTYPE.CIRCLE, 3, 3, 1);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                CombatUtils.quickSwap(p, held);
                p.attackStrengthTicker = ticks;
            }
            p.resetFallDistance();
            return true;
        }
        return false;

    }

    @Override
    public void tick() {
        if (getOwner() != null && distanceToSqr(getOwner()) > 8 * 8)
            falling = true;
        if (falling && !intangible())
            addDeltaMovement(new Vec3(0, -0.04, 0));//todo remove later in custom throws
        super.tick();
        if (!level().isClientSide)
            if (getOwner() instanceof Player p) {
                if (p.distanceToSqr(this) > 32 * 32) pickup(p);
            }
    }

    @Override
    public void setDeltaMovement(@NotNull Vec3 vec3) {
        super.setDeltaMovement(vec3);
        //makes sure weapons don't start clipping into walls when they get hit by explosions etc.
        if (dormant) {
            dormant = false;
            setIntangible(false);
        }
    }

    public boolean pickup(Player p) {
        //if holding nothing, prioritize this slot
        int slot = -1;
        InteractionHand h = InteractionHand.MAIN_HAND;
        boolean success = p.getAbilities().instabuild;
        if (p.getMainHandItem().isEmpty()) slot = p.getInventory().selected;
        else if (p.getOffhandItem().isEmpty()) {
            //special offhand handling
            slot = Inventory.SLOT_OFFHAND;
            h = InteractionHand.OFF_HAND;
            success = true;
            p.setItemInHand(InteractionHand.OFF_HAND, getPickResult());
        }
        CombatUtils.allowCombatHotbarPickup = true;
        if (!success)
            success = p.getInventory().add(slot, getPickResult());
        CombatUtils.allowCombatHotbarPickup = false;
        if (success) {
            this.remove(RemovalReason.UNLOADED_WITH_PLAYER);

            //pickup flourish
            ItemStack held = p.getMainHandItem();
            int ticks = p.attackStrengthTicker;
            try {
                CombatUtils.quickSwap(p, getHeldItem());
                CombatData.getCap(p).tickProc("canBreach");
                FlyingWeaponData.getCap(p).forceRefreshWeapons();
                CombatUtils.sweep(p, null, h, WeaponStats.SWEEPTYPE.CIRCLE, 3, 3, 1);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                CombatUtils.quickSwap(p, held);
                p.attackStrengthTicker = ticks;
            }
            p.resetFallDistance();
            //TimeSlowData.getCap(p).alterSpeed(40, 0.3);
        }
        return success;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy() && getOwner() instanceof Player p && !pickup(p)) {
            return;
        }
        super.remove(reason);
    }


    @Override
    protected boolean onHitEntity(List<Entity> targets) {
        if (intangible()) return false;
        boolean ret = super.onHitEntity(targets);
        if (getHeldItem().getItem() instanceof BlockItem)
            for (Entity a : alreadyHit) {
                a.setDeltaMovement(getDeltaMovement());
            }
        if (ret) {
            //lose all velocity and start dropping to the ground
            setDeltaMovement(Vec3.ZERO);
            falling = true;
        }
        return ret;
    }

    @Override
    protected void onHitBlock(BlockPos blockPos, Direction hitFace, Vec3 location) {
        if (intangible()) return;
        super.onHitBlock(blockPos, hitFace, location);
        setIntangible(true);
        falling = false;
        dormant = true;
    }
}
