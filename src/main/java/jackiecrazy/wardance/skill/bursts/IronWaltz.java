package jackiecrazy.wardance.skill.bursts;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.capability.timeslow.TimeSlowData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.config.weapon.interactions.Animation;
import jackiecrazy.wardance.config.weapon.interactions.SweepAttack;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.entity.skill.EchoWeapon;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.ReworkConstants;
import jackiecrazy.wardance.utils.SweepAnimationBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.joml.Vector4d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class IronWaltz extends Skill {

    @Override
    public HashSet<String> getTags() {
        return burst;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return burst;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            if (!StylishData.getCap(caster).drainAdrenaline(0.005f / stats.getEffectiveness())) {
                markUsed(caster);
                CombatData.getCap(caster).setSpirit(0);
                return true;
            }
        }
        return false;
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster, SkillData sd) {
        if (!StylishData.getCap(caster).maxAdrenaline()) return CastStatus.ADRENALINE;
        return super.castingCheck(caster, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof ProjectileImpactEvent pie && procPoint.getPhase()== EventPriority.HIGHEST && state==STATE.ACTIVE && pie.getProjectile().getOwner()==caster) {
            ringOfBlades(caster, pie.getProjectile().position(), (int)stats.getArbitraryFloat());
        }
        if (procPoint instanceof PlayInteractionEvent.Interaction e && e.getPhase() == EventPriority.HIGHEST) {
            if (e.getInteraction().hasInteractionType(WeaponInteractions.WeaponInteraction.InteractionType.SWEEP) || e.getInteraction().hasInteractionType(WeaponInteractions.WeaponInteraction.InteractionType.ANIMATE))
                if ((state == STATE.HOLSTERED && cast(caster, 1)) || state == STATE.ACTIVE) {
                    //ring of blades
                    stats.addArbitraryFloat(1);
                    Animation am = null;
                    if (e.getInteraction().getInteractionOfType(WeaponInteractions.WeaponInteraction.InteractionType.ANIMATE) instanceof Animation a) {
                        am = a;
                    } else if (e.getInteraction().getInteractionOfType(WeaponInteractions.WeaponInteraction.InteractionType.SWEEP) instanceof SweepAttack a) {
                        am = new Animation().setAction(SweepAnimationBuilder.managerFromBasic(10, a.getType(), a.getBase(), a.getHitInfo(), a.getRangeMult()));
                    }
                    ringOfBlades(caster, caster.getEyePosition(), (int)stats.getArbitraryFloat());
                }
        }
        //leave echoes on projectile
    }

    private void ringOfBlades(LivingEntity caster, Vec3 pos, int ordinal) {
        int radius = 1;
//        int numOfBlades = 2;
//        int numInRing = ordinal;
//        while (numInRing >= numOfBlades) {
//            numInRing -= numOfBlades;
//            radius++;
//            numOfBlades += 2;
//        }
        //if (numInRing != 0) return;
        //they'll go outwards in 2-4-6-8 for radius 1-2-3-4, so quiver start index is
//        ItemStack[] l = new ItemStack[radius * 2];
//        Arrays.fill(l, new ItemStack(Items.IRON_SWORD));
        ItemStack is = new ItemStack(Items.IRON_SWORD);

        //fill the slots in
        if (caster instanceof Player p) {
//            int startIndex = (radius) * (radius - 1);//0, 2, 6, 12
//            int endIndex = (radius) * (radius + 1);//1, 5, 11, 19

            List<ItemStack> allInQuiver = QuiverData.getData(p).getAllQuiveredItems(-1);
            allInQuiver.sort((first, second) -> (int) (100 * (CombatUtils.getPostureAtk(null, null, null, null, 7, second) - CombatUtils.getPostureAtk(null, null, null, null, 7, first))));
//            for (int i = startIndex; i < Math.min(endIndex, allInQuiver.size()); i++)
//                l[i - startIndex] = allInQuiver.get(i);
            if (allInQuiver.size() > ordinal) {
                is = allInQuiver.get(ordinal);
            }
        }
//        for (int o = 0; o < l.length; o++) {
//            ItemStack is = l[o].copy();
//            float angle = numInRing * (360f / numOfBlades);
//            double x = Math.cos(Mth.DEG_TO_RAD * angle);
//            double y = Math.sin(Mth.DEG_TO_RAD * angle);
        EchoWeapon awe = new EchoWeapon(WarEntities.ECHO.get(), caster.level());
        awe.setHeldItem(is);
        awe.setOwner(caster);
        awe.setEffect(FlyingWeaponEffect.TRAIL, FlyingWeaponEffect.WEAPON);
        awe.setSkillUsed(this);
        awe.moveTo(pos);
        awe.lock(caster);
//            awe.lockLook(new Vec3(y, 0, x));//caster.getLookAngle().multiply(1,0,1).normalize().add(y, 0, x).normalize());
        awe.setIdlePose(new MotionManagers.FixedMM(new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, radius)), 2));
        final int y = 0;
        final int z = 1;
        final List<MotionFrame> CIRCLE = new ArrayList<>(List.of(
                new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, radius), 90).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON).setHit(new HitInfo(0, 0.1, 0.1, false, false, 1))),
                new MotionFrame(new Vec3(-1, 0, 0), new Vec3(0, 0, radius), 90),
                new MotionFrame(new Vec3(0, 0, -1), new Vec3(0, 0, radius), 90),
                new MotionFrame(new Vec3(1, 0, 0), new Vec3(0, 0, radius), 90),
                new MotionFrame(new Vec3(0, 0, 1), new Vec3(0, 0, radius), 90)));
//            if (radius % 2 == 0) Collections.reverse(CIRCLE);
        for (int a = 0; a < 20; a++)
//                for (MotionManager mm : anim.getAnimations())
//                    awe.queuePath(mm, 3, 3);
            awe.queuePath(new MotionManagers.DefinitionMM(new MotionGroup(CIRCLE, EasingFunctionEnum.LINEAR, 15)));
        caster.level().addFreshEntity(awe);
        FlyingWeaponData.getCap(caster).addExtraWeapon("echo", awe);
//        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            prev.setArbitraryFloat(0);
            StylishData.getCap(caster).resetAdrenaline();
            finale(caster);
            //FlyingWeaponData.getCap(caster).dismissWeapons("echo");
            return true;
        }
        return boundCast(prev, from, to);
    }


    private void finale(LivingEntity caster) {
        //every echo flies back to you, dealing damage to everything along the way
        for (FlyingItemEntity fwe : FlyingWeaponData.getCap(caster).getExtraWeapons("echo")) {
            fwe.setMotionTarget(caster);
            final EchoWeapon echo = (EchoWeapon) fwe;
            echo.clearPath();
            echo.unlockLook();
            echo.yeet(caster.getEyePosition(), 2);
            echo.setHitInfo(new HitInfo(0, 0.1, 0.1, false, false, 1));
            echo.setMoveset(null, 0);
            echo.recalculateOrientation(echo.getIdlePose().getEndFrame().renderOrientation(), 1);
            TimeSlowData.getCap(echo).alterSpeed(10, 0);
            echo.setState(FlyingItemEntity.STATE.THROW_TRACK);
            //fwe.setState(FlyingItemEntity.STATE.THROW_TRACK);
        }
        FlyingWeaponData.getCap(caster).dismissWeapons("echo");
    }


}
