package jackiecrazy.wardance.capability.action;

import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.meta.UpdateClientPermissionPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

public class AmoPermissions implements IAction {

    Player boundTo;

    boolean parry = true, sweep = true, posture = true, combat = true, skill = true;

    public AmoPermissions() {

    }

    public AmoPermissions(Player bind) {
        boundTo = bind;
    }

    @Override
    public boolean canParry() {
        return parry;
    }

    @Override
    public boolean canDealPostureDamage() {
        return posture;
    }

    @Override
    public boolean canEnterCombatMode() {
        return combat;
    }

    @Override
    public boolean canSelectSkills() {
        return skill;
    }

    @Override
    public void setParry(boolean yes) {
        parry = yes;
        sync();
    }

    @Override
    public boolean canSweep() {
        return sweep;
    }

    @Override
    public void setSweep(boolean yes) {
        sweep = yes;
        sync();
    }

    @Override
    public void setPosture(boolean yes) {
        posture = yes;
        sync();
    }

    @Override
    public void setCombat(boolean yes) {
        combat = yes;
        sync();
    }

    @Override
    public void setSkill(boolean yes) {
        skill = yes;
        sync();
    }

    @Override
    public void read(CompoundTag from) {
        parry = from.getBoolean("parry");
        posture = from.getBoolean("posture");
        combat = from.getBoolean("combat");
        skill = from.getBoolean("skill");
        sweep = from.getBoolean("sweep");
    }

    @Override
    public CompoundTag write() {
        CompoundTag ct = new CompoundTag();
        ct.putBoolean("parry", parry);
        ct.putBoolean("posture", posture);
        ct.putBoolean("combat", combat);
        ct.putBoolean("skill", skill);
        ct.putBoolean("sweep", sweep);
        return ct;
    }

    private void sync() {
        if (boundTo instanceof ServerPlayer sp)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateClientPermissionPacket(boundTo.getId(), write()));
    }
}
