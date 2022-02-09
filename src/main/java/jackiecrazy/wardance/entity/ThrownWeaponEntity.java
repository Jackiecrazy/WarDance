package jackiecrazy.wardance.entity;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Arrays;

@MethodsReturnNonnullByDefault
public class ThrownWeaponEntity extends AbstractArrowEntity {

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
    Hand hand = Hand.MAIN_HAND;
    ItemStack stack = ItemStack.EMPTY;
    private Skill s= WarSkills.VITAL_STRIKE.get();

    public ThrownWeaponEntity(EntityType<? extends ThrownWeaponEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public ThrownWeaponEntity setStack(ItemStack is) {
        stack = is;
        return this;
    }

    @Override
    protected ItemStack getArrowStack() {
        return stack;
    }

    protected void registerData() {
        super.registerData();
        //this.dataManager.register(SKILL, WarSkills.WEAPON_THROW.get());
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.read(compound);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult result) {
        Entity entity = result.getEntity();
        float f = (float)this.getMotion().length();
        int i = MathHelper.ceil(MathHelper.clamp((double)f * this.getDamage(), 0.0D, 2.147483647E9D));
        if (this.getPierceLevel() > 0) {
            if (this.piercedEntities == null) {
                this.piercedEntities = new IntOpenHashSet(5);
            }

            if (this.hitEntities == null) {
                this.hitEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercedEntities.size() >= this.getPierceLevel() + 1) {
                this.remove();
                return;
            }

            this.piercedEntities.add(entity.getEntityId());
        }

        if (this.getIsCritical()) {
            long j = (long)this.rand.nextInt(i / 2 + 2);
            i = (int)Math.min(j + (long)i, 2147483647L);
        }

        Entity entity1 = this.getShooter();
        DamageSource damagesource;
        if (entity1 == null) {
            damagesource = DamageSource.causeArrowDamage(this, this);
        } else {
            damagesource = DamageSource.causeArrowDamage(this, entity1);
            if (entity1 instanceof LivingEntity) {
                ((LivingEntity)entity1).setLastAttackedEntity(entity);
            }
        }

        boolean flag = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getFireTimer();
        if (this.isBurning() && !flag) {
            entity.setFire(5);
        }

        if (entity.attackEntityFrom(damagesource, (float)i)) {
            if (flag) {
                return;
            }

            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)entity;
                if (!this.world.isRemote && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCountInEntity(livingentity.getArrowCountInEntity() + 1);
                }

                if (this.knockbackStrength > 0) {
                    Vector3d vector3d = this.getMotion().mul(1.0D, 0.0D, 1.0D).normalize().scale((double)this.knockbackStrength * 0.6D);
                    if (vector3d.lengthSquared() > 0.0D) {
                        livingentity.addVelocity(vector3d.x, 0.1D, vector3d.z);
                    }
                }

                if (!this.world.isRemote && entity1 instanceof LivingEntity) {
                    EnchantmentHelper.applyThornEnchantments(livingentity, entity1);
                    EnchantmentHelper.applyArthropodEnchantments((LivingEntity)entity1, livingentity);
                }

                this.arrowHit(livingentity);
                if (livingentity != entity1 && livingentity instanceof PlayerEntity && entity1 instanceof ServerPlayerEntity && !this.isSilent()) {
                    ((ServerPlayerEntity)entity1).connection.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.HIT_PLAYER_ARROW, 0.0F));
                }

                if (!entity.isAlive() && this.hitEntities != null) {
                    this.hitEntities.add(livingentity);
                }

                if (!this.world.isRemote && entity1 instanceof ServerPlayerEntity) {
                    ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)entity1;
                    if (this.hitEntities != null && this.getShotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.test(serverplayerentity, this.hitEntities);
                    } else if (!entity.isAlive() && this.getShotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.test(serverplayerentity, Arrays.asList(entity));
                    }
                }
            }

            this.playSound(this.getHitEntitySound(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.remove();
            }
        } else {
            entity.forceFireTicks(k);
            this.setMotion(this.getMotion().scale(-0.1D));
            this.rotationYaw += 180.0F;
            this.prevRotationYaw += 180.0F;
            if (!this.world.isRemote && this.getMotion().lengthSquared() < 1.0E-7D) {
                if (this.pickupStatus == AbstractArrowEntity.PickupStatus.ALLOWED) {
                    this.entityDropItem(this.getArrowStack(), 0.1F);
                }

                this.remove();
            }
        }
    }
}
