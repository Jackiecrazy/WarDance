package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.*;

public class VariationListWidget extends ExtendedList<VariationListWidget.VariationEntry> {
    private final int listWidth;
    private SkillSelectionScreen parent;


    public VariationListWidget(SkillSelectionScreen parent, int listWidth, int top, int bottom) {
        super(parent.getMinecraftInstance(), listWidth, parent.height, top, bottom, parent.getFontRenderer().FONT_HEIGHT + 8);
        this.parent = parent;
        this.listWidth = listWidth;
        this.func_244606_c(false);
        //this.func_244605_b(false);
        //this.refreshList();
    }

    private static String stripControlCodes(String value) { return net.minecraft.util.StringUtils.stripControlCodes(value); }

    @Override
    protected int getScrollbarPosition() {
        return this.listWidth + x0 - 7;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public void refreshList() {
        this.clearEntries();
        if (parent.selectedSkill != null)
            parent.buildVariationList(parent.selectedSkill.getSkill(), this::addEntry, mod -> new VariationEntry(mod, this.parent));
    }

    @Override
    protected void renderBackground(MatrixStack mStack) {

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
