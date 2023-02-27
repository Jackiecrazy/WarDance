package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateSkillSelectionPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
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
            150, 0, 150, 300, 300
    };
    private static final int[] fixedV = {
            0, 150, 150, 150, 0
    };
    private static final int PADDING = 6;
    private final List<SkillCategory> unsortedBases;
    private final SkillSliceButton[] skillPie = new SkillSliceButton[5];
    private final PassiveButton[] passives = new PassiveButton[5];
    private final int numButtons = 2;//1 for tag toggling, 1 for stat toggling
    public SkillListWidget.CategoryEntry selectedSkill = null;
    public VariationListWidget.VariationEntry selectedVariation = null;
    private SkillListWidget skillList;
    private VariationListWidget variationList;
    private SkillSelectionScreen.InfoPanel skillInfo;
    private int listWidth;
    private List<SkillCategory> bases;
    private int buttonMargin = 1;
    private String lastFilterText = "";

    private TextFieldWidget search;

    private boolean sorted = false;
    private AdvancedData advancedData = AdvancedData.NORMAL;

    public SkillSelectionScreen() {
        super(new TranslationTextComponent("wardance.skillselection.title"));//
        this.bases = Skill.variationMap.keySet().stream().filter(this::selectable).collect(Collectors.toList());//hmm

        this.unsortedBases = Collections.unmodifiableList(this.bases);
    }

    private static String stripControlCodes(String value) {return net.minecraft.util.StringUtils.stripColor(value);}

    private boolean selectable(SkillCategory s) {
        for (Skill sub : Skill.variationMap.get(s))
            if (CasterData.getCap(Minecraft.getInstance().player).isSkillSelectable(sub)) return true;
        return false;
    }
    //private SkillSelectionScreen.SortType sortType = SkillSelectionScreen.SortType.NORMAL;
    /*
    when you first enter you are greeted with an identical screen with a single slot on the right for your stance/art/style.
    The center info screen gives a welcome message and basic instructions on how to pick a style, and the styles are listed on the left
    after you pick a style, the slot moves into the top right corner and the skill screen comes out.
    you can return to the style screen by clicking that slot again, but picking another style will void your current selection.
    rough filter of number of colors you can pick would be useful, as well as color-specific styles?
    (info before picking a style and changing styles should be different to reflect this)
    on the top left, a filter bar for colors. Selecting skills from a color puts that color at the forefront, and reaching your color cap will gray out all other colors
    this means each color needs some kind of distinguishable icon that's shaded over!
    extra tooltips on mousing over colors: name, short description
    red: dominance. You are mighty. Crush your foes.
    green: resolution. Never give in, be the last standing.
    gray: subterfuge. The deadliest strike is the one unseen.
    orange: relentlessness. Implacable, unstoppable. Win with style.
    cyan: perception. See all, reach all. None can escape your grasp.
    violet: decay. All shall be dust.
    1 color styles: major bonus
    Survivor: active to gain (might*2) absorption hearts
    Specialist: +40% effectiveness on all skills, 60% at max fury
    Dance of Destruction: each time you cast a skill, follow up with "stomp"; amplify stomp size at max fury
    Berserker Blood: can only use red skills. Fury gain is massively improved, use at max to emit a damaging scream
    2 color styles: decent bonus
    Boulder Brace, Flame Dance, Wind Scar, Timberfall, Frost Fang: you know what we are
    3 color styles: mini playstyle tweak
    Sapper: you cannot use conventional weapons, but have 1.2x skill effectiveness
    Nothing Personal: after using a skill, teleport behind the target relative to your current location
    Walk of Dionysus: fall down drunk when stunned, creating a shockwave and extending stun immunity after recovering. Fury is consumed to aoe stun at max
    Ippon-datara: max posture -50%, restore half of lost posture after landing from a jump
    Serenade of Pain: all damage -40%. projectile and melee damage leave marks that cause the other to deal +40% damage by consuming said mark. Each bar of fury reduces damage penalty
    4 color styles: disadvantage that can be exploited
    Unstable Spirit: each time you use a skill, create a non-griefing and indiscriminate explosion. Explosion damage reduced by 30% per bar of fury
    Blood Tax: all skills that cost might or spirit instead cost half the amount in health. Consume other resources to heal at max fury
    Gambler's Whimsy: only certain skills are castable. The selection increases with fury and rerolls after every cast
    5+ color styles: major disadvantage that has to be worked around
    Nature's Equilibrium: 5 colors, cannot use the same color twice in a row
    Sifu: any color, can only deal posture damage to non-staggered targets (special: instead of dying, mobs will run away, leaving drops and exp as usual)

    a skill list widget that lists all castable skills, composed of skill entries, on the very very left
    a skill entry is made from the main skill icon, the name, and a smaller assortment of icons representing available variations. They can be kind of generic across every skill
    clicking a skill highlights it and will call the main screen to display text down the middle to describe the skill
    A little panel at the bottom is reserved for displaying variation descriptions, to show the base and variation effects in a single screen.
    On the right is the skill octagon and four slots to click skills into
    these slots can be tinted blue if it is selected, grey if it's selectable, orange if it's not selectable, yellow if they share a parent skill, and red if the two skills are incompatible (such as breathing fire while chanting a spell).
    Upon closing, the octagon is sent to the server, double checked, and finalized.
    skill "effectiveness"/"proc coefficient" attribute that determines certain attributes
     */

    @Override
    public void init() {
        for (SkillCategory mod : bases) {
            listWidth = Math.max(listWidth, getFontRenderer().width(mod.name().getString()) + 20);
        }
        listWidth = Math.max(Math.min(listWidth, width / 5), 100);
        listWidth += listWidth % numButtons != 0 ? (numButtons - listWidth % numButtons) : 0;

        int skillCircleWidth = 150;
        int infoWidth = this.width - this.listWidth - skillCircleWidth - (PADDING * 4);
        int doneButtonWidth = Math.min(infoWidth, 200);
        int y = this.height - 20 - PADDING;
        this.addButton(new Button(((listWidth + PADDING + this.width - doneButtonWidth) / 2), y, doneButtonWidth, 20,
                new TranslationTextComponent("gui.done"), b -> SkillSelectionScreen.this.onClose()));
        //y -= 20 + PADDING;

        //y -= 14 + PADDING + 1;
        search = new TextFieldWidget(getFontRenderer(), PADDING + 1, y, listWidth - 2, 14, new TranslationTextComponent("fml.menu.mods.search"));

        int fullButtonHeight = PADDING + 20 + PADDING;
        this.skillList = new SkillListWidget(this, listWidth, PADDING, search.y - getFontRenderer().lineHeight - PADDING);
        this.skillList.setLeftPos(PADDING);

        int split = (this.height - PADDING * 2 - fullButtonHeight) * 2 / 3;
        this.skillInfo = new InfoPanel(this.minecraft, infoWidth, split, PADDING);
        this.variationList = new VariationListWidget(this, infoWidth - 9, split + PADDING * 2, search.y - getFontRenderer().lineHeight - PADDING);
        this.variationList.setLeftPos(PADDING * 2 + listWidth);

        List<Skill> oldList = CasterData.getCap(Minecraft.getInstance().player).getEquippedSkills();
        for (int d = 0; d < skillPie.length; d++) {
            skillPie[d] = new SkillSliceButton(this, width - skillCircleWidth, PADDING / 2, skillCircleWidth, fixedU[d], fixedV[d], radial, d);
            skillPie[d].setSkill(oldList.get(d));
            children.add(skillPie[d]);
        }

        for (int d = 0; d < passives.length; d++) {
            passives[d] = new PassiveButton(this, width - skillCircleWidth + d * (31), PADDING + skillCircleWidth, 23, d);
            passives[d].setSkill(oldList.get(d + skillPie.length));
            children.add(passives[d]);
        }

        children.add(search);
        children.add(skillList);
        children.add(variationList);
        children.add(skillInfo);
        search.setFocus(false);
        search.setCanLoseFocus(true);

        //final int width = listWidth / numButtons;
//        int x = PADDING;
//        addButton(AdvancedData.NORMAL.button = new Button(x, PADDING, width - buttonMargin, 20, AdvancedData.NORMAL.getButtonText(), b -> resortMods(AdvancedData.NORMAL)));
//        x += width + buttonMargin;
//        addButton(AdvancedData.A_TO_Z.button = new Button(x, PADDING, width - buttonMargin, 20, AdvancedData.A_TO_Z.getButtonText(), b -> resortMods(AdvancedData.A_TO_Z)));
//        x += width + buttonMargin;
//        addButton(AdvancedData.Z_TO_A.button = new Button(x, PADDING, width - buttonMargin, 20, AdvancedData.Z_TO_A.getButtonText(), b -> resortMods(AdvancedData.Z_TO_A)));
        resortMods(AdvancedData.NORMAL);
        updateCache();
        setInitialFocus(skillList);
    }

    @Override
    public void tick() {
        search.tick();
        skillList.setSelected(selectedSkill);
        variationList.setSelected(selectedVariation);

        if (!search.getValue().equals(lastFilterText)) {
            reloadMods();
            sorted = false;
        }

        if (!sorted) {
            reloadMods();
            bases.sort(advancedData);
            skillList.refreshList();
            variationList.refreshList();
            if (selectedSkill != null) {
                selectedSkill = skillList.children().stream().filter(e -> e.getCategory() == selectedSkill.getCategory()).findFirst().orElse(null);
                updateCache();
            }
            sorted = true;
        }
    }

    public <T extends ExtendedList.AbstractListEntry<T>> void buildSkillList(Consumer<T> modListViewConsumer, Function<SkillCategory, T> newEntry) {
        bases.forEach(mod -> modListViewConsumer.accept(newEntry.apply(mod)));
    }

    public <T extends ExtendedList.AbstractListEntry<T>> void buildVariationList(SkillCategory s, Consumer<T> modListViewConsumer, Function<Skill, T> newEntry) {
        Skill.variationMap.get(s).forEach(mod -> {
            if (CasterData.getCap(Minecraft.getInstance().player).isSkillSelectable(mod))
                modListViewConsumer.accept(newEntry.apply(mod));
        });
    }

    private void reloadMods() {
        this.bases = this.unsortedBases.stream().
                filter(mi -> StringUtils.toLowerCase(stripControlCodes(mi.name().getString())).contains(StringUtils.toLowerCase(search.getValue()))).collect(Collectors.toList());
        lastFilterText = search.getValue();
    }

    boolean isValidInsertion(Skill insert) {
        for (SkillSelectionButton ssb : skillPie)
            if (ssb.getSkill() != null && ssb.getSkill().isFamily(insert)) return false;
        for (SkillSelectionButton ssb : passives)
            if (ssb.getSkill() != null && ssb.getSkill().isFamily(insert)) return false;
        return CasterData.getCap(Minecraft.getInstance().player).isSkillSelectable(insert);
    }

    private void resortMods(AdvancedData newSort) {
        this.advancedData = newSort;

        for (AdvancedData sort : AdvancedData.values()) {
            if (sort.button != null)
                sort.button.active = advancedData != sort;
        }
        sorted = false;
    }

    @Override
    public void render(MatrixStack mStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(mStack);
        this.skillList.render(mStack, mouseX, mouseY, partialTicks);
        this.variationList.render(mStack, mouseX, mouseY, partialTicks);
        if (this.skillInfo != null) {
            this.skillInfo.render(mStack, mouseX, mouseY, partialTicks);
            RenderSystem.disableScissor();
        }
        for (SkillSliceButton ssb : skillPie) {
            ssb.render(mStack, mouseX, mouseY, partialTicks);
        }
        for (PassiveButton pb : passives) {
            pb.render(mStack, mouseX, mouseY, partialTicks);
        }

        ITextComponent text = new TranslationTextComponent("fml.menu.mods.search");
        int x = skillList.getLeft() + ((skillList.getRight() - skillList.getLeft()) / 2) - (getFontRenderer().width(text) / 2);
        getFontRenderer().draw(mStack, text.getVisualOrderText(), x, search.y - getFontRenderer().lineHeight, 0xFFFFFF);
        this.search.render(mStack, mouseX, mouseY, partialTicks);
        super.render(mStack, mouseX, mouseY, partialTicks);
    }

    public Minecraft getMinecraftInstance() {
        return minecraft;
    }

    public FontRenderer getFontRenderer() {
        return font;
    }

    public void setSelectedSkill(SkillListWidget.CategoryEntry entry) {
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
            this.skillInfo.clearInfo();
            List<String> lines = new ArrayList<>();
            lines.add(new TranslationTextComponent("wardance:skills_general").getString() + "\n");
            lines.add(new TranslationTextComponent("wardance:skills_colors").getString() + "\n");
            lines.add(new TranslationTextComponent("wardance:skills_terms").getString() + "\n");
            skillInfo.setInfo(lines, null);
            return;
        }
        SkillCategory selectedSkill = this.selectedSkill.getCategory();
        List<String> lines = new ArrayList<>();

        lines.add(TextFormatting.BOLD+""+TextFormatting.UNDERLINE+selectedSkill.name().getString()+TextFormatting.RESET+"\n");
        lines.add(selectedSkill.description().getString());
        if (selectedVariation != null) {
            //lines.add(String.valueOf(selectedVariation.getSkill().getColor().getRGB()));
            lines.add("\n");
            lines.add(selectedVariation.getSkill().description().getString());
            if (minecraft != null && minecraft.options.renderDebug) {
                lines.add("\n");
                lines.add(TextFormatting.DARK_GRAY+new TranslationTextComponent("wardance:skill_tag").getString() + selectedVariation.getSkill().getTags(minecraft.player).getValues()+"\n");
                lines.add(TextFormatting.DARK_GRAY+new TranslationTextComponent("wardance:skill_soft_incompat").getString() + selectedVariation.getSkill().getSoftIncompatibility(minecraft.player).getValues()+"\n");
                lines.add(TextFormatting.DARK_GRAY+new TranslationTextComponent("wardance:skill_hard_incompat").getString() + selectedVariation.getSkill().getHardIncompatibility(minecraft.player).getValues()+TextFormatting.RESET);
            }
        }
        lines.add("\n");
        skillInfo.setInfo(lines, null);
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        String s = this.search.getValue();
        AdvancedData sort = this.advancedData;
        SkillListWidget.CategoryEntry selected = this.selectedSkill;
        this.init(mc, width, height);
        this.search.setValue(s);
        this.selectedSkill = selected;
        if (!this.search.getValue().isEmpty())
            reloadMods();
        if (sort != AdvancedData.NORMAL)
            resortMods(sort);
        updateCache();
    }

    @Override
    public void onClose() {
        List<Skill> newSkills = new ArrayList<>();
        for (SkillSliceButton ssb : skillPie)
            newSkills.add(ssb.getSkill());
        for (PassiveButton pb : passives)
            newSkills.add(pb.getSkill());
        CasterData.getCap(Minecraft.getInstance().player).setEquippedSkills(newSkills);
        CombatChannel.INSTANCE.sendToServer(new UpdateSkillSelectionPacket(newSkills));
        this.minecraft.setScreen(null);
    }

    private enum AdvancedData implements Comparator<SkillCategory> {
        NORMAL,
        A_TO_Z {
            @Override
            protected int compare(String name1, String name2) {return name1.compareTo(name2);}
        },
        Z_TO_A {
            @Override
            protected int compare(String name1, String name2) {return name2.compareTo(name1);}
        };

        Button button;

        protected int compare(String name1, String name2) {return 0;}

        @Override
        public int compare(SkillCategory o1, SkillCategory o2) {
            String name1 = StringUtils.toLowerCase(stripControlCodes(o1.name().getString()));
            String name2 = StringUtils.toLowerCase(stripControlCodes(o2.name().getString()));
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
            scrollDistance = 0;
        }

        void clearInfo() {
            this.logoPath = null;
            this.lines = Collections.emptyList();
            scrollDistance = 0;
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
                    ret.addAll(LanguageMap.getInstance().getVisualOrder(font.getSplitter().splitLines(chat, maxTextLength, Style.EMPTY)));
                }
            }
            return ret;
        }

        @Override
        public int getContentHeight() {
            int height = 0;
            height += (lines.size() * font.lineHeight);
            if (height < this.bottom - this.top - 8)
                height = this.bottom - this.top - 8;
            return height;
        }

        @Override
        protected int getScrollAmount() {
            return font.lineHeight * 3;
        }

        @Override
        protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY) {
            if (logoPath != null) {
                Minecraft.getInstance().getTextureManager().bind(logoPath);
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
                    SkillSelectionScreen.this.font.drawShadow(mStack, line, left + PADDING, relativeY, 0xFFFFFF);
                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                }
                relativeY += font.lineHeight;
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

            int lineIdx = (int) (offset / font.lineHeight);
            if (lineIdx >= lines.size() || lineIdx < 1)
                return null;

            IReorderingProcessor line = lines.get(lineIdx - 1);
            if (line != null) {
                return font.getSplitter().componentStyleAtWidth(line, mouseX);
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
            //this.drawGradientRect(, this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);

        }
    }
}
