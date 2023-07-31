package jackiecrazy.wardance.capability.action;

import net.minecraft.nbt.CompoundTag;

public interface IAction {
    public boolean canParry();

    public boolean canDealPostureDamage();

    public boolean canEnterCombatMode();

    public boolean canSelectSkills();

    public void setParry(boolean yes);

    public boolean canSweep();

    public void setSweep(boolean yes);

    public void setPosture(boolean yes);

    public void setCombat(boolean yes);

    public void setSkill(boolean yes);

    //TODO see stealth display or land distracted/unaware backstabs
    public void read(CompoundTag from);

    public CompoundTag write();
}
