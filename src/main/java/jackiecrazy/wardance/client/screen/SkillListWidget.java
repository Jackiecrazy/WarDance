package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.skill.SkillCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;

public class SkillListWidget extends ObjectSelectionList<SkillListWidget.CategoryEntry> {
    private final int listWidth;
    private SkillSelectionScreen parent;


    public SkillListWidget(SkillSelectionScreen parent, int listWidth, int top, int bottom) {
        super(parent.getMinecraftInstance(), listWidth, parent.height, top, bottom, parent.getFontRenderer().lineHeight + 8);
        this.setRenderBackground(false);
        this.parent = parent;
        this.listWidth = listWidth;
        this.setRenderTopAndBottom(false);
        //this.setRenderBackground(false);
        //this.refreshList();
    }

    private static String stripControlCodes(String value) {return net.minecraft.util.StringUtil.stripColor(value);}

    @Override
    protected int getScrollbarPosition() {
        return this.listWidth + 7;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public void refreshList() {
        this.clearEntries();
        parent.buildSkillList(this::addEntry, mod -> new CategoryEntry(mod, this.parent));
    }

    @Override
    protected void renderBackground(PoseStack mStack) {
        RenderSystem.disableTexture();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(20, 20, 20, 255).endVertex();
        bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(20, 20, 20, 255).endVertex();
        bufferbuilder.vertex((double)this.x1, (double)this.y0+3, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(20, 20, 20, 255).endVertex();
        bufferbuilder.vertex((double)this.x0, (double)this.y0+3, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(20, 20, 20, 255).endVertex();
        tessellator.end();
        RenderSystem.enableTexture();
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.disableTexture();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        int color=this.isFocused()?255:160;
        bufferbuilder.vertex((double)this.x0+1, (double)this.y1+1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.vertex((double)this.x1, (double)this.y1+1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.vertex((double)this.x1, (double)this.y0+3, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.vertex((double)this.x0+1, (double)this.y0+3, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        tessellator.end();
        RenderSystem.enableTexture();
        double d0 = this.minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int) ((double) (this.getRowLeft()) * d0), (int) ((double) (this.height - this.y1) * d0), (int) ((double) (this.getScrollbarPosition() - 10) * d0), (int) ((double) (this.height - (this.height - this.y1) - this.y0 - 4) * d0));
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.disableScissor();
    }

    @Override
    protected void renderDecorations(PoseStack matrixStack, int mouseX, int mouseY) {

    }

    public class CategoryEntry extends ObjectSelectionList.Entry<CategoryEntry> {
        private final SkillCategory s;
        private SkillSelectionScreen parent;

        public CategoryEntry(SkillCategory skill, SkillSelectionScreen sss) {
            s = skill;
            parent = sss;
        }

        public SkillCategory getCategory() {
            return s;
        }

        @Override
        public void render(PoseStack ms, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean something, float partialTicks) {
            Component name = s.name();
            //ITextComponent version = new StringTextComponent(stripControlCodes(MavenVersionStringHelper.artifactVersionToString(modInfo.getVersion())));
            //VersionChecker.CheckResult vercheck = VersionChecker.getResult(modInfo);
            Font font = this.parent.getFontRenderer();
            font.draw(ms, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(name, listWidth))), left + 3, top + 2, 0xFFFFFF);
            //font.draw(ms, LanguageMap.getInstance().getVisualOrder(ITextProperties.composite(font.substrByWidth(version, listWidth))), left + 3, top + 2 + font.FONT_HEIGHT, 0xCCCCCC);
            //lil' skill icon
            Minecraft.getInstance().getTextureManager().bind(s.icon());
            RenderSystem.color4f(1, 1, 1, 1);
            RenderSystem.pushMatrix();
            GuiComponent.blit(ms, getLeft() + width - 12, top + entryHeight / 4, 0, 0, 8, 8, 8, 8);
            RenderSystem.popMatrix();
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            parent.setSelectedSkill(this);
            SkillListWidget.this.setSelected(this);
            return false;
        }
    }
}
