package jackiecrazy.wardance.client.screen.ponder;

import jackiecrazy.wardance.config.weapon.WeaponStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

// StyleTabButton.java
class StyleTabButton extends Button {
    private final WeaponStats.AttackType index;
    private final String stateKey;
    private boolean selected = false;

    public StyleTabButton(int x, int y, int width, int height, WeaponStats.AttackType index, String stateKey) {
        super(x, y, width, height, Component.empty(), btn -> {}, DEFAULT_NARRATION);
        this.index = index;
        this.stateKey = stateKey;
    }

    public WeaponStats.AttackType getType() { return index; }
    public void setSelected(boolean selected) { this.selected = selected; }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color = selected ? 0xFFAA00 : 0xFFFFFF;
        String display = (index == WeaponStats.AttackType.UNDEFINED ? "Summary" : stateKey.replace("_", " "));

        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, selected ? 0x88FFAA00 : 0x44FFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, display, getX() + 4, getY() + 8, color);

        // Icon rendering (expand with your own atlas)
    }
}