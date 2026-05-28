package jackiecrazy.wardance.client.screen.ponder;

import jackiecrazy.wardance.WarContainers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StudyTheBlade extends AbstractContainerMenu {

    public StudyTheBlade(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(WarContainers.WEEB_MENU.get(), containerId);

        // Add Player Inventory (3 rows of 9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 
                    28 + col * 18, 162 + row * 18));
            }
        }

        // Add Hotbar (bottom row)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 28 + col * 18, 220));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    // Optional: Quick move handling (Shift-click)
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY; // We don't need transfer for this style screen
    }
}