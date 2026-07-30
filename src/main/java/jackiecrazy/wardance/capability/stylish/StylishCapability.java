package jackiecrazy.wardance.capability.stylish;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.IStyleCapability;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.UpdateClientStylePacket;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.ComboRanks;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;

public class StylishCapability implements IStyleCapability {
    public static final UUID WOUND = UUID.fromString("982bbbb2-bbd0-4166-801a-560d1a4149c8");
    public static final int MAX_FINISHER_CHARGE = 10;
    public static final int TRACKED_FRESHNESS_ACTIONS = 10;
    public static final int COMBO_TIMER = 200;
    public static final int ADRENALINE_TIMER = 300;
    private static final UUID STYLISH = UUID.fromString("1896391d-0d6c-4a3e-a4a5-5e3c9d173b80");
    private final WeakReference<LivingEntity> dude;
    private boolean combat;
    private float adrenaline;
    private int finisherBar = 0;
    private int meleeFinisher, rangedFinisher;
    private int comboTimer, adrenalineTimer;
    //there's no real reason to save this
    private Queue<String> freshness = new LinkedList<>();
    private float combo;
    private boolean dirty = true;
    private boolean deathDoor = false, canDeathDoor = true;
    private double deathDoorReduction = 0;
    private int hitTimer = 0;
    private boolean recalcHealth = true;

    public StylishCapability(LivingEntity dude) {
        this.dude = new WeakReference<>(dude);
    }

    public static @NotNull String getNormalAttackString(LivingEntity seme) {
        return CombatData.getCap(seme).isOffhandAttack() + CombatUtils.getAttackState(seme).name();
    }

    @Override
    public boolean isCombatMode() {
        return combat;
    }

    @Override
    public void toggleCombatMode(boolean on) {
        combat = on;
        if (dude.get() != null) {
            LivingEntity guy = dude.get();
            if (on)
                SkillUtils.modifyAttribute(guy, ForgeMod.STEP_HEIGHT_ADDITION.get(), WOUND, 0.9, AttributeModifier.Operation.ADDITION);
            else SkillUtils.removeAttribute(guy, ForgeMod.STEP_HEIGHT_ADDITION.get(), WOUND);
        }
        markDirty();
    }

    @Override
    public void resetAdrenaline() {
        if (maxAdrenaline()) {
            canDeathDoor = true;
            markDirty();
        }
        IStyleCapability.super.resetAdrenaline();
    }

    @Override
    public float getAdrenaline() {
        return adrenaline;
    }

    @Override
    public void setAdrenaline(float to) {
        adrenaline = to;
        markDirty();
    }

    @Override
    public float addAdrenaline(float amount) {
        if (dude.get() != null)
            amount *= dude.get().getAttributeValue(WarAttributes.ADRE_BON.get());
        float ret = 0;
        boolean ddoor = !maxAdrenaline();
        adrenaline += amount;
        if (adrenaline > 1) {
            ret = adrenaline - 1;
            adrenaline = 1;
        }
        if (ddoor && maxAdrenaline()) canDeathDoor = true;
        adrenalineTimer = ADRENALINE_TIMER;
        markDirty();
        return ret;
    }

    @Override
    public boolean isDyingFast() {
        //return hitTimer > 0;
        return false;
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
        if (comboTimer < 0)
            adrenalineTimer--;
        hitTimer--;
        if (adrenalineTimer <= 0) {
            if ((hitTimer < -800 || guy.hasEffect(MobEffects.REGENERATION)) && deathDoorReduction != 0) {
                deathDoorReduction += 0.00125;
                recalcHealth = true;
                deathDoorReduction = Math.min(deathDoorReduction, 0);
                if (deathDoorReduction == 0) {
                    canDeathDoor = true;
                    markDirty();
                }
            }
            adrenalineTimer = 0;
            adrenaline = 0;
        }

        if (deathDoor && guy.tickCount % 20 == 0) {
            //slowly drain max health, ends when player max health<1
            final double dtime = guy.getAttributeValue(WarAttributes.DDOOR_TIME.get());
            if (dtime <= 0)
                deathDoorReduction = -0.999;
            else {
                double drain = 0.05;//5% per second
                //minimum half a health point each time
                //drain = Math.max(1 / guy.getMaxHealth(), drain) / dtime;
                deathDoorReduction -= drain;
                guy.setHealth(1);
                recalcHealth = true;
                if (getCombo() > ComboRanks.A) {
                    stabilize();
                    if (guy instanceof Player pl) {
                        pl.displayClientMessage(Component.translatable("wardance.deathdoor.recovered").withStyle(ChatFormatting.GREEN), true);
                    }
                } else if (guy.getMaxHealth() <= 1) {
                    canDeathDoor = false;
                    stabilize();
                    if (guy instanceof Player pl) {
                        pl.displayClientMessage(Component.translatable("wardance.deathdoor.succumbed").withStyle(ChatFormatting.RED), true);
                    }
                }
            }
        }
        if (recalcHealth) {
            SkillUtils.modifyAttribute(guy, Attributes.MAX_HEALTH, WOUND, deathDoorReduction, AttributeModifier.Operation.MULTIPLY_TOTAL);
            recalcHealth = false;
        }

        if (dirty) {
            if (guy == null || guy.level().isClientSide) return;
            CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> guy), new UpdateClientStylePacket(guy.getId(), write()));
            if (!(guy instanceof FakePlayer) && guy instanceof ServerPlayer sp)
                CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateClientStylePacket(guy.getId(), write()));

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
        markDirty();
    }

    @Override
    public float getCombo() {
        return combo;
    }

    private float prevCombo;

    @Override
    public void addCombo(float amount, @Nonnull String source) {
        //calculate freshness
        float fresh = getFreshness(source);
        amount *= fresh;
        //reset combo timer even if too stale
        refresh();
        //too stale!
        if (amount <= 0) return;
        if (dude.get() instanceof Player le) {
            //fully rally if super duper fresh
            if (fresh >= 1)
                CombatData.getCap(le).rally(1);
            final float effectiveCombo = getCombo() - 1;
            if(effectiveCombo!=prevCombo) {
                SkillUtils.modifyAttribute(le, Attributes.MOVEMENT_SPEED, STYLISH, 0.06 * effectiveCombo, AttributeModifier.Operation.MULTIPLY_BASE);
                SkillUtils.modifyAttribute(le, Attributes.ATTACK_SPEED, STYLISH, 0.02 * effectiveCombo, AttributeModifier.Operation.MULTIPLY_TOTAL);
                SkillUtils.modifyAttribute(le, WarAttributes.SKILL_EFFECTIVENESS.get(), STYLISH, 0.02 * effectiveCombo, AttributeModifier.Operation.MULTIPLY_TOTAL);
                SkillUtils.modifyAttribute(le, WarAttributes.AIR_GRAVITY.get(), STYLISH, -Math.min(0.9, 0.15 * effectiveCombo), AttributeModifier.Operation.MULTIPLY_TOTAL);
                SkillUtils.modifyAttribute(le, ForgeMod.ENTITY_REACH.get(), STYLISH, 0.02 * effectiveCombo, AttributeModifier.Operation.MULTIPLY_TOTAL);
                SkillUtils.modifyAttribute(le, Attributes.LUCK, STYLISH, effectiveCombo, AttributeModifier.Operation.ADDITION);
                FlyingWeaponData.getCap(le).getWeapon(InteractionHand.MAIN_HAND).ifPresent(fwe -> fwe.setSpeed(1 + effectiveCombo * 0.1f));
                FlyingWeaponData.getCap(le).getWeapon(InteractionHand.OFF_HAND).ifPresent(fwe -> fwe.setSpeed(1 + effectiveCombo * 0.1f));
                prevCombo = effectiveCombo;
            }
        }
        combo += amount;
        addAdrenaline(amount / 6);
        freshness.add(source);
        while (freshness.size() > TRACKED_FRESHNESS_ACTIONS) {
            freshness.poll();
        }
        markDirty();
    }

    @Override
    public void resetCombo() {
        combo = 1;
        freshness.clear();
        if (dude.get() instanceof Player p) {
            SkillUtils.removeAttribute(p, Attributes.MOVEMENT_SPEED, STYLISH);
            SkillUtils.removeAttribute(p, Attributes.ATTACK_SPEED, STYLISH);
            SkillUtils.removeAttribute(p, WarAttributes.SKILL_EFFECTIVENESS.get(), STYLISH);
            SkillUtils.removeAttribute(p, Attributes.LUCK, STYLISH);
            SkillUtils.removeAttribute(p, WarAttributes.AIR_GRAVITY.get(), STYLISH);
            SkillUtils.removeAttribute(p, ForgeMod.ENTITY_REACH.get(), STYLISH);
            FlyingWeaponData.getCap(p).getWeapon(InteractionHand.MAIN_HAND).ifPresent(fwe -> fwe.setSpeed(1));
            FlyingWeaponData.getCap(p).getWeapon(InteractionHand.OFF_HAND).ifPresent(fwe -> fwe.setSpeed(1));
        }
        markDirty();
    }

    @Override
    public void refresh() {
        comboTimer = COMBO_TIMER;
        adrenalineTimer = ADRENALINE_TIMER;
    }

    @Override
    public int getTriggerTime(boolean melee) {
        return melee ? meleeFinisher : rangedFinisher;
    }

    @Override
    public void setTriggerTime(int time, boolean melee) {
        if (melee) meleeFinisher = time;
        else rangedFinisher = time;
        markDirty();
    }

    @Override
    public void addTriggerTime(int time, boolean melee) {
        if (melee) meleeFinisher += time;
        else rangedFinisher += time;
        markDirty();
    }

    @Override
    public int getTriggerBar() {
        return finisherBar;
    }

    @Override
    public void setTriggerBar(int amnt) {
        finisherBar = Math.min(amnt, MAX_FINISHER_CHARGE);
        markDirty();
    }

    @Override
    public void resetTriggerBar() {
        finisherBar = 0;
        markDirty();
    }

    @Override
    public void addTriggerBar(int amnt) {
        setTriggerBar(finisherBar + amnt);
        markDirty();
    }

    @Override
    public boolean canTrigger() {
        return canDeathDoor;
    }

    @Override
    public boolean isDeathDoor() {
        return deathDoor;
    }

    @Override
    public boolean avoidDeath() {
        if (dude.get() != null) {
            LivingEntity p = dude.get();
            if (!canDeathDoor || p.getMaxHealth() <= 1) {
                //sorry bud
                return false;
            }
            if (!deathDoor) {
                if (p instanceof Player pl) {
                    pl.displayClientMessage(Component.translatable("wardance.deathdoor." + WarDance.rand.nextInt(5)).withStyle(ChatFormatting.RED), true);
                }
                resetCombo();
                CombatData.getCap(p).knockdown(CombatConfig.knockdownDurationPlayer);
                deathDoor = true;
                dirty = true;
            }
            hitTimer = 60;
            return true;
        }
        return false;
    }

    @Override
    public void stabilize() {
        deathDoor = false;
        dirty = true;
    }

    @Override
    public Collection<String> getFreshness() {
        return freshness;
    }

    @Override
    public float getFreshness(String s) {
        float fresh = 1;
        for (String str : freshness) {
            if (s.equals(str)) {
                fresh -= 0.5f;
            }
        }
        //trail. Add spirit on fresh action.
        return fresh;
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
        t.putBoolean("canddoor", canDeathDoor);
        t.putBoolean("ddoor", deathDoor);
        t.putDouble("healthDown", deathDoorReduction);
        t.putInt("hit", hitTimer);
        ListTag fresh = new ListTag();
        for (String s : freshness)
            fresh.add(StringTag.valueOf(s));
        t.put("freshness", fresh);
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
        canDeathDoor = t.getBoolean("canddoor");
        deathDoor = t.getBoolean("ddoor");
        deathDoorReduction = t.getDouble("healthDown");
        hitTimer = t.getInt("hit");
        ListTag fresh = t.getList("freshness", Tag.TAG_STRING);
        if(!fresh.isEmpty())freshness.clear();
        for(Tag s:fresh){
            if(s instanceof StringTag st){
                freshness.add(st.getAsString());
            }
        }
    }

    private void markDirty() {
        dirty = true;
    }
}
