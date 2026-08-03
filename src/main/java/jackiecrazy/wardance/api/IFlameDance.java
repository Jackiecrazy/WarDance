package jackiecrazy.wardance.api;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Unique;

public interface IFlameDance {
    EntityDataAccessor<Boolean> STRIPPED = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    void warDance$stripFireResist(boolean toggle);
}
