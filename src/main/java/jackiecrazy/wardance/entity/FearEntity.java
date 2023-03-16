package jackiecrazy.wardance.entity;

import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.ITetherAnchor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class FearEntity extends Entity implements ITetherAnchor {

    private Entity feared;
    private LivingEntity wuss;

    public FearEntity(EntityType<? extends FearEntity> type, Level worldIn) {
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
    public Vec3 getTetheredOffset() {
        return Vec3.ZERO;
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
                move(MoverType.SELF, new Vec3((x + WarDance.rand.nextFloat() - 0.5) * 0.05, (y + WarDance.rand.nextFloat() - 0.5) * 0.05, (z + WarDance.rand.nextFloat() - 0.5) * 0.05));
                markHurt();
                float rotate = GeneralUtils.deg((float) Mth.atan2(x, z));
                if (!wuss.hasEffect(FootworkEffects.FEAR.get()))
                    removeAfterChangingDimensions();
                setYRot(-rotate);
                wuss.setYRot(-rotate);
                updateTetheringVelocity();
            } else removeAfterChangingDimensions();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("cthulhu") && level instanceof ServerLevel) {
            feared = ((ServerLevel) level).getEntity(compound.getUUID("cthulhu"));
        } else feared = level.getEntity(compound.getInt("yogsothoth"));
        if (compound.hasUUID("lovecraft") && level instanceof ServerLevel) {
            Entity potWuss = ((ServerLevel) level).getEntity(compound.getUUID("lovecraft"));
            if (potWuss instanceof LivingEntity)
                wuss = (LivingEntity) potWuss;
        } else {
            Entity potWuss = level.getEntity(compound.getInt("bruh"));
            if (potWuss instanceof LivingEntity)
                wuss = (LivingEntity) potWuss;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (feared != null) {
            compound.putUUID("cthulhu", feared.getUUID());
            compound.putInt("yogsothoth", feared.getId());
        }
        if (wuss != null) {
            compound.putUUID("lovecraft", wuss.getUUID());
            compound.putInt("bruh", wuss.getId());
        }
    }
}
