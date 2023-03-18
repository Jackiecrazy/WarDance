package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.wardance.skill.SkillCategory;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class SkillCategoryButton extends ImageButton {
    private SkillSelectionScreen parent;
    private SkillSelectionScreen.SkillCategorySort sort;

    public SkillCategoryButton(SkillSelectionScreen screen, SkillSelectionScreen.SkillCategorySort scc, List<FormattedCharSequence> display, int x, int y, int width, int height, ResourceLocation icon) {
        super(x, y, width, height, 0, 0, 0, icon, width, height,
                b -> screen.filterSkills(scc.cat),
                (butt, stack, butx, buty) -> screen.renderTooltip(stack, display, butx, buty),
                scc.getText());
        parent = screen;
        sort = scc;
    }

    @Override
    public void renderButton(PoseStack matrixStack, int x, int y, float a) {
        SkillCategory s = sort.cat;
        float r = s.getColor().getRed() / 255f;
        float g = s.getColor().getGreen() / 255f;
        float b = s.getColor().getBlue() / 255f;
        if (!this.active)
            RenderSystem.setShaderColor(r, g, b, 1);
        RenderSystem.setShaderTexture(0, s.icon());
        RenderSystem.enableDepthTest();
        blit(matrixStack, this.x, this.y, 0, 0, this.width, this.height, width, height);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (this.isHovered) {
            this.renderToolTip(matrixStack, x, y);
        }
    }
}
