package jackiecrazy.wardance.client.screen.skill;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

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
    public int getRowWidth() {
        return this.listWidth;
    }

    @Override
    protected void renderBackground(GuiGraphics mStack) {
        mStack.fill(x0, y0, x1, y1, -16777216);
    }

    @Override
    protected void renderDecorations(GuiGraphics matrixStack, int mouseX, int mouseY) {

    }

    @Override
    public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        int color = this.isFocused() ? 255 : 160;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        //RenderSystem.disableTexture();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex((double) this.x0 + 1, (double) this.y1 + 1, 0.0D).uv((float) this.x0 / 32.0F, (float) (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.vertex((double) this.x1, (double) this.y1 + 1, 0.0D).uv((float) this.x1 / 32.0F, (float) (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.vertex((double) this.x1, (double) this.y0 + 3, 0.0D).uv((float) this.x1 / 32.0F, (float) (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.vertex((double) this.x0 + 1, (double) this.y0 + 3, 0.0D).uv((float) this.x0 / 32.0F, (float) (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        //RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        double d0 = this.minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int) ((double) (this.getRowLeft()) * d0), (int) ((double) (this.height - this.y1) * d0), (int) ((double) (this.getScrollbarPosition() - 10) * d0), (int) ((double) (this.height - (this.height - this.y1) - this.y0 - 4) * d0));
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.disableScissor();
    }

    @Override
    protected int getScrollbarPosition() {
        return this.listWidth + 7;
    }

    public void refreshList() {
        this.clearEntries();
        setScrollAmount(0);
        parent.buildSkillList(this::addEntry, mod -> new CategoryEntry(mod, this.parent));
    }

    public class CategoryEntry extends ObjectSelectionList.Entry<CategoryEntry> {
        private final Skill s;
        private SkillSelectionScreen parent;

        public CategoryEntry(Skill skill, SkillSelectionScreen sss) {
            s = skill;
            parent = sss;
        }

        public Skill getSkill() {
            return s;
        }

        @Override
        public void render(GuiGraphics ms, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean something, float partialTicks) {
            Component name = s.getDisplayName(null);
            //ITextComponent version = new StringTextComponent(stripControlCodes(MavenVersionStringHelper.artifactVersionToString(modInfo.getVersion())));
            //VersionChecker.CheckResult vercheck = VersionChecker.getResult(modInfo);
            Font font = this.parent.getFontRenderer();
            ms.drawString(font, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(name, listWidth))), left + 3, top + 2, 0xffffff);
            //font.draw(ms, LanguageMap.getInstance().getVisualOrder(ITextProperties.composite(font.substrByWidth(version, listWidth))), left + 3, top + 2 + font.FONT_HEIGHT, 0xCCCCCC);
            //lil' skill icon
            RenderSystem.setShaderTexture(0, s.icon());
            float r = s.getColor().getRed() / 255f;
            float g = s.getColor().getGreen() / 255f;
            float b = s.getColor().getBlue() / 255f;
            RenderSystem.setShaderColor(r, g, b, 1);
            ms.pose().pushPose();
            ms.blit(s.icon(), getLeft() + width - 12, top + entryHeight / 4, 0, 0, 8, 8, 8, 8);
            ms.pose().popPose();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            parent.setSelectedSkill(this);
            SkillListWidget.this.setSelected(this);
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", s.getDisplayName(null));
        }
    }
}
