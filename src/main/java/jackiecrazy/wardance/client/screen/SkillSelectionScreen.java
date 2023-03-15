package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateSkillSelectionPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.gui.ScreenUtils;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.loading.StringUtils;

import javax.annotation.Nonnull;
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

    private Button doneButton;
    private String lastFilterText = "";

    private EditBox search;

    private boolean sorted = false;
    private AdvancedData advancedData = AdvancedData.NORMAL;

    public SkillSelectionScreen() {
        super(Component.translatable("wardance.skillselection.title"));//
        this.bases = Skill.variationMap.keySet().stream().filter(this::selectable).collect(Collectors.toList());//hmm

        this.unsortedBases = Collections.unmodifiableList(this.bases);
    }

    private static String stripControlCodes(String value) {return net.minecraft.util.StringUtil.stripColor(value);}

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
        Survivor: taking fatal damage at max fury negates all damage and healing for 10 seconds, you are healed 20% for each mob killed in this time (100% for player/boss)
        Specialist: +40% effectiveness on all skills, 60% at max fury
        Berserker Blood: can only use red skills. Fury gain is massively improved, use at max to emit a damaging scream
    2 color styles: decent bonus
        Boulder Brace:
        Flame Dance:
        Wind Scar:
        Timberfall:
        Frost Fang: you know what we are
    3 color styles: mini playstyle tweak
        Dance of Destruction: each time you cast a skill, follow up with "stomp"; stomp will consume fury to strengthen itself
        Walk of Dionysus: fall down drunk when stunned, creating a shockwave and extending stun immunity after recovering. Active aoe stun
        Ippon-datara: max posture -50%, restore half of lost posture after landing from a jump
        Serenade of Pain: all damage -30%. projectile and melee damage leave marks that cause the other to instead deal +(fury*15)% damage by consuming said mark.
    4 color styles: disadvantage that can be exploited
        Unstable Spirit: each time you use a skill, create a non-griefing and indiscriminate explosion. Explosion damage reduced by 30% per bar of fury
        Blood Tax: all skills that cost might or spirit instead cost half the amount in health. Activate to trade health for fury
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
        doneButton = Button.builder(Component.translatable("gui.done"), b -> SkillSelectionScreen.this.onClose()).bounds(((listWidth + PADDING + this.width - doneButtonWidth) / 2), y, doneButtonWidth, 20).build();
        this.addRenderableWidget(doneButton);
        //y -= 20 + PADDING;

        //y -= 14 + PADDING + 1;
        search = new EditBox(getFontRenderer(), PADDING + 1, y, listWidth - 2, 14, Component.translatable("fml.menu.mods.search"));

        int fullButtonHeight = PADDING + 20 + PADDING;
        this.skillList = new SkillListWidget(this, listWidth, PADDING, search.getY() - getFontRenderer().lineHeight - PADDING);
        this.skillList.setLeftPos(PADDING);

        int split = (this.height - PADDING * 2 - fullButtonHeight) * 2 / 3;
        this.skillInfo = new InfoPanel(this.minecraft, infoWidth, split, PADDING);
        this.variationList = new VariationListWidget(this, infoWidth - 9, split + PADDING * 2, search.getY() - getFontRenderer().lineHeight - PADDING);
        this.variationList.setLeftPos(PADDING * 2 + listWidth);

        List<Skill> oldList = CasterData.getCap(Minecraft.getInstance().player).getEquippedSkills();
        for (int d = 0; d < skillPie.length; d++) {
            skillPie[d] = new SkillSliceButton(this, width - skillCircleWidth, PADDING / 2, skillCircleWidth, fixedU[d], fixedV[d], radial, d);
            skillPie[d].setSkill(oldList.get(d));
            addRenderableWidget(skillPie[d]);
        }

        for (int d = 0; d < passives.length; d++) {
            passives[d] = new PassiveButton(this, width - skillCircleWidth + d * (31), PADDING + skillCircleWidth, 23, d);
            passives[d].setSkill(oldList.get(d + skillPie.length));
            addRenderableWidget(passives[d]);
        }

        addRenderableWidget(search);
        addRenderableWidget(skillList);
        addRenderableWidget(variationList);
        addRenderableWidget(skillInfo);
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

    public <T extends ObjectSelectionList.Entry<T>> void buildSkillList(Consumer<T> modListViewConsumer, Function<SkillCategory, T> newEntry) {
        bases.forEach(mod -> modListViewConsumer.accept(newEntry.apply(mod)));
    }

    public <T extends ObjectSelectionList.Entry<T>> void buildVariationList(SkillCategory s, Consumer<T> modListViewConsumer, Function<Skill, T> newEntry) {
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
    public void render(PoseStack mStack, int mouseX, int mouseY, float partialTicks) {
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

        Component text = Component.translatable("fml.menu.mods.search");
        int x = skillList.getLeft() + ((skillList.getRight() - skillList.getLeft()) / 2) - (getFontRenderer().width(text) / 2);
        getFontRenderer().draw(mStack, text.getVisualOrderText(), x, search.getY() - getFontRenderer().lineHeight, 0xFFFFFF);
        this.search.render(mStack, mouseX, mouseY, partialTicks);
        super.render(mStack, mouseX, mouseY, partialTicks);
    }

    public Minecraft getMinecraftInstance() {
        return minecraft;
    }

    public Font getFontRenderer() {
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
            lines.add(Component.translatable("wardance:skills_general").getString() + "\n");
            lines.add(Component.translatable("wardance:skills_colors").getString() + "\n");
            lines.add(Component.translatable("wardance:skills_terms").getString() + "\n");
            skillInfo.setInfo(lines, null);
            return;
        }
        SkillCategory selectedSkill = this.selectedSkill.getCategory();
        List<String> lines = new ArrayList<>();

        lines.add(ChatFormatting.BOLD + "" + ChatFormatting.UNDERLINE + selectedSkill.name().getString() + ChatFormatting.RESET + "\n");
        lines.add(selectedSkill.description().getString());
        if (selectedVariation != null) {
            //lines.add(String.valueOf(selectedVariation.getSkill().getColor().getRGB()));
            lines.add("\n");
            lines.add(selectedVariation.getSkill().description().getString());
            if (minecraft != null && minecraft.options.renderDebug) {
                lines.add("\n");
                lines.add(ChatFormatting.DARK_GRAY + Component.translatable("wardance:skill_tag").getString() + selectedVariation.getSkill().getTags(minecraft.player) + "\n");
                lines.add(ChatFormatting.DARK_GRAY + Component.translatable("wardance:skill_soft_incompat").getString() + selectedVariation.getSkill().getSoftIncompatibility(minecraft.player) + "\n");
                lines.add(ChatFormatting.DARK_GRAY + Component.translatable("wardance:skill_hard_incompat").getString() + selectedVariation.getSkill().getHardIncompatibility(minecraft.player) + ChatFormatting.RESET);
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

        Component getButtonText() {
            return Component.translatable("fml.menu.mods." + StringUtils.toLowerCase(name()));
        }
    }

    class InfoPanel extends ScrollPanel {
        private ResourceLocation logoPath;
        private List<FormattedCharSequence> lines = Collections.emptyList();

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

        private List<FormattedCharSequence> resizeContent(List<String> lines) {
            List<FormattedCharSequence> ret = new ArrayList<>();
            for (String line : lines) {
                if (line == null) {
                    ret.add(null);
                    continue;
                }

                Component chat = ForgeHooks.newChatWithLinks(line, false);
                int maxTextLength = this.width - 12;
                if (maxTextLength >= 0) {
                    ret.addAll(Language.getInstance().getVisualOrder(font.getSplitter().splitLines(chat, maxTextLength, Style.EMPTY)));
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
        protected void drawPanel(PoseStack mStack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
            if (logoPath != null) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, logoPath);
                // Draw the logo image inscribed in a rectangle with width entryWidth (minus some padding) and height 50
                int headerHeight = 50;
                ScreenUtils.blitInscribed(mStack, left + PADDING, relativeY, width - (PADDING * 2), headerHeight, 64, 64, false, true);
                relativeY += headerHeight + PADDING;
            }

            for (FormattedCharSequence line : lines) {
                if (line != null) {
                    RenderSystem.enableBlend();
                    SkillSelectionScreen.this.font.drawShadow(mStack, line, left + PADDING, relativeY, 0xFFFFFF);
                    //RenderSystem.disableAlphaTest();
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

            FormattedCharSequence line = lines.get(lineIdx - 1);
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
