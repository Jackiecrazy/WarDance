package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.ITetherAnchor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class FearEntity extends Entity implements ITetherAnchor {

    private Entity feared;
    private LivingEntity wuss;

    public FearEntity(EntityType<? extends FearEntity> type, World worldIn) {
        super(type, worldIn);
        //setSize(0.5f, 0.5f);
    }

    @Override
    public Entity getTetheringEntity() {
        return wuss;
    }

    @Override
    public void setTetheringEntity(Entity to) {
        if (to instanceof LivingEntity)
            wuss = (LivingEntity) to;
    }

    @Nullable
    @Override
    public Vector3d getTetheredOffset() {
        return Vector3d.ZERO;
    }

    @Nullable
    @Override
    public Entity getTetheredEntity() {
        return this;
    }

    @Override
    public void setTetheredEntity(Entity to) {
        //do nothing
    }

    public void setFearSource(Entity source) {
        feared = source;
    }

    @Override
    public double getTetherLength() {
        return 2;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!level.isClientSide) {
            if (feared != null && wuss != null) {
                double x = getX() - feared.getX();
                double z = getZ() - feared.getZ();
                x = x == 0 ? 0 : 40 * wuss.getAttributeValue(Attributes.MOVEMENT_SPEED) / x;
                double y = wuss.isOnGround() ? 0 : 0.02;
                z = z == 0 ? 0 : 40 * wuss.getAttributeValue(Attributes.MOVEMENT_SPEED) / z;
                move(MoverType.SELF, new Vector3d((x + WarDance.rand.nextFloat() - 0.5) * 0.05, (y + WarDance.rand.nextFloat() - 0.5) * 0.05, (z + WarDance.rand.nextFloat() - 0.5) * 0.05));
                markHurt();
                float rotate = GeneralUtils.deg((float) MathHelper.atan2(x, z));
                if (!wuss.hasEffect(FootworkEffects.FEAR.get()))
                    removeAfterChangingDimensions();
                yRot = wuss.yRot = -rotate;
                updateTetheringVelocity();
            } else removeAfterChangingDimensions();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
        if (compound.hasUUID("cthulhu") && level instanceof ServerWorld) {
            feared = ((ServerWorld) level).getEntity(compound.getUUID("cthulhu"));
        } else feared = level.getEntity(compound.getInt("yogsothoth"));
        if (compound.hasUUID("lovecraft") && level instanceof ServerWorld) {
            Entity potWuss = ((ServerWorld) level).getEntity(compound.getUUID("lovecraft"));
            if (potWuss instanceof LivingEntity)
                wuss = (LivingEntity) potWuss;
        } else {
            Entity potWuss = level.getEntity(compound.getInt("bruh"));
            if (potWuss instanceof LivingEntity)
                wuss = (LivingEntity) potWuss;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        if (feared != null) {
            compound.putUUID("cthulhu", feared.getUUID());
            compound.putInt("yogsothoth", feared.getId());
        }
        if (wuss != null) {
            compound.putUUID("lovecraft", wuss.getUUID());
            compound.putInt("bruh", wuss.getId());
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return new SSpawnObjectPacket(this);
    }
}
