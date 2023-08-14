package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.awt.*;

public class SkillSliceButton extends SkillSelectionButton {
    private static final int[] iconX = {
            63,
            88,
            88,
            39,
            39
    };
    private static final int[] iconY = {
            63,
            39,
            88,
            88,
            39
    };
    private boolean wasHovered;

    public SkillSliceButton(SkillSelectionScreen sss, int xIn, int yIn, int sides, int xTexStartIn, int yTexStartIn, ResourceLocation resourceLocationIn, int index) {
        super(xIn, yIn, sides, sides, xTexStartIn, yTexStartIn, 0, resourceLocationIn, 450, 450, (a) -> {});
        this.index = index;
        parent = sss;
    }

    @Override
    public void onPress() {
        if (isValidSelection()&&!parent.contains(getParentSelection()))
            s = getParentSelection();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isHovered) {
            if (this.clicked(mouseX, mouseY)) {
                if (button == 0) {
                    if(s!=null)
                        parent.displaySkillInfo(s);
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(mouseX, mouseY);
                    return true;
                } else if (button == 1) {
                    s = null;
                }
            }
        }
        return false;
    }

    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            final int x = this.getX();
            final int y = this.getY();
            int centeredx = mouseX - x - width / 2, centeredy = mouseY - y - height / 2;
            //get direction
            double angle = Math.toDegrees(Mth.atan2(centeredx, -centeredy));
            if (angle < 45) angle += 720;
            //at 45/135/215/305 deg, the distance cutoff should be 430, otherwise 700
            double cutoffy = centeredx > 0 ? 26 - centeredx : centeredx + 26;
            boolean distance = centeredy > cutoffy;
            if (centeredy < 0) {
                cutoffy = centeredx < 0 ? -26 - centeredx : centeredx - 26;
                distance = cutoffy > centeredy;
            }
            int hoverIndex = distance ? (int) Math.floor((angle / 90) % 4) + 1 : 0;
            this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height && index == hoverIndex;
//            if (this.wasHovered != this.isHovered) {
//                if (this.isHovered) {
//                    if (this.isFocused()) {
//                        this.queueNarration(200);
//                    } else {
//                        this.queueNarration(750);
//                    }
//                } else {
//                    this.nextNarration = Long.MAX_VALUE;
//                }
//            }

            if (this.visible) {
                matrixStack.pushPose();
                //blue if it is the literal skill, grey if it is selectable, orange if it is not selectable, red if they share a parent, yellow if they're incompatible
                if (!this.isHovered) {
                    RenderSystem.setShaderColor(0.6f, 0.6f, 0.6f, 1);
                }
                applySlotTint();
                this.renderButton(matrixStack, mouseX, mouseY, partialTicks);
                if (s != null) {
                    RenderSystem.setShaderTexture(0, s.icon());
                    Color c = s.getColor();
                    RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                    GuiComponent.blit(matrixStack, x + iconX[index], y + iconY[index], 0, 0, 24, 24, 24, 24);
                }
                RenderSystem.setShaderColor(1f, 1f, 1f, 1);
                matrixStack.popPose();
            }

            //this.narrate();
            this.wasHovered = this.isHovered;
        }
    }
}
