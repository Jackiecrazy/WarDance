package jackiecrazy.wardance.client.screen.scroll;

import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class SkillButton extends ImageButton {

    private static final ResourceLocation passive = new ResourceLocation(WarDance.MODID, "textures/skill/passive.png");
    protected ScrollScreen parent;
    protected Skill s;

    public SkillButton(ScrollScreen sss, int xIn, int yIn, int sides, Skill skill) {
        super(xIn, yIn, sides, sides, 0, 0, 0, passive, (a) -> {});
        parent = sss;
        s = skill;
    }

    protected void applySlotTint() {
        if (getParentSelection() != null) {
            if (getParentSelection() == getSkill())//is the skill, highlight
                RenderSystem.setShaderColor(173 / 255f, 216 / 255f, 230 / 255f, 1);
        }
    }

    public Skill getSkill() {
        return s;
    }

    public void setSkill(Skill skill) {
        s = skill;
    }

    Skill getParentSelection() {
        return parent.selected;
    }

    @Override
    public void onPress() {
        parent.selected = getSkill();
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
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public void renderWidget(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, passive);
        if (!this.isHovered) {
            RenderSystem.setShaderColor(0.6f, 0.6f, 0.6f, 1);
        }
        applySlotTint();
        //base
        matrixStack.blit(passive, this.getX(), this.getY(), 0, 0, this.width, this.height, width, height);
        //skill
        if (s != null) {
            RenderSystem.setShaderTexture(0, s.icon());
            Color c = s.getColor();
            RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            matrixStack.blit(s.icon(), this.getX(), this.getY(), 0, 0, this.width, this.height, width, height);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
//        if (this.isHoveredOrFocused()) {
//            this.render(matrixStack, mouseX, mouseY, partialTicks);
//        }
    }
}
