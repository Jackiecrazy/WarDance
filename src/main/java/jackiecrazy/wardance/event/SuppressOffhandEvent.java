package jackiecrazy.wardance.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;

@Event.HasResult
public class SuppressOffhandEvent extends LivingEvent {
    private ItemStack mainStack;

    public SuppressOffhandEvent(LivingEntity e, ItemStack stack) {
        super(e);
        mainStack = stack;
    }

    public ItemStack getMainStack() {
        return mainStack;
    }
}
