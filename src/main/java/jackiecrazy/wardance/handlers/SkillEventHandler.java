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
import net.minecraft.util.math.RayTraceResult;
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
        //System.out.println("I really cannot replicate this, so here's some debug statements.");
        boolean flag = !e.wakeImmediately() && (!e.updateWorld() || e.getPlayer().world.isDaytime());
        if ((flag || ResourceConfig.sleepingHealsDecay == ResourceConfig.ThirdOption.FORCED) && e.getEntityLiving().isServerWorld()) {
            //System.out.println("This means sleeping flags are called properly on the server.");
            if (ResourceConfig.sleepingHealsDecay != ResourceConfig.ThirdOption.FALSE) {
                //System.out.println("Config option is true, resetting FBW.");
                final ICombatCapability cap = CombatData.getCap(e.getPlayer());
                float res = cap.getResolve() + 1;
                cap.addFatigue(-res * cap.getTrueMaxPosture() / 10);
                cap.addBurnout(-res * cap.getTrueMaxSpirit() / 10);
                cap.addWounding(-res * GeneralUtils.getMaxHealthBeforeWounding(e.getPlayer()) / 10);
                cap.setResolve(0);
            }
            ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
            for (Skill s : isc.getEquippedSkills()) {
                isc.changeSkillState(s, Skill.STATE.INACTIVE);
            }
        } else System.out.println(e.wakeImmediately() + " " + !e.updateWorld() + " " + e.getPlayer().world.isDaytime());
        //System.out.println("wakeImmediately: "+e.wakeImmediately()+", update world: "+e.updateWorld()+", is daytime: "+e.getPlayer().world.isDaytime()+", recharge: "+CombatConfig.sleepingHealsDecay);

    }

    @SubscribeEvent
    public static void casting(SkillCastEvent e) {
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        if (!e.getEntityLiving().isServerWorld()) return;
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getEntityLiving()));
        }
    }
//
//
//    @SubscribeEvent
//    public static void everything(LivingEvent e) {
//        if(e instanceof LivingEvent.LivingUpdateEvent)return;//no update events to reduce lag
//        if (!e.getEntityLiving().isServerWorld()) return;//STOPSHIP yee
//        for (SkillData s : CasterData.getCap(e.getEntityLiving()).getActiveSkills().values()) {
//            s.getSkill().onSuccessfulProc(e.getEntityLiving(), s, e.getEntityLiving(), e);
//        }
//        //System.out.println("crit "+e.isCanceled());
//    }

    @SubscribeEvent
    public static void dodge(DodgeEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent
    public static void forceCrit(CriticalHitEvent e) {
        if (!e.getEntityLiving().isServerWorld() || !(e.getTarget() instanceof LivingEntity)) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, (LivingEntity) e.getTarget()));
        }
    }

    @SubscribeEvent
    public static void stabbery(EntityAwarenessEvent e) {
        if (e.getAttacker() == null) return;
        if (!e.getEntityLiving().isServerWorld()) return;
        final ISkillCapability cap = CasterData.getCap(e.getAttacker());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntityLiving()));
        }
    }

    @SubscribeEvent
    public static void attackFlags(LivingAttackEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            final LivingEntity trueSource = (LivingEntity) e.getSource().getTrueSource();
            final ISkillCapability cap = CasterData.getCap(trueSource);
            for (Skill s : cap.getEquippedSkills()) {
                cap.getSkillData(s).ifPresent(d -> s.onProc(trueSource, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        //System.out.println("attack "+e.isCanceled());
    }

    @SubscribeEvent
    public static void mightFlags(AttackMightEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        final ISkillCapability cap = CasterData.getCap(e.getAttacker());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getAttacker(), e, d.getState(), d, e.getEntityLiving()));
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent
    public static void gainMightFlags(GainMightEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity attacker = e.getEntityLiving();
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent
    public static void healFlags(LivingHealEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent
    public static void postureFlags(GainPostureEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent
    public static void spiritFlags(RegenSpiritEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        final ISkillCapability cap = CasterData.getCap(e.getEntityLiving());
        for (Skill s : cap.getEquippedSkills()) {
            cap.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, null));
        }
    }

    @SubscribeEvent
    public static void knockbackFlags(MeleeKnockbackEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
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

    @SubscribeEvent
    public static void hurtFlags(LivingHurtEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getTrueSource();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getSource().getTrueSource()));
        }
    }

    @SubscribeEvent
    public static void staggerFlags(StaggerEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
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

    @SubscribeEvent
    public static void damageFlags(LivingDamageEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getTrueSource();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getSource().getTrueSource()));
        }
    }

    @SubscribeEvent
    public static void deathFlag(LivingDeathEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getTrueSource();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (Skill s : isc.getEquippedSkills()) {
                isc.getSkillData(s).ifPresent(d -> s.onProc(attacker, e, d.getState(), d, e.getEntityLiving()));
            }
        }
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getSource().getTrueSource()));
        }
    }

    @SubscribeEvent
    public static void parryFlags(ParryEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
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

    @SubscribeEvent
    public static void projectileImpact(ProjectileImpactEvent e) {
        if (e.getEntity().world.isRemote) return;
        Entity proj = e.getEntity();
        if (proj instanceof ProjectileEntity) {
            Entity shooter = ((ProjectileEntity) proj).getShooter();
            if (shooter instanceof LivingEntity) {
                ISkillCapability isc = CasterData.getCap((LivingEntity) shooter);
                for (Skill s : isc.getEquippedSkills()) {
                    isc.getSkillData(s).ifPresent(d -> s.onProc((LivingEntity) shooter, e, d.getState(), d, e.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY ? (Entity) e.getRayTraceResult().hitInfo : null));
                }
            }
        }

    }

    @SubscribeEvent
    public static void parryFlags(ProjectileParryEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        final ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (Skill s : isc.getEquippedSkills()) {
            isc.getSkillData(s).ifPresent(d -> s.onProc(e.getEntityLiving(), e, d.getState(), d, e.getProjectile() instanceof ProjectileEntity ? ((ProjectileEntity) e.getEntity()).getShooter() : null));
        }
    }
}
