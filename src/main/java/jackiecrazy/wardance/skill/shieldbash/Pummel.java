package jackiecrazy.wardance.skill.shieldbash;

import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;
import java.util.UUID;

public class Pummel extends ShieldBash {
    private static UUID pummel = UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1");

    @Override
    public boolean isPassive(LivingEntity caster) {
        return true;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 0;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent lae && lae.getEntity() == target && DamageUtils.isMeleeAttack(lae.getSource()) && procPoint.getPhase() == EventPriority.HIGHEST) {
            updateCasterShieldDamage(caster, target);
        }
        stats.setState(STATE.INACTIVE);
    }

    @Override
    protected boolean showArchetypeDescription() {
        return false;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        updateCasterShieldDamage(caster, null);
        return false;
    }

    @Override
    public void onEquip(LivingEntity caster) {
        updateCasterShieldDamage(caster, null);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        updateCasterShieldDamage(caster, null);
        super.onUnequip(caster, stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return passive(prev, from, to);
    }

    private void updateCasterShieldDamage(LivingEntity caster, @Nullable LivingEntity target) {
        final ItemStack stack = caster.getMainHandItem();
        final boolean shield = CombatUtils.isShield(caster, stack);
        if(!shield)return;
        float attack = CombatUtils.getPostureAtk(caster, target, InteractionHand.MAIN_HAND, 0, stack);
        SkillUtils.modifyAttribute(caster, Attributes.ATTACK_DAMAGE, pummel, attack, AttributeModifier.Operation.ADDITION);
    }
}
