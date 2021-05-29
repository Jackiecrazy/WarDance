package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class SkillSliceButton extends SkillSelectionButton {
    private static final int[] iconX = {
            63,
            100,
            116,
            102,
            63,
            26,
            11,
            26
    };
    private static final int[] iconY = {
            12,
            27,
            63,
            100,
            115,
            100,
            63,
            27
    };
    private boolean wasHovered;

    public SkillSliceButton(SkillSelectionScreen sss, int xIn, int yIn, int sides, int xTexStartIn, int yTexStartIn, ResourceLocation resourceLocationIn, int index) {
        super(xIn, yIn, sides, sides, xTexStartIn, yTexStartIn, 0, resourceLocationIn, 450, 450, (a) -> {});
        this.index = index;
        parent = sss;
    }

    @Override
    public void onPress() {
        if (isValidSelection())
            s = getParentSelection();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isHovered) {
            if (this.clicked(mouseX, mouseY)) {
                if (button == 0) {
                    this.playDownSound(Minecraft.getInstance().getSoundHandler());
                    this.onClick(mouseX, mouseY);
                    return true;
                } else if (button == 1) {
                    s = null;
                }
            }
        }
        return false;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            int centeredx = mouseX - x - width / 2, centeredy = mouseY - y - height / 2;
            boolean distance = centeredy * centeredy + centeredx * centeredx > 800;
            //get direction
            double angle = Math.toDegrees(MathHelper.atan2(centeredx, -centeredy));
            if (angle < 0) angle += 360;
            int hoverIndex = distance ? (int) Math.floor(((angle + 22.5) / 45) % 8) : -1;
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height && index == hoverIndex;
            if (this.wasHovered != this.isHovered()) {
                if (this.isHovered()) {
                    if (this.isFocused()) {
                        this.queueNarration(200);
                    } else {
                        this.queueNarration(750);
                    }
                } else {
                    this.nextNarration = Long.MAX_VALUE;
                }
            }

            if (this.visible) {
                matrixStack.push();
                //blue if it is the literal skill, grey if it is selectable, orange if it is not selectable, red if they share a parent, yellow if they're incompatible
                if (!this.isHovered) {
                    RenderSystem.color4f(0.6f, 0.6f, 0.6f, 1);
                }
                applySlotTint();
                this.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
                if (s != null) {
                    Minecraft.getInstance().textureManager.bindTexture(s.icon());
                    Color c = s.getColor();
                    RenderSystem.color4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                    AbstractGui.blit(matrixStack, x + iconX[index], y + iconY[index], 0, 0, 24, 24, 24, 24);
                }
                RenderSystem.color4f(1f, 1f, 1f, 1);
                matrixStack.pop();
            }

            this.narrate();
            this.wasHovered = this.isHovered();
        }
    }
}
