package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.wardance.WarDance;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class PassiveButton extends SkillSelectionButton {
    private static final ResourceLocation passive = new ResourceLocation(WarDance.MODID, "textures/skill/passive.png");

    public PassiveButton(SkillSelectionScreen sss, int xIn, int yIn, int sides, int index) {
        super(xIn, yIn, sides, sides, 0, 0, 0, passive, (a) -> {});
        this.index = index;
        parent = sss;
        isPassive = true;
    }

    @Override
    public void onPress() {
        if (isValidSelection() && !parent.contains(getParentSelection()))
            s = getParentSelection();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (this.clicked(mouseX, mouseY)) {
                if (button == 0) {
                    if (s != null)
                        parent.displaySkillInfo(s);
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(mouseX, mouseY);
                    return true;
                } else if (button == 1) {
                    s = null;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, passive);
        if (!this.isHovered) {
            RenderSystem.setShaderColor(0.6f, 0.6f, 0.6f, 1);
        }
        applySlotTint();
        //base
        blit(matrixStack, this.getX(), this.getY(), 0, 0, this.width, this.height, width, height);
        //skill
        if (s != null) {
            RenderSystem.setShaderTexture(0, s.icon());
            Color c = s.getColor();
            RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            blit(matrixStack, this.getX(), this.getY(), 0, 0, this.width, this.height, width, height);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
//        if (this.isHoveredOrFocused()) {
//            this.render(matrixStack, mouseX, mouseY, partialTicks);
//        }
    }
}
