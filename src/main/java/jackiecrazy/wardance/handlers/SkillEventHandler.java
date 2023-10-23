package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.event.*;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.event.*;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class SkillEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void sleep(PlayerWakeUpEvent e) {
        boolean flag = !e.wakeImmediately() && (!e.updateLevel() || e.getEntity().level().isDay());
        if ((flag || ResourceConfig.sleepingHealsDecay == ResourceConfig.ThirdOption.FORCED) && e.getEntity().isEffectiveAi()) {
//            if (ResourceConfig.sleepingHealsDecay != ResourceConfig.ThirdOption.FALSE) {
//                final ICombatCapability cap = CombatData.getCap(e.getPlayer());
//                float res = cap.getResolve() + 1;
//                cap.addFatigue(-res * cap.getTrueMaxPosture() / 10);
//                cap.addBurnout(-res * cap.getTrueMaxSpirit() / 10);
//                cap.addWounding(-res * GeneralUtils.getMaxHealthBeforeWounding(e.getPlayer()) / 10);
//                cap.setResolve(0);
//            }
            ISkillCapability isc = CasterData.getCap(e.getEntity());
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                if (s != null) {
                    isc.getSkillData(s).ifPresent(a -> a.setDuration(-1));
                    isc.changeSkillState(s, Skill.STATE.INACTIVE);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void casting(SkillCastEvent e) {
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        if (!e.getEntity().isEffectiveAi()) return;
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getEntity()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void dodge(DodgeEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void forceCrit(CriticalHitEvent e) {
        if (!e.getEntity().isEffectiveAi() || !(e.getTarget() instanceof LivingEntity)) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, (LivingEntity) e.getTarget()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void stabbery(EntityAwarenessEvent e) {
        if (e.getAttacker() == null) return;
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getAttacker());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntity()));
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attackFlags(LivingAttackEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof final LivingEntity trueSource) {
            final ISkillCapability cap = CasterData.getCap(trueSource);
            for (Skill s : cap.getEquippedSkillsAndStyle()) {
                cap.getSkillData(s).ifPresent(d -> s.onProc(trueSource, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getSource().getEntity() instanceof LivingEntity attacker ? attacker : null));
        }
        //System.out.println("attack "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mightFlags(AttackMightEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getAttacker());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntity()));
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void gainMightFlags(GainMightEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        LivingEntity attacker = e.getEntity();
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void healFlags(LivingHealEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void postureFlags(GainPostureEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void spiritFlags(RegenSpiritEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void knockbackFlags(MeleeKnockbackEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void hurtFlags(LivingHurtEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void kbFlags(DamageKnockbackEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getDamageSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getDamageSource().getEntity() instanceof LivingEntity attacker ? attacker : null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void kbFlagss(DamageKnockbackEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getDamageSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getDamageSource().getEntity() instanceof LivingEntity attacker ? attacker : null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void staggerFlags(StunEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void damageFlags(LivingDamageEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getSource().getEntity() instanceof LivingEntity attacker ? attacker : null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void deathFlag(LivingDeathEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getSource().getEntity() instanceof LivingEntity attacker ? attacker : null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void parryFlags(ParryEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void projectileImpact(ProjectileImpactEvent e) {
        if (e.getEntity().level().isClientSide) return;
        Entity proj = e.getEntity();
        if (proj instanceof Projectile) {
            Entity shooter = ((Projectile) proj).getOwner();
            if (shooter instanceof LivingEntity) {
                ISkillCapability isc = CasterData.getCap((LivingEntity) shooter);
                for (Skill s : isc.getEquippedSkillsAndStyle()) {
                    isc.getSkillData(s).ifPresent(d -> s.onProc((LivingEntity) shooter, e, d.getState(), d, null));
                }
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void parryFlags(ProjectileParryEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void castingE(SkillCastEvent e) {
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        if (!e.getEntity().isEffectiveAi()) return;
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getTarget()));
        }
        ISkillCapability isc = CasterData.getCap(e.getTarget());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getTarget(), e, d.getState(), d, e.getEntity()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dodgE(DodgeEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void forceCriT(CriticalHitEvent e) {
        if (!e.getEntity().isEffectiveAi() || !(e.getTarget() instanceof LivingEntity)) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, (LivingEntity) e.getTarget()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void stabberY(EntityAwarenessEvent e) {
        if (e.getAttacker() == null) return;
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getAttacker());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntity()));
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attackFlagS(LivingAttackEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof final LivingEntity trueSource) {
            final ISkillCapability cap = CasterData.getCap(trueSource);
            for (Skill s : cap.getEquippedSkillsAndStyle()) {
                cap.getSkillData(s).ifPresent(d -> s.onProc(trueSource, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getSource().getEntity() instanceof LivingEntity attacker ? attacker : null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void mightFlagS(AttackMightEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getAttacker());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntity()));
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void gainMightFlagS(GainMightEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        LivingEntity attacker = e.getEntity();
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void healFlagS(LivingHealEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void postureFlagS(GainPostureEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void spiritFlagS(RegenSpiritEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void luckFlagH(LuckEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void luckFlagL(LuckEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void knockbackFlagS(MeleeKnockbackEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hurtFlagS(LivingHurtEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getSource().getEntity() instanceof LivingEntity attacker ? attacker : null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void consumePosture(ConsumePostureEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        ISkillCapability isc = CasterData.getCap(e.getAttacker());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntity()));
        }
        isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void consumePostureE(ConsumePostureEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        ISkillCapability isc = CasterData.getCap(e.getAttacker());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntity()));
        }
        isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void staggerFlagS(StunEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void expose(ExposeEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void exposeS(ExposeEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void exposeD(ExposeAttackEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void exposeDS(ExposeAttackEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void damageFlagS(LivingDamageEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getSource().getEntity() instanceof LivingEntity attacker ? attacker : null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void deathFlagS(LivingDeathEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getSource().getEntity() instanceof LivingEntity attacker ? attacker : null));
        }
        Marks.getCap(e.getEntity()).clearMarks();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void parryFlagS(ParryEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void projectileImpacT(ProjectileImpactEvent e) {
        if (e.getEntity().level().isClientSide) return;
        Entity proj = e.getEntity();
        if (proj instanceof Projectile) {
            Entity shooter = ((Projectile) proj).getOwner();
            if (shooter instanceof LivingEntity) {
                ISkillCapability isc = CasterData.getCap((LivingEntity) shooter);
                for (Skill s : isc.getEquippedSkillsAndStyle()) {
                    isc.getSkillData(s).ifPresent(d -> s.onProc((LivingEntity) shooter, e, d.getState(), d, null));
                }
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void parryFlagS(ProjectileParryEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        final ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void resourcE(SkillResourceEvent e) {
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        if (!e.getEntity().isEffectiveAi()) return;
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getEntity()));
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void resource(SkillResourceEvent e) {
        final ISkillCapability cap = CasterData.getCap(e.getEntity());
        if (!e.getEntity().isEffectiveAi()) return;
        for (Skill s : cap.getEquippedSkillsAndStyle()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getEntity()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drops(LivingDropsEvent e) {
        if (e.getEntity() != null && !e.getEntity().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dropse(LivingDropsEvent e) {
        if (e.getEntity() != null && !e.getEntity().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drops(LootingLevelEvent e) {
        if (e.getEntity() != null && !e.getEntity().isEffectiveAi()) return;
        if (e.getDamageSource() == null) return;
        if (e.getDamageSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dropse(LootingLevelEvent e) {
        if (e.getEntity() != null && !e.getEntity().isEffectiveAi()) return;
        if (e.getDamageSource() == null) return;
        if (e.getDamageSource().getEntity() instanceof LivingEntity attacker) {
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkillsAndStyle()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntity()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void exp(LivingExperienceDropEvent e) {
        if (e.getEntity() == null || !e.getEntity().isEffectiveAi()) return;
        if (e.getAttackingPlayer() == null) return;
        ISkillCapability isc = CasterData.getCap(e.getAttackingPlayer());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getAttackingPlayer(), e, d.getState(), d, e.getEntity()));
        }
        isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttackingPlayer()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void expe(LivingExperienceDropEvent e) {
        if (e.getEntity() == null || !e.getEntity().isEffectiveAi()) return;
        if (e.getAttackingPlayer() == null) return;
        ISkillCapability isc = CasterData.getCap(e.getAttackingPlayer());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getAttackingPlayer(), e, d.getState(), d, e.getEntity()));
        }
        isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttackingPlayer()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void exp(FractureEvent e) {
        if (e.getEntity() == null || !e.getEntity().isEffectiveAi()) return;
        ISkillCapability  isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
        if (e.getAttacker() == null) return;
        isc = CasterData.getCap(e.getAttacker());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntity()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void expe(FractureEvent e) {
        if (e.getEntity() == null || !e.getEntity().isEffectiveAi()) return;
        ISkillCapability  isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, e.getAttacker()));
        }
        if (e.getAttacker() == null) return;
        isc = CasterData.getCap(e.getAttacker());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntity()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void jump(LivingEvent.LivingJumpEvent e) {
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void jumpe(LivingEvent.LivingJumpEvent e) {
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void fall(LivingFallEvent e) {
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void falle(LivingFallEvent e) {
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void sweep(SweepEvent e) {
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void sweepe(SweepEvent e) {
        ISkillCapability isc = CasterData.getCap(e.getEntity());
        for (Skill s : isc.getEquippedSkillsAndStyle()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntity(), e, d.getState(), d, null));
        }
    }
}
