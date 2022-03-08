package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

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
    private PlayerEntity p;

    public DashboardScreen(PlayerEntity player) {
        super(new TranslationTextComponent("wardance.gui.dashboard"));
        p = player;
        this.passEvents = true;
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(INVENTORY_BACKGROUND);
        int i = width / 2 - 90;
        int j = height / 2 - 83;
        this.blit(matrixStack, i, j, 0, 0, 180, 166);
        InventoryScreen.renderEntityInInventory(i + 51, j + 75, 30, (float) (i + 51) - this.oldMouseX, (float) (j + 75 - 50) - this.oldMouseY, p);
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.oldMouseX = (float) mouseX;
        this.oldMouseY = (float) mouseY;
    }
}
