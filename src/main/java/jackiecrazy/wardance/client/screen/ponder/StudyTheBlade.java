package jackiecrazy.wardance.client.screen.ponder;

import jackiecrazy.wardance.WarContainers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StudyTheBlade extends AbstractContainerMenu {

    public StudyTheBlade(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(WarContainers.WEEB_MENU.get(), containerId);

        // Add Player Inventory (3 rows of 9)
        int playerY = 10 + 8 * 18 + 20;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, playerY + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, playerY + 58));
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

    @Override
    public void clicked(int p_150400_, int p_150401_, ClickType p_150402_, Player p_150403_) {
        super.clicked(p_150400_, p_150401_, p_150402_, p_150403_);
    }
}