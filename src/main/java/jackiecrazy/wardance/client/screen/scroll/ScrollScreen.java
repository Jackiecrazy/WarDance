package jackiecrazy.wardance.client.screen.scroll;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import jackiecrazy.wardance.client.screen.TooltipUtils;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.LearnScrollPacket;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.ScreenUtils;
import net.minecraftforge.client.gui.widget.ScrollPanel;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScrollScreen extends Screen {
    private static final int PADDING = 6, SPACING = 20, MAX_WIDTH = 100;
    Skill selected;
    private InfoPanel panel;
    private Skill[] skills;
    private SkillButton[] buttons;
    private Button okay, cancel;
    private boolean offhand;

    public ScrollScreen(boolean off, Skill... skills) {
        super(Component.translatable("wardance.scroll.title"));
        this.skills = skills;
        offhand = off;
    }

    @Override
    public void render(PoseStack mStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(mStack);
        super.render(mStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void init() {
        super.init();
        renderables.clear();
        final int length = skills.length;
        final int workingWidth = width - (SPACING * 2) - (PADDING * 2);
        //new line if too many
        int rows = 1;
        int size = 0;
        int perRow = 0;
        int fitsIn;
        while (size < 16 && rows < 10) {
            perRow = Mth.ceil(length / (float) rows);
            fitsIn = workingWidth / perRow;
            size = 2;
            while (size < fitsIn)
                size *= 2;
            //size=Math.min(64, size);
            //don't get too big
            int otherSize = Math.min(width, height) / (2 * rows);
            while (otherSize < Math.min(width, height) / (2 * rows))
                otherSize *= 2;
            size = Math.min(otherSize / 2, size / 2);
            rows++;
        }
        rows--;

        buttons = new SkillButton[length];
        for (int x = 0; x < length; x++) {
            int onRow = Mth.floor((float) x / perRow);
            //find out how many icons go on each line
            int thisRow = x >= length - (length % perRow) ? (length % perRow) : perRow;
            int workingX = x;
            int edge = 0;
            int spacer = (workingWidth - size * thisRow) / (thisRow + 1);//thanks algebra
            if (thisRow != perRow) {
                //last row
                workingX = x % perRow;
            }
            buttons[x] = new SkillButton(this, SPACING + spacer + edge + (size + spacer) * (workingX % thisRow), PADDING + (size + PADDING) * onRow, size, skills[x]);
            addRenderableWidget(buttons[x]);
        }
        //generate the width of panel
        int startY = PADDING + (size + PADDING) * rows;
        final int height = this.height - SPACING - startY;
        okay = new CloseButton(this, width - SPACING - PADDING - 12, startY + height / 3 - 12, true);
        cancel = new CloseButton(this, width - SPACING - PADDING - 12, startY + height * 2 / 3 - 12, false);
        int panelWidth = workingWidth - PADDING - 12 - PADDING;
        panel = new InfoPanel(Minecraft.getInstance(), panelWidth, height, startY, SPACING);
        addRenderableWidget(okay);
        addRenderableWidget(cancel);
        addRenderableWidget(panel);
    }

    public void displaySkillInfo(Skill s) {
        panel.displaySkillInfo(s);
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

    static class CloseButton extends ImageButton {
        private static final ResourceLocation cross = new ResourceLocation("realms", "textures/gui/realms/reject_icon.png");
        private static final ResourceLocation check = new ResourceLocation("realms", "textures/gui/realms/accept_icon.png");
        private ScrollScreen parent;
        private boolean finalize;

        public CloseButton(ScrollScreen sss, int xIn, int yIn, boolean confirm) {
            super(xIn, yIn, 20, 20, 0, 0, 0, confirm ? check : cross, 24, 12, (a) -> {});
            parent = sss;
            finalize = confirm;
        }

        @Override
        public void onPress() {
            if (finalize && parent.selected != null) {
                CombatChannel.INSTANCE.sendToServer(new LearnScrollPacket(parent.selected, parent.offhand));
                Minecraft.getInstance().setScreen(null);
            } else if (!finalize) {
                Minecraft.getInstance().setScreen(null);
            }
        }

        public void renderButton(PoseStack stack, int x, int y, float partial) {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            int i = this.getYImage(this.isHoveredOrFocused());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            this.blit(stack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
            this.blit(stack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            this.renderBg(stack, minecraft, x, y);
            RenderSystem.setShaderTexture(0, finalize ? check : cross);

            float pos = this.isHovered ? 0.5f : 0f;
            blit(stack, this.x, this.y, 0, pos, this.width, this.height, 40, 20);
        }
    }

    class InfoPanel extends ScrollPanel {
        private ResourceLocation logoPath;
        private List<FormattedCharSequence> lines = Collections.emptyList();
        private Skill skill;

        InfoPanel(Minecraft mcIn, int widthIn, int heightIn, int topIn, int left) {
            super(mcIn, widthIn, heightIn, topIn, left, 4, 1);
            clearInfo();
        }

        private void displaySkillInfo(Skill s) {
            clearInfo();
            List<String> lines = new ArrayList<>();
            skill = s;
            ResourceLocation icon = new ResourceLocation("wardance:textures/skill/random.png");
            if (s != null) {
                lines.add(ChatFormatting.BOLD + "" + ChatFormatting.UNDERLINE + s.getDisplayName(null).getString() + ChatFormatting.RESET + "\n");
                lines.add(s.description().getString());
                icon = s.icon();
            } else {
                lines.add(ChatFormatting.BOLD + "" + ChatFormatting.UNDERLINE + Component.translatable("wardance.scroll.random.title").getString() + ChatFormatting.RESET + "\n");
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
            int height = 1;
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
            fill(mStack, left, top, right, bottom, 0xffA9A9A9);
            fill(mStack, left + 1, top + 1, right - 1, bottom - 1, -16777216);
            if (logoPath != null) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.enableBlend();
                Color c = skill.getColor();
                RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
                RenderSystem.setShaderTexture(0, logoPath);
                // Draw the logo image inscribed in a rectangle with width entryWidth (minus some padding) and height 50
                int headerHeight = 50;
                ScreenUtils.blitInscribed(mStack, left + width / 2 - 32, relativeY, width - (PADDING * 2), headerHeight, 64, 64, false, true);
                relativeY += headerHeight + PADDING;
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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
