package jackiecrazy.wardance.event;

import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class SweepEvent extends LivingEvent {
    private final double oangle, oscale;
    private final InteractionHand hand;
    private final ItemStack stack;
    private double a, b;
    private WeaponStats.SWEEPTYPE t;
    private WeaponStats.SWEEPSTATE state;

    public SweepEvent(LivingEntity entity, InteractionHand hand, ItemStack stack, WeaponStats.SWEEPTYPE type, double base, double scale) {
        super(entity);
        oangle = a = base;
        oscale = b = scale;
        this.hand = hand;
        this.stack = stack;
        t = type;
        state= CombatUtils.getSweepState(entity);
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    public double getOriginalBase() {
        return oangle;
    }

    public double getFinalizedWidth() {
        return a + (b * EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, this.getEntity()));
    }

    public double getOriginalScaling() {
        return oscale;
    }

    public double getBase() {
        return a;
    }

    public void setBase(double a) {
        this.a = a;
    }

    public double getScaling() {
        return b;
    }

    public void setScaling(double a) {
        this.b = a;
    }

    public WeaponStats.SWEEPTYPE getType() {
        return t;
    }

    public void setType(WeaponStats.SWEEPTYPE t) {
        this.t = t;
    }

    public WeaponStats.SWEEPSTATE getState() {
        return state;
    }
}
