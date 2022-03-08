package jackiecrazy.wardance.mixin;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ProjectileEntity.class)
public interface ProjectileImpactMixin {
    @Invoker
    public void callOnHit(RayTraceResult rtr);
}
