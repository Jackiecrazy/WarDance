package jackiecrazy.wardance.client.screen.ponder;

import jackiecrazy.wardance.config.weapon.WeaponStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.Locale;

class StudyStateButton extends Button {
    private final StudyTheBladeScreen parent;
    private final WeaponStats.AttackType index;
    private final String stateKey;
    private boolean selected = false;

    public StudyStateButton(StudyTheBladeScreen s,
                            int x,
                            int y,
                            int width,
                            int height,
                            WeaponStats.AttackType index,
                            String stateKey) {
        super(x, y, width, height, Component.empty(), btn -> {
        }, DEFAULT_NARRATION);
        parent = s;
        this.index = index;
        this.stateKey = stateKey;
    }

    @Override
    public void onClick(double p_93371_, double p_93372_) {
        super.onClick(p_93371_, p_93372_);
        parent.updateTabContent(index);
    }

    public WeaponStats.AttackType getType() {
        return index;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color = selected ? 0xFFAA00 : 0xFFFFFF;
        String str = stateKey.toLowerCase(Locale.ROOT);
        String unloc = "wardance.tooltip.sweep." + str;

        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, selected ? 0x88FFAA00 : 0x44FFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable(unloc, "").getString(), getX() + 4, getY() + 8, color);

        // Icon rendering (expand with your own atlas)
    }
}