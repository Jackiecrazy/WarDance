package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.event.*;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
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
        boolean flag = !e.wakeImmediately() && (!e.updateWorld() || e.getPlayer().level.isDay());
        if ((flag || ResourceConfig.sleepingHealsDecay == ResourceConfig.ThirdOption.FORCED) && e.getEntityLiving().isEffectiveAi()) {
            if (ResourceConfig.sleepingHealsDecay != ResourceConfig.ThirdOption.FALSE) {
                final ICombatCapability cap = CombatData.getCap(e.getPlayer());
                float res = cap.getResolve() + 1;
                cap.addFatigue(-res * cap.getTrueMaxPosture() / 10);
                cap.addBurnout(-res * cap.getTrueMaxSpirit() / 10);
                cap.addWounding(-res * GeneralUtils.getMaxHealthBeforeWounding(e.getPlayer()) / 10);
                cap.setResolve(0);
            }
            ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
            for (Skill s : isc.getEquippedSkills()) {
                if (s != null)
                    isc.changeSkillState(s, Skill.STATE.INACTIVE);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void casting(SkillCastEvent e) {
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        if (!e.getEntityLiving().isEffectiveAi()) return;
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getEntityLiving()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void dodge(DodgeEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void forceCrit(CriticalHitEvent e) {
        if (!e.getEntityLiving().isEffectiveAi() || !(e.getTarget() instanceof LivingEntity)) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, (LivingEntity) e.getTarget()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void stabbery(EntityAwarenessEvent e) {
        if (e.getAttacker() == null) return;
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getAttacker());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntityLiving()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attackFlags(LivingAttackEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity) {
            final LivingEntity trueSource = (LivingEntity) e.getSource().getEntity();
            final ISkillCapability cap = CasterData.getCap(trueSource);
            for (Skill s : cap.getEquippedSkills()) {
                cap.getSkillData(s).ifPresent(d -> s.onProc(trueSource, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        //System.out.println("attack "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mightFlags(AttackMightEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getAttacker());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntityLiving()));
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void gainMightFlags(GainMightEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        LivingEntity attacker = e.getEntityLiving();
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void healFlags(LivingHealEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void postureFlags(GainPostureEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void spiritFlags(RegenSpiritEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void knockbackFlags(MeleeKnockbackEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void hurtFlags(LivingHurtEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getEntity();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void staggerFlags(StaggerEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void damageFlags(LivingDamageEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getEntity();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void deathFlag(LivingDeathEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getEntity();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void parryFlags(ParryEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void projectileImpact(ProjectileImpactEvent e) {
        if (e.getEntity().level.isClientSide) return;
        Entity proj = e.getEntity();
        if (proj instanceof ProjectileEntity) {
            Entity shooter = ((ProjectileEntity) proj).getOwner();
            if (shooter instanceof LivingEntity) {
                ISkillCapability isc = CasterData.getCap((LivingEntity) shooter);
                for (Skill s : isc.getEquippedSkills()) {
                    isc.getSkillData(s).ifPresent(d -> s.onProc((LivingEntity) shooter, e, d.getState(), d, null));
                }
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void parryFlags(ProjectileParryEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void castingE(SkillCastEvent e) {
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        if (!e.getEntityLiving().isEffectiveAi()) return;
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getEntityLiving()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dodgE(DodgeEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void forceCriT(CriticalHitEvent e) {
        if (!e.getEntityLiving().isEffectiveAi() || !(e.getTarget() instanceof LivingEntity)) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, (LivingEntity) e.getTarget()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void stabberY(EntityAwarenessEvent e) {
        if (e.getAttacker() == null) return;
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getAttacker());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntityLiving()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attackFlagS(LivingAttackEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity) {
            final LivingEntity trueSource = (LivingEntity) e.getSource().getEntity();
            final ISkillCapability cap = CasterData.getCap(trueSource);
            for (Skill s : cap.getEquippedSkills()) {
                cap.getSkillData(s).ifPresent(d -> s.onProc(trueSource, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        //System.out.println("attack "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void mightFlagS(AttackMightEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getAttacker());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntityLiving()));
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void gainMightFlagS(GainMightEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        LivingEntity attacker = e.getEntityLiving();
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void healFlagS(LivingHealEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void postureFlagS(GainPostureEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void spiritFlagS(RegenSpiritEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void knockbackFlagS(MeleeKnockbackEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hurtFlagS(LivingHurtEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getEntity();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void staggerFlagS(StaggerEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void damageFlagS(LivingDamageEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getEntity();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void deathFlagS(LivingDeathEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getEntity();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void parryFlagS(ParryEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getAttacker()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void projectileImpacT(ProjectileImpactEvent e) {
        if (e.getEntity().level.isClientSide) return;
        Entity proj = e.getEntity();
        if (proj instanceof ProjectileEntity) {
            Entity shooter = ((ProjectileEntity) proj).getOwner();
            if (shooter instanceof LivingEntity) {
                ISkillCapability isc = CasterData.getCap((LivingEntity) shooter);
                for (Skill s : isc.getEquippedSkills()) {
                    isc.getSkillData(s).ifPresent(d -> s.onProc((LivingEntity) shooter, e, d.getState(), d, null));
                }
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void parryFlagS(ProjectileParryEvent e) {
        if (!e.getEntityLiving().isEffectiveAi()) return;
        final ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void resourcE(SkillResourceEvent e) {
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        if (!e.getEntityLiving().isEffectiveAi()) return;
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getEntityLiving()));
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void resource(SkillResourceEvent e) {
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        if (!e.getEntityLiving().isEffectiveAi()) return;
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getEntityLiving()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drops(LivingDropsEvent e) {
        if (e.getEntityLiving()!=null&&!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getEntity();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dropse(LivingDropsEvent e) {
        if (e.getEntityLiving()!=null&&!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getEntity();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drops(LootingLevelEvent e) {
        if (e.getEntityLiving()!=null&&!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getDamageSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getDamageSource().getEntity();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dropse(LootingLevelEvent e) {
        if (e.getEntityLiving()!=null&&!e.getEntityLiving().isEffectiveAi()) return;
        if (e.getDamageSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getDamageSource().getEntity();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }
}
