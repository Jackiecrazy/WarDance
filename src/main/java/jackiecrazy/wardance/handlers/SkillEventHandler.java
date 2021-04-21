package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.skill.ProcPoint;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class SkillEventHandler {
    @SubscribeEvent
    public static void forceCrit(CriticalHitEvent e) {
        for (SkillData s : CasterData.getCap(e.getEntityLiving()).getActiveSkills().values()) {
            if (s.getSkill().getTags(e.getEntityLiving()).contains(ProcPoint.modify_crit)) {
                s.getSkill().onSuccessfulProc(e.getEntityLiving(), s, e.getEntityLiving(), e);
            }
        }
    }

    @SubscribeEvent
    public static void attackFlags(LivingAttackEvent e) {
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getTrueSource();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (SkillData s : isc.getActiveSkills().values()) {
                if (s.getSkill().getTags(attacker).contains(ProcPoint.normal_attack)) {
                    s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
                }
            }
            if (CombatData.getCap(attacker).getCachedCooldown() > 0.9f)
                for (Skill s : isc.getSkillCooldowns().keySet()) {
                    if (s.getTags(attacker).contains(ProcPoint.recharge_normal)) {
                        isc.decrementSkillCooldown(s, 1);
                    }
                }
        }
    }

    @SubscribeEvent
    public static void hurtFlags(LivingHurtEvent e) {
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getTrueSource();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (SkillData s : isc.getActiveSkills().values()) {
                if (s.getSkill().getTags(attacker).contains(ProcPoint.on_hurt)) {
                    s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
                }
            }
        }
    }

    @SubscribeEvent
    public static void damageFlags(LivingDamageEvent e) {
        if (e.getSource().getTrueSource() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) e.getSource().getTrueSource();
            ISkillCapability isc = CasterData.getCap(attacker);
            for (SkillData s : isc.getActiveSkills().values()) {
                if (s.getSkill().getTags(attacker).contains(ProcPoint.on_damage)) {
                    s.getSkill().onSuccessfulProc(attacker, s, e.getEntityLiving(), e);
                }
            }
        }
    }

    @SubscribeEvent
    public static void parryFlags(ParryEvent e) {
        for (SkillData s : CasterData.getCap(e.getAttacker()).getActiveSkills().values()) {
            if (s.getSkill().getTags(e.getAttacker()).contains(ProcPoint.on_being_parried)) {
                s.getSkill().onSuccessfulProc(e.getAttacker(), s, e.getEntityLiving(), e);
            }
        }
        for (SkillData s : CasterData.getCap(e.getEntityLiving()).getActiveSkills().values()) {
            if (s.getSkill().getTags(e.getEntityLiving()).contains(ProcPoint.on_parry)) {
                s.getSkill().onSuccessfulProc(e.getEntityLiving(), s, e.getEntityLiving(), e);
            }
        }
    }

    @SubscribeEvent
    public static void parryFlags(ProjectileParryEvent e) {
        Entity proj = e.getProjectile();
        if (proj instanceof ProjectileEntity) {
            Entity shooter = ((ProjectileEntity) proj).func_234616_v_();
            if (shooter instanceof LivingEntity)
                for (SkillData s : CasterData.getCap((LivingEntity) shooter).getActiveSkills().values()) {
                    if (s.getSkill().getTags((LivingEntity) shooter).contains(ProcPoint.on_projectile_impact)) {
                        s.getSkill().onSuccessfulProc((LivingEntity) shooter, s, e.getEntityLiving(), e);
                    }
                }
        }
        for (SkillData s : CasterData.getCap(e.getEntityLiving()).getActiveSkills().values()) {
            if (s.getSkill().getTags(e.getEntityLiving()).contains(ProcPoint.on_projectile_parry)) {
                s.getSkill().onSuccessfulProc(e.getEntityLiving(), s, e.getEntityLiving(), e);
            }
        }
    }
}
