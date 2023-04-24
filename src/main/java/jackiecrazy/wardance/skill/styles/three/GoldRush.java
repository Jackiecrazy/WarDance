package jackiecrazy.wardance.skill.styles.three;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.skill.styles.ColorRestrictionStyle;
import jackiecrazy.wardance.utils.DamageUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)

public class GoldRush extends ColorRestrictionStyle {
    private static final AttributeModifier luck = new AttributeModifier(UUID.fromString("77723885-afb9-4937-9c02-612ee5b6135a"), "frost fang bonus", 2, AttributeModifier.Operation.ADDITION);

    public GoldRush() {
        super(3, false, SkillColors.gold);
    }

    @SubscribeEvent()
    public static void spread(LivingAttackEvent e) {
        //spread mark
        if (DamageUtils.isMeleeAttack(e.getSource()) && e.getSource().getEntity() instanceof LivingEntity attacker)
            Marks.getCap(attacker).getActiveMark(WarSkills.GOLD_RUSH.get()).ifPresent(a -> {
                a.addArbitraryFloat(-1);
                ((GoldRush) WarSkills.GOLD_RUSH.get()).mark(a.getCaster(attacker.level), e.getEntity(), 10, 1);
            });
    }

    @SubscribeEvent()
    public static void loot(LootingLevelEvent e) {
        //phat loot
        if (e.getDamageSource().getEntity() instanceof LivingEntity attacker)
            Marks.getCap(attacker).getActiveMark(WarSkills.GOLD_RUSH.get()).ifPresent(a -> {
                int lvl = 0;
                if (a.getArbitraryFloat() >= 10)
                    lvl++;
                if (a.getArbitraryFloat() >= 6)
                    lvl++;
                if (a.getArbitraryFloat() >= 3)
                    lvl++;
                if (a.getArbitraryFloat() >= 1)
                    lvl++;
                e.setLootingLevel(e.getLootingLevel() + lvl);
            });
    }

    @SubscribeEvent()
    public static void pain(ParryEvent e) {
        //more damage
        Marks.getCap(e.getEntity()).getActiveMark(WarSkills.GOLD_RUSH.get()).ifPresent(a -> e.setPostureConsumption(e.getPostureConsumption() * (1 + a.getArbitraryFloat() * 0.1f)));
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 3);
        }
        return passive(prev, from, to);
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
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint.getPhase() != EventPriority.LOWEST) return;
        if (procPoint instanceof LivingAttackEvent lae && state == STATE.INACTIVE && lae.getEntity() == target && cast(caster)) {
            mark(caster, target, 10, 1);
        }
    }

    @Nullable
    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            sd.addArbitraryFloat(existing.getArbitraryFloat());
            sd.setArbitraryFloat(Math.min(10, sd.getArbitraryFloat()));
        }
        return sd;
    }

    @Override
    public void mark(LivingEntity caster, LivingEntity target, float duration, float arb) {
        super.mark(caster, target, duration, arb);
    }
}
