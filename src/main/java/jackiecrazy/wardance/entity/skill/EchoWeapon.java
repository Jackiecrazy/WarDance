package jackiecrazy.wardance.entity.skill;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.wardance.config.weapon.interactions.Animation;
import jackiecrazy.wardance.entity.FlyingWeaponEntity;
import jackiecrazy.wardance.entity.ThrownWeaponEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;

public class EchoWeapon extends FlyingWeaponEntity {
    private Animation moveset;
    private int cooldown;

    public EchoWeapon(EntityType<? extends FlyingItemEntity> type,
                      Level level) {
        super(type, level);
    }

    public void setMoveset(Animation moveset, int cooldown) {
        this.moveset = moveset;
        this.cooldown = cooldown;
    }

    @Override
    protected void returnToIdle(int duration) {
        super.returnToIdle(duration);
        alreadyHit.clear();
//        animTicker++;
//        if (animTicker > cooldown && moveset != null) {
//            animTicker = 0;
//            for (MotionManager mm : moveset.getAnimations())
//                queuePath(mm, 2, 2);
//            setIntangible(false);
//            while (!trailHistory.isEmpty()) trailHistory.pop();
//        }
    }

    @Override
    public boolean hasEffect(FlyingWeaponEffect f) {
        return f==FlyingWeaponEffect.AFTERIMAGE;
    }

    @Override
    public void tick() {
        if(getOwner()==null) {
            remove(RemovalReason.KILLED);
            return;
        }
        super.tick();
        if (getState() ==STATE.THROW_TRACK && distanceToSqr(getOwner().getEyePosition())<2)
            remove(RemovalReason.KILLED);
    }

    @Override
    protected boolean onHitEntity(List<Entity> targets) {
//        if(targets.contains(getOwner())){
//            invalidateWhenDone();
//            return true;
//        }
//        if(getOwner().distanceToSqr(this)<4){
//            invalidateWhenDone();
//            return true;
//        }
        return super.onHitEntity(targets);
    }

    @Override
    public void setState(STATE s) {
        super.setState(s);
        moveset=null;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void unlock() {
        super.unlock();
    }

    @Override
    public void remove(RemovalReason r) {
        if(r==RemovalReason.DISCARDED){

            return;
        }
        super.remove(r);
    }
}
