package jackiecrazy.wardance.capability.permission;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PermissionData implements ICapabilitySerializable<CompoundTag> {
    public static Capability<IPermission> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static IPermission OHNO = new AmoPermissions();
    protected final IPermission instance;
    public PermissionData() {
        instance = new AmoPermissions();
    }
    public PermissionData(Player sp) {
        instance = new AmoPermissions(sp);
    }

    public static IPermission getCap(LivingEntity le) {
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
        return instance.write();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.read(nbt);
    }
}
