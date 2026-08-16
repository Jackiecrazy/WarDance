package jackiecrazy.wardance.skill.bursts;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.entity.flyingweapon.FlyingWeaponEffect;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.move.motionframe.*;
import jackiecrazy.footwork.utils.EasingFunctionEnum;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.Animation;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.entity.GrappleEntity;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.entity.skill.AsuraWeaponEntity;
import jackiecrazy.wardance.event.ConsumePostureEvent;
import jackiecrazy.wardance.event.GrappleEvent;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class Asura extends Skill {
    protected static final List<MotionFrame> SWING1 = List.of(
            new MotionFrame(new Vec3(0, 1, 0.4), new Vec3(0, 0, 1)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL).setHit(new HitInfo(0, 0.7, 1, true, true, 2))),
            new MotionFrame(new Vec3(0, 0, 1.2), new Vec3(0, 0, 1)));
    protected static final MotionManager SWING_1 = new MotionManagers.DefinitionMM(new MotionGroup(SWING1, EasingFunctionEnum.IN_OUT_SINE, 10));
    protected static final List<MotionFrame> SWING2 = List.of(
            new MotionFrame(new Vec3(1, 0.6, 0.6), new Vec3(0, 0, 1)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL).setHit(new HitInfo(0, 1, 1, false, true, 1))),
            new MotionFrame(new Vec3(0, 0, 1.2), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(-1, -0.6, 0.6), new Vec3(0, 0, 1)));
    protected static final MotionManager SWING_2 = new MotionManagers.DefinitionMM(new MotionGroup(SWING2, EasingFunctionEnum.IN_OUT_SINE, 10));
    protected static final List<MotionFrame> SWING3 = List.of(
            new MotionFrame(new Vec3(0, -0.2, 1), new Vec3(0, 0, 1)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON, FlyingWeaponEffect.TRAIL).setHit(new HitInfo(2, 1, 1, false, true, 1))),
            new MotionFrame(new Vec3(1, -0.2, 0), new Vec3(0, 0, 1)),
            new MotionFrame(new Vec3(0, -0.2, -1), new Vec3(0, 0, 1)));
    protected static final MotionManager SWING_3 = new MotionManagers.DefinitionMM(new MotionGroup(SWING3, EasingFunctionEnum.IN_OUT_SINE, 10));
    /*
    grow two more arms and make all weapons visible. Each attack strikes with all three arms from one side and always breaches.
    your grapples will always yank and you gain invul and twohanding
    lasts 10 seconds, each kill extends it
     */
    private static final MotionFrame[] IDLES = {
            new MotionFrame(new Vec3(0.8, 1, 0), new Vec3(0, 0, 1.1)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)),
            new MotionFrame(new Vec3(-0.8, 1, 0), new Vec3(0, 0, 1.1)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)),
            new MotionFrame(new Vec3(1, 0.2, 0), new Vec3(0, 0, 1.1)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)),
            new MotionFrame(new Vec3(-1, 0.2, 0), new Vec3(0, 0, 1.1)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)),
            new MotionFrame(new Vec3(1, -0.6, 0), new Vec3(0, 0, 1.1)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON)),
            new MotionFrame(new Vec3(-1, -0.6, 0), new Vec3(0, 0, 1.1)).setEffects(new FrameEffects().setEffects(FlyingWeaponEffect.WEAPON))
    };
    private static final AttributeModifier berserk = new AttributeModifier(UUID.fromString("a2124c38-73e3-4551-9df4-e06e117600c1"), "berserk twohanding bonus", 3, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier berserk1 = new AttributeModifier(UUID.fromString("a2124c38-73e3-4551-9df4-e06e117600c1"), "berserk attack speed bonus", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier berserk2 = new AttributeModifier(UUID.fromString("a2124c38-73e3-4551-9df4-e06e117600c1"), "berserk attack damage reduction", -0.7, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private final HashSet<String> tag = makeTag(SkillTags.offensive, SkillTags.physical);

    @Override
    public float mightCost(LivingEntity caster) {
        return 1;
    }

    @Override
    public HashSet<String> getTags() {
        return tag;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            if (!StylishData.getCap(caster).drainAdrenaline(0.005f / stats.getEffectiveness())) {
                markUsed(caster);
                CombatData.getCap(caster).setSpirit(0);
                return true;
            }
            CombatData.getCap(caster).setSpirit(100);
            if (caster.getMainHandItem().is(WeaponStats.TWO_HANDED) && caster.getOffhandItem().is(WeaponStats.TWO_HANDED))
                completeChallenge(caster);
//            if(FlyingWeaponData.getCap(caster).hasGrapple())
//                FlyingWeaponData.getCap(caster).getGrapple().setHookStrength(10);
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
        if (state == STATE.ACTIVE) {
            if (procPoint instanceof LivingDeathEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
                StylishData.getCap(caster).addAdrenaline(0.14f);
            }
            if (procPoint instanceof StunEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
                StylishData.getCap(caster).addAdrenaline(0.14f);
            }
            if (procPoint instanceof GrappleEvent e && e.getEntity() == caster) {
                e.getGrapple().setHookStrength(300);
                e.getGrapple().retract(GrappleEntity.ACTION.YANK);
            }
            if (procPoint.getPhase() == EventPriority.LOWEST && procPoint instanceof LivingDamageEvent e && e.getEntity() == caster) {
                e.setAmount(0);
            }
            if (procPoint.getPhase() == EventPriority.LOWEST && procPoint instanceof ConsumePostureEvent e && e.getEntity() == caster) {
                e.setPostureConsumption(0);
            }
            if (procPoint instanceof LivingAttackEvent lae && lae.getEntity() == target && procPoint.getPhase() == EventPriority.HIGHEST) {
                CombatData.getCap(caster).tickProc("canBreach");
            }
            if (procPoint instanceof PlayInteractionEvent.Interaction e && e.getPhase() == EventPriority.HIGHEST) {
                InteractionHand ih = e.getHand();
                stats.flagCondition(!stats.isCondition());
                int i = 0;
                //there should be 6
                WeaponInteractions.WeaponInteraction ie = e.getInteraction().getInteractionOfType(WeaponInteractions.WeaponInteraction.InteractionType.ANIMATE);
                for (FlyingItemEntity fwe : FlyingWeaponData.getCap(caster).getExtraWeapons("asura")) {
                    i++;
                    int randDelay = WarDance.rand.nextInt(i + 3);
                    if ((i % 2 == 0) != (ih == InteractionHand.OFF_HAND))//left side
                        continue;
                    if (ie instanceof Animation a) {
                        for (MotionManager mm : a.getAnimations())
                            fwe.queuePath(stats.isCondition() ^ i % 2 == 0 ? mm.flipFrames() : mm, randDelay, randDelay);
                    } else {
                        //if ((i <= 2 && ih == InteractionHand.MAIN_HAND) || (i > 2 && ih == InteractionHand.OFF_HAND) || e.getStack().is(WeaponStats.TWO_HANDED))
                        MotionManager path = i > 4 ? SWING_3 : i > 2 ? SWING_2 : SWING_1;
                        if (i % 2 == 0)
                            path = path.flipFrames();
                        fwe.queuePath(path, randDelay, randDelay);
                    }

                }
            }
        }
    }


    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.INACTIVE && to == STATE.HOLSTERED && cast(caster, 9)) {
            SkillUtils.addAttribute(caster, WarAttributes.TWO_HANDING.get(), berserk);
            SkillUtils.addAttribute(caster, Attributes.ATTACK_SPEED, berserk1);
            SkillUtils.addAttribute(caster, Attributes.ATTACK_DAMAGE, berserk2);
            CombatUtils.triggerSteveTime(caster, 30);
            caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.8f + WarDance.rand.nextFloat() * 0.5f, 0.75f + WarDance.rand.nextFloat() * 0.5f);
            //look for the 6 weapons with the highest posture damage
            ItemStack[] arms = {
                    ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
            };
            if (caster instanceof Player p) {
                List<ItemStack> allInQuiver = QuiverData.getData(p).getAllQuiveredItems(-1);
                allInQuiver.sort((first, second) -> (int) (100 * (CombatUtils.getPostureAtk(null, null, null, null, 7, second) - CombatUtils.getPostureAtk(null, null, null, null, 7, first))));
                for (int i = 0; i < Math.min(arms.length, allInQuiver.size()); i++)
                    arms[i] = allInQuiver.get(i);
            }
            for (int i = 0; i < IDLES.length; i++) {
                AsuraWeaponEntity awe = new AsuraWeaponEntity(WarEntities.ASURA.get(), caster.level());
                awe.setHeldItem(arms[i]);
                awe.setCosmeticItem(arms[i]);
                awe.setOwner(caster);
                awe.setSkillUsed(this);
                awe.moveTo(caster.getEyePosition());
                awe.setIdlePose(new MotionManagers.FixedMM(IDLES[i], 2));
                awe.setUniversalOffset(IDLES[i].direction().scale(0.3));
                caster.level().addFreshEntity(awe);
                FlyingWeaponData.getCap(caster).addExtraWeapon("asura", awe);
            }
        }
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            StylishData.getCap(caster).resetAdrenaline();
            SkillUtils.removeAttribute(caster, WarAttributes.TWO_HANDING.get(), berserk);
            SkillUtils.removeAttribute(caster, Attributes.ATTACK_SPEED, berserk1);
            SkillUtils.removeAttribute(caster, Attributes.ATTACK_DAMAGE, berserk2);
            FlyingWeaponData.getCap(caster).dismissWeapons("asura");
            return true;
        }
        return instantCast(prev, from, to);
    }


}
