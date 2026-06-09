package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import jackiecrazy.wardance.client.screen.skill.SkillSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.gui.widget.ScrollPanel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class InfoPanel extends ScrollPanel {
    static final Pattern TOOLTIP_PATTERN = Pattern.compile(
            "\\{[^}]*\\}", Pattern.CASE_INSENSITIVE);
    private final Screen parent;
    private Font font;
    private ResourceLocation logoPath;
    protected static final int PADDING = 6;
    private List<FormattedCharSequence> lines = Collections.emptyList();

    public InfoPanel(Screen skillSelectionScreen,
                     Minecraft mcIn, int widthIn, int heightIn, int left, int topIn) {
        super(mcIn, widthIn, heightIn, topIn, left);
        this.parent = skillSelectionScreen;
        font=mcIn.font;
    }

    public void setInfo(Component c){
        setInfo(List.of(c.getString()), null);
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
        height += (lines.size() * (font.lineHeight + 2));
        if (height < this.bottom - this.top - 8) height = this.bottom - this.top - 8;
        return height;
    }

    @Override
    protected void drawBackground(GuiGraphics matrix, Tesselator tess, float partialTick) {
        matrix.fill(this.left, this.top, this.right, this.bottom, 0xFFA0A0A0);
        matrix.fill(left + 1, top + 1, right - 1, bottom - 1, 0xFF000000);
        //super.drawBackground(matrix, tess, partialTick);
    }

    @Override
    protected void drawPanel(GuiGraphics mStack,
                             int entryRight,
                             int relativeY,
                             Tesselator tess,
                             int mouseX,
                             int mouseY) {
        if (logoPath != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, logoPath);
            // Draw the logo image inscribed in a rectangle with width entryWidth (minus some padding) and height 50
            int headerHeight = 50;
            mStack.blitInscribed(logoPath, left + width / 2 - 32, relativeY, width - (PADDING * 2), headerHeight, 64, 64, false, true);
            relativeY += headerHeight + PADDING;
        }

        for (FormattedCharSequence line : lines) {
            if (line != null) {
                RenderSystem.enableBlend();
                mStack.drawString(font, line, left + PADDING, relativeY, 0xFFFFFF);
                //RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
            }
            relativeY += font.lineHeight + 2;
        }
    }

    @Override
    protected int getScrollAmount() {
        return (font.lineHeight + 2) * 3;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final Style component = findTextLine((int) mouseX, (int) mouseY);
        if (component != null) {
            parent.handleComponentClicked(component);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics matrix, int mouseX, int mouseY, float partialTick) {
        super.render(matrix, mouseX, mouseY, partialTick);

        final Style component = findTextLine(mouseX, mouseY);
        if (component != null) {
            matrix.renderComponentHoverEffect(font, component, mouseX, mouseY);
        }
    }

    private Style findTextLine(final int mouseX, final int mouseY) {
        double offset = (mouseY - top) + border + scrollDistance + 1;
        int xoff = (mouseX - left) - border;
        if (logoPath != null) {
            offset -= 50;
        }
        if (offset <= 0 || xoff < 1) return null;

        int lineIdx = (int) (offset / (font.lineHeight + 2));
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
