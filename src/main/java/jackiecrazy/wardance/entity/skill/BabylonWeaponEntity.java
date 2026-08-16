package jackiecrazy.wardance.entity.skill;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.ActionSets;
import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.footwork.utils.ActionJsonAdapters;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import jackiecrazy.wardance.entity.WarEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.awt.*;
import java.util.List;

public class BabylonWeaponEntity extends FlyingWeaponEntity {
    public static final HitInfo BABY = new HitInfo(0, 0.7, 1, false, true, 1).setSpirit_multiplier((double) 0.0F);
    public int size = 0;
    private List<ItemStack> conjureList = List.of(new ItemStack(Items.GOLDEN_SWORD));
    private Entity target;
    private int toggleTime = -1;

    public BabylonWeaponEntity(EntityType<? extends FlyingItemEntity> type,
                               Level level) {
        super(type, level);
        setState(STATE.FOLLOW);
        setEffect();
        //setEffect();
        setTrailColor(Color.YELLOW);
        setIntangible(true);
    }

    public void setConjureList(List<ItemStack> conjureList) {
        this.conjureList = conjureList;
    }

    @Override
    public boolean skipAttackInteraction(Entity ent) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void prepareToFire(Entity t) {
        target = t;
        toggleTime = 20;
        setEffect(FlyingWeaponEffect.WEAPON);
        setFlipRender(true);
        lock(getOwner());
    }

    public void fireTowards(Entity t) {
        //create a ghost thrown weapon, target it towards the mob, and fire!
        ThrownWeaponEntity fwe = new ThrownWeaponEntity(WarEntities.THROWN_WEAPON.get(), level());
        if (conjureList.isEmpty()) conjureList.add(new ItemStack(Items.GOLDEN_SWORD));
        //level().playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
        final ItemStack held = conjureList.get(WarDance.rand.nextInt(conjureList.size()));
        fwe.setHeldItem(held.copyWithCount(1));
        LivingEntity player = getOwner();
        fwe.setOwner(player);
        fwe.moveTo(position());
        fwe.setState(STATE.THROW_TRACK);
        fwe.setMotionTarget(t);
        fwe.setFake(true).setFlourish(false).setEffect(FlyingWeaponEffect.WEAPON);
        fwe.setPierce(9999).setLodgeEntity(false).setInteractionRange(1);

        //fwe.yeet(pos, strength);
        fwe.setInteractionRange(1f);
        fwe.setEmbedActions(List.of(ActionJsonAdapters.gson.fromJson(ActionSets.moves.get(new ResourceLocation("wardance:expire_10s")), Action[].class)));
        fwe.setHitInfo(BABY).yeet(t.getEyePosition(), 2);
        level().addFreshEntity(fwe);
    }

    private boolean validTarget(Entity a) {
        return a != getOwner()
                && !a.isRemoved()
                //&& !alreadyHit.contains(a)
                && TargetingUtils.isHostile(a, getOwner())
                && (a instanceof LivingEntity le && Marks.getCap(le).isMarked(skillUsed));
    }

    @Override
    public void tick() {
        super.tick();
        toggleTime--;
        if (!level().isClientSide && toggleTime == 0) {
            fireTowards(target);
            setFlipRender(false);
        }
        if (toggleTime == -20)
            setEffect();
    }

    @Override
    protected void updateClientData() {
        super.updateClientData();
        if (hasEffect(FlyingWeaponEffect.WEAPON)) {
            if (flipClientRender())
                size++;
            else size--;
        }

    }

    @Override
    public void invalidateWhenDone() {
        fading = true;
    }

    @Override
    public void unlock() {
        super.unlock();
    }

    @Override
    public boolean isIdle() {
        return !hasEffect(FlyingWeaponEffect.WEAPON);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.DISCARDED && hasEffect(FlyingWeaponEffect.WEAPON))
            invalidateWhenDone();
        else super.remove(reason);
    }
}
