package jackiecrazy.wardance.client.screen.skill;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.client.screen.TooltipUtils;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateSkillSelectionPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillArchetype;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.gui.ScreenUtils;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import net.minecraftforge.fml.loading.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SkillSelectionScreen extends Screen {
    public static final int SKILL_CIRCLE_WIDTH = 150;
    protected static final int PADDING = 6;
    private static final ResourceLocation radial = new ResourceLocation(WarDance.MODID, "textures/skill/radialhud.png");
    private static final int[] fixedU = {150, 0, 150, 300, 300};
    private static final int[] fixedV = {0, 150, 150, 150, 0};
    protected final SkillSliceButton[] skillPie = new SkillSliceButton[5];
    protected final PassiveButton[] passives = new PassiveButton[5];
    private final List<Skill> unsortedSkills;
    private final List<Skill> unsortedStyles;
    private final int numButtons = Skill.categoryMap.size();
    private final Comparator<SkillCategorySort> CATEGORYSORT = Comparator.comparing(o -> StringUtils.toLowerCase(stripControlCodes(o.getText().getString())));
    private final Comparator<Skill> SKILLSORT = Comparator.comparing(o -> StringUtils.toLowerCase(stripControlCodes(o.getDisplayName(null).getString())));
    private final Comparator<SkillStyle> STYLESORT = new Comparator<SkillStyle>() {
        @Override
        public int compare(SkillStyle o1, SkillStyle o2) {
            //styles with more colors overall get pushed to the bottom
            if (o1.getMaxColorsForSorting() - o2.getMaxColorsForSorting() != 0)
                return o1.getMaxColorsForSorting() - o2.getMaxColorsForSorting();
            //styles with specific color requirements are put at the top. The more specific the requirements, the higher they rank
            if (o1.getMaxColors() - o2.getMaxColors() != 0)
                return o1.getMaxColors() - o2.getMaxColors();
            //alphabet
            return SKILLSORT.compare(o1, o2);
        }
    };
    public SkillListWidget.CategoryEntry selectedSkill = null;
    protected List<Skill> skillCache;
    //public VariationListWidget.VariationEntry selectedVariation = null;
    protected SkillListWidget skillList;
    //private VariationListWidget variationList;
    protected SkillSelectionScreen.InfoPanel skillInfo;
    protected int listWidth;
    protected List<Skill> bases;
    protected List<SkillStyle> stylebases;
    protected int buttonMargin = 4;
    protected Button doneButton;
    protected String lastFilterText = "";
    protected EditBox search;
    protected boolean filtered = false;
    protected ArrayList<SkillCategorySort> filters = new ArrayList<>();
    protected SkillCategory displayedCategory = SkillColors.none;
    protected SkillStyleButton style;
    boolean refresh = false;
    int update = 0;
    private boolean cacheDirty;

    public SkillSelectionScreen() {
        //initialize all skills
        super(Component.translatable("wardance.skillselection.title"));
        bases = new ArrayList<>();
        for (List<Skill> list : Skill.categoryMap.values()) {
            this.bases.addAll(list);
        }
        bases.sort(SKILLSORT);
        stylebases = new ArrayList<>();
        stylebases.addAll(SkillStyle.styleList);
        stylebases.sort(STYLESORT);

        this.unsortedSkills = Collections.unmodifiableList(this.bases);
        this.unsortedStyles = Collections.unmodifiableList(this.bases);
    }

    private static String stripControlCodes(String value) {return net.minecraft.util.StringUtil.stripColor(value);}

    private boolean selectable(SkillArchetype s) {
        for (Skill sub : Skill.variationMap.get(s))
            if (CasterData.getCap(Minecraft.getInstance().player).isSkillSelectable(sub)) return true;
        return false;
    }

    public <T extends ObjectSelectionList.Entry<T>> void buildSkillList(Consumer<T> modListViewConsumer, Function<Skill, T> newEntry) {
        if (style.getSkill() == null) stylebases.forEach(mod -> {
            if (CasterData.getCap(Minecraft.getInstance().player).isSkillSelectable(mod))
                modListViewConsumer.accept(newEntry.apply(mod));
        });
        else bases.forEach(mod -> {
            if (CasterData.getCap(Minecraft.getInstance().player).isSkillSelectable(mod))
                modListViewConsumer.accept(newEntry.apply(mod));
        });
    }

//    public <T extends ObjectSelectionList.Entry<T>> void buildVariationList(SkillCategory s, Consumer<T> modListViewConsumer, Function<Skill, T> newEntry) {
//        Skill.colorMap.get(s).forEach(mod -> {
//            if (CasterData.getCap(Minecraft.getInstance().player).isSkillSelectable(mod))
//                modListViewConsumer.accept(newEntry.apply(mod));
//        });
//    }

    private void reloadSkills() {//
        this.bases = this.unsortedSkills.stream().filter(mi -> (mi.getCategory() == displayedCategory || displayedCategory == SkillColors.none) && StringUtils.toLowerCase(stripControlCodes(mi.getDisplayName(null).getString())).contains(StringUtils.toLowerCase(search.getValue()))).collect(Collectors.toList());
        lastFilterText = search.getValue();
    }

    boolean contains(Skill query) {
        for (SkillSelectionButton ssb : skillPie)
            if (ssb.getSkill() == query)
                return true;
        for (SkillSelectionButton ssb : passives)
            if (ssb.getSkill() == query)
                return true;
        return false;
    }

    boolean isValidInsertion(Skill insert) {
        final LocalPlayer player = Minecraft.getInstance().player;
        //cannot insert any skill without a style
        if (style.getStyle() == null && !(insert instanceof SkillStyle)) return false;
        //cannot insert skills contrary to style
        if (style.getSkill() != null && !style.getStyle().isEquippableWith(insert, collectSkills())) return false;
//        if (style.getStyle() != null && getNumColors().size() > style.getStyle().getMaxColors())
//            return false;
        //current active conflict
        for (SkillSelectionButton ssb : skillPie)
            if (ssb.getSkill() != null && (!ssb.getSkill().isEquippableWith(insert, player)))
                return false;
        //current passive conflict
        for (SkillSelectionButton ssb : passives)
            if (ssb.getSkill() != null && (!ssb.getSkill().isEquippableWith(insert, player)))
                return false;
        return true;
    }

    void filterSkills(SkillCategory newSort) {
        this.displayedCategory = newSort;

        for (SkillCategorySort sort : filters) {
            if (sort.button != null) sort.button.active = displayedCategory != sort.cat;
        }
        filtered = false;
    }

    @Override
    public void render(PoseStack mStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(mStack);
        renderSearchText(mStack);
        super.render(mStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        List<Skill> newSkills = collectSkills();
        CasterData.getCap(Minecraft.getInstance().player).setStyle(style.getStyle());
        CasterData.getCap(Minecraft.getInstance().player).setEquippedSkills(newSkills);
        CombatChannel.INSTANCE.sendToServer(new UpdateSkillSelectionPacket(style.getStyle(), newSkills));
        this.minecraft.setScreen(null);
    }

    @Override
    public void init() {
        filters.clear();
        final ISkillCapability cap = CasterData.getCap(Minecraft.getInstance().player);

        //style
        if (style == null) {
            style = new SkillStyleButton(this, width - SKILL_CIRCLE_WIDTH / 2 - 12, PADDING + SKILL_CIRCLE_WIDTH / 2 - 12, 23);
            style.setSkill(cap.getStyle());
        }
        //if no style, halt some developments
        final boolean noStyle = style.getSkill() == null;
        int noStyleOffset = 0;
        if (noStyle) {
            style.x = width - SKILL_CIRCLE_WIDTH / 2 - 12;
            style.y = PADDING + SKILL_CIRCLE_WIDTH / 2 - 12;
            noStyleOffset = width;
        } else {
            style.x = width - style.getWidth() - PADDING;
            style.y = height - style.getHeight() - PADDING;
        }

        //filter buttons
        for (SkillCategory sc : Skill.categoryMap.keySet()) {
            if (sc != SkillColors.none && sc != SkillColors.white) filters.add(new SkillCategorySort(sc));
        }
        filters.sort(CATEGORYSORT);
        filters.add(0, new SkillCategorySort(SkillColors.white));
        filters.add(0, new SkillCategorySort(SkillColors.none));

        List<? extends Skill> reference = noStyle ? stylebases : bases;

        //create skill list
        for (Skill mod : reference) {
            listWidth = Math.max(listWidth, getFontRenderer().width(mod.getDisplayName(null).getString()) + 20);
        }
        listWidth = Math.max(Math.min(listWidth, width / 5), 100);
        listWidth += listWidth % numButtons != 0 ? (numButtons - listWidth % numButtons) : 0;
        int infoWidth = this.width - this.listWidth - SKILL_CIRCLE_WIDTH - (PADDING * 4);

        //done button
        int doneButtonWidth = Math.min(infoWidth, 200);
        int y = this.height - 20 - PADDING;
        doneButton = new Button(((listWidth + PADDING + this.width - doneButtonWidth) / 2), y, doneButtonWidth, 20, Component.translatable("gui.done"), b -> SkillSelectionScreen.this.onClose());

        //search bar
        search = new EditBox(getFontRenderer(), PADDING + 1, y, listWidth - 2, 14, Component.translatable("fml.menu.mods.search"));

        //skill list
        int fullButtonHeight = PADDING + 16 + PADDING + 16 + PADDING;
        this.skillList = new SkillListWidget(this, listWidth, fullButtonHeight, search.y - getFontRenderer().lineHeight - PADDING);
        this.skillList.setLeftPos(PADDING);

        //skill info
        int split = (this.height - (PADDING) * 2 - 20);
        this.skillInfo = new InfoPanel(this.minecraft, infoWidth, split, skillList.getRight() + PADDING, PADDING);
//        this.variationList = new VariationListWidget(this, infoWidth - 9, split + PADDING * 2, search.y - getFontRenderer().lineHeight - PADDING);
//        this.variationList.setLeftPos(PADDING * 2 + listWidth);

        //currently equipped skills
        List<Skill> oldList = skillCache == null ? cap.getEquippedSkills() : skillCache;
        for (int d = 0; d < skillPie.length; d++) {
            skillPie[d] = new SkillSliceButton(this, width - SKILL_CIRCLE_WIDTH + noStyleOffset-PADDING, PADDING / 2, SKILL_CIRCLE_WIDTH, fixedU[d], fixedV[d], radial, d);
            if (!noStyle)
                skillPie[d].setSkill(oldList.get(d));
        }

        for (int d = 0; d < passives.length; d++) {
            passives[d] = new PassiveButton(this, width - SKILL_CIRCLE_WIDTH + d * (31) + noStyleOffset-PADDING, PADDING + SKILL_CIRCLE_WIDTH, 23, d);
            if (!noStyle)
                passives[d].setSkill(oldList.get(d + skillPie.length));
        }


        final int width = Math.min(2 * listWidth / numButtons, 16);
        int x = PADDING;

        //add filter buttons
        boolean firstRow = true;
        for (SkillCategorySort scc : filters) {
            ArrayList<FormattedCharSequence> display = new ArrayList<>();
            display.addAll(this.minecraft.font.split(scc.cat.name(), Math.max(this.width / 2 - 43, 170)));
            display.addAll(this.minecraft.font.split(scc.cat.description(), Math.max(this.width / 2 - 43, 170)));
            scc.button = new SkillCategoryButton(this, scc, display, x, PADDING + (firstRow ? 0 : 16 + PADDING), width, 16, scc.cat.icon());

            if (!firstRow) {
                //first two down, draw a little line
                if (x == PADDING) {
                    x += PADDING;
                }
                x += width + buttonMargin;
            }
            firstRow = !firstRow;
        }
        filterSkills(SkillColors.none);
        addWidgets();
        updateCache();
        setInitialFocus(skillList);
    }

    @Override
    public void tick() {
        search.tick();
        skillList.setSelected(selectedSkill);
        //variationList.setSelected(selectedVariation);
        if (refresh) {
            String s = this.search.getValue();
            SkillCategory sort = this.displayedCategory;
            SkillListWidget.CategoryEntry selected = this.selectedSkill;
            //store current selection before reset
            skillCache = collectSkills();
            this.init(getMinecraftInstance(), width, height);
            this.search.setValue(s);
            this.selectedSkill = selected;
            if (!this.search.getValue().isEmpty()) reloadSkills();
            if (sort != SkillColors.none) filterSkills(sort);
            updateCache();
            refresh = false;
        }

        if (!search.getValue().equals(lastFilterText)) {
            reloadSkills();
            filtered = false;
        }

        if (!filtered) {
            reloadSkills();
            //bases.sort(advancedData);
            skillList.refreshList();
            //variationList.refreshList();
            if (selectedSkill != null) {
                selectedSkill = skillList.children().stream().filter(e -> e.getSkill() == selectedSkill.getSkill()).findFirst().orElse(null);
                updateCache();
            }
            filtered = true;
        }
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        String s = this.search.getValue();
        SkillCategory sort = this.displayedCategory;
        SkillListWidget.CategoryEntry selected = this.selectedSkill;
        skillCache = collectSkills();
        this.init(mc, width, height);
        this.search.setValue(s);
        this.selectedSkill = selected;
        if (!this.search.getValue().isEmpty()) reloadSkills();
        if (sort != SkillColors.none) filterSkills(sort);
        updateCache();
    }

    protected void renderSearchText(PoseStack mStack) {
        Component text = Component.translatable("fml.menu.mods.search");
        int x = skillList.getLeft() + ((skillList.getRight() - skillList.getLeft()) / 2) - (getFontRenderer().width(text) / 2);
        getFontRenderer().draw(mStack, text.getVisualOrderText(), x, search.y - getFontRenderer().lineHeight, 0xFFFFFF);
    }

    protected void addWidgets() {
        //use a builder in 1.19.3
        this.addRenderableWidget(doneButton);
        for (SkillSliceButton skillSliceButton : skillPie) {
            addRenderableWidget(skillSliceButton);
        }
        for (PassiveButton passive : passives) {
            addRenderableWidget(passive);
        }
        //add everything!
        addRenderableWidget(skillList);
        //addRenderableWidget(variationList);
        addRenderableWidget(search);
        search.setFocus(false);
        search.setCanLoseFocus(true);
        addRenderableWidget(skillInfo);
        addRenderableWidget(style);
        for (SkillCategorySort scc : filters)
            addRenderableWidget(scc.button);
    }

    public List<Skill> collectSkills() {
        List<Skill> newSkills = new ArrayList<>();
        for (SkillSliceButton ssb : skillPie)
            newSkills.add(ssb.getSkill());
        for (PassiveButton pb : passives)
            newSkills.add(pb.getSkill());
        return newSkills;
    }

    public Minecraft getMinecraftInstance() {
        return minecraft;
    }

    public Font getFontRenderer() {
        return font;
    }

    public void setSelectedSkill(SkillListWidget.CategoryEntry entry) {
        this.selectedSkill = entry == this.selectedSkill ? null : entry;
//        selectedVariation = null;
//        variationList.refreshList();
        updateCache();
    }

    List<SkillCategory> getNumColors() {
        ArrayList<SkillCategory> colors = new ArrayList<>();
        for (SkillSelectionButton a : passives) {
            if (a.getSkill() != null && !colors.contains(a.getSkill().getCategory())) {
                colors.add(a.getSkill().getCategory());
            }
        }
        for (SkillSelectionButton a : skillPie) {
            if (a.getSkill() != null && !colors.contains(a.getSkill().getCategory())) {
                colors.add(a.getSkill().getCategory());
            }
        }
        if (selectedSkill != null && !colors.contains(selectedSkill.getSkill().getCategory()))
            colors.add(selectedSkill.getSkill().getCategory());
        return colors;
    }

    protected void updateCache() {
        if (style.getStyle() == null && selectedSkill == null) {
            this.skillInfo.clearInfo();
            List<String> lines = new ArrayList<>();
            lines.add(Component.translatable("wardance.skills_style").getString() + "\n");
            skillInfo.setInfo(lines, null);
            return;
        }
        if (selectedSkill == null) {
            this.skillInfo.clearInfo();
            List<String> lines = new ArrayList<>();
            lines.add(Component.translatable("wardance.skills_general").getString() + "\n");
            skillInfo.setInfo(lines, null);
            return;
        }
        Skill selectedSkill = this.selectedSkill.getSkill();
        displaySkillInfo(selectedSkill);
    }

    protected void displaySkillInfo(Skill s) {
        skillInfo.clearInfo();
        List<String> lines = new ArrayList<>();

        lines.add(ChatFormatting.BOLD + "" + ChatFormatting.UNDERLINE + s.getDisplayName(null).getString() + ChatFormatting.RESET + "\n");
        lines.add(s.description().getString());
        lines.add("\n");
        skillInfo.setInfo(lines, null);
    }

    protected static class SkillCategorySort {
        protected SkillCategory cat;
        protected Button button;

        SkillCategorySort(SkillCategory sc) {
            cat = sc;
        }

        public Component getText() {
            return cat.name();
        }

    }

    protected class InfoPanel extends ScrollPanel {
        static final Pattern TOOLTIP_PATTERN = Pattern.compile(
                "\\{[^}]*\\}", Pattern.CASE_INSENSITIVE);
        private ResourceLocation logoPath;
        private List<FormattedCharSequence> lines = Collections.emptyList();

        public InfoPanel(Minecraft mcIn, int widthIn, int heightIn, int left, int topIn) {
            super(mcIn, widthIn, heightIn, topIn, left);
        }

        public void setInfo(List<String> lines, ResourceLocation logoPath) {
            this.logoPath = logoPath;
            this.lines = resizeContent(lines);
            scrollDistance = 0;
        }

        public void setInfoRaw(List<FormattedCharSequence> seq) {
            lines = seq;
        }

        public void clearInfo() {
            this.logoPath = null;
            this.lines = Collections.emptyList();
            scrollDistance = 0;
        }

        private List<FormattedCharSequence> resizeContent(List<String> lines) {
            List<FormattedCharSequence> ret = new ArrayList<>();
            for (String line : lines) {
                if (line == null) {
                    ret.add(null);
                    continue;
                }

                Component chat = TooltipUtils.tooltipText(line);
                int maxTextLength = this.width - 12;
                if (maxTextLength >= 0) {
                    ret.addAll(Language.getInstance().getVisualOrder(font.getSplitter().splitLines(chat, maxTextLength, chat.getStyle())));
                }
            }
            return ret;
        }

        @Override
        public int getContentHeight() {
            int height = 0;
            height += (lines.size() * (font.lineHeight+2));
            if (height < this.bottom - this.top - 8) height = this.bottom - this.top - 8;
            return height;
        }

        @Override
        protected void drawBackground(PoseStack matrix, Tesselator tess, float partialTick) {
            fill(matrix, this.left, this.top, this.right, this.bottom, 0xFFA0A0A0);
            fill(matrix, left+1, top+1, right-1, bottom-1, 0xFF000000);
            //super.drawBackground(matrix, tess, partialTick);
        }

        @Override
        protected void drawPanel(PoseStack mStack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
            if (logoPath != null) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, logoPath);
                // Draw the logo image inscribed in a rectangle with width entryWidth (minus some padding) and height 50
                int headerHeight = 50;
                ScreenUtils.blitInscribed(mStack, left + width / 2 - 32, relativeY, width - (PADDING * 2), headerHeight, 64, 64, false, true);
                relativeY += headerHeight + PADDING;
            }

            for (FormattedCharSequence line : lines) {
                if (line != null) {
                    RenderSystem.enableBlend();
                    SkillSelectionScreen.this.font.drawShadow(mStack, line, left + PADDING, relativeY, 0xFFFFFF);
                    //RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                }
                relativeY += font.lineHeight + 2;
            }
        }

        @Override
        protected int getScrollAmount() {
            return (font.lineHeight+2) * 3;
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
        public void render(PoseStack matrix, int mouseX, int mouseY, float partialTick) {
            super.render(matrix, mouseX, mouseY, partialTick);

            final Style component = findTextLine(mouseX, mouseY);
            if (component != null) {
                SkillSelectionScreen.this.renderComponentHoverEffect(matrix, component, mouseX, mouseY);
            }
        }

        private Style findTextLine(final int mouseX, final int mouseY) {
            double offset = (mouseY - top) + border + scrollDistance + 1;
            int xoff = (mouseX - left) - border;
            if (logoPath != null) {
                offset -= 50;
            }
            if (offset <= 0 || xoff < 1) return null;

            int lineIdx = (int) (offset / (font.lineHeight+2));
            if (lineIdx >= lines.size() || lineIdx < 1)
                return null;

            FormattedCharSequence line = lines.get(lineIdx - 1);
            if (line != null) {
                return font.getSplitter().componentStyleAtWidth(line, xoff);
            }
            return null;
        }

        @Nonnull
        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(@Nonnull NarrationElementOutput p_169152_) {
        }
    }
}
