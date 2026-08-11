package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkDamageArchetype;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class PestilentEdge extends SkillStyle {

    public PestilentEdge() {
        super(2);
    }

    @SubscribeEvent
    public static void extras(MobEffectEvent.Added e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getEffectInstance().getEffect().getCategory() == MobEffectCategory.HARMFUL) {
            LivingEntity target = e.getEntity();
            final Skill pestilent = WarSkills.PESTILENT_EDGE.get();
            Marks.getCap(target).getActiveMark(pestilent).ifPresent(a -> {
                float curr = a.getDuration();
                LivingEntity caster = a.getCaster(target.level());
                if (caster == null) return;
                if (curr < 10) {
                    //plague damage
                    target.hurt(new CombatDamageSource(caster).setDamageTyping(FootworkDamageArchetype.MAGICAL).setSkillUsed(pestilent).setProcSkillEffects(true).setDamageDealer(null).setAttackingHand(null), 2);
                }
                if (curr > 5) {
                    //spread debuff
                    AreaEffectCloud areaeffectcloud = new AreaEffectCloud(caster.level(), target.getX(), target.getY(), target.getZ());
                    areaeffectcloud.setOwner(caster);
                    areaeffectcloud.setRadius(Math.min(10, curr - 10));
                    areaeffectcloud.setDuration((int) (140 * SkillUtils.getSkillEffectiveness(caster)));
                    areaeffectcloud.addEffect(new MobEffectInstance(e.getEffectInstance()));
                    caster.level().addFreshEntity(areaeffectcloud);
                }
            });
        }
    }

    @Override
    public boolean markTick(@Nullable LivingEntity caster, LivingEntity target, SkillData sd) {
        float prev = sd.getDuration();
        sd.addDuration(0.05f);
        float interval = sd.getDuration() < 5 ? 0.5f : 1;
        if (sd.getDuration() % interval <= prev % interval) {
            if (caster.distanceToSqr(target) > 32 * 32) {
                //too far, expire
                sd.setDuration(-9999);
            }
            //plague damage
            float amnt = sd.getDuration() < 10 ? 2 : 1;
            target.hurt(new CombatDamageSource(caster).setDamageTyping(FootworkDamageArchetype.MAGICAL).setSkillUsed(this).setProcSkillEffects(true).setDamageDealer(null).setAttackingHand(null), amnt);
        }
        return ((int) (sd.getDuration() * 20)) % 20 == 0;
    }

    @Override
    public @Nullable SkillData onMarked(LivingEntity caster,
                                        LivingEntity target,
                                        SkillData sd,
                                        @Nullable SkillData existing) {
        return sd;
    }

    @Override
    public void onProc(LivingEntity caster,
                       Event procPoint,
                       STATE state,
                       SkillData stats,
                       @Nullable LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent hurt && hurt.getEntity() == target) {
            if (!CombatData.getCap(caster).alreadyProc("oncePerAttack") && CombatUtils.getAttackState(caster) == WeaponStats.AttackType.DRAW_ATTACK) {
                for (LivingEntity le : stats.getTargets())
                    removeMark(le);
                stats.clearTargets();
                stats.addTarget(target);
                mark(caster, target, 0.5f);
            }
        }

        if (procPoint instanceof LivingHurtEvent cpe && cpe.getPhase() == EventPriority.HIGHEST && DamageUtils.isMeleeAttack(cpe.getSource()) && cpe.getEntity() == target) {
            if (target != null && !cpe.isCanceled() && isMarked(target)) {
                if (cpe.getSource() instanceof CombatDamageSource cds) {
                    cds.setProcSkillEffects(true);
                    cds.setSkillUsed(this);
                }
                MobEffectInstance transfer = null;
                for (MobEffectInstance mei : caster.getActiveEffects().stream().toList()) {
                    if (mei.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                        transfer = mei;
                    }
                }
                if (transfer != null) {
                    caster.removeEffect(transfer.getEffect());
                    target.addEffect(new MobEffectInstance(transfer));
                }
            }
        }
    }
}
