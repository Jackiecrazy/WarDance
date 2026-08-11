package jackiecrazy.wardance.event;

import jackiecrazy.wardance.entity.GrappleEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class GrappleEvent extends LivingEvent {
    private GrappleEntity grapple;

    public GrappleEvent(LivingEntity player, GrappleEntity g) {
        super(player);
        grapple = g;
    }

    public GrappleEntity getGrapple() {
        return grapple;
    }
}
