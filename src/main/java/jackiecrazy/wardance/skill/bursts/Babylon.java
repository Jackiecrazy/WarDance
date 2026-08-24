package jackiecrazy.wardance.skill.bursts;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.event.GainAdrenalineEvent;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.move.motionframe.render.RenderItemGroup;
import jackiecrazy.footwork.move.motionframe.render.RenderNode;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.entity.skill.BabylonPortal;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Babylon extends Skill {
    /*
    conjure weapons in a large space above you while levitating upwards.
    glow every mob, mark every mob you set your crosshair on, their glow turns red
    after 3 seconds of windup continuously fire weapons at marked mobs. Weapons have infinite pierce and tracking flight
     */

    @Override
    public CastStatus castingCheck(LivingEntity caster, SkillData sd) {
        if (!StylishData.getCap(caster).maxAdrenaline()) return CastStatus.ADRENALINE;
        return super.castingCheck(caster, sd);
    }

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
    public boolean showsMark(SkillData mark, LivingEntity target) {
        return false;
    }

    @Override
    public boolean markTick(@Nullable LivingEntity caster, LivingEntity target, SkillData sd) {
        return markTickDown(sd);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            if (!StylishData.getCap(caster).drainAdrenaline(1 / (200f * stats.getEffectiveness()))) {
                markUsed(caster);
                CombatData.getCap(caster).setSpirit(0);
                return true;
            }
            CombatData.getCap(caster).setSpirit(100);
            //hitscan entities...
            LivingEntity le = SkillUtils.aimLiving(caster, 32);
            if (le != null) {
                stats.addTarget(le);
                mark(caster, le, 10);
                le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30));
            }
            //create weapons...
            //fire every half a second
            if (caster.tickCount % 7 == 0 && !stats.getTargets().isEmpty()) {
                //every half a second, send a signal to a random portal to fire a weapon
                List<FlyingItemEntity> portals = FlyingWeaponData.getCap(caster).getExtraWeapons("babylon");
                List<LivingEntity> targets = new ArrayList<>(stats.getTargets());
                Collections.shuffle(portals);
                portals.stream().filter(FlyingItemEntity::isIdle).findAny().ifPresent(a->{
                    BabylonPortal portal = (BabylonPortal) a;
                    Entity target = targets.get(WarDance.rand.nextInt(targets.size()));

                    // fire the projectile
                    portal.prepareToFire(target);
                });
                stats.getTargets().removeIf(LivingEntity::isDeadOrDying);
            }
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public void onProc(LivingEntity caster,
                       Event procPoint,
                       STATE state,
                       SkillData stats,
                       @Nullable LivingEntity target) {

        if (state == STATE.ACTIVE && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (procPoint instanceof LivingEntityUseItemEvent.Tick e) {
                e.setDuration(e.getDuration() - 100);
            } else if (procPoint instanceof PlayInteractionEvent.Post e)
                e.setCooldown(e.getOriginalState() == WeaponStats.AttackState.THROW ? 1 : 0.5);
            if (procPoint instanceof GainAdrenalineEvent gme) {
                gme.setQuantity(0);
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.INACTIVE && to == STATE.HOLSTERED && cast(caster, 9)) {
            CasterData.getCap(caster).removeActiveTag(SkillTags.state);
            //TimeSlowData.getCap(caster).alterSpeed(180, 0);
            babylon(caster);
            CombatData.getCap(caster).setIframe(180);
            return true;
        }
        if (from == STATE.ACTIVE && to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            StylishData.getCap(caster).resetAdrenaline();
            FlyingWeaponData.getCap(caster).dismissWeapons("babylon");
            prev.clearTargets();
            return true;
        }
        return instantCast(prev, from, to);
    }

    @Override
    public Color getColor() {
        return Color.white;//has its own coloring
    }

    private void babylon(LivingEntity caster) {
        List<ItemStack> allInQuiver = List.of(new ItemStack(Items.GOLDEN_SWORD));
        if (caster instanceof Player p) {
            allInQuiver = QuiverData.getData(p).getAllQuiveredItems(-1);
        }
        //create portals behind you
        for (int i = -3; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                BabylonPortal awe = new BabylonPortal(WarEntities.BABYLON.get(), caster.level());
                awe.setCosmeticItem(new RenderItemGroup(
                        new RenderNode.BlockNode(Blocks.NETHER_PORTAL.defaultBlockState(), new Vec3(90, 0, 0), new Vec3(0,-0.5,0)),
                        new RenderNode.BlockNode(Blocks.NETHER_PORTAL.defaultBlockState(), new Vec3(90, 0, 30), new Vec3(0,-0.5,0)),
                        new RenderNode.BlockNode(Blocks.NETHER_PORTAL.defaultBlockState(), new Vec3(90, 0, 60), new Vec3(0,-0.5,0))
                ));
                awe.setOwner(caster);
                awe.setSkillUsed(this);
                awe.setConjureList(allInQuiver);
                awe.lock(caster);
                final Vec3 preOff = new Vec3(i * 1.7 + (j == 1 ? 0.85 : 0), j * 1.2 + 2, 0.7);
                awe.moveTo(caster.getEyePosition().add(preOff));
                awe.setUniversalOffset(preOff);
                caster.level().addFreshEntity(awe);
                awe.setIdlePose(new MotionManagers.FixedMM(new MotionFrame(new Vec3(0,0,1), Vec3.ZERO), 2).setAngularVelocity(new Vector3f(0, 0, 20)));
                FlyingWeaponData.getCap(caster).addExtraWeapon("babylon", awe);
            }
        }
    }
}
