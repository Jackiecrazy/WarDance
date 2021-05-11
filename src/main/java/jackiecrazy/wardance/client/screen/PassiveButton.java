package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

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
        if (isValidSelection())
            s = getParentSelection();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (this.clicked(mouseX, mouseY)) {
                if (button == 0) {
                    this.playDownSound(Minecraft.getInstance().getSoundHandler());
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

    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft.getInstance().getTextureManager().bindTexture(passive);
        if (!this.isHovered) {
            RenderSystem.color4f(0.6f, 0.6f, 0.6f, 1);
        }
        applySlotTint();
        blit(matrixStack, this.x, this.y, 0, 0, this.width, this.height, width, height);
        if (s != null) {
            Minecraft.getInstance().getTextureManager().bindTexture(s.icon());
            Color c = s.getColor();
            RenderSystem.color4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            blit(matrixStack, this.x, this.y, 0, 0, this.width, this.height, width, height);
        }
        RenderSystem.color4f(1, 1, 1, 1);
        if (this.isHovered()) {
            this.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
}
