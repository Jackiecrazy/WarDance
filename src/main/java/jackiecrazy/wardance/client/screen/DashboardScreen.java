package jackiecrazy.wardance.client.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class DashboardScreen extends Screen {
    /** The old x position of the mouse pointer */
    private float oldMouseX;
    /** The old y position of the mouse pointer */
    private float oldMouseY;

    public DashboardScreen(PlayerEntity player) {
        super(new TranslationTextComponent("container.crafting"));
        this.passEvents = true;
    }

//    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
//        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//        this.minecraft.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
//        int i = this.guiLeft;
//        int j = this.guiTop;
//        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
//        InventoryScreen.drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - this.oldMouseX, (float)(j + 75 - 50) - this.oldMouseY, this.minecraft.player);
//    }
//
//    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
//        this.renderBackground(matrixStack);
//        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
//        this.recipeBookGui.func_238924_c_(matrixStack, this.guiLeft, this.guiTop, mouseX, mouseY);
//        this.oldMouseX = (float)mouseX;
//        this.oldMouseY = (float)mouseY;
//    }
}
