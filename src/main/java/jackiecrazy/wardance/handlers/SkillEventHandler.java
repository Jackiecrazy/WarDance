package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.event.*;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCooldownData;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
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
                CombatData.getCap(e.getPlayer()).setFatigue(0);
                CombatData.getCap(e.getPlayer()).setBurnout(0);
                CombatData.getCap(e.getPlayer()).setWounding(0);
            }
            ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
            for (SkillCooldownData s : isc.getSkillCooldowns().values()) {
                isc.coolSkill(s.getSkill());
            }
        } else System.out.println(e.wakeImmediately() + " " + !e.updateWorld() + " " + e.getPlayer().world.isDaytime());
        //System.out.println("wakeImmediately: "+e.wakeImmediately()+", update world: "+e.updateWorld()+", is daytime: "+e.getPlayer().world.isDaytime()+", recharge: "+CombatConfig.sleepingHealsDecay);

    }

    @SubscribeEvent
    public static void casting(SkillCastEvent e) {
        ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        if (!e.getEntityLiving().isServerWorld()) return;
        for (SkillData s : CasterData.getCap(e.getEntityLiving()).getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(e.getEntityLiving()).contains(ProcPoints.on_cast)) {
                s.getSkill().onSuccessfulProc(e.getEntityLiving(), s, e.getEntityLiving(), e);
            }
        }
        for (Skill s : isc.getSkillCooldowns().keySet()) {
            if (s.getProcPoints(e.getEntityLiving()).contains(ProcPoints.recharge_cast)) {
                s.onCooldownProc(e.getEntityLiving(), isc.getSkillCooldowns().get(s), e);
            }
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
        for (SkillData s : CasterData.getCap(e.getEntityLiving()).getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(e.getEntityLiving()).contains(ProcPoints.on_dodge)) {
                s.getSkill().onSuccessfulProc(e.getEntityLiving(), s, e.getEntityLiving(), e);
            }
        }
        //System.out.println("crit "+e.isCanceled());
    }

    @SubscribeEvent
    public static void forceCrit(CriticalHitEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        for (SkillData s : CasterData.getCap(e.getEntityLiving()).getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(e.getEntityLiving()).contains(ProcPoints.modify_crit)) {
                s.getSkill().onSuccessfulProc(e.getEntityLiving(), s, e.getEntityLiving(), e);
            }
        }
        //System.out.println("crit "+e.isCanceled());
    }

    @SubscribeEvent
    public static void stabbery(EntityAwarenessEvent e) {
        if (e.getAttacker() == null) return;
        if (!e.getEntityLiving().isServerWorld()) return;
        for (SkillData s : CasterData.getCap(e.getAttacker()).getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(e.getAttacker()).contains(ProcPoints.change_awareness)) {
                s.getSkill().onSuccessfulProc(e.getAttacker(), s, e.getEntityLiving(), e);
            }
        }
    }

    @SubscribeEvent
    public static void attackFlags(LivingAttackEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (CombatUtils.isMeleeAttack(e.getSource()) && e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getTrueSource();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (SkillData s : isc.getActiveSkills().values()) {
                if (s.getSkill().getProcPoints(attacker).contains(ProcPoints.normal_attack)) {
                    s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
                }
            }
            if (CombatData.getCap(attacker).getCachedCooldown() > 0.9f)
                for (Skill s : isc.getSkillCooldowns().keySet()) {
                    if (s.getProcPoints(attacker).contains(ProcPoints.recharge_normal)) {
                        s.onCooldownProc(attacker, isc.getSkillCooldowns().get(s), e);
                    }
                }
        }
        //System.out.println("attack "+e.isCanceled());
    }

    @SubscribeEvent
    public static void mightFlags(AttackMightEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity attacker = e.getAttacker();
        ISkillCapability isc = CasterData.getCap(attacker);
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(attacker).contains(ProcPoints.attack_might)) {
                s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
            }
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent
    public static void gainMightFlags(GainMightEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity attacker = e.getEntityLiving();
        ISkillCapability isc = CasterData.getCap(attacker);
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(attacker).contains(ProcPoints.change_might)) {
                s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
            }
        }
        //System.out.println("might "+e.isCanceled());
    }

    @SubscribeEvent
    public static void healFlags(LivingHealEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity entity = e.getEntityLiving();
        ISkillCapability isc = CasterData.getCap(entity);
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(entity).contains(ProcPoints.change_heals)) {
                s.getSkill().onSuccessfulProc(entity, s, e.getEntityLiving(), e);
            }
        }
    }

    @SubscribeEvent
    public static void postureFlags(GainPostureEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity entity = e.getEntityLiving();
        ISkillCapability isc = CasterData.getCap(entity);
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(entity).contains(ProcPoints.change_posture_regeneration)) {
                s.getSkill().onSuccessfulProc(entity, s, e.getEntityLiving(), e);
            }
        }
    }

    @SubscribeEvent
    public static void spiritFlags(RegenSpiritEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity entity = e.getEntityLiving();
        ISkillCapability isc = CasterData.getCap(entity);
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(entity).contains(ProcPoints.change_spirit)) {
                s.getSkill().onSuccessfulProc(entity, s, e.getEntityLiving(), e);
            }
        }
    }

    @SubscribeEvent
    public static void knockbackFlags(MeleeKnockbackEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (CombatUtils.isMeleeAttack(e.getDamageSource()) && e.getDamageSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (SkillData s : isc.getActiveSkills().values()) {
                if (s.getSkill().getProcPoints(attacker).contains(ProcPoints.knockback)) {
                    s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
                }
            }
        }
    }

    @SubscribeEvent
    public static void hurtFlags(LivingHurtEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getTrueSource();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (SkillData s : isc.getActiveSkills().values()) {
                if (s.getSkill().getProcPoints(attacker).contains(ProcPoints.on_hurt) && (!s.getSkill().getProcPoints(attacker).contains(ProcPoints.melee) || CombatUtils.isMeleeAttack(e.getSource()))) {
                    s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
                }
            }
        }
        LivingEntity defender = e.getEntityLiving();
        ISkillCapability isc = CasterData.getCap(defender);
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(defender).contains(ProcPoints.on_being_hurt)) {
                s.getSkill().onSuccessfulProc(defender, s, e.getEntityLiving(), e);
            }
        }
    }

    @SubscribeEvent
    public static void staggerFlags(StaggerEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (e.getAttacker() != null) {
            LivingEntity attacker = e.getAttacker();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (SkillData s : isc.getActiveSkills().values()) {
                if (s.getSkill().getProcPoints(attacker).contains(ProcPoints.on_stagger)) {
                    s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
                }
            }
        }
        LivingEntity defender = e.getEntityLiving();
        ISkillCapability isc = CasterData.getCap(defender);
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(defender).contains(ProcPoints.on_being_staggered)) {
                s.getSkill().onSuccessfulProc(defender, s, e.getEntityLiving(), e);
            }
        }
    }

    @SubscribeEvent
    public static void damageFlags(LivingDamageEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getTrueSource();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (SkillData s : isc.getActiveSkills().values()) {
                if (s.getSkill().getProcPoints(attacker).contains(ProcPoints.on_damage) && (!s.getSkill().getProcPoints(attacker).contains(ProcPoints.melee) || CombatUtils.isMeleeAttack(e.getSource()))) {
                    s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
                }
            }
        }
        LivingEntity defender = e.getEntityLiving();
        ISkillCapability isc = CasterData.getCap(defender);
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(defender).contains(ProcPoints.on_being_damaged)) {
                s.getSkill().onSuccessfulProc(defender, s, e.getEntityLiving(), e);
            }
        }
    }

    @SubscribeEvent
    public static void deathFlag(LivingDeathEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getTrueSource();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (SkillData s : isc.getActiveSkills().values()) {
                if (s.getSkill().getProcPoints(attacker).contains(ProcPoints.on_kill)) {
                    s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
                }
            }
        }
        LivingEntity defender = e.getEntityLiving();
        ISkillCapability isc = CasterData.getCap(defender);
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(defender).contains(ProcPoints.on_death)) {
                s.getSkill().onSuccessfulProc(defender, s, e.getEntityLiving(), e);
            }
        }
    }

    @SubscribeEvent
    public static void parryFlags(ParryEvent e) {
        for (SkillData s : CasterData.getCap(e.getAttacker()).getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(e.getAttacker()).contains(ProcPoints.change_parry_result)) {
                s.getSkill().onSuccessfulProc(e.getAttacker(), s, e.getEntityLiving(), e);
            }
        }
        //System.out.println("parry check "+e.isCanceled());
        if (!e.getEntityLiving().isServerWorld() || !e.canParry()) return;
        for (SkillData s : CasterData.getCap(e.getAttacker()).getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(e.getAttacker()).contains(ProcPoints.on_being_parried)) {
                s.getSkill().onSuccessfulProc(e.getAttacker(), s, e.getEntityLiving(), e);
            }
        }
        final ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(e.getEntityLiving()).contains(ProcPoints.on_parry)) {
                s.getSkill().onSuccessfulProc(e.getEntityLiving(), s, e.getAttacker(), e);
            }
        }
        for (Skill s : isc.getSkillCooldowns().keySet()) {
            if (s.getProcPoints(e.getEntityLiving()).contains(ProcPoints.recharge_parry)) {
                s.onCooldownProc(e.getEntityLiving(), isc.getSkillCooldowns().get(s), e);
            }
        }
        //System.out.println("parry "+e.isCanceled());
    }

    @SubscribeEvent
    public static void parryFlags(ProjectileParryEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        Entity proj = e.getProjectile();
        if (proj instanceof ProjectileEntity) {
            Entity shooter = ((ProjectileEntity) proj).getShooter();
            if (shooter instanceof LivingEntity)
                for (SkillData s : CasterData.getCap((LivingEntity) shooter).getActiveSkills().values()) {
                    if (s.getSkill().getProcPoints((LivingEntity) shooter).contains(ProcPoints.on_projectile_impact)) {
                        s.getSkill().onSuccessfulProc((LivingEntity) shooter, s, e.getEntityLiving(), e);
                    }
                }
        }
        final ISkillCapability isc = CasterData.getCap(e.getEntityLiving());
        for (SkillData s : isc.getActiveSkills().values()) {
            if (s.getSkill().getProcPoints(e.getEntityLiving()).contains(ProcPoints.on_projectile_parry)) {
                s.getSkill().onSuccessfulProc(e.getEntityLiving(), s, e.getEntityLiving(), e);
            }
        }
        if (e.getOriginalPostureConsumption() != 0)
            for (Skill s : isc.getSkillCooldowns().keySet()) {
                if (s.getProcPoints(e.getEntityLiving()).contains(ProcPoints.recharge_parry)) {
                    s.onCooldownProc(e.getEntityLiving(), isc.getSkillCooldowns().get(s), e);
                }
            }
    }
}
