package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class SkillSliceButton extends ImageButton {
    private final SkillSelectionScreen parent;
    private final int index;
    private boolean wasHovered;
    private Skill s;

    public SkillSliceButton(SkillSelectionScreen sss, int xIn, int yIn, int sides, int xTexStartIn, int yTexStartIn, ResourceLocation resourceLocationIn, int index) {
        super(xIn, yIn, sides, sides, xTexStartIn, yTexStartIn, 0, resourceLocationIn, 450, 450, (a) -> {});
        this.index = index;
        parent = sss;
    }

    @Override
    public void onPress() {
        if (parent.selectedVariation != null)
            s = parent.selectedVariation.getSkill();
        else if (parent.selectedSkill != null)
            s = parent.selectedSkill.getSkill();
        else s = null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
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
                if(!this.isHovered)
                    RenderSystem.color4f(0.6f, 0.6f, 0.6f, 1);
                //TODO more colors
                this.renderButton(matrixStack, mouseX, mouseY, partialTicks);
                if (s != null) {
                    Minecraft.getInstance().textureManager.bindTexture(s.icon());
                    blit(matrixStack, x, y, 16, 16, 16, 16);
                }
                RenderSystem.color4f(1f, 1f, 1f, 1);
                matrixStack.pop();
            }

            this.narrate();
            this.wasHovered = this.isHovered();
        }
    }
}
