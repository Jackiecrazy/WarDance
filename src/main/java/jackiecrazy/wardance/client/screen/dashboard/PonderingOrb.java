package jackiecrazy.wardance.client.screen.dashboard;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.wardance.WarDance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

public class PonderingOrb extends ImageButton {
    private final ResourceLocation interior;
    double xVelocity, yVelocity, partialX, partialY;
    float alpha, alphaPer;
    private DashboardScreen parent;

    public PonderingOrb(DashboardScreen parent, ResourceLocation interior, Button.OnPress click, Component tooltip) {
        super(1, 1, 16, 16, 0, 0, 0, interior, 256, 256, click, (button, pose, x, y) -> {
            parent.renderTooltip(pose, tooltip, x, y);
        }, Component.empty());
        this.interior = interior;
        this.parent = parent;
    }

    void init(int atX, int atY, int size) {
        partialX = atX;
        partialY = atY;
        this.x = Math.min(atX, parent.width - size);
        this.y = Math.min(atY, parent.height - size);
        width = height = size;
        xVelocity = (WarDance.rand.nextFloat() - 0.5);
        yVelocity = (WarDance.rand.nextFloat() - 0.5);
        alpha = 0;
        alphaPer = 0.01f + WarDance.rand.nextFloat() / 100;
    }

    void init() {
        init(WarDance.rand.nextInt(parent.width - width), WarDance.rand.nextInt(parent.height - height), width);
    }

    boolean overlap(PonderingOrb other) {
        int x1 = x, y1 = y, x2 = x + width, y2 = y + height;
        int ox1 = other.x, oy1 = other.y, ox2 = other.x + other.width, oy2 = other.y + other.height;

        return (inRange(x1, ox1, ox2) && inRange(y1, oy1, oy2)) ||
                (inRange(x2, ox1, ox2) && inRange(y1, oy1, oy2)) ||
                (inRange(x1, ox1, ox2) && inRange(y2, oy1, oy2)) ||
                (inRange(x2, ox1, ox2) && inRange(y2, oy1, oy2))
                ;
    }

    boolean inRange(int base, int from, int to) {
        return base <= to && base >= from;
    }

    public void renderButton(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        //move around slowly unless you're hovering over it
        //may need to be in parent instead to handle collisions
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.interior);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderColor(1, 1f, 1, alpha);
        RenderSystem.enableDepthTest();
        blit(matrixStack, this.x, this.y, 0, 0, this.width + 1, this.height + 1, this.width, this.height);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        if (parent.focus == this) {
            this.renderToolTip(matrixStack, mouseX, mouseY);
            alpha += 0.03;
            alpha = Math.min(1.5f, alpha);
        } else {
            if (alpha < -0.5)
                init();
            if (alpha > 2)
                alphaPer = -Mth.abs(alphaPer);
            alpha += alphaPer;
            xVelocity = Mth.clamp(xVelocity, -0.7, 0.7);
            yVelocity = Mth.clamp(yVelocity, -0.7, 0.7);
            //handle literal edge cases
            if (partialX + width + xVelocity >= parent.width)
                xVelocity = -Math.abs(xVelocity);
            if (partialX + xVelocity <= 0)
                xVelocity = Math.abs(xVelocity);
            if (partialY + height + yVelocity >= parent.height)
                yVelocity = -Math.abs(yVelocity);
            if (partialY + yVelocity <= 0)
                yVelocity = Math.abs(yVelocity);
            partialX += xVelocity;
            partialY += yVelocity;
            x = (int) partialX;
            y = (int) partialY;
        }
        isHovered = hovered(mouseX, mouseY);
    }

    private boolean hovered(int mouseX, int mouseY) {
        double dx = x + width / 2d - mouseX;
        double dy = y + height / 2d - mouseY;
        double distance = (dx * dx + dy * dy);
        if (distance <= (width * height) / 4f) {
            parent.focus = this;
            return true;
        }
        if (parent.focus == this) parent.focus = null;
        return false;
    }
}
