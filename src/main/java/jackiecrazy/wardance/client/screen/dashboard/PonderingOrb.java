package jackiecrazy.wardance.client.screen.dashboard;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.wardance.WarDance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PonderingOrb extends ImageButton {
    private static final ResourceLocation passive = new ResourceLocation(WarDance.MODID, "textures/skill/passive.png");
    private final ResourceLocation interior;
    private double xVelocity, yVelocity, partialX, partialY;
    private DashboardScreen parent;

    public PonderingOrb(DashboardScreen parent, ResourceLocation interior, Button.OnPress click, Button.OnTooltip tooltip) {
        super(1, 1, 16, 16, 0, 0, 0, passive, 256, 256, click, tooltip, Component.empty());
        this.interior = interior;
        this.parent = parent;
    }

    void init(int parentW, int parentH, int atX, int atY, int size) {
        partialX = atX;
        partialY = atY;
        this.x = atX;
        this.y = atY;
        width = height = size;
    }

    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        partialX += xVelocity;
        partialY += yVelocity;
        //move around slowly unless you're hovering over it
        //may need to be in parent instead to handle collisions
            super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderTexture(0, this.interior);

        RenderSystem.enableDepthTest();
        blit(matrixStack, this.x, this.y, 0, 0, this.width, this.height, 64, 64);
    }
}
