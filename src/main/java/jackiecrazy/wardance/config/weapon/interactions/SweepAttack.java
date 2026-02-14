package jackiecrazy.wardance.config.weapon.interactions;

import jackiecrazy.footwork.move.motionframe.HitInfo;
import jackiecrazy.wardance.client.RenderUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public class SweepAttack extends WeaponInteractions.WeaponInteraction {
    public static final SweepAttack NOTHING = new SweepAttack(SWEEPTYPE.NONE, 0, 0);
    public static final SweepAttack DEFAULT_FAN = new SweepAttack(SWEEPTYPE.CONE, 30, 30);
    public static final SweepAttack DEFAULT_CLEAVE = new SweepAttack(SWEEPTYPE.CLEAVE, 30, 30);
    public static final SweepAttack DEFAULT_NONE = new SweepAttack(SWEEPTYPE.NONE, 0, 0);
    private static final SweepAttack DEFAULT_IMPACT = new SweepAttack(SWEEPTYPE.IMPACT, 1, 1.5);
    private static final SweepAttack DEFAULT_LINE = new SweepAttack(SWEEPTYPE.LINE, 1, 1.5);
    private static final SweepAttack DEFAULT_CIRCLE = new SweepAttack(SWEEPTYPE.CIRCLE, 1, 1.5);

    HitInfo attack_info = new HitInfo();
    private double sweep_base = 0;
    private double sweep_scale = 0;
    private SWEEPTYPE sweep = SWEEPTYPE.NONE;
    private double range_multiplier = 1;

    public SweepAttack(){
        super();
    }

    public SweepAttack(SWEEPTYPE t, double b, double s) {
        super();
        sweep = t;
        sweep_base = b;
        sweep_scale = s;
    }

    private static ChatFormatting getColorFromValue(double a) {
        if (a > 1) return ChatFormatting.GREEN;
        else if (a < 0) return ChatFormatting.YELLOW;
        else return ChatFormatting.RED;
    }

    @Override
    public TYPE getInteractionType() {
        return TYPE.SWEEP;
    }

    public Component getToolTip(ItemStack e, boolean advanced) {
        String advance = "";
        double finalized = sweep_base + (sweep_scale * e.getEnchantmentLevel(Enchantments.SWEEPING_EDGE));
        MutableComponent sweepTip = Component.translatable("wardance.tooltip.sweep." + sweep, Component.literal(String.valueOf(finalized)).withStyle(ChatFormatting.AQUA));
        //grab different tooltips if and only if they are different
        double damage = getHitInfo().getDamageScale();
        double posture = getHitInfo().getPostureScale();
        if (getHitInfo().getKnockback() != NOTHING.getHitInfo().getKnockback()) {
            MutableComponent cp = Component.literal(RenderUtils.formatter.format(getHitInfo().getKnockback()) + "x");
            if (!advanced) {
                if (getHitInfo().getKnockback() > 1) cp = Component.translatable("wardance.tooltip.more");
                else if (getHitInfo().getKnockback() < 0) cp = Component.translatable("wardance.tooltip.negative");
                else cp = Component.translatable("wardance.tooltip.less");
            }
            sweepTip.append(Component.translatable("wardance.tooltip.sweep.knockback", cp.withStyle(getColorFromValue(getHitInfo().getKnockback()))));
        }
        if (getHitInfo().isCrit()) {
            sweepTip.append(Component.translatable("wardance.tooltip.sweep.crit").withStyle(ChatFormatting.GOLD));
            damage *= getHitInfo().getCritDamage();
            posture *= getHitInfo().getCritDamage();
        }
        if (damage != 1)
            sweepTip.append(Component.translatable("wardance.tooltip.sweep.damage" + advance, Component.literal(RenderUtils.formatter.format(damage * 100) + "%").withStyle(getColorFromValue(damage))));
        if (posture != 1)
            sweepTip.append(Component.translatable("wardance.tooltip.sweep.posture" + advance, Component.literal(RenderUtils.formatter.format(posture * 100) + "%").withStyle(getColorFromValue(posture))));
        sweepTip = sweepTip.withStyle(ChatFormatting.WHITE);
        return sweepTip;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SweepAttack other
                && other.sweep == sweep
                && other.sweep_scale == sweep_scale
                && other.getHitInfo().getDamageScale() == getHitInfo().getDamageScale()
                && other.getHitInfo().getPostureScale() == getHitInfo().getPostureScale()
                && other.getHitInfo().getCritDamage() == getHitInfo().getCritDamage()
                && other.getHitInfo().isCrit() == getHitInfo().isCrit()
                && other.getHitInfo().getKnockback() == getHitInfo().getKnockback()
                && other.sweep_base == sweep_base;
    }

    public SweepAttack clone() {
        SweepAttack ret = new SweepAttack(sweep, sweep_base, sweep_scale);
        getHitInfo().copyTo(ret.getHitInfo());
        return ret;
    }

    public double getBase() {
        return sweep_base;
    }

    public double getScaling() {
        return sweep_scale;
    }

    public SWEEPTYPE getType() {
        return sweep;
    }

    public void write(FriendlyByteBuf f) {
        super.write(f);
        f.writeInt(sweep.ordinal());
        f.writeDouble(sweep_base);
        f.writeDouble(sweep_scale);
        //getHitInfo().write(f);
    }

    public WeaponInteractions.WeaponInteraction read(FriendlyByteBuf f) {
        super.read(f);
        sweep = SWEEPTYPE.values()[f.readInt()];
        sweep_base = f.readDouble();
        sweep_scale = f.readDouble();
        //getHitInfo().read(f);
        return this;
    }

    public SweepAttack finisherCopy(boolean breach) {
        SweepAttack ret = clone();
        ret.getHitInfo().breach = breach;
        ret.getHitInfo().crit = true;
        ret.getHitInfo().damage_scale = 1;
        ret.getHitInfo().crit_damage = 2;
        return ret;
    }

    public SweepAttack preFinishCopy() {
        SweepAttack ret = clone();
        ret.getHitInfo().damage_scale = 0.3f;
        return ret;
    }

    public double getRangeMult() {
        return range_multiplier;
    }

    public HitInfo getHitInfo() {
        return attack_info;
    }

    public enum SWEEPTYPE {
        NONE, CONE,//horizontal fan area in front of the entity up to max range, base and scale add angle
        CLEAVE,//cone but vertical
        LINE,//1 block wide line up to max range, base and scale add to thickness
        IMPACT,//splash at point of impact or furthest distance if no mob aimed, base and scale add radius
        CIRCLE//splash with entity as center, ignores range, base and scale add radius
    }
}
