package jackiecrazy.wardance.skill.projectile;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.MovementUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class Countershot extends Skill {
    //dodging or otherwise iframing through damage doubles item charge speed for 3 seconds

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            prev.setState(STATE.INACTIVE);
        return passive(prev, from, to);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent lae && lae.getPhase() == EventPriority.HIGHEST && lae.getEntity() == caster) {
            if (MovementUtils.hasInvFrames(caster))
                cast(caster, 3);
        }
        if (state==STATE.ACTIVE && procPoint instanceof LivingEntityUseItemEvent.Tick e && e.getPhase() == EventPriority.HIGHEST) {
            e.setDuration(e.getDuration() - 1);
        }
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return activeTick(stats);
    }
}
