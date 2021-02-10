package jackiecrazy.wardance.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

public class HandedCritEvent extends CriticalHitEvent {
    private Hand hand;
    private ItemStack stack;

    public static CriticalHitEvent modifiedCrit(PlayerEntity player, Entity target, boolean vanillaCrit, float critModifier, Hand hand, ItemStack stack) {
        CriticalHitEvent hitResult = new HandedCritEvent(player, target, critModifier, vanillaCrit, stack, hand);
        MinecraftForge.EVENT_BUS.post(hitResult);
        if (hitResult.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || (vanillaCrit && hitResult.getResult() == net.minecraftforge.eventbus.api.Event.Result.DEFAULT))
        {
            return hitResult;
        }
        return null;
    }

    public HandedCritEvent(PlayerEntity player, Entity target, float damageModifier, boolean vanillaCritical, ItemStack stack, Hand hand) {
        super(player, target, damageModifier, vanillaCritical);
        this.stack = stack;
        this.hand = hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    public Hand getHand() {
        return hand;
    }

}
