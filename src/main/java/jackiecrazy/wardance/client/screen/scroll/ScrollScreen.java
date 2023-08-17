package jackiecrazy.wardance.client.screen.scroll;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import jackiecrazy.wardance.client.screen.TooltipUtils;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScrollScreen extends Screen {
    private static final int PADDING = 6, SPACING = 20, MAX_WIDTH = 100;
    Skill selected;
    private InfoPanel[] panels = new InfoPanel[4];
    private Skill[] skills;

    public ScrollScreen(Skill... skills) {
        super(Component.translatable("wardance.scroll.title"));
        this.skills = skills;
    }

    @Override
    public void render(PoseStack mStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(mStack);
        super.render(mStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void init() {
        super.init();
        //todo add skill buttons that reflect their clicks onto the info panel
        final int length = skills.length;
        panels = new InfoPanel[length];
        //generate the width of each panel
        int panelWidth = Math.min(MAX_WIDTH, (width - (SPACING * 2) - (PADDING * 2)) / length - SPACING);
        //find the starting location
        int panelStart = (int) (width / 2 - (length / 2d) * (panelWidth + SPACING));
        for (int a = 0; a < length; a++) {
            panels[a] = new InfoPanel(Minecraft.getInstance(), panelWidth, height - SPACING * 2, SPACING, panelStart + (panelWidth + SPACING) * a, skills[a]);
        }
        for (InfoPanel ip : panels) {
            addRenderableWidget(ip);
        }
    }

    /*
    draw black rectangles with gray borders, add floaty stuff later
    draw skill icon
    render skill description
    if skill selection is set to random, change display information
    scroll further specs:
        allow choosing the skill to be received
        change 1 in NBT to accept an array of ids
        If the array length is 1, just give them the skill
        otherwise show this screen
     */

    class InfoPanel extends ScrollPanel {
        private ResourceLocation logoPath;
        private List<FormattedCharSequence> lines = Collections.emptyList();

        InfoPanel(Minecraft mcIn, int widthIn, int heightIn, int topIn, int left, Skill s) {
            super(mcIn, widthIn, heightIn, topIn, left, 4, 1);
            clearInfo();
            List<String> lines = new ArrayList<>();
            ResourceLocation icon = new ResourceLocation("wardance:textures/skill/random.png");
            if (s != null) {
                lines.add(ChatFormatting.BOLD + "" + ChatFormatting.UNDERLINE + s.getDisplayName(null).getString() + ChatFormatting.RESET + "\n");
                lines.add(s.description().getString());
                icon = s.icon();
            } else {
                lines.add(ChatFormatting.BOLD + "" + ChatFormatting.UNDERLINE + Component.translatable("wardance.scroll.random") + ChatFormatting.RESET + "\n");
                lines.add(Component.translatable("wardance.scroll.random.desc").getString());
            }
            lines.add("\n");
            setInfo(lines, icon);
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
            if (logoPath != null)
                height += 64;
            height += (lines.size() * font.lineHeight);
            if (height < this.bottom - this.top - 6) height = this.bottom - this.top - 6;
            return height;
        }

        @Override
        protected void drawBackground(PoseStack matrix, Tesselator tess, float partialTick) {
            //this.drawGradientRect(matrix, this.left+1, this.top+1, this.right-1, this.bottom-1, 0xC0101010, 0xC0101010);
            //super.drawBackground(matrix, tess, partialTick);
        }

        @Override
        protected void drawPanel(PoseStack mStack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
            RenderSystem.enableBlend();
            //fill(mStack, left , top, right, bottom, 0xffA9A9A9);
            //fill(mStack, left+ 1, top+1, right-1, bottom-1, -16777216);
            //fixme covers up the left tooltips
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
                    ScrollScreen.this.font.drawShadow(mStack, line, left + PADDING, relativeY, 0xFFFFFF);
                    //RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                }
                relativeY += font.lineHeight;
            }
        }

        @Override
        protected int getScrollAmount() {
            return font.lineHeight * 3;
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            final Style component = findTextLine((int) mouseX, (int) mouseY);
            if (component != null) {
                ScrollScreen.this.handleComponentClicked(component);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(PoseStack matrix, int mouseX, int mouseY, float partialTick) {
            super.render(matrix, mouseX, mouseY, partialTick);

            final Style component = findTextLine(mouseX, mouseY);
            if (component != null) {
                ScrollScreen.this.renderComponentHoverEffect(matrix, component, mouseX, mouseY);
            }
        }

        private Style findTextLine(final int mouseX, final int mouseY) {
            double offset = (mouseY - top) + border + scrollDistance + 1;
            int xoff = (mouseX - left) - border;
            if (logoPath != null) {
                offset -= 50;
            }
            if (offset <= 0 || xoff < 1) return null;

            int lineIdx = (int) (offset / font.lineHeight);
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
