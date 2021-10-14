package jackiecrazy.wardance.entity;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.ITetherAnchor;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.utils.GeneralUtils;
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
    protected void registerData() {

    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!world.isRemote) {
            if (feared != null && wuss != null) {
                double x = getPosX() - feared.getPosX();
                double z = getPosZ() - feared.getPosZ();
                x = x == 0 ? 0 : 40 * wuss.getAttributeValue(Attributes.MOVEMENT_SPEED) / x;
                double y = wuss.isOnGround() ? 0 : 0.02;
                z = z == 0 ? 0 : 40 * wuss.getAttributeValue(Attributes.MOVEMENT_SPEED) / z;
                move(MoverType.SELF, new Vector3d((x + WarDance.rand.nextFloat() - 0.5) * 0.05, (y + WarDance.rand.nextFloat() - 0.5) * 0.05, (z + WarDance.rand.nextFloat() - 0.5) * 0.05));
                markVelocityChanged();
                float rotate = GeneralUtils.deg((float) MathHelper.atan2(x, z));
                if (!wuss.isPotionActive(WarEffects.FEAR.get()))
                    setDead();
                rotationYaw = wuss.rotationYaw = -rotate;
                updateTetheringVelocity();
            } else setDead();
        }
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        if (compound.hasUniqueId("cthulhu") && world instanceof ServerWorld) {
            feared = ((ServerWorld) world).getEntityByUuid(compound.getUniqueId("cthulhu"));
        } else feared = world.getEntityByID(compound.getInt("yogsothoth"));
        if (compound.hasUniqueId("lovecraft") && world instanceof ServerWorld) {
            Entity potWuss = ((ServerWorld) world).getEntityByUuid(compound.getUniqueId("lovecraft"));
            if (potWuss instanceof LivingEntity)
                wuss = (LivingEntity) potWuss;
        } else {
            Entity potWuss = world.getEntityByID(compound.getInt("bruh"));
            if (potWuss instanceof LivingEntity)
                wuss = (LivingEntity) potWuss;
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        if (feared != null) {
            compound.putUniqueId("cthulhu", feared.getUniqueID());
            compound.putInt("yogsothoth", feared.getEntityId());
        }
        if (wuss != null) {
            compound.putUniqueId("lovecraft", wuss.getUniqueID());
            compound.putInt("bruh", wuss.getEntityId());
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this);
    }
}
