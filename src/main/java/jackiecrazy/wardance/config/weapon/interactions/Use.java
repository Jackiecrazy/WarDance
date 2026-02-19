package jackiecrazy.wardance.config.weapon.interactions;

import net.minecraft.network.FriendlyByteBuf;

//uhh
public class Use extends WeaponInteractions.WeaponInteraction {
    private double use_speed = 1;

    @Override
    public InteractionType getInteractionType() {
        return InteractionType.USE;
    }

    public Use clone() {
        Use ret = new Use();
        ret.use_speed = use_speed;
        return ret;
    }

    public double getUseSpeed() {
        return use_speed;
    }


    public void write(FriendlyByteBuf f) {
        super.write(f);
        f.writeDouble(use_speed);
        //f.writeBoolean(continuous);
    }

    public WeaponInteractions.WeaponInteraction read(FriendlyByteBuf f) {
        super.read(f);
        use_speed = f.readDouble();
        //continuous = f.readBoolean();
        return this;
    }
}
