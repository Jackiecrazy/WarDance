package jackiecrazy.wardance.capability.flyingweapon;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.checkerframework.checker.units.qual.C;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlyingWeaponData implements ICapabilitySerializable<CompoundTag> {
    public static Capability<IFlyingWeapon> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static IFlyingWeapon OHNO = new IFlyingWeapon.DummyFlyingWeapon();
    protected final IFlyingWeapon instance;

    public FlyingWeaponData() {
        instance = new IFlyingWeapon.DummyFlyingWeapon();
    }

    public FlyingWeaponData(Player sp) {
        instance = new FlyingWeaponCapability(sp);
    }

    public static IFlyingWeapon getCap(LivingEntity le) {
        if (le == null) return OHNO;
        return le.getCapability(CAP).orElse(OHNO);//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, LazyOptional.of(() -> instance));
    }

    @Override
    public CompoundTag serializeNBT() {
        //do not persist
        for (InteractionHand hand : InteractionHand.values()) {
            if (instance.getWeapon(hand) != null)
                instance.getWeapon(hand).remove(Entity.RemovalReason.DISCARDED);
        }
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
