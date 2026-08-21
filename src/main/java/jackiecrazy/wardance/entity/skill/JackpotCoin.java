package jackiecrazy.wardance.entity.skill;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.ActionSets;
import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.utils.ActionJsonAdapters;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.entity.IRicochetPriority;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import jackiecrazy.wardance.entity.WarEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class JackpotCoin extends Entity implements IRicochetPriority {
    public static final HitInfo BABY = new HitInfo(0, 0.5, 1, false, true, 1).setSpirit_multiplier((double) 0.0F);
    public boolean spent = false;
    private int spentTickDown = 60;

    public JackpotCoin(EntityType<? extends JackpotCoin> p_27403_,
                       Level p_27404_) {
        super(p_27403_, p_27404_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0D);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public void tick() {
        super.tick();
        move(MoverType.SELF, getDeltaMovement());
        if (getDeltaMovement().y > 0||spent)
            addDeltaMovement(new Vec3(0, -Math.min(0.05, getDeltaMovement().y), 0));
//        if(tickCount%100==0)spent=false;
        if (tickCount > 300 || (spent&&spentTickDown-- < 0)) discard();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource p_20122_) {
        return spent || !(p_20122_ instanceof CombatDamageSource cds) || !(cds.getEntity() instanceof LivingEntity) || !(cds.getDirectEntity() instanceof ThrownWeaponEntity);
    }

    public boolean hurt(DamageSource source, float p_27425_) {
        if (this.isInvulnerableTo(source) || spent) {
            return false;
        } else {
            //shoot off a fragment
            playSound(SoundEvents.COPPER_HIT);
            //if(level() instanceof ServerLevel sl)
            final CombatDamageSource cds = ((CombatDamageSource) source);
            final ThrownWeaponEntity twe = (ThrownWeaponEntity) cds.getDirectEntity();
            final LivingEntity caster = (LivingEntity) cds.getEntity();
            level().playSound(null, caster, SoundEvents.COPPER_HIT, SoundSource.PLAYERS, 1, 1.4f + WarDance.rand.nextFloat() * 0.4f);
            twe.setBounce(2);
            twe.setEffect(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.AFTERIMAGE);
            twe.setGravity(0);
            if (!twe.ricochet()) {
                twe.yeet(position().add((WarDance.rand.nextDouble() - 0.5) * 5, (WarDance.rand.nextDouble()) * -5, (WarDance.rand.nextDouble() - 0.5) * 5), 2);
                twe.setPierce(999);
                twe.setBounce(0);
            }
            ThrownWeaponEntity fwe = new ThrownWeaponEntity(WarEntities.THROWN_WEAPON.get(), level());
            fwe.setHeldItem(twe.getHeldItem().copy());
            fwe.setCosmeticItem(new ItemStack(Items.GOLD_NUGGET));
            fwe.setOwner(caster);
            fwe.moveTo(position());
//            fwe.setSkillUsed(War);
            fwe.setFake(true).setFlourish(false).setEffect(FlyingWeaponEffect.WEAPON);
            fwe.setPierce(9999).setBounce(1).setLodgeEntity(false).setInteractionRange(1);

            //fwe.yeet(pos, strength);
            fwe.setInteractionRange(1f);
            fwe.setEmbedActions(List.of(ActionJsonAdapters.gson.fromJson(ActionSets.moves.get(new ResourceLocation("wardance:expire_10s")), Action[].class)));
            fwe.setHitInfo(BABY);
            fwe.setDeltaMovement(new Vec3(0, 0, 2));
            if (fwe.ricochet(JackpotCoin.class)) {
                level().addFreshEntity(fwe);
            }
            discard();
            return super.hurt(source, p_27425_);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {

    }

    @Override
    public boolean isValidRicochetTarget() {
        return !spent;
    }
}
