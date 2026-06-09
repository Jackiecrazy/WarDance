// QuiverScreen.java
package jackiecrazy.wardance.client.screen.ponder;

import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.capability.quiver.QuiverMenu;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.Arrays;
import java.util.Iterator;

@OnlyIn(Dist.CLIENT)
public class QuiverScreen extends AbstractContainerScreen<QuiverMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("wardance", "textures/gui/quiver_gui.png");
    private final QuiverData capability;

    public QuiverScreen(QuiverMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.capability = menu.getCapability();

        int columns = menu.getMaxVisibleColumns();
        this.imageWidth = 176; // Extra space for overflow + labels
        this.imageHeight = 30 + 8 * 18 + 100;     // 8 colors + player inv
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        //colorize and draw each row
        final Iterator<SkillCategory> iterator = Arrays.stream(QuiverData.ORDER).iterator();
        int j=0;
        //guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, 19);
        final int barSpace = 18;
        while (iterator.hasNext()) {
            int slots = menu.getUsableSlots(j);
            SkillCategory sc = iterator.next();
            Color c = sc.getColor();
            if(CasterData.getCap(minecraft.player).getEquippedColors().contains(sc)) {
                RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            }
            //draw available slots
            guiGraphics.blit(TEXTURE, x, 19+y+ barSpace *j, 0, 19+ 18 *j, 7+ 18 *slots, 18);
            //then the little cap
            RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            guiGraphics.blit(TEXTURE, x+7+ barSpace *slots, 19+y+ 18 *j, 7+ 18 *9, 19+ 18 *j, 18, 18);
            guiGraphics.blit(TEXTURE, x, 19+y+ barSpace *j, 0, 19+ 18 *j, 8, 18);
            j++;
            RenderSystem.setShaderColor(1,1,1,1);
        }

        int remainingY = imageHeight-(barSpace *(j))+3;
        guiGraphics.blit(TEXTURE, x, y+169, 0, 169, this.imageWidth, 87);

        // Draw color labels on the left
        String[] colors = {"White", "Gold", "Purple", "Red", "Green", "Cyan", "Blue", "Gray"};
        for (int i = 0; i < 8; i++) {
            //guiGraphics.drawString(font, colors[i], x - 32, y + 24 + i * 18, 0xFFFFFF);
        }

        // Optional: Draw locked slots as faded
//        int maxCol = menu.getMaxVisibleColumns();
//        for (int color = 0; color < 8; color++) {
//            int visible = capability.getVisibleSlots(color);
//            for (int col = visible; col < QuiverData.SLOTS_PER_QUIVER; col++) {
//                int sx = x + 8 + col * 18;
//                int sy = y + 20 + color * 18;
//                guiGraphics.fill(sx, sy, sx + 16, sy + 16, 0x88AAAAAA);
//            }
//        }
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
        //no labels here, nope
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}