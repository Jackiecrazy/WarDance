package jackiecrazy.wardance.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class HandedAttackEvent extends AttackEntityEvent {
    private Hand hand;
    private ItemStack stack;

    public static boolean onModifiedAttack(PlayerEntity player, Entity target, Hand hand, ItemStack stack) {
        if (hand == Hand.MAIN_HAND && !ForgeHooks.onPlayerAttackTarget(player, target)) return false;
        if (MinecraftForge.EVENT_BUS.post(new HandedAttackEvent(player, target, hand, stack))) {
            return false;
        } else {
            return stack.isEmpty() || !stack.getItem().onLeftClickEntity(stack, player, target);
        }

    }

    public HandedAttackEvent(PlayerEntity player, Entity target, Hand hand, ItemStack stack) {
        super(player, target);
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
