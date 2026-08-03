package jackiecrazy.wardance.capability.aerial;

import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.UpdateAirPacket;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.Direction;
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
    private static final UUID GRAVITY1 = UUID.fromString("e2118f5d-8a42-43c2-ba39-6e2264a26ca5");
    private static final UUID WALL_GRAV = UUID.fromString("e2118f5c-8a42-43c2-bf39-6e6264a26cad");
    private final ArrayList<Tuple<Integer, Double>> modify = new ArrayList<>();
    WeakReference<Entity> bind;
    private double speed = 1;
    private int longest;
    private int off;
    private WallState state = WallState.NONE;
    private Direction direction = Direction.DOWN;
    private int aerial = 0;

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
                //fixme the server is not aware of the player's wall state so updating attribute causes them to rapidly start sliding down
                p.getAttribute(ForgeMod.ENTITY_GRAVITY.get()).removeModifier(GRAVITY);
                p.getAttribute(ForgeMod.ENTITY_GRAVITY.get()).addTransientModifier(new AttributeModifier(GRAVITY, "time slow", speed - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            if (!bound.level().isClientSide)
                CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> bound), new UpdateAirPacket(bound.getId(), spd, longest));
        }

    }

    @Override
    public boolean isAerialMode() {
        return aerial > 0;
    }

    @Override
    public void setAerialMode(boolean toggle) {
        //todo save
        if (toggle)
            aerial = 30;
        else aerial=-1;
        if (bind != null && bind.get() instanceof Player p) {
            if (toggle)
                SkillUtils.modifyAttribute(p, ForgeMod.ENTITY_GRAVITY.get(), GRAVITY1, p.getAttributeValue(WarAttributes.AIR_GRAVITY.get()) - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
            else SkillUtils.removeAttribute(p, ForgeMod.ENTITY_GRAVITY.get(), GRAVITY1);
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
        off--;
        int prev = aerial;
        aerial--;
        if (prev >= 0 && aerial < 0) setAerialMode(false);
        if (bind != null) {
            final Entity bound = bind.get();

            if (bound != null && bound.onGround()) {
                setAerialMode(false);
            }
        }
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

    @Override
    public WallState getState() {
        return state;
    }

    @Override
    public boolean setState(WallState state) {
        if (this.state == state)
            return false;
        //validate the state
        this.state = state;
        if (bind.get() instanceof LivingEntity e) {
            if (state.noGravity) {
                SkillUtils.modifyAttribute(e, ForgeMod.ENTITY_GRAVITY.get(), WALL_GRAV, -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
            } else
                SkillUtils.removeAttribute(e, ForgeMod.ENTITY_GRAVITY.get(), WALL_GRAV);
            if (state == WallState.NONE) {
                //temporarily stick on the surface
                noOffFor(5);
                //CombatChannel.INSTANCE.sendToServer(new UpdateWallPacket(state));
            } else noOffFor(0);
        }
        return true;
    }

    @Override
    public Direction getWallDir() {
        if (getState() != WallState.WALL_SLIDE && getState() != WallState.CLING) return null;
        return direction;
    }

    @Override
    public void setWallDir(Direction dir) {
        direction = dir;
    }

    @Override
    public boolean enforcedNoOff() {
        return off > 0;
    }

    @Override
    public void noOffFor(int ticks) {
        off = ticks;
    }
}
