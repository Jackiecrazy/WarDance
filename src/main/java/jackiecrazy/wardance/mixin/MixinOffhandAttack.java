package jackiecrazy.wardance.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerEntity.class)
public abstract class MixinOffhandAttack extends LivingEntity {

    protected MixinOffhandAttack(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * here we go...
     * @author jackiecrazy
     */
    @Overwrite
    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        //replaced all "this" with casted "player"
        PlayerEntity player=(PlayerEntity) (Object) this;
        //TODO replace forge hook with custom hook if player is offhand attacking
        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(player, targetEntity)) return;
        if (targetEntity.canBeAttackedWithItem()) {
            if (!targetEntity.hitByEntity(player)) {
                //TODO fix attack damage attribute calculation, deleting main hand and adding offhand damage
                float damage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float extraDamage;
                if (targetEntity instanceof LivingEntity) {
                    //TODO replace all instances of getHeldItemMainhand() with hand sensitive version
                    extraDamage = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), ((LivingEntity)targetEntity).getCreatureAttribute());
                } else {
                    extraDamage = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
                }

                float cooledStrength = player.getCooledAttackStrength(0.5F);
                damage = damage * (0.2F + cooledStrength * cooledStrength * 0.8F);
                extraDamage = extraDamage * cooledStrength;
                player.resetCooldown();
                if (damage > 0.0F || extraDamage > 0.0F) {
                    boolean cooled = cooledStrength > 0.9F;
                    boolean sprintCooled = false;
                    int knockback = 0;
                    knockback = knockback + EnchantmentHelper.getKnockbackModifier(player);
                    if (player.isSprinting() && cooled) {
                        player.world.playSound((PlayerEntity)null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
                        ++knockback;
                        sprintCooled = true;
                    }

                    //onGround no longer public, changed to getter
                    boolean crit = cooled && player.fallDistance > 0.0F && !player.isOnGround() && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Effects.BLINDNESS) && !player.isPassenger() && targetEntity instanceof LivingEntity;
                    crit = crit && !player.isSprinting();
                    net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks.getCriticalHit(player, targetEntity, crit, crit ? 1.5F : 1.0F);
                    crit = hitResult != null;
                    if (crit) {
                        damage *= hitResult.getDamageModifier();
                    }

                    damage = damage + extraDamage;
                    boolean sweep = false;
                    double distanceWalkedLastTick = (double)(player.distanceWalkedModified - player.prevDistanceWalkedModified);
                    //onGround no longer public, changed to getter
                    if (cooled && !crit && !sprintCooled && player.isOnGround() && distanceWalkedLastTick < (double)player.getAIMoveSpeed()) {
                        ItemStack itemstack = player.getHeldItem(Hand.MAIN_HAND);
                        if (itemstack.getItem() instanceof SwordItem) {
                            sweep = true;
                        }
                    }

                    float health = 0.0F;
                    boolean burn = false;
                    //TODO modify to only check specific hand
                    int fireAspect = EnchantmentHelper.getFireAspectModifier(player);
                    if (targetEntity instanceof LivingEntity) {
                        health = ((LivingEntity)targetEntity).getHealth();
                        if (fireAspect > 0 && !targetEntity.isBurning()) {
                            burn = true;
                            targetEntity.setFire(1);
                        }
                    }

                    Vector3d targetMotion = targetEntity.getMotion();
                    boolean dealDamage = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
                    if (dealDamage) {
                        if (knockback > 0) {
                            if (targetEntity instanceof LivingEntity) {
                                ((LivingEntity)targetEntity).applyKnockback((float)knockback * 0.5F, (double) MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F))));
                            } else {
                                targetEntity.addVelocity((double)(-MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)) * (float)knockback * 0.5F), 0.1D, (double)(MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F)) * (float)knockback * 0.5F));
                            }

                            player.setMotion(player.getMotion().mul(0.6D, 1.0D, 0.6D));
                            player.setSprinting(false);
                        }

                        if (sweep) {
                            //TODO rewrite sweep for full damage and effects
                            float sweepRatio = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * damage;

                            for(LivingEntity livingentity : player.world.getEntitiesWithinAABB(LivingEntity.class, targetEntity.getBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
                                if (livingentity != player && livingentity != targetEntity && !player.isOnSameTeam(livingentity) && (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity)livingentity).hasMarker()) && player.getDistanceSq(livingentity) < 9.0D) {
                                    livingentity.applyKnockback(0.4F, (double)MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F))));
                                    livingentity.attackEntityFrom(DamageSource.causePlayerDamage(player), sweepRatio);
                                }
                            }

                            player.world.playSound((PlayerEntity)null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
                            player.spawnSweepParticles();
                        }

                        if (targetEntity instanceof ServerPlayerEntity && targetEntity.velocityChanged) {
                            ((ServerPlayerEntity)targetEntity).connection.sendPacket(new SEntityVelocityPacket(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.setMotion(targetMotion);
                        }

                        if (crit) {
                            player.world.playSound((PlayerEntity)null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(targetEntity);
                        }

                        if (!crit && !sweep) {
                            if (cooled) {
                                player.world.playSound((PlayerEntity)null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                player.world.playSound((PlayerEntity)null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (extraDamage > 0.0F) {
                            player.onEnchantmentCritical(targetEntity);
                        }

                        player.setLastAttackedEntity(targetEntity);
                        if (targetEntity instanceof LivingEntity) {
                            EnchantmentHelper.applyThornEnchantments((LivingEntity)targetEntity, player);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(player, targetEntity);
                        ItemStack attackingStack = player.getHeldItemMainhand();
                        Entity entity = targetEntity;
                        if (targetEntity instanceof EnderDragonPartEntity) {
                            entity = ((EnderDragonPartEntity)targetEntity).dragon;
                        }

                        if (!player.world.isRemote && !attackingStack.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = attackingStack.copy();
                            attackingStack.hitEntity((LivingEntity)entity, player);
                            if (attackingStack.isEmpty()) {
                                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copy, Hand.MAIN_HAND);
                                player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (targetEntity instanceof LivingEntity) {
                            float dealtDamage = health - ((LivingEntity)targetEntity).getHealth();
                            player.addStat(Stats.DAMAGE_DEALT, Math.round(dealtDamage * 10.0F));
                            if (fireAspect > 0) {
                                targetEntity.setFire(fireAspect * 4);
                            }

                            if (player.world instanceof ServerWorld && dealtDamage > 2.0F) {
                                int k = (int)((double)dealtDamage * 0.5D);
                                ((ServerWorld)player.world).spawnParticle(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getPosX(), targetEntity.getPosYHeight(0.5D), targetEntity.getPosZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        player.addExhaustion(0.1F);
                    } else {
                        player.world.playSound((PlayerEntity)null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);
                        if (burn) {
                            targetEntity.extinguish();
                        }
                    }
                }

            }
        }
    }
}
