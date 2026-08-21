package jackiecrazy.wardance.skill.misc;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.footwork.utils.EffectUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.MeleePostureEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)

public class SplitTheSpoils extends Skill {
    /*
    Whenever you breach or kill a mob, collect bounty money depending on how big/powerful they were and how much money they had.
    Money disappears after a while.
    More money gives more looting but increases damage taken/decreases speed.
    Cast to move half your money to someone else, buffing and rallying them in the process.
    Money will be added to the bounty for kill/breaching that mob.
     */

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void morePain(MeleePostureEvent.Pre e) {
        Marks.getCap(e.getEntity()).getActiveMark(WarSkills.SPOILS.get()).ifPresent(a -> {
            //more pain
            e.setPostureConsumption(e.getPostureConsumption() * (1 + a.getArbitraryFloat() * 0.05f));
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void morePain(LivingHurtEvent e) {
        Marks.getCap(e.getEntity()).getActiveMark(WarSkills.SPOILS.get()).ifPresent(a -> {
            //more pain
            e.setAmount(e.getAmount() * (1 + a.getArbitraryFloat() * 0.05f));
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void deathTake(LivingDeathEvent e) {
        Marks.getCap(e.getEntity()).getActiveMark(WarSkills.SPOILS.get()).ifPresent(a -> {
            //more pain
            if (e.getSource().getEntity() instanceof LivingEntity le)
                ((SplitTheSpoils) WarSkills.SPOILS.get()).addBounty(le, e.getEntity());
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void stunTake(StunEvent e) {
        Marks.getCap(e.getEntity()).getActiveMark(WarSkills.SPOILS.get()).ifPresent(a -> {
            //more pain
            if (e.getAttacker() != null)
                ((SplitTheSpoils) WarSkills.SPOILS.get()).addBounty(e.getAttacker(), e.getEntity());
        });
    }

    @SubscribeEvent()
    public static void loot(LootingLevelEvent e) {
        //phat loot
        if (e.getDamageSource() != null && e.getDamageSource().getEntity() instanceof LivingEntity attacker)
            Marks.getCap(attacker).getActiveMark(WarSkills.SPOILS.get()).ifPresent(a -> {
                e.setLootingLevel(e.getLootingLevel() + (int) (a.getDuration() / 10));
            });
    }

    @Override
    public int getAimRange(LivingEntity caster, SkillData sd) {
        return 16;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity target = SkillUtils.aimLiving(caster, getAimRange(caster, prev));
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && target != null && cast(caster, target)) {
            passBounty(caster, target);
            return true;
        }
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            return true;
        }
        return boundCast(prev, from, to);
    }

    @Override
    public HashSet<String> getTags() {
        return offensive;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        cooldownTick(stats);
        return super.equippedTick(caster, stats);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {

        return markTickDown(sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData pd, LivingEntity target) {
        if (procPoint.getPhase() != EventPriority.LOWEST) return;
        if (procPoint instanceof StunEvent) {
            addBounty(caster, target);
        }
        if (procPoint instanceof LivingDeathEvent lde && lde.getSource().getEntity() == caster) {
            addBounty(caster, target);
        }
    }

    public void addBounty(LivingEntity caster, LivingEntity target) {
        double money = Math.log(target.getMaxHealth());
        money*=money;
        if (isMarked(target)) {
            money += getExistingMark(target).getDuration();
            removeMark(target);
        }
        mark(caster, caster, (float) money);
    }

    public void passBounty(LivingEntity caster, LivingEntity target) {
        //gives half your stacks to them, causing them to rally and gain resistance
        if (!hasMark(caster)) return;
        float money = getExistingMark(caster).getDuration() / 2;
        getExistingMark(caster).setDuration(money);
        mark(caster, target, money);
    }

    @Nullable
    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        CombatData.getCap(target).addPosture(sd.getDuration());
        EffectUtils.stackPot(target, new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, (int) (sd.getDuration() * 10), 0), EffectUtils.StackingMethod.MAX_DURATION);
        if (existing != null) {
            sd.addDuration(existing.getDuration());
        }
        return sd;
    }
}
