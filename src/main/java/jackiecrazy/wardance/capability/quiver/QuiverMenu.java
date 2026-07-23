// QuiverMenu.java
package jackiecrazy.wardance.capability.quiver;

import jackiecrazy.wardance.WarContainers;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.SyncQuiverPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class QuiverMenu extends AbstractContainerMenu {

    public static final int OVERFLOW_START = QuiverData.NUM_QUIVERS * QuiverData.SLOTS_PER_QUIVER;
    public static final int PLAYER_INV_START = OVERFLOW_START + QuiverData.OVERFLOW_SIZE; // Will be adjusted dynamically
    public static final int HOTBAR_START = PLAYER_INV_START + 27;
    private final QuiverData capability;
    private final Player player;

    public QuiverMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, (QuiverData) null);
    }

    public QuiverMenu(int containerId, Inventory playerInventory, QuiverData cap) {
        super(WarContainers.QUIVER_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.capability = (cap != null) ? cap :
                player.getCapability(QuiverData.QUIVER_CAP).orElse(new QuiverData());

        int maxVisibleColumns = getMaxVisibleColumns();

        // Add quiver slots: 8 rows (colors) × up to 9 columns
        for (int color = 0; color < QuiverData.NUM_QUIVERS; color++) {
            IItemHandler handler = capability.getQuiver(color);
            QuiverMenu m = this;

            for (int col = 0; col < QuiverData.SLOTS_PER_QUIVER; col++) {
                int x = 8 + col * 18;
                int y = 20 + color * 18;

                int finalCol = col;
                int finalColor = color;
                this.addSlot(new SlotItemHandler(handler, finalCol, x, y) {
                    @Override
                    public boolean isActive() {
                        return m.capability.getVisibleSlots(finalColor) > finalCol; // Only active slots for this row
                    }
                });
            }
        }

        // Overflow
        IItemHandler overflow = capability.getOverflow();
        for (int i = 0; i < QuiverData.OVERFLOW_SIZE; i++) {
            this.addSlot(new SlotItemHandler(overflow, i, 200, 20 + i * 18));
        }

        // Player Inventory (positioned below)
        int playerY = 10 + 8 * 18 + 20;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, playerY + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, playerY + 58));
        }

        if (!player.level().isClientSide) {
            syncToClient();
        }
    }

    public int getUsableSlots(int index) {
        return capability.getVisibleSlots(index);
    }

    public int getMaxVisibleColumns() {
        int max = 1;
        boolean pokey = false;
        for (int i = 0; i < QuiverData.NUM_QUIVERS; i++) {
            int prevMax = max;
            max = Math.min(max, capability.getVisibleSlots(i));
            if (max != prevMax)
                pokey = true;
        }
        if (!pokey)
            max += 1;
        return max;
    }

    private void syncToClient() {
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            CombatChannel.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                    new SyncQuiverPacket(capability.serializeNBT())
            );
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }


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
                if (WeaponStats.isWeapon(player, stack)) {
                    if (capability.sheathe(stack, true))
                        slot.set(ItemStack.EMPTY);
                    capability.markDirty(player); // your method
                    return ItemStack.EMPTY;
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
        if (slotId > 0 && slotId < OVERFLOW_START) {
            Slot s = getSlot(slotId);
            if (s.getItem().getMaxStackSize() == 1) {
                ItemStack stack = s.getItem();
                stack.getOrCreateTag().putInt("quiverColorIndex", slotId/QuiverData.SLOTS_PER_QUIVER);
                stack.getOrCreateTag().putInt("quiverSlotIndex", slotId%QuiverData.SLOTS_PER_QUIVER);
            }
        }
        if (!player.level().isClientSide) {
            capability.updateAvailableSlots();
            syncToClient();
        }
    }

    public QuiverData getCapability() {
        return capability;
    }
}