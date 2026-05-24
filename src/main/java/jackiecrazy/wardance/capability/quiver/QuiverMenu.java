// QuiverMenu.java
package jackiecrazy.wardance.capability.quiver;

import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.SyncQuiverPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class QuiverMenu extends AbstractContainerMenu {

    private final QuiverData capability;
    private final Player player;

    // Slot index offsets
    public static final int QUIVER_SLOTS_START = 0;
    public static final int OVERFLOW_START = QuiverData.NUM_QUIVERS * QuiverData.SLOTS_PER_QUIVER;
    public static final int PLAYER_INV_START = OVERFLOW_START + QuiverData.OVERFLOW_SIZE;
    public static final int HOTBAR_START = PLAYER_INV_START + 27;

    public QuiverMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null);
    }

    // Server constructor
    public QuiverMenu(int containerId, Inventory playerInventory, QuiverData cap) {
        super(ModMenus.QUIVER_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.capability = cap != null ? cap : player.getCapability(QuiverData.QUIVER_CAP).orElse(new QuiverData());

        // Add all quiver slots (8 quivers × 10 slots) - always present to prevent desync
        for (int quiver = 0; quiver < QuiverData.NUM_QUIVERS; quiver++) {
            IItemHandler handler = capability.getQuiver(quiver);
            for (int slot = 0; slot < QuiverData.SLOTS_PER_QUIVER; slot++) {
                int x = 30 + quiver * 18;
                int y = 20 + slot * 18;
                this.addSlot(new SlotItemHandler(handler, slot, x, y) {
                    @Override
                    public boolean isActive() {
                        // Client will decide visibility via screen, but slot always exists
                        return true;
                    }
                });
            }
        }

        // Overflow slots (non-weapons)
        IItemHandler overflow = capability.getOverflow();
        for (int i = 0; i < QuiverData.OVERFLOW_SIZE; i++) {
            this.addSlot(new SlotItemHandler(overflow, i, 200, 20 + i * 18));
        }

        // Player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 30 + j * 18, 200 + i * 18));
            }
        }

        // Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 30 + i * 18, 258));
        }

        // Initial sync to client
        if (!player.level().isClientSide) {
            syncToClient();
        }
    }

    private void syncToClient() {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            CompoundTag data = capability.serializeNBT();
            CombatChannel.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                new SyncQuiverPacket(data)
            );
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // Prevent accidental overwriting + handle custom assignment
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem().copy();
            moved = stack.copy();

            // Quiver slots -> player inv
            if (index < OVERFLOW_START) {
                if (!this.moveItemStackTo(stack, PLAYER_INV_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Overflow or player -> quivers (with assignment logic)
            else {
                // Try to put weapons into their preferred quiver or random
                if (capability.isWeapon(stack)) {
                    capability.assignOnInsert(stack, index % QuiverData.NUM_QUIVERS, 0); // simplified
                    slot.set(ItemStack.EMPTY);
                    capability.markDirty(player); // your method
                    return moved;
                } else {
                    // Non-weapon -> overflow
                    if (!this.moveItemStackTo(stack, OVERFLOW_START, OVERFLOW_START + QuiverData.OVERFLOW_SIZE, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return moved;
    }

    // Called when player clicks a slot
    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        super.clicked(slotId, button, clickType, player);
        // Re-sync after any change to prevent desync
        if (!player.level().isClientSide) {
            syncToClient();
            capability.updateVisibleSlots();
        }
    }

    public QuiverData getCapability() {
        return capability;
    }
}