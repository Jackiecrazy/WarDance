package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class DashboardScreen extends Screen {
    private static final ResourceLocation INVENTORY_BACKGROUND = new ResourceLocation("wardance:gui/dashboard.png");
    /**
     * The old x position of the mouse pointer
     */
    private float oldMouseX;
    /**
     * The old y position of the mouse pointer
     */
    private float oldMouseY;
    private Player p;

    public DashboardScreen(Player player) {
        super(Component.translatable("wardance.gui.dashboard"));
        p = player;
        this.passEvents = true;
    }

    protected void drawGuiContainerBackgroundLayer(PoseStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, INVENTORY_BACKGROUND);
        int i = width / 2 - 90;
        int j = height / 2 - 83;
        this.blit(matrixStack, i, j, 0, 0, 180, 166);
        InventoryScreen.renderEntityInInventory(i + 51, j + 75, 30, (float) (i + 51) - this.oldMouseX, (float) (j + 75 - 50) - this.oldMouseY, p);
    }

    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.oldMouseX = (float) mouseX;
        this.oldMouseY = (float) mouseY;
    }
}
