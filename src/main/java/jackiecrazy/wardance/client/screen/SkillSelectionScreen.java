package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateSkillSelectionPacket;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.loading.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkillSelectionScreen extends Screen {
    private static final ResourceLocation radial = new ResourceLocation(WarDance.MODID, "textures/skill/radialhud.png");
    private static final int[] fixedU = {
            150, 300, 0, 150, 300, 0, 150, 300
    };
    private static final int[] fixedV = {
            0, 0, 150, 150, 150, 300, 300, 300
    };
    private static final int PADDING = 6;
    private final List<Skill> unsortedSkills;
    private final SkillSliceButton[] skillPie = new SkillSliceButton[8];
    private final PassiveButton[] passives = new PassiveButton[4];
    private final int numButtons = SkillSelectionScreen.SortType.values().length;
    public SkillListWidget.SkillEntry selectedSkill = null;
    public VariationListWidget.VariationEntry selectedVariation = null;
    private SkillListWidget skillList;
    private VariationListWidget variationList;
    private SkillSelectionScreen.InfoPanel modInfo;
    private int listWidth;
    private List<Skill> skills;
    private int buttonMargin = 1;
    private String lastFilterText = "";

    private TextFieldWidget search;

    private boolean sorted = false;
    private SortType sortType = SortType.NORMAL;

    public SkillSelectionScreen() {
        super(new TranslationTextComponent("wardance.skillselection.title"));
        this.skills = Skill.variationMap.keySet().stream().filter((a) -> CasterData.getCap(Minecraft.getInstance().player).isSkillSelectable(a)).collect(Collectors.toList());//hmm
        this.unsortedSkills = Collections.unmodifiableList(this.skills);
    }

    private static String stripControlCodes(String value) { return net.minecraft.util.StringUtils.stripControlCodes(value); }
    //private SkillSelectionScreen.SortType sortType = SkillSelectionScreen.SortType.NORMAL;
    /*
    there needs to be a skill list widget that lists all castable skills, composed of skill entries, on the very very left, and maybe a search bar as well
    a skill entry is made from the main skill icon, the name, and a smaller assortment of icons representing available variations. They can be kind of generic across every skill
    clicking a skill highlights it and will call the main screen to display text down the middle to describe the skill
    A little panel at the bottom is reserved for displaying variation descriptions, to show the base and variation effects in a single screen.
    On the right is the skill octagon and four slots to click skills into
    these slots can be tinted blue if it is selected, grey if it's selectable, orange if it's not selectable, yellow if they share a parent skill, and red if the two skills are incompatible (such as breathing fire while chanting a spell).
    Upon closing, the octagon is sent to the server, double checked, and finalized.
     */

    @Override
    public void init() {
        for (Skill mod : skills) {
            listWidth = Math.max(listWidth, getFontRenderer().getStringWidth(mod.getDisplayName(null).getString()) + 20);
        }
        listWidth = Math.max(Math.min(listWidth, width / 5), 100);
        listWidth += listWidth % numButtons != 0 ? (numButtons - listWidth % numButtons) : 0;

        int skillCircleWidth = 150;
        int infoWidth = this.width - this.listWidth - skillCircleWidth - (PADDING * 4);
        int doneButtonWidth = Math.min(infoWidth, 200);
        int y = this.height - 20 - PADDING;
        this.addButton(new Button(((listWidth + PADDING + this.width - doneButtonWidth) / 2), y, doneButtonWidth, 20,
                new TranslationTextComponent("gui.done"), b -> SkillSelectionScreen.this.closeScreen()));
        //y -= 20 + PADDING;

        //y -= 14 + PADDING + 1;
        search = new TextFieldWidget(getFontRenderer(), PADDING + 1, y, listWidth - 2, 14, new TranslationTextComponent("fml.menu.mods.search"));

        int fullButtonHeight = PADDING + 20 + PADDING;
        this.skillList = new SkillListWidget(this, listWidth, fullButtonHeight, search.y - getFontRenderer().FONT_HEIGHT - PADDING);
        this.skillList.setLeftPos(PADDING);

        int split = (this.height - PADDING * 2 - fullButtonHeight) * 2 / 3;
        this.modInfo = new InfoPanel(this.minecraft, infoWidth, split, PADDING);
        this.variationList = new VariationListWidget(this, infoWidth - 9, split + PADDING * 2, search.y - getFontRenderer().FONT_HEIGHT - PADDING);
        this.variationList.setLeftPos(PADDING * 2 + listWidth);

        List<Skill> oldList = CasterData.getCap(Minecraft.getInstance().player).getEquippedSkills();
        for (int d = 0; d < skillPie.length; d++) {
            skillPie[d] = new SkillSliceButton(this, width - skillCircleWidth, PADDING / 2, skillCircleWidth, fixedU[d], fixedV[d], radial, d);
            skillPie[d].setSkill(oldList.get(d));
            children.add(skillPie[d]);
        }

        for (int d = 0; d < passives.length; d++) {
            passives[d] = new PassiveButton(this, width - skillCircleWidth + d * (32 + PADDING), PADDING + skillCircleWidth, 23, d);
            passives[d].setSkill(oldList.get(d + skillPie.length));
            children.add(passives[d]);
        }

        children.add(search);
        children.add(skillList);
        children.add(variationList);
        children.add(modInfo);
        search.setFocused2(false);
        search.setCanLoseFocus(true);

        final int width = listWidth / numButtons;
        int x = PADDING;
        addButton(SkillSelectionScreen.SortType.NORMAL.button = new Button(x, PADDING, width - buttonMargin, 20, SkillSelectionScreen.SortType.NORMAL.getButtonText(), b -> resortMods(SkillSelectionScreen.SortType.NORMAL)));
        x += width + buttonMargin;
        addButton(SkillSelectionScreen.SortType.A_TO_Z.button = new Button(x, PADDING, width - buttonMargin, 20, SkillSelectionScreen.SortType.A_TO_Z.getButtonText(), b -> resortMods(SkillSelectionScreen.SortType.A_TO_Z)));
        x += width + buttonMargin;
        addButton(SkillSelectionScreen.SortType.Z_TO_A.button = new Button(x, PADDING, width - buttonMargin, 20, SkillSelectionScreen.SortType.Z_TO_A.getButtonText(), b -> resortMods(SkillSelectionScreen.SortType.Z_TO_A)));
        resortMods(SkillSelectionScreen.SortType.NORMAL);
        updateCache();
        setFocusedDefault(skillList);
    }

    @Override
    public void tick() {
        search.tick();
        skillList.setSelected(selectedSkill);
        variationList.setSelected(selectedVariation);

        if (!search.getText().equals(lastFilterText)) {
            reloadMods();
            sorted = false;
        }

        if (!sorted) {
            reloadMods();
            skills.sort(sortType);
            skillList.refreshList();
            variationList.refreshList();
            if (selectedSkill != null) {
                selectedSkill = skillList.getEventListeners().stream().filter(e -> e.getSkill() == selectedSkill.getSkill()).findFirst().orElse(null);
                updateCache();
            }
            sorted = true;
        }
    }

    public <T extends ExtendedList.AbstractListEntry<T>> void buildSkillList(Consumer<T> modListViewConsumer, Function<Skill, T> newEntry) {
        skills.forEach(mod -> modListViewConsumer.accept(newEntry.apply(mod)));
    }

    public <T extends ExtendedList.AbstractListEntry<T>> void buildVariationList(Skill s, Consumer<T> modListViewConsumer, Function<Skill, T> newEntry) {
        Skill.variationMap.get(s).forEach(mod -> modListViewConsumer.accept(newEntry.apply(mod)));
    }

    private void reloadMods() {
        this.skills = this.unsortedSkills.stream().
                filter(mi -> StringUtils.toLowerCase(stripControlCodes(mi.getDisplayName(null).getString())).contains(StringUtils.toLowerCase(search.getText()))).collect(Collectors.toList());
        lastFilterText = search.getText();
    }

    boolean isValidInsertion(Skill insert) {
        for (SkillSelectionButton ssb : skillPie)
            if (ssb.getSkill() != null && ssb.getSkill().isFamily(insert)) return false;
        for (SkillSelectionButton ssb : passives)
            if (ssb.getSkill() != null && ssb.getSkill().isFamily(insert)) return false;
        return true;
    }

    private void resortMods(SkillSelectionScreen.SortType newSort) {
        this.sortType = newSort;

        for (SkillSelectionScreen.SortType sort : SkillSelectionScreen.SortType.values()) {
            if (sort.button != null)
                sort.button.active = sortType != sort;
        }
        sorted = false;
    }

    @Override
    public void render(MatrixStack mStack, int mouseX, int mouseY, float partialTicks) {
        this.skillList.render(mStack, mouseX, mouseY, partialTicks);
        this.variationList.render(mStack, mouseX, mouseY, partialTicks);
        if (this.modInfo != null)
            this.modInfo.render(mStack, mouseX, mouseY, partialTicks);
        for (SkillSliceButton ssb : skillPie) {
            ssb.render(mStack, mouseX, mouseY, partialTicks);
        }
        for (PassiveButton pb : passives) {
            pb.render(mStack, mouseX, mouseY, partialTicks);
        }

        ITextComponent text = new TranslationTextComponent("fml.menu.mods.search");
        int x = skillList.getLeft() + ((skillList.getRight() - skillList.getLeft()) / 2) - (getFontRenderer().getStringPropertyWidth(text) / 2);
        getFontRenderer().func_238422_b_(mStack, text.func_241878_f(), x, search.y - getFontRenderer().FONT_HEIGHT, 0xFFFFFF);
        this.search.render(mStack, mouseX, mouseY, partialTicks);
        super.render(mStack, mouseX, mouseY, partialTicks);
    }

    public Minecraft getMinecraftInstance() {
        return minecraft;
    }

    public FontRenderer getFontRenderer() {
        return font;
    }

    public void setSelectedSkill(SkillListWidget.SkillEntry entry) {
        this.selectedSkill = entry == this.selectedSkill ? null : entry;
        selectedVariation = null;
        variationList.refreshList();
        updateCache();
    }

    public void setSelectedVariation(VariationListWidget.VariationEntry entry) {
        this.selectedVariation = entry == this.selectedVariation ? null : entry;
        updateCache();
    }

    private void updateCache() {
        if (selectedSkill == null) {
            this.modInfo.clearInfo();
            List<String> lines = new ArrayList<>();
            lines.add(new TranslationTextComponent("wardance:skills_general").getString() + "\n");
            lines.add(new TranslationTextComponent("wardance:skills_colors").getString() + "\n");
            lines.add(new TranslationTextComponent("wardance:skills_terms").getString() + "\n");
            modInfo.setInfo(lines, null);
            return;
        }
        Skill selectedSkill = this.selectedSkill.getSkill();
        List<String> lines = new ArrayList<>();

        lines.add(selectedSkill.baseName().getString());
        lines.add(selectedSkill.baseDescription().getString());
        if (selectedVariation != null) {
            //lines.add(String.valueOf(selectedVariation.getSkill().getColor().getRGB()));
            lines.add("\n");
            lines.add(selectedVariation.getSkill().description().getString());
        }
        lines.add("\n");
        //TODO add keywords
        modInfo.setInfo(lines, selectedSkill.icon());
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        String s = this.search.getText();
        SkillSelectionScreen.SortType sort = this.sortType;
        SkillListWidget.SkillEntry selected = this.selectedSkill;
        this.init(mc, width, height);
        this.search.setText(s);
        this.selectedSkill = selected;
        if (!this.search.getText().isEmpty())
            reloadMods();
        if (sort != SkillSelectionScreen.SortType.NORMAL)
            resortMods(sort);
        updateCache();
    }

    @Override
    public void closeScreen() {
        List<Skill> newSkills = new ArrayList<>();
        for (SkillSliceButton ssb : skillPie)
            newSkills.add(ssb.getSkill());
        for (PassiveButton pb : passives)
            newSkills.add(pb.getSkill());
        CasterData.getCap(Minecraft.getInstance().player).setEquippedSkills(newSkills);
        CombatChannel.INSTANCE.sendToServer(new UpdateSkillSelectionPacket(newSkills));
        this.minecraft.displayGuiScreen(null);
    }

    private enum SortType implements Comparator<Skill> {
        NORMAL,
        A_TO_Z {
            @Override
            protected int compare(String name1, String name2) { return name1.compareTo(name2); }
        },
        Z_TO_A {
            @Override
            protected int compare(String name1, String name2) { return name2.compareTo(name1); }
        };

        Button button;

        protected int compare(String name1, String name2) { return 0; }

        @Override
        public int compare(Skill o1, Skill o2) {
            String name1 = StringUtils.toLowerCase(stripControlCodes(o1.getDisplayName(null).getString()));
            String name2 = StringUtils.toLowerCase(stripControlCodes(o2.getDisplayName(null).getString()));
            return compare(name1, name2);
        }

        ITextComponent getButtonText() {
            return new TranslationTextComponent("fml.menu.mods." + StringUtils.toLowerCase(name()));
        }
    }

    class InfoPanel extends ScrollPanel {
        private ResourceLocation logoPath;
        private List<IReorderingProcessor> lines = Collections.emptyList();

        InfoPanel(Minecraft mcIn, int widthIn, int heightIn, int topIn) {
            super(mcIn, widthIn, heightIn, topIn, skillList.getRight() + PADDING);
        }

        void setInfo(List<String> lines, ResourceLocation logoPath) {
            this.logoPath = logoPath;
            this.lines = resizeContent(lines);
        }

        void clearInfo() {
            this.logoPath = null;
            this.lines = Collections.emptyList();
        }

        private List<IReorderingProcessor> resizeContent(List<String> lines) {
            List<IReorderingProcessor> ret = new ArrayList<>();
            for (String line : lines) {
                if (line == null) {
                    ret.add(null);
                    continue;
                }

                ITextComponent chat = ForgeHooks.newChatWithLinks(line, false);
                int maxTextLength = this.width - 12;
                if (maxTextLength >= 0) {
                    ret.addAll(LanguageMap.getInstance().func_244260_a(font.getCharacterManager().func_238362_b_(chat, maxTextLength, Style.EMPTY)));
                }
            }
            return ret;
        }

        @Override
        public int getContentHeight() {
            int height = 50;
            height += (lines.size() * font.FONT_HEIGHT);
            if (height < this.bottom - this.top - 8)
                height = this.bottom - this.top - 8;
            return height;
        }

        @Override
        protected int getScrollAmount() {
            return font.FONT_HEIGHT * 3;
        }

        @Override
        protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY) {
            if (logoPath != null) {
                Minecraft.getInstance().getTextureManager().bindTexture(logoPath);
                RenderSystem.enableBlend();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                // Draw the logo image inscribed in a rectangle with width entryWidth (minus some padding) and height 50
                int headerHeight = 50;
                GuiUtils.drawInscribedRect(mStack, left + PADDING, relativeY, width - (PADDING * 2), headerHeight, 64, 64, false, true);
                relativeY += headerHeight + PADDING;
            }

            for (IReorderingProcessor line : lines) {
                if (line != null) {
                    RenderSystem.enableBlend();
                    SkillSelectionScreen.this.font.drawTextWithShadow(mStack, line, left + PADDING, relativeY, 0xFFFFFF);
                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                }
                relativeY += font.FONT_HEIGHT;
            }

            final Style component = findTextLine(mouseX, mouseY);
            if (component != null) {
                SkillSelectionScreen.this.renderComponentHoverEffect(mStack, component, mouseX, mouseY);
            }
        }

        private Style findTextLine(final int mouseX, final int mouseY) {
            double offset = (mouseY - top) + border + scrollDistance + 1;
            if (logoPath != null) {
                offset -= 50;
            }
            if (offset <= 0)
                return null;

            int lineIdx = (int) (offset / font.FONT_HEIGHT);
            if (lineIdx >= lines.size() || lineIdx < 1)
                return null;

            IReorderingProcessor line = lines.get(lineIdx - 1);
            if (line != null) {
                return font.getCharacterManager().func_243239_a(line, mouseX);
            }
            return null;
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            final Style component = findTextLine((int) mouseX, (int) mouseY);
            if (component != null) {
                SkillSelectionScreen.this.handleComponentClicked(component);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        protected void drawBackground() {
        }
    }
}
