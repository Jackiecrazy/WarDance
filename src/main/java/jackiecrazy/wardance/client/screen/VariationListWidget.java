package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;

import javax.annotation.Nonnull;

public class VariationListWidget extends ObjectSelectionList<VariationListWidget.VariationEntry> {
    private final int listWidth;
    private SkillSelectionScreen parent;


    public VariationListWidget(SkillSelectionScreen parent, int listWidth, int top, int bottom) {
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
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int color = this.isFocused() ? 255 : 160;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.disableTexture();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex((double) this.x0 + 1, (double) this.y1 + 1, 0.0D).uv((float) this.x0 / 32.0F, (float) (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.vertex((double) this.x1 - 2, (double) this.y1 + 1, 0.0D).uv((float) this.x1 / 32.0F, (float) (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.vertex((double) this.x1 - 2, (double) this.y0 + 3, 0.0D).uv((float) this.x1 / 32.0F, (float) (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        bufferbuilder.vertex((double) this.x0 + 1, (double) this.y0 + 3, 0.0D).uv((float) this.x0 / 32.0F, (float) (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(color, color, color, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        double d0 = this.minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int) ((double) (this.getRowLeft()) * d0), (int) ((double) (this.height - this.y1) * d0), (int) ((double) (this.getScrollbarPosition() - 120) * d0), (int) ((double) (this.height - (this.height - this.y1) - this.y0 - 4) * d0));
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.disableScissor();
    }

    @Override
    protected void renderDecorations(PoseStack matrixStack, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBackground(PoseStack mStack) {
        fill(mStack, x0, y0, x1, y1+3, -16777216);
    }

    public class VariationEntry extends ObjectSelectionList.Entry<VariationEntry> {
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
        public void render(PoseStack ms, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean something, float partialTicks) {
            Component name = s.getDisplayName(null);
            //ITextComponent version = new StringTextComponent(stripControlCodes(MavenVersionStringHelper.artifactVersionToString(modInfo.getVersion())));
            //VersionChecker.CheckResult vercheck = VersionChecker.getResult(modInfo);
            Font font = this.parent.getFontRenderer();
            font.draw(ms, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(name, listWidth))), left + 3, top + 2, s.getColor().getRGB());
            //font.draw(ms, LanguageMap.getInstance().getVisualOrder(ITextProperties.composite(font.substrByWidth(version, listWidth))), left + 3, top + 2 + font.FONT_HEIGHT, 0xCCCCCC);
            //lil' skill icon
//            Minecraft.getInstance().getTextureManager().bindTexture(s.icon());
//            RenderSystem.setShaderColor(1, 1, 1, 1);
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

        @Nonnull
        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", s.getDisplayName(null));
        }
    }
}
