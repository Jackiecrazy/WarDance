package jackiecrazy.wardance.client.screen.skill;

import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SkillStyleButton extends SkillSelectionButton {
    private static final ResourceLocation passive = new ResourceLocation(WarDance.MODID, "textures/skill/passive.png");
    private int flashTime;

    public SkillStyleButton(SkillSelectionScreen sss, int xIn, int yIn, int sides) {
        super(xIn, yIn, sides, sides, 0, 0, 0, passive, 256, 256, (a) -> {});
        parent = sss;
    }

    @Override
    public void onPress() {
        if (isValidSelection()) {
            s = getParentSelection();
            parent.setSelectedSkill(null);
            parent.refresh = true;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (this.clicked(mouseX, mouseY)) {
                if (button == 0) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    //send style data to parent
                    if (getStyle() != null)
                        parent.displaySkillInfo(getStyle());
                    this.onClick(mouseX, mouseY);
                    return true;
                } else if (button == 1 && getParentSelection() == null) {
                    s = null;
                    parent.refresh = true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, passive);
        if (!this.isHovered || !isValidSelection()) {
            RenderSystem.setShaderColor(0.6f, 0.6f, 0.6f, 1);
        }
        if (this.isHovered && getParentSelection() == null) {//delete
            RenderSystem.setShaderColor(220 / 255f, 20 / 255f, 60 / 255f, 1);
        }
        applySlotTint();
        //base
        graphics.blit(passive, this.getX(), this.getY(), 0, 0, this.width, this.height, width, height);
        //skill
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (s != null) {
            RenderSystem.setShaderTexture(0, s.icon());
            //Color c = s.getColor();
            //RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            graphics.blit(s.icon(), this.getX(), this.getY(), 0, 0, this.width, this.height, width, height);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
//        if (this.isHoveredOrFocused()) {
//            this.render(matrixStack, mouseX, mouseY, partialTicks);
//        }
        if (this.isHovered) {
            graphics.pose().translate(0, -10, 100);
            graphics.renderTooltip(parent.getMinecraftInstance().font, Component.translatable("wardance.tooltip.resetStyle" + (getParentSelection() == null ? "3" : (parent.isValidInsertion(getParentSelection()) ? "2" : "1"))), mouseX, mouseY);
            //graphics.pose().translate(0, 10, -100);
        }
    }

    @Override
    protected void applySlotTint() {
        flashTime++;
        if (flashTime > 20) flashTime = -20;
        if (getParentSelection() != null && flashTime > 0) {
            if (getParentSelection() != null && !parent.isValidInsertion(getParentSelection()))//just can't stick it in for some reason
                RenderSystem.setShaderColor(220 / 255f, 20 / 255f, 60 / 255f, 1);
        }
    }

    @Override
    boolean isValidSelection() {
        Skill s = getParentSelection();
        if (!(s instanceof SkillStyle)) return false;
        if (!parent.isValidInsertion(s))
            return false;
        return true;
    }
}
