package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.StealthUtils;
import jackiecrazy.wardance.capability.resources.NewCombatCapability;
import jackiecrazy.wardance.mixin.LivingEntityAccessors;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillArchetypes;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;

public class Pickpocket extends Grapple {
    /*
    drop an item from the loot table.
    also usable on unaware enemies, costs 1 spirit in that case.
     */


    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.none;
    }

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target, SkillData stats) {
        if (!cast(caster, target)) return;
        ((LivingEntityAccessors) target).callDropFromLootTable(target.getLastDamageSource(), true);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
//        if (procPoint instanceof LootingLevelEvent le && !stats.isCondition() && state == STATE.ACTIVE)
//            le.setLootingLevel(0);
    }

    @Override
    public int getAimRange(LivingEntity caster, SkillData sd) {
        return 3;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity target = SkillUtils.aimLiving(caster, getAimRange(caster, prev));
        if (target != null && StealthUtils.INSTANCE.getAwareness(caster, target) == StealthUtils.Awareness.UNAWARE)
            if (from == STATE.INACTIVE && to == STATE.ACTIVE &&
                    CombatData.getCap(caster).consumeSpirit(NewCombatCapability.MAX_SPIRIT)) {
                prev.flagCondition(true);
                performEffect(caster, target, prev);
            }
        if (to == STATE.COOLING)
            prev.flagCondition(false);
        return super.onStateChange(caster, prev, from, to);
    }
}
