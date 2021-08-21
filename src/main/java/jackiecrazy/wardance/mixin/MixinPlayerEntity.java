package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
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

//    @Inject(method = "attackTargetEntityWithCurrentItem", locals = LocalCapture.CAPTURE_FAILSOFT,
//            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/entity/player/PlayerEntity;resetCooldown()V"))
//    private void noReset(Entity targetEntity, CallbackInfo ci, float f, float f1, float f2) {
//        CombatData.getCap(this).setCachedCooldown(f2);
//    } //Mohist why

    @Inject(method = "attackTargetEntityWithCurrentItem", locals = LocalCapture.CAPTURE_FAILSOFT,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private void store(Entity targetEntity, CallbackInfo ci, float f, float f1, float f2, boolean flag, boolean flag1, float i, boolean flag2, CriticalHitEvent hitResult, boolean flag3, double d0, float f4, boolean flag4, int j, Vector3d vector3d) {
        tempCrit = flag2;
        tempCdmg = hitResult == null ? 1 : hitResult.getDamageModifier();
    }

    @Redirect(method = "attackTargetEntityWithCurrentItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/DamageSource;causePlayerDamage(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/DamageSource;"))
    private DamageSource customDamageSource(PlayerEntity player) {
        DamageSource d = new CombatDamageSource("player", player).setDamageDealer(player.getHeldItemMainhand()).setAttackingHand(CombatData.getCap(player).isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND).setProcAttackEffects(true).setProcNormalEffects(true).setCrit(tempCrit).setCritDamage(tempCdmg).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL);
        ds = d;
        return d;
    }

    @Redirect(method = "attackTargetEntityWithCurrentItem",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;distanceWalkedModified:F", opcode = Opcodes.GETFIELD))
    private float noSweep(PlayerEntity player) {
        if (GeneralConfig.betterSweep)
            return Float.MAX_VALUE;
        return distanceWalkedModified;
    }

    @Inject(method = "attackTargetEntityWithCurrentItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private void stats(Entity targetEntity, CallbackInfo ci) {
        targetEntity.hurtResistantTime = 0;
        if (targetEntity instanceof LivingEntity) {
            ((LivingEntity) targetEntity).hurtTime = ((LivingEntity) targetEntity).maxHurtTime = 0;
        }
    }

    @Inject(method = "blockUsingShield",
            at = @At("TAIL"))
    private void block(LivingEntity entityIn, CallbackInfo ci) {
        if(CasterData.getCap(entityIn).isTagActive("disableShield")){
            ((PlayerEntity)(Object)this).disableShield(true);
        }
    }

    @Redirect(method = "attackTargetEntityWithCurrentItem",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/entity/LivingEntity;applyKnockback(FDD)V"))
    private void mark(LivingEntity livingEntity, float strength, double ratioX, double ratioZ) {
        MeleeKnockbackEvent mke = new MeleeKnockbackEvent(this, ds, livingEntity, strength, ratioX, ratioZ);
        MinecraftForge.EVENT_BUS.post(mke);
        livingEntity.applyKnockback(mke.getStrength(), mke.getRatioX(), mke.getRatioZ());
    }

//    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "TAIL"))
//    private void resetCD(Entity targetEntity, CallbackInfo ci) {
//        this.resetCooldown();
//    }

//    /**
//     * here we go...
//     *
//     * @author jackiecrazy
//     */
//    @Overwrite
//    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
//        //replaced all "this" with casted "player"
//        PlayerEntity player = (PlayerEntity) (Object) this;
//        final ICombatCapability cap = CombatData.getCap(player);
//        Hand h = cap.isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND;
//        //introduce stack variable to hand
//        ItemStack stack = player.getHeldItem(h);
//        //replace forge hook with custom hook if player is offhand attacking
//        if (!HandedAttackEvent.onModifiedAttack(player, targetEntity, h, stack)) return;
//        if (targetEntity.canBeAttackedWithItem()) {
//            if (!targetEntity.hitByEntity(player)) {
//                //wipes out main hand attack damage and adds in offhand attack damage
//                float damage = (float) GeneralUtils.getAttributeValueHandSensitive(player, Attributes.ATTACK_DAMAGE, h);
//                float extraDamage;
//                if (targetEntity instanceof LivingEntity) {
//                    //no longer on main hand, centralize to stack
//                    extraDamage = EnchantmentHelper.getModifierForCreature(stack, ((LivingEntity) targetEntity).getCreatureAttribute());
//                } else {
//                    //no longer necessarily on main hand, centralize to stack
//                    extraDamage = EnchantmentHelper.getModifierForCreature(stack, CreatureAttribute.UNDEFINED);
//                }
//
//                //replace cooldown with cooldown checker that respects both hands
//                float cooledStrength = CombatUtils.getCooledAttackStrength(player, h, 0.5f);
//                damage = damage * (0.2F + cooledStrength * cooledStrength * 0.8F);
//                extraDamage = extraDamage * cooledStrength;
//                //due to handedness and convenience for events, cooldown reset is moved to the bottom.
//                if (damage > 0.0F || extraDamage > 0.0F) {
//                    boolean cooled = cooledStrength > 0.9F;
//                    boolean sprintCooled = false;
//                    int knockback = 0;
//                    //replace knockback check with straight check for hand. The knockback enchantment only checks for main hand, so this is necessary
//                    knockback = knockback + EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, stack);
//                    if (player.isSprinting() && cooled) {
//                        player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
//                        ++knockback;
//                        sprintCooled = true;
//                    }
//
//                    //onGround no longer public, changed to getter
//                    boolean crit = cooled && player.fallDistance > 0.0F && !player.isOnGround() && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Effects.BLINDNESS) && !player.isPassenger() && targetEntity instanceof LivingEntity;
//                    crit = crit && !player.isSprinting();
//                    //replace forge hook with custom hook. I've never seen a mod actually use this except me, but welp.
//                    CriticalHitEvent hitResult = HandedCritEvent.modifiedCrit(player, targetEntity, crit, crit ? 1.5F : 1.0F, h, stack);
//                    crit = hitResult != null;
//                    if (crit) {
//                        damage *= hitResult.getDamageModifier();
//                    }
//
//                    damage = damage + extraDamage;
//                    boolean sweep = false;
//                    double distanceWalkedLastTick = (double) (player.distanceWalkedModified - player.prevDistanceWalkedModified);
//                    //onGround no longer public, changed to getter
//                    if (cooled && !crit && !sprintCooled && player.isOnGround() && distanceWalkedLastTick < (double) player.getAIMoveSpeed()) {
//                        //no longer on main hand, centralize to stack
//                        if (stack.getItem() instanceof SwordItem) {
//                            sweep = true;
//                        }
//                    }
//
//                    float health = 0.0F;
//                    boolean burn = false;
//                    //replace fire check with straight check for hand. The fire aspect enchantment only checks for main hand, so this is necessary
//                    int fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, stack);
//                    if (targetEntity instanceof LivingEntity) {
//                        health = ((LivingEntity) targetEntity).getHealth();
//                        if (fireAspect > 0 && !targetEntity.isBurning()) {
//                            burn = true;
//                            targetEntity.setFire(1);
//                        }
//                    }
//
//                    Vector3d targetMotion = targetEntity.getMotion();
//                    //reset hurt time
//                    targetEntity.hurtResistantTime = 0;
//                    if (targetEntity instanceof LivingEntity) {
//                        ((LivingEntity) targetEntity).hurtTime = ((LivingEntity) targetEntity).maxHurtTime = 0;
//                    }
//                    //change damage source dealt and add stats
//                    boolean dealDamage = targetEntity.attackEntityFrom(new CombatDamageSource("player", player).setDamageDealer(stack).setAttackingHand(h).setProcAttackEffects(true).setProcAutoEffects(true).setCrit(crit).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL), damage);
//                    cap.addCombo(0.2f);
//                    cap.addMight((CombatUtils.getCooldownPeriod(player, h) * CombatUtils.getCooldownPeriod(player, h)) / 781.25f * (1 + (cap.getCombo() / 10f)));
//                    cap.consumePosture(0);
//                    if (dealDamage) {
//                        if (knockback > 0) {
//                            knockback *= cooledStrength;
//                            if (targetEntity instanceof LivingEntity) {
//                                ((LivingEntity) targetEntity).applyKnockback((float) knockback * 0.5F, (double) MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)), (double) (-MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F))));
//                            } else {
//                                targetEntity.addVelocity((double) (-MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)) * (float) knockback * 0.5F), 0.1D, (double) (MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F)) * (float) knockback * 0.5F));
//                            }
//
//                            player.setMotion(player.getMotion().mul(0.6D, 1.0D, 0.6D));
//                            player.setSprinting(false);
//                        }
//
//                        if (sweep) {
//
//                            float sweepRatio = 1.0F + EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING, stack) * damage;
//
//                            for (LivingEntity livingentity : player.world.getEntitiesWithinAABB(LivingEntity.class, targetEntity.getBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
//                                if (livingentity != player && livingentity != targetEntity && !player.isOnSameTeam(livingentity) && (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity) livingentity).hasMarker()) && player.getDistanceSq(livingentity) < 9.0D) {
//                                    livingentity.applyKnockback(0.4F, (double) MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)), (double) (-MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F))));
//                                    livingentity.attackEntityFrom(DamageSource.causePlayerDamage(player), sweepRatio);
//                                }
//                            }
//
//                            player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
//                            player.spawnSweepParticles();
//                        }
//
//                        if (targetEntity instanceof ServerPlayerEntity && targetEntity.velocityChanged) {
//                            ((ServerPlayerEntity) targetEntity).connection.sendPacket(new SEntityVelocityPacket(targetEntity));
//                            targetEntity.velocityChanged = false;
//                            targetEntity.setMotion(targetMotion);
//                        }
//
//                        if (crit) {
//                            player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
//                            player.onCriticalHit(targetEntity);
//                        }
//
//                        if (!crit && !sweep) {
//                            if (cooled) {
//                                player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
//                            } else {
//                                player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
//                            }
//                        }
//
//                        if (extraDamage > 0.0F) {
//                            player.onEnchantmentCritical(targetEntity);
//                        }
//
//                        player.setLastAttackedEntity(targetEntity);
//                        if (targetEntity instanceof LivingEntity) {
//                            EnchantmentHelper.applyThornEnchantments((LivingEntity) targetEntity, player);
//                        }
//
//                        EnchantmentHelper.applyArthropodEnchantments(player, targetEntity);
//                        Entity entity = targetEntity;
//                        if (targetEntity instanceof EnderDragonPartEntity) {
//                            entity = ((EnderDragonPartEntity) targetEntity).dragon;
//                        }
//
//                        //no longer on main hand, centralize to stack
//                        if (!player.world.isRemote && !stack.isEmpty() && entity instanceof LivingEntity) {
//                            ItemStack copy = stack.copy();
//                            stack.hitEntity((LivingEntity) entity, player);
//                            if (stack.isEmpty()) {
//                                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copy, h);
//                                player.setHeldItem(h, ItemStack.EMPTY);
//                            }
//                        }
//
//                        if (targetEntity instanceof LivingEntity) {
//                            float dealtDamage = health - ((LivingEntity) targetEntity).getHealth();
//                            player.addStat(Stats.DAMAGE_DEALT, Math.round(dealtDamage * 10.0F));
//                            if (fireAspect > 0) {
//                                targetEntity.setFire(fireAspect * 4);
//                            }
//
//                            if (player.world instanceof ServerWorld && dealtDamage > 2.0F) {
//                                int k = (int) ((double) dealtDamage * 0.5D);
//                                ((ServerWorld) player.world).spawnParticle(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getPosX(), targetEntity.getPosYHeight(0.5D), targetEntity.getPosZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
//                            }
//                        }
//
//                        player.addExhaustion(0.1F);
//                    } else {
//                        player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);
//                        if (burn) {
//                            targetEntity.extinguish();
//                        }
//                    }
//                }
//                //cooldown moved to the bottom,
//                CombatUtils.setHandCooldown(player, h, 0, false);
//            }
//        }
//    }
}
