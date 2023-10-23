package jackiecrazy.wardance.client.screen.skill;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.wardance.skill.SkillCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class SkillCategoryButton extends ImageButton {
    private SkillSelectionScreen parent;
    private SkillSelectionScreen.SkillCategorySort sort;
    private List<FormattedCharSequence> d;

    public SkillCategoryButton(SkillSelectionScreen screen, SkillSelectionScreen.SkillCategorySort scc, List<FormattedCharSequence> display, int x, int y, int width, int height, ResourceLocation icon) {
        super(x, y, width, height, 0, 0, 0, icon, width, height,
                b -> screen.filterSkills(scc.cat),
                scc.getText());
        d=display;
        parent = screen;
        sort = scc;
    }

    @Override
    public void renderWidget(GuiGraphics gg, int x, int y, float a) {
        PoseStack matrixStack=gg.pose();
        SkillCategory s = sort.cat;
        float r = s.getColor().getRed() / 255f;
        float g = s.getColor().getGreen() / 255f;
        float b = s.getColor().getBlue() / 255f;
        if (!this.active || parent.style.getStyle()==null || parent.getNumColors().contains(sort.cat))
            RenderSystem.setShaderColor(r, g, b, 1);
        RenderSystem.setShaderTexture(0, s.icon());
        RenderSystem.enableDepthTest();
        gg.blit(s.icon(), getX(), getY(), 0, 0, this.width, this.height, width, height);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (this.isHovered) {
            gg.renderTooltip(parent.getFontRenderer(), d, x, y);
        }
    }
}
