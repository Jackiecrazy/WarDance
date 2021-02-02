package jackiecrazy.wardance.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class CombatStorage implements Capability.IStorage<ICombatCapability> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<ICombatCapability> capability, ICombatCapability iCombatCapability, Direction direction) {
        CompoundNBT c=new CompoundNBT();
        c.putFloat("qi", iCombatCapability.getQi());
        c.putFloat("posture", iCombatCapability.getPosture());
        c.putFloat("combo", iCombatCapability.getCombo());
        c.putFloat("spirit", iCombatCapability.getSpirit());
        c.putFloat("maxpos", iCombatCapability.getTrueMaxPosture());
        c.putFloat("maxspi", iCombatCapability.getTrueMaxSpirit());
        c.putFloat("burnout", iCombatCapability.getBurnout());
        c.putFloat("fatigue", iCombatCapability.getFatigue());
        c.putFloat("wounding", iCombatCapability.getWounding());
        c.putInt("combocd", iCombatCapability.getComboGrace());
        c.putInt("qicd", iCombatCapability.getQiGrace());
        c.putInt("posturecd", iCombatCapability.getPostureGrace());
        c.putInt("spiritcd", iCombatCapability.getSpiritGrace());
        c.putInt("shield", iCombatCapability.getShieldTime());
        c.putInt("staggerc", iCombatCapability.getStaggerCount());
        c.putInt("staggert", iCombatCapability.getStaggerTime());
        c.putInt("offhandcd", iCombatCapability.getOffhandCooldown());
        c.putInt("roll", iCombatCapability.getRollTime());
        c.putInt("bMain", iCombatCapability.getHandBind(Hand.MAIN_HAND));
        c.putInt("bOff", iCombatCapability.getHandBind(Hand.OFF_HAND));
        c.putBoolean("offhand", iCombatCapability.isOffhandAttack());
        c.putBoolean("combat", iCombatCapability.isCombatMode());
        return c;
    }

    @Override
    public void readNBT(Capability<ICombatCapability> capability, ICombatCapability iCombatCapability, Direction direction, INBT inbt) {
        if(inbt instanceof CompoundNBT) {
            CompoundNBT c= (CompoundNBT)inbt;
            iCombatCapability.setQi(c.getFloat("qi"));
            iCombatCapability.setPosture(c.getFloat("posture"));
            iCombatCapability.setCombo(c.getFloat("combo"));
            iCombatCapability.setSpirit(c.getFloat("spirit"));
            iCombatCapability.setTrueMaxPosture(c.getFloat("maxpos"));
            iCombatCapability.setTrueMaxSpirit(c.getFloat("maxspi"));
            iCombatCapability.setBurnout(c.getFloat("burnout"));
            iCombatCapability.setWounding(c.getFloat("wounding"));
            iCombatCapability.setFatigue(c.getFloat("fatigue"));
            iCombatCapability.setComboGrace(c.getInt("combocd"));
            iCombatCapability.setQiGrace(c.getInt("qicd"));
            iCombatCapability.setPostureGrace(c.getInt("posturecd"));
            iCombatCapability.setSpiritGrace(c.getInt("spiritcd"));
            iCombatCapability.setShieldTime(c.getInt("shield"));
            iCombatCapability.setStaggerCount(c.getInt("staggerc"));
            iCombatCapability.setStaggerTime(c.getInt("staggert"));
            iCombatCapability.setOffhandCooldown(c.getInt("offhandcd"));
            iCombatCapability.setRollTime(c.getInt("roll"));
            iCombatCapability.setHandBind(Hand.MAIN_HAND, c.getInt("bMain"));
            iCombatCapability.setHandBind(Hand.OFF_HAND, c.getInt("bOff"));
            iCombatCapability.setOffhandAttack(c.getBoolean("offhand"));
            iCombatCapability.toggleCombatMode(c.getBoolean("combat"));
        }
    }
}
