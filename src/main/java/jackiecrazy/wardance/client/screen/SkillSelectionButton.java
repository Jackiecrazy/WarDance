package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.client.gui.widget.button.Button.ITooltip;

public abstract class SkillSelectionButton extends ImageButton {
    protected SkillSelectionScreen parent;
    protected int index;
    protected boolean isPassive = false;
    protected Skill s;

    public SkillSelectionButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, IPressable onPressIn) {
        super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, onPressIn);
    }

    public SkillSelectionButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, int textureWidth, int textureHeight, IPressable onPressIn) {
        super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, textureWidth, textureHeight, onPressIn);
    }

    public SkillSelectionButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffText, ResourceLocation resourceLocation, int textureWidth, int textureHeight, IPressable onPress, ITextComponent title) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffText, resourceLocation, textureWidth, textureHeight, onPress, title);
    }

    public SkillSelectionButton(int p_i244513_1_, int p_i244513_2_, int p_i244513_3_, int p_i244513_4_, int p_i244513_5_, int p_i244513_6_, int p_i244513_7_, ResourceLocation p_i244513_8_, int p_i244513_9_, int p_i244513_10_, IPressable p_i244513_11_, ITooltip p_i244513_12_, ITextComponent p_i244513_13_) {
        super(p_i244513_1_, p_i244513_2_, p_i244513_3_, p_i244513_4_, p_i244513_5_, p_i244513_6_, p_i244513_7_, p_i244513_8_, p_i244513_9_, p_i244513_10_, p_i244513_11_, p_i244513_12_, p_i244513_13_);
    }

    protected void applySlotTint() {
        if (getParentSelection() != null) {
            if (getParentSelection() == getSkill())
                RenderSystem.color4f(173 / 255f, 216 / 255f, 230 / 255f, 1);
            else if (getParentSelection().isFamily(s))
                RenderSystem.color4f(220 / 255f, 20 / 255f, 60 / 255f, 1);
            else if (getParentSelection().isPassive(Minecraft.getInstance().player) != isPassive)
                RenderSystem.color4f(230 / 255f, 110 / 255f, 0 / 255f, 1);
//            else if (getParentSelection().getParentSkill() != null && getSkill() != null && (getParentSelection().getParentSkill() == getSkill() || getParentSelection().getParentSkill() == getSkill().getParentSkill()))
//                RenderSystem.color4f(220 / 255f, 20 / 255f, 60 / 255f, 1);
//            else if (getSkill() != null && getSkill().getParentSkill() != null && (getSkill().getParentSkill() == getParentSelection() || getSkill().getParentSkill() == getParentSelection().getParentSkill()))
//                RenderSystem.color4f(220 / 255f, 20 / 255f, 60 / 255f, 1);
            else if (!getParentSelection().isCompatibleWith(s, Minecraft.getInstance().player))
                RenderSystem.color4f(200 / 255f, 160 / 255f, 0 / 255f, 1);
        }
    }

    public Skill getSkill() {
        return s;
    }

    public void setSkill(Skill skill) {
        s = skill;
    }

    Skill getParentSelection() {
        return parent.selectedVariation == null ? parent.selectedSkill == null ? null : parent.selectedSkill.getSkill() : parent.selectedVariation.getSkill();
    }

    boolean isValidSelection() {
        Skill s = getParentSelection();
        if (s == null) return false;
        if (s.isPassive(Minecraft.getInstance().player) != isPassive) return false;
        if (!parent.isValidInsertion(s))
            return false;
        return true;
    }
}
