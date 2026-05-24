// QuiverMenu.java
package jackiecrazy.wardance.capability.quiver;

import jackiecrazy.wardance.WarContainers;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.SyncQuiverPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class QuiverMenu extends AbstractContainerMenu {

    private final QuiverData capability;
    private final Player player;

    public static final int PLAYER_INV_START = 80; // Will be adjusted dynamically
    public static final int HOTBAR_START = PLAYER_INV_START + 27;

    public QuiverMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, (QuiverData) null);
    }

    public QuiverMenu(int containerId, Inventory playerInventory, QuiverData cap) {
        super(WarContainers.QUIVER_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.capability = (cap != null) ? cap :
                player.getCapability(QuiverData.QUIVER_CAP).orElse(new QuiverData());

        int maxVisibleColumns = calculateMaxVisibleColumns();

        // Add quiver slots: 8 rows (colors) × up to 10 columns
        for (int color = 0; color < QuiverData.NUM_QUIVERS; color++) {
            IItemHandler handler = capability.getQuiver(color);
            int visible = capability.getVisibleSlots(color);

            for (int col = 0; col < QuiverData.SLOTS_PER_QUIVER; col++) {
                int x = 30 + col * 18;
                int y = 30 + color * 18;

                int finalCol = col;
                this.addSlot(new SlotItemHandler(handler, finalCol, x, y) {
                    @Override
                    public boolean isActive() {
                        return finalCol < visible; // Only active slots for this row
                    }
                });
            }
        }

        // Overflow
        IItemHandler overflow = capability.getOverflow();
        for (int i = 0; i < QuiverData.OVERFLOW_SIZE; i++) {
            this.addSlot(new SlotItemHandler(overflow, i, 30 + maxVisibleColumns * 18 + 30, 20 + i * 18));
        }

        // Player Inventory (positioned below)
        int playerY = 20 + 8 * 18 + 20;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 30 + j * 18, playerY + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 30 + i * 18, playerY + 58));
        }

        if (!player.level().isClientSide) {
            syncToClient();
        }
    }

    private int calculateMaxVisibleColumns() {
        int max = 1;
        for (int i = 0; i < QuiverData.NUM_QUIVERS; i++) {
            max = Math.max(max, capability.getVisibleSlots(i));
        }
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
        // Same logic as before - omitted for brevity, copy from previous version
        // Make sure to call capability.updateVisibleSlots() and syncToClient() after moves
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        super.clicked(slotId, button, clickType, player);
        if (!player.level().isClientSide) {
            capability.updateVisibleSlots();
            syncToClient();
        }
    }

    public QuiverData getCapability() {
        return capability;
    }

    public int getMaxVisibleColumns() {
        return calculateMaxVisibleColumns();
    }
}