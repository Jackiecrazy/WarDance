package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;

public class VariationListWidget extends ExtendedList<VariationListWidget.VariationEntry> {
    private final int listWidth;
    private SkillSelectionScreen parent;


    public VariationListWidget(SkillSelectionScreen parent, int listWidth, int top, int bottom) {
        super(parent.getMinecraftInstance(), listWidth, parent.height, top, bottom, parent.getFontRenderer().FONT_HEIGHT + 8);
        this.func_244605_b(false);
        this.parent = parent;
        this.listWidth = listWidth;
        this.func_244606_c(false);
        //this.func_244605_b(false);
        //this.refreshList();
    }

    private static String stripControlCodes(String value) {return net.minecraft.util.StringUtils.stripControlCodes(value);}

    @Override
    protected int getScrollbarPosition() {
        return this.listWidth + x0 + 3;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public void refreshList() {
        this.clearEntries();
        if (parent.selectedSkill != null)
            parent.buildVariationList(parent.selectedSkill.getCategory(), this::addEntry, mod -> new VariationEntry(mod, this.parent));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        final int color = this.isFocused()?255:160;
        bufferbuilder.pos((double)this.x0+1, (double)this.y1+1, 0.0D).tex((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.pos((double)this.x1-2, (double)this.y1+1, 0.0D).tex((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.pos((double)this.x1-2, (double)this.y0+3, 0.0D).tex((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.pos((double)this.x0+1, (double)this.y0+3, 0.0D).tex((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        tessellator.draw();
        RenderSystem.enableTexture();
        double d0 = this.minecraft.getMainWindow().getGuiScaleFactor();
        RenderSystem.enableScissor((int) ((double) (this.getRowLeft()) * d0), (int) ((double) (this.height - this.y1) * d0), (int) ((double) (this.getScrollbarPosition() - 120) * d0), (int) ((double) (this.height - (this.height - this.y1) - this.y0 - 4) * d0));
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.disableScissor();
    }

    @Override
    protected void renderDecorations(MatrixStack matrixStack, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBackground(MatrixStack mStack) {
        RenderSystem.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos((double)this.x0, (double)this.y1, 0.0D).tex((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(20, 20, 20, 255).endVertex();
        bufferbuilder.pos((double)this.x1, (double)this.y1, 0.0D).tex((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(20, 20, 20, 255).endVertex();
        bufferbuilder.pos((double)this.x1, (double)this.y0+3, 0.0D).tex((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(20, 20, 20, 255).endVertex();
        bufferbuilder.pos((double)this.x0, (double)this.y0+3, 0.0D).tex((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(20, 20, 20, 255).endVertex();
        tessellator.draw();
        RenderSystem.enableTexture();
    }

    public class VariationEntry extends ExtendedList.AbstractListEntry<VariationEntry> {
        private final Skill s;
        private SkillSelectionScreen parent;

        public VariationEntry(Skill skill, SkillSelectionScreen sss) {
            s = skill;
            parent = sss;
        }

        public Skill getSkill() {
            return s;
        }

        @Override
        public void render(MatrixStack ms, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean something, float partialTicks) {
            ITextComponent name = s.getDisplayName(null);
            //ITextComponent version = new StringTextComponent(stripControlCodes(MavenVersionStringHelper.artifactVersionToString(modInfo.getVersion())));
            //VersionChecker.CheckResult vercheck = VersionChecker.getResult(modInfo);
            FontRenderer font = this.parent.getFontRenderer();
            font.func_238422_b_(ms, LanguageMap.getInstance().func_241870_a(ITextProperties.func_240655_a_(font.func_238417_a_(name, listWidth))), left + 3, top + 2, s.getColor().getRGB());
            //font.func_238422_b_(ms, LanguageMap.getInstance().func_241870_a(ITextProperties.func_240655_a_(font.func_238417_a_(version, listWidth))), left + 3, top + 2 + font.FONT_HEIGHT, 0xCCCCCC);
            //lil' skill icon
//            Minecraft.getInstance().getTextureManager().bindTexture(s.icon());
//            RenderSystem.color4f(1, 1, 1, 1);
//            RenderSystem.pushMatrix();
//            AbstractGui.blit(ms, getLeft() + width - 12, top + entryHeight / 4, 0, 0, 8, 8, 64, 16);
//            RenderSystem.popMatrix();
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            parent.setSelectedVariation(this);
            VariationListWidget.this.setSelected(this);
            return false;
        }
    }
}
