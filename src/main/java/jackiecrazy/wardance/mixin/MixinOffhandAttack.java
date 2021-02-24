package jackiecrazy.wardance.mixin;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.capability.ICombatCapability;
import jackiecrazy.wardance.events.HandedAttackEvent;
import jackiecrazy.wardance.events.HandedCritEvent;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
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
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PlayerEntity.class)
public abstract class MixinOffhandAttack extends LivingEntity {

    protected MixinOffhandAttack(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * here we go...
     *
     * @author jackiecrazy
     */
    @Overwrite
    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        //replaced all "this" with casted "player"
        PlayerEntity player = (PlayerEntity) (Object) this;
        final ICombatCapability cap = CombatData.getCap(player);
        Hand h = cap.isOffhandAttack() ? Hand.OFF_HAND : Hand.MAIN_HAND;
        //introduce stack variable to hand
        ItemStack stack = player.getHeldItem(h);
        //replace forge hook with custom hook if player is offhand attacking
        if (!HandedAttackEvent.onModifiedAttack(player, targetEntity, h, stack)) return;
        if (targetEntity.canBeAttackedWithItem()) {
            if (!targetEntity.hitByEntity(player)) {
                //wipes out main hand attack damage and adds in offhand attack damage
                float damage = (float) GeneralUtils.getAttributeValueHandSensitive(player, Attributes.ATTACK_DAMAGE, h);
                float extraDamage;
                if (targetEntity instanceof LivingEntity) {
                    //no longer on main hand, centralize to stack
                    extraDamage = EnchantmentHelper.getModifierForCreature(stack, ((LivingEntity) targetEntity).getCreatureAttribute());
                } else {
                    //no longer necessarily on main hand, centralize to stack
                    extraDamage = EnchantmentHelper.getModifierForCreature(stack, CreatureAttribute.UNDEFINED);
                }

                //replace cooldown with cooldown checker that respects both hands
                float cooledStrength = CombatUtils.getCooledAttackStrength(player, h, 0.5f);
                damage = damage * (0.2F + cooledStrength * cooledStrength * 0.8F);
                extraDamage = extraDamage * cooledStrength;
                //due to handedness and convenience for events, cooldown reset is moved to the bottom.
                if (damage > 0.0F || extraDamage > 0.0F) {
                    boolean cooled = cooledStrength > 0.9F;
                    boolean sprintCooled = false;
                    int knockback = 0;
                    //replace knockback check with straight check for hand. The knockback enchantment only checks for main hand, so this is necessary
                    knockback = knockback + EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, stack);
                    if (player.isSprinting() && cooled) {
                        player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
                        ++knockback;
                        sprintCooled = true;
                    }

                    //onGround no longer public, changed to getter
                    boolean crit = cooled && player.fallDistance > 0.0F && !player.isOnGround() && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Effects.BLINDNESS) && !player.isPassenger() && targetEntity instanceof LivingEntity;
                    crit = crit && !player.isSprinting();
                    //replace forge hook with custom hook. I've never seen a mod actually use this except me, but welp.
                    CriticalHitEvent hitResult = HandedCritEvent.modifiedCrit(player, targetEntity, crit, crit ? 1.5F : 1.0F, h, stack);
                    crit = hitResult != null;
                    if (crit) {
                        damage *= hitResult.getDamageModifier();
                    }

                    damage = damage + extraDamage;
                    boolean sweep = false;
                    double distanceWalkedLastTick = (double) (player.distanceWalkedModified - player.prevDistanceWalkedModified);
                    //onGround no longer public, changed to getter
                    if (cooled && !crit && !sprintCooled && player.isOnGround() && distanceWalkedLastTick < (double) player.getAIMoveSpeed()) {
                        //no longer on main hand, centralize to stack
                        if (stack.getItem() instanceof SwordItem) {
                            sweep = true;
                        }
                    }

                    float health = 0.0F;
                    boolean burn = false;
                    //replace fire check with straight check for hand. The fire aspect enchantment only checks for main hand, so this is necessary
                    int fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, stack);
                    if (targetEntity instanceof LivingEntity) {
                        health = ((LivingEntity) targetEntity).getHealth();
                        if (fireAspect > 0 && !targetEntity.isBurning()) {
                            burn = true;
                            targetEntity.setFire(1);
                        }
                    }

                    Vector3d targetMotion = targetEntity.getMotion();
                    boolean dealDamage = targetEntity.attackEntityFrom(new CombatDamageSource("player", player).setDamageDealer(stack).setAttackingHand(h).setProcAttackEffects(true).setProcAutoEffects(true).setCrit(crit), damage);
                    cap.addCombo(0.2f);
                    cap.addQi(4 / CombatUtils.getCooldownPeriod(player, h) * 0.004f * (1 + (cap.getCombo() / 10f)));
                    cap.consumePosture(0);
                    if (dealDamage) {
                        if (knockback > 0) {
                            if (targetEntity instanceof LivingEntity) {
                                ((LivingEntity) targetEntity).applyKnockback((float) knockback * 0.5F, (double) MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)), (double) (-MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F))));
                            } else {
                                targetEntity.addVelocity((double) (-MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)) * (float) knockback * 0.5F), 0.1D, (double) (MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F)) * (float) knockback * 0.5F));
                            }

                            player.setMotion(player.getMotion().mul(0.6D, 1.0D, 0.6D));
                            player.setSprinting(false);
                        }

                        if (sweep) {
                            //TODO rewrite sweep for full damage and effects
                            float sweepRatio = 1.0F + EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING, stack) * damage;

                            for (LivingEntity livingentity : player.world.getEntitiesWithinAABB(LivingEntity.class, targetEntity.getBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
                                if (livingentity != player && livingentity != targetEntity && !player.isOnSameTeam(livingentity) && (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity) livingentity).hasMarker()) && player.getDistanceSq(livingentity) < 9.0D) {
                                    livingentity.applyKnockback(0.4F, (double) MathHelper.sin(player.rotationYaw * ((float) Math.PI / 180F)), (double) (-MathHelper.cos(player.rotationYaw * ((float) Math.PI / 180F))));
                                    livingentity.attackEntityFrom(DamageSource.causePlayerDamage(player), sweepRatio);
                                }
                            }

                            player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
                            player.spawnSweepParticles();
                        }

                        if (targetEntity instanceof ServerPlayerEntity && targetEntity.velocityChanged) {
                            ((ServerPlayerEntity) targetEntity).connection.sendPacket(new SEntityVelocityPacket(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.setMotion(targetMotion);
                        }

                        if (crit) {
                            player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(targetEntity);
                        }

                        if (!crit && !sweep) {
                            if (cooled) {
                                player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (extraDamage > 0.0F) {
                            player.onEnchantmentCritical(targetEntity);
                        }

                        player.setLastAttackedEntity(targetEntity);
                        if (targetEntity instanceof LivingEntity) {
                            //TODO mixin thorns helper to account for offhand
                            EnchantmentHelper.applyThornEnchantments((LivingEntity) targetEntity, player);
                        }

                        //TODO mixin thorns helper to account for offhand
                        EnchantmentHelper.applyArthropodEnchantments(player, targetEntity);
                        Entity entity = targetEntity;
                        if (targetEntity instanceof EnderDragonPartEntity) {
                            entity = ((EnderDragonPartEntity) targetEntity).dragon;
                        }

                        //no longer on main hand, centralize to stack
                        if (!player.world.isRemote && !stack.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = stack.copy();
                            stack.hitEntity((LivingEntity) entity, player);
                            if (stack.isEmpty()) {
                                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copy, h);
                                player.setHeldItem(h, ItemStack.EMPTY);
                            }
                        }

                        if (targetEntity instanceof LivingEntity) {
                            float dealtDamage = health - ((LivingEntity) targetEntity).getHealth();
                            player.addStat(Stats.DAMAGE_DEALT, Math.round(dealtDamage * 10.0F));
                            if (fireAspect > 0) {
                                targetEntity.setFire(fireAspect * 4);
                            }

                            if (player.world instanceof ServerWorld && dealtDamage > 2.0F) {
                                int k = (int) ((double) dealtDamage * 0.5D);
                                ((ServerWorld) player.world).spawnParticle(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getPosX(), targetEntity.getPosYHeight(0.5D), targetEntity.getPosZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        player.addExhaustion(0.1F);
                    } else {
                        player.world.playSound((PlayerEntity) null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);
                        if (burn) {
                            targetEntity.extinguish();
                        }
                    }
                }
                //cooldown moved to the bottom
                CombatUtils.setHandCooldown(player, h, 0, false);
            }
        }
    }
}
