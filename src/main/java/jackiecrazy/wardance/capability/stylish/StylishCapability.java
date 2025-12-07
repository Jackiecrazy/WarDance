package jackiecrazy.wardance.capability.stylish;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.IStyleCapability;
import jackiecrazy.footwork.event.*;
import jackiecrazy.wardance.config.*;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.combat.UpdateClientStylePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.*;

public class StylishCapability implements IStyleCapability {

    public static final int MAX_FINISHER_CHARGE = 10;
    public static final int TRACKED_FRESHNESS_ACTIONS = 7;
    public static final int COMBO_TIMER = 160;
    private final WeakReference<LivingEntity> dude;
    private boolean combat;
    private float adrenaline;
    private int finisherBar = 0;
    private int meleeFinisher, rangedFinisher;
    private int comboTimer;
    //there's no real reason to save this
    private Queue<String> freshness = new LinkedList<>();
    private float combo;

    public StylishCapability(LivingEntity dude) {
        this.dude = new WeakReference<>(dude);
    }

    @Override
    public boolean isCombatMode() {
        return combat;
    }

    @Override
    public void toggleCombatMode(boolean on) {
        combat = on;
    }

    @Override
    public float getAdrenaline() {
        return adrenaline;
    }

    @Override
    public void setAdrenaline(float to) {
        adrenaline = to;
        sync();
    }

    @Override
    public float addAdrenaline(float amount) {
        float ret = 0;
        adrenaline += amount;
        if (adrenaline > 1) {
            ret = adrenaline - 1;
            adrenaline = 1;
        }
        return ret;
    }

    @Override
    public void tick() {
        meleeFinisher++;
        if (meleeFinisher > 20) meleeFinisher = 20;
        rangedFinisher++;
        if (rangedFinisher > 20) rangedFinisher = 20;
        final LivingEntity guy = dude.get();
        final ICombatCapability cap = CombatData.getCap(dude.get());
        if (guy.isSprinting() || guy.isUsingItem() || guy.isFallFlying() || !guy.onGround() || guy.isBlocking() ||
                cap.isDodging() || cap.isIframe() || cap.isParrying()) {
            //slower combo drain
        } else comboTimer--;
        comboTimer--;
        if (comboTimer == 0) {
            combo = 1;
            resetCombo();
        }
    }

    @Override
    public void processAttack(boolean melee) {
        if (CombatData.getCap(dude.get()).alreadyProc("noFinisherCharge")) return;
        if (melee) {
            while (meleeFinisher >= 10) {
                meleeFinisher -= 10;
                finisherBar++;
            }
            meleeFinisher = 0;
        } else {
            while (rangedFinisher >= 10) {
                rangedFinisher -= 10;
                finisherBar++;
            }
            rangedFinisher = 0;
        }
        sync();
    }

    @Override
    public float getCombo() {
        return combo;
    }

    @Override
    public void addCombo(float amount, @Nonnull String source) {
        //calculate freshness
        float decr = amount / 2;
        boolean fresh=true;
        for (String str : freshness) {
            if (source.equals(str)){
                fresh=false;
                amount -= decr;
            }
        }
        //trail. Add spirit on fresh action.
        if(fresh)CombatData.getCap(dude.get()).addSpirit(1);
        //reset combo timer even if too stale
        refresh();
        //too stale!
        if (amount <= 0) return;
        combo += amount;
        addAdrenaline(amount / 6);
        freshness.add(source);
        while (freshness.size() > TRACKED_FRESHNESS_ACTIONS) {
            freshness.poll();
        }
        sync();
    }

    @Override
    public void resetCombo() {
        combo /= 2;
        if (combo <= 1) {
            combo = 1;
            freshness.clear();
        }
        sync();
    }

    @Override
    public void refresh() {
        comboTimer = COMBO_TIMER;
    }

    @Override
    public int getTriggerTime(boolean melee) {
        return melee ? meleeFinisher : rangedFinisher;
    }

    @Override
    public void setTriggerTime(int time, boolean melee) {
        if (melee) meleeFinisher = time;
        else rangedFinisher = time;
        sync();
    }

    @Override
    public void addTriggerTime(int time, boolean melee) {
        if (melee) meleeFinisher += time;
        else rangedFinisher += time;
        sync();
    }

    @Override
    public int getTriggerBar() {
        return finisherBar;
    }

    @Override
    public void setTriggerBar(int amnt) {
        finisherBar = Math.min(amnt, MAX_FINISHER_CHARGE);
        sync();
    }

    @Override
    public void resetTriggerBar() {
        finisherBar = 0;
        sync();
    }

    @Override
    public void addTriggerBar(int amnt) {
        setTriggerBar(finisherBar + amnt);
        sync();
    }

    @Override
    public boolean canTrigger() {
        return getTriggerBar() >= MAX_FINISHER_CHARGE;
    }

    @Override
    public void addOrb(Color of) {

    }

    @Override
    public boolean hasOrb(Color of) {
        return false;
    }

    @Override
    public void removeOrb(Color of) {

    }

    @Override
    public CompoundTag write() {
        CompoundTag t = new CompoundTag();
        t.putBoolean("combat", combat);
        t.putInt("finisher", finisherBar);
        t.putInt("finisherM", meleeFinisher);
        t.putInt("finisherR", rangedFinisher);
        t.putFloat("adr", adrenaline);
        t.putInt("comboTimer", comboTimer);
        t.putFloat("combo", combo);
        return t;
    }

    @Override
    public void read(CompoundTag t) {
        combat = t.getBoolean("combat");
        finisherBar = t.getInt("finisher");
        meleeFinisher = t.getInt("finisherM");
        rangedFinisher = t.getInt("finisherR");
        adrenaline = t.getFloat("adr");
        comboTimer = t.getInt("comboTimer");
        combo = t.getFloat("combo");
    }

    private void sync() {
        LivingEntity elb = dude.get();
        if (elb == null || elb.level().isClientSide) return;
        CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> elb), new UpdateClientStylePacket(elb.getId(), write()));
        if (!(elb instanceof FakePlayer) && elb instanceof ServerPlayer sp)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateClientStylePacket(elb.getId(), write()));

    }
}
