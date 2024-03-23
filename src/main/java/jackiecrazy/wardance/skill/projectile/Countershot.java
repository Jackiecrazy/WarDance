package jackiecrazy.wardance.skill.projectile;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Countershot extends Skill {
    //dodging or otherwise iframing through damage doubles item charge speed for 3 seconds
    @SubscribeEvent(receiveCanceled = true)
    public static void snakebite(LivingAttackEvent e) {
        if (!e.getEntity().isEffectiveAi()) return;
        LivingEntity entity = e.getEntity();
        //todo detect parries
    }

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.INACTIVE);
        return false;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
    }
}
