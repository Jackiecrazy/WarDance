package jackiecrazy.wardance.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ThrownWeaponEntity extends AbstractArrowEntity {

    private static final DataParameter<Byte> LOYALTY_LEVEL = EntityDataManager.createKey(ThrownWeaponEntity.class, DataSerializers.BYTE);
    private static final DataParameter<CompoundNBT> COMPOUND_STACK = EntityDataManager.createKey(ThrownWeaponEntity.class, DataSerializers.COMPOUND_NBT);
    private static final DataParameter<BlockPos> DESTROYED_BLOCK = EntityDataManager.createKey(ThrownWeaponEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> SHOULD_DESTROY = EntityDataManager.createKey(ThrownWeaponEntity.class, DataSerializers.BOOLEAN);
    Hand hand = Hand.MAIN_HAND;
    ItemStack stack = ItemStack.EMPTY;

    protected ThrownWeaponEntity(EntityType<? extends AbstractArrowEntity> type, World worldIn) {
        super(type, worldIn);
    }

    protected ThrownWeaponEntity(EntityType<? extends AbstractArrowEntity> type, double x, double y, double z, World worldIn) {
        super(type, x, y, z, worldIn);
    }

    protected ThrownWeaponEntity(EntityType<? extends AbstractArrowEntity> type, LivingEntity shooter, World worldIn) {
        super(type, shooter, worldIn);
    }

    public ThrownWeaponEntity setStack(ItemStack is) {
        stack = is;
        return this;
    }

    @Override
    protected ItemStack getArrowStack() {
        return stack;
    }

    protected void registerData() {
        super.registerData();
        this.dataManager.register(COMPOUND_STACK, new CompoundNBT());
        this.dataManager.register(LOYALTY_LEVEL, (byte) 0);
        this.dataManager.register(DESTROYED_BLOCK, BlockPos.ZERO);
        this.dataManager.register(SHOULD_DESTROY, false);
    }
}
