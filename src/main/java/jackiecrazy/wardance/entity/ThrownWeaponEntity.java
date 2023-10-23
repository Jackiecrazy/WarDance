package jackiecrazy.wardance.entity;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

@MethodsReturnNonnullByDefault
public class ThrownWeaponEntity extends AbstractArrow {

    /*
    Weapon throw: your next swing consumes 2 might to throw the weapon, dealing normal attack damage on impact and creating a decently loud sound. The weapon will automatically return to you after 3 seconds
quantum entanglement: cast while the weapon is out to consume 3 spirit and switch places with the weapon. This can be repeated until you retrieve the weapon manually.
undertow: the thrown weapon, upon landing, creates a 3-block radius circle that slows and continually damages enemies caught in it, at a rate of 1 magic damage/second, costing 0.5 spirit per second.
force retrieval: consume 3 spirit per cast when the weapon is out to launch it towards you, piercing through and dealing damage to enemies in the path. It's not homing, and thus can be repeatedly invoked and dodged away from to extend dps time.
manifold assault: channel for 0.6 seconds and consume 3 spirit to launch a phantom version of the weapon at all nearby enemies, the weapon remains in your inventory.
end him rightly: keep the weapon in your hand and throw the pommel instead, distracting the target for 1 second. Following up with the weapon itself deals 2.5x critical damage, and might is refunded if the target dies.
     */

    //data parameters are used to sync data!
    //private static final DataParameter<Skill> SKILL = EntityDataManager.createKey(ThrownWeaponEntity.class, SkillUtils.SKILLSERIALIZER);
    InteractionHand hand = InteractionHand.MAIN_HAND;
    ItemStack stack = ItemStack.EMPTY;
    private Skill s = WarSkills.VITAL_STRIKE.get();

    public ThrownWeaponEntity(EntityType<? extends ThrownWeaponEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public ThrownWeaponEntity setStack(ItemStack is) {
        stack = is;
        return this;
    }

    @Override
    protected ItemStack getPickupItem() {
        return stack;
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        //this.dataManager.register(SKILL, WarSkills.WEAPON_THROW.get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.load(compound);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        float f = (float) this.getDeltaMovement().length();
        int i = Mth.ceil(Mth.clamp((double) f * this.getBaseDamage(), 0.0D, 2.147483647E9D));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.remove(RemovalReason.KILLED);
                return;
            }

            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (this.isCritArrow()) {
            long j = (long) this.random.nextInt(i / 2 + 2);
            i = (int) Math.min(j + (long) i, 2147483647L);
        }

        Entity entity1 = this.getOwner();
        DamageSource damagesource;
        if (entity1 == null) {
            damagesource = entity.level().damageSources().arrow(this, this);
        } else {
            damagesource = entity.level().damageSources().arrow(this, entity1);
            if (entity1 instanceof LivingEntity) {
                ((LivingEntity) entity1).setLastHurtMob(entity);
            }
        }

        boolean flag = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getRemainingFireTicks();
        if (this.isOnFire() && !flag) {
            entity.setSecondsOnFire(5);
        }

        if (entity.hurt(damagesource, (float) i)) {
            if (flag) {
                return;
            }

            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity) entity;
                if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (this.knockback > 0) {
                    Vec3 vector3d = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double) this.knockback * 0.6D);
                    if (vector3d.lengthSqr() > 0.0D) {
                        livingentity.push(vector3d.x, 0.1D, vector3d.z);
                    }
                }

                if (!this.level().isClientSide && entity1 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) entity1, livingentity);
                }

                this.doPostHurtEffects(livingentity);
                if (livingentity != entity1 && livingentity instanceof Player && entity1 instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer) entity1).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }

                if (!this.level().isClientSide && entity1 instanceof ServerPlayer) {
                    ServerPlayer serverplayerentity = (ServerPlayer) entity1;
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayerentity, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayerentity, Arrays.asList(entity));
                    }
                }
            }

            this.playSound(this.getDefaultHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.remove(RemovalReason.KILLED);
            }
        } else {
            entity.setRemainingFireTicks(k);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
            setYRot(getYRot() + 180f);
            this.yRotO += 180.0F;
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.remove(RemovalReason.KILLED);
            }
        }
    }
}
