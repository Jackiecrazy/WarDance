package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class SkillSelectionButton extends ImageButton {
    protected SkillSelectionScreen parent;
    protected int index;
    protected boolean isPassive = false;
    protected Skill s;

    public SkillSelectionButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, OnPress onPressIn) {
        super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, onPressIn);
    }

    public SkillSelectionButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, int textureWidth, int textureHeight, OnPress onPressIn) {
        super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, textureWidth, textureHeight, onPressIn);
    }

    public SkillSelectionButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffText, ResourceLocation resourceLocation, int textureWidth, int textureHeight, OnPress onPress, Component title) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffText, resourceLocation, textureWidth, textureHeight, onPress, title);
    }
    protected void applySlotTint() {
        if (getParentSelection() != null) {
            if (getParentSelection() == getSkill())//is the skill, highlight
                RenderSystem.setShaderColor(173 / 255f, 216 / 255f, 230 / 255f, 1);
            else if (getParentSelection().isFamily(s))//is family, cannot equip
                RenderSystem.setShaderColor(220 / 255f, 20 / 255f, 60 / 255f, 1);
            else if (getParentSelection().isPassive(Minecraft.getInstance().player) != isPassive)//passive/active slot mixup
                RenderSystem.setShaderColor(230 / 255f, 110 / 255f, 0 / 255f, 1);
            else if (!getParentSelection().isCompatibleWith(s, Minecraft.getInstance().player))//incompatibility
                RenderSystem.setShaderColor(200 / 255f, 160 / 255f, 0 / 255f, 1);
        }
    }

    public Skill getSkill() {
        return s;
    }

    public void setSkill(Skill skill) {
        s = skill;
    }

    Skill getParentSelection() {
        return parent.selectedSkill == null ? null : parent.selectedSkill.getSkill();
    }

    boolean isValidSelection() {
        Skill s = getParentSelection();
        if (s == null) return false;
        if (s.isPassive(Minecraft.getInstance().player) != isPassive) return false;
        if (!parent.isValidInsertion(s))
            return false;
        return true;
    }

    protected int getX() {
        return x;
    }

    protected int getY() {
        return y;
    }
}
