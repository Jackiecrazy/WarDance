package jackiecrazy.wardance.entity.skill;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class AsuraWeaponEntity extends FlyingWeaponEntity {
    public AsuraWeaponEntity(EntityType<? extends FlyingItemEntity> type,
                             Level level) {
        super(type, level);
    }
}
