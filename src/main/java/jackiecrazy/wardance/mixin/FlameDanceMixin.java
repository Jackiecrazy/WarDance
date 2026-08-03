package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.api.IFlameDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.skill.styles.two.FlameDance;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = 9000)
public abstract class FlameDanceMixin implements IFlameDance {

//    @Unique
//    @Override
//    public void warDance$stripFireResist(boolean toggle){
//        ((Entity)(Object)this).getEntityData().set(STRIPPED, toggle);
//    }

    @Inject(method = "extinguishFire", at = @At("HEAD"), cancellable = true)
    private void justdie(CallbackInfo ci) {
        if ((Object)this instanceof LivingEntity le) {
            Marks.getCap(le).getActiveMark(WarSkills.FLAME_DANCE.get()).ifPresent(a -> {
                if (a.getArbitraryFloat() > FlameDance.MAX_FLAME_STACKS/2) ci.cancel();
            });
            //ci.cancel();
        }
    }

    @Inject(method = "fireImmune", at = @At("RETURN"), cancellable = true)
    private void sticky(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof LivingEntity le) {
            Marks.getCap(le).getActiveMark(WarSkills.FLAME_DANCE.get()).ifPresent(a -> {
                if (a.getArbitraryFloat() > FlameDance.MAX_FLAME_STACKS/2){
                    cir.setReturnValue(false);
                }
            });
        }
    }
}