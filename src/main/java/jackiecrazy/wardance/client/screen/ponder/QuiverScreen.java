// QuiverScreen.java
package jackiecrazy.wardance.client.screen.ponder;

import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.capability.quiver.QuiverMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class QuiverScreen extends AbstractContainerScreen<QuiverMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("wardance", "textures/gui/quiver.png");
    private final QuiverData capability;

    public QuiverScreen(QuiverMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 280;
        this.imageHeight = 280;
        this.capability = menu.getCapability();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Render only visible slots per quiver column
        for (int quiver = 0; quiver < QuiverData.NUM_QUIVERS; quiver++) {
            int visible = capability.getVisibleSlots(quiver);
            for (int slot = 0; slot < QuiverData.SLOTS_PER_QUIVER; slot++) {
                if (slot >= visible) {
                    // Optionally draw a "locked" overlay
                    int sx = x + 29 + quiver * 18;
                    int sy = y + 19 + slot * 18;
                    guiGraphics.fill(sx, sy, sx + 16, sy + 16, 0x77AAAAAA); // semi-transparent gray
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title etc.
        guiGraphics.drawString(this.font, this.title, 8, 6, 0xFFFFFF);
    }
}