package jackiecrazy.wardance.capability.aerial;

import jackiecrazy.footwork.capability.timeslow.ITimeChange;
import jackiecrazy.footwork.networking.FootworkChannel;
import jackiecrazy.footwork.networking.UpdateTimeSlowPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.PacketDistributor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.UUID;

public class AerialCapability implements IAerialMode {
    private static final UUID GRAVITY = UUID.fromString("e2118f5c-8a42-43c2-bf39-6e6264a26ca5");
    private final ArrayList<Tuple<Integer, Double>> modify = new ArrayList<>();
    WeakReference<Entity> bind;
    private double speed = 1;
    private int longest;

    public AerialCapability() {
    }

    public AerialCapability(Entity bindTo) {
        bind = new WeakReference<>(bindTo);
    }

    private void recalculateSpeed() {
        double spd = 1;
        longest = 0;
        for (Tuple<Integer, Double> entry : modify) {
            if (entry.getB() < spd) spd = entry.getB();
            if (entry.getA() > longest) longest = entry.getA();
        }
        speed = spd;
        if (bind != null) {
            final Entity bound = bind.get();

            if (bound instanceof LivingEntity p) {
                p.getAttribute(ForgeMod.ENTITY_GRAVITY.get()).removeModifier(GRAVITY);
                p.getAttribute(ForgeMod.ENTITY_GRAVITY.get()).addTransientModifier(new AttributeModifier(GRAVITY, "time slow", speed - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

    }

    @Override
    public void alterGravity(int ticks, double speed) {
        modify.add(new Tuple<>(ticks, speed));
        recalculateSpeed();
    }

    @Override
    public void tick() {
        modify.forEach(a -> a.setA(a.getA() - 1));
        if (modify.stream().anyMatch(a -> a.getA() <= 0)) {
            modify.removeIf(a -> a.getA() <= 0);
            recalculateSpeed();
        }
        longest--;
    }

    @Override
    public void resetSpeed() {
        speed = 1;
    }

    @Override
    public double getEffectiveSpeed() {
        return speed;
    }

    @Override
    public int getTimeRemaining() {
        return longest;
    }
}
