package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.event.MeleeKnockbackEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {

    private static boolean tempCrit;
    private static float tempCdmg;
    private static DamageSource ds;

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

//    @Inject(method = "attack", locals = LocalCapture.CAPTURE_FAILSOFT,
//            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/entity/player/PlayerEntity;resetCooldown()V"))
//    private void noReset(Entity targetEntity, CallbackInfo ci, float f, float f1, float f2) {
//        CombatData.getCap(this).setCachedCooldown(f2);
//    } //Mohist why



    @Inject(method = "attack", locals = LocalCapture.CAPTURE_FAILSOFT,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hurt(Lnet/minecraft/util/DamageSource;F)Z"))
    private void store(Entity targetEntity, CallbackInfo ci, float f, float f1, float f2, boolean flag, boolean flag1, float i, boolean flag2, CriticalHitEvent hitResult, boolean flag3, double d0, float f4, boolean flag4, int j, Vector3d vector3d) {
        tempCrit = flag2;
        tempCdmg = hitResult == null ? 1 : hitResult.getDamageModifier();
    }

    @Redirect(method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/DamageSource;playerAttack(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/DamageSource;"))
    private DamageSource customDamageSource(PlayerEntity player) {
        CombatDamageSource d = new CombatDamageSource("player", player).setDamageDealer(player.getMainHandItem()).setAttackingHand(CombatData.getCap(player).isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND).setProcAttackEffects(true).setProcNormalEffects(true).setCrit(tempCrit).setCritDamage(tempCdmg).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL);
        ds = d;
        return d;
    }

    @Redirect(method = "attack",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;walkDist:F", opcode = Opcodes.GETFIELD))
    private float noSweep(PlayerEntity player) {
        if (GeneralConfig.betterSweep)
            return Float.MAX_VALUE;
        return walkDist;
    }

    @Inject(method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hurt(Lnet/minecraft/util/DamageSource;F)Z"))
    private void stats(Entity targetEntity, CallbackInfo ci) {
        targetEntity.invulnerableTime = 0;
        if (targetEntity instanceof LivingEntity) {
            ((LivingEntity) targetEntity).hurtTime = ((LivingEntity) targetEntity).hurtDuration = 0;
        }
    }

    @Redirect(method = "attack",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/entity/LivingEntity;knockback(FDD)V"))
    private void mark(LivingEntity livingEntity, float strength, double ratioX, double ratioZ) {
        MeleeKnockbackEvent mke = new MeleeKnockbackEvent(this, ds, livingEntity, strength, ratioX, ratioZ);
        MinecraftForge.EVENT_BUS.post(mke);
        livingEntity.knockback(mke.getStrength(), mke.getRatioX(), mke.getRatioZ());
    }
}
