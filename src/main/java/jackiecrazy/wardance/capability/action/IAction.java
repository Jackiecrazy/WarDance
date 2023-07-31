package jackiecrazy.wardance.capability.action;

import net.minecraft.world.entity.LivingEntity;

public interface IAction {
    public boolean canParry(LivingEntity attacker);
    public boolean canDealPostureDamage(LivingEntity to);
    public boolean canEnterCombatMode(LivingEntity to);
    public boolean canSelectSkills(LivingEntity to);
    //TODO see stealth display or land distracted/unaware backstabs

}
