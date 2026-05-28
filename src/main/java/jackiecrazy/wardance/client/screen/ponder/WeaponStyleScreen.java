// com/yourmod/client/gui/WeaponStyleScreen.java
package jackiecrazy.wardance.client.screen.ponder;

import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.capability.quiver.QuiverMenu;
import jackiecrazy.wardance.client.screen.InfoPanel;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WeaponStyleScreen extends AbstractContainerScreen<StudyTheBlade> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.tryBuild(WarDance.MODID, "textures/gui/weapon_style.png");
    private static final int TAB_WIDTH = 28;
    private static final int TAB_HEIGHT = 24;
    private static final ResourceLocation TEXTURE = new ResourceLocation("wardance", "textures/gui/quiver_gui.png");
    private final Player player;
    private final List<StyleTabButton> tabButtons = new ArrayList<>();
    private final InfoPanel mainInfo = new InfoPanel(this, minecraft, 180, 120, 120, 80); // Main description area
    private final InfoPanel tips = new InfoPanel(this, minecraft, 80, 40, 320, 55); // Small right box
    private ItemStack displayedStack = ItemStack.EMPTY;
    private WeaponStats.AttackType tab = null; // null = Summary?

    public WeaponStyleScreen(StudyTheBlade menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        player = playerInventory.player;
        this.imageWidth = 176; // Extra space for overflow + labels
        this.imageHeight = 30 + 8 * 18 + 100;     // 8 colors + player inv
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        //colorize and draw each row
        final Iterator<SkillCategory> iterator = Skill.categoryMap.keySet().iterator();
        int j = 8;
        //guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, 19);
        final int barSpace = 18;
        RenderSystem.setShaderColor(1, 1, 1, 1);

        int remainingY = imageHeight - (barSpace * (j)) + 3;
        guiGraphics.blit(TEXTURE, x, y + 169, 0, 169, this.imageWidth, 87);
    }

    @Override
    protected void init() {
        tabButtons.clear();
        mainInfo.clearInfo();
        tips.clearInfo();

        int tabX = 20;
        int tabY = 40;

        for (WeaponStats.AttackType t : WeaponStats.AttackType.values()) {
            StyleTabButton tab = new StyleTabButton(tabX, tabY + t.ordinal() * TAB_HEIGHT, TAB_WIDTH, TAB_HEIGHT, t, t.name());
            tabButtons.add(tab);
            addRenderableWidget(tab);
        }

        // Animation / Item Render Area (top left of info panel)
        // We'll handle rendering in render()

        // Text areas are custom widgets

        addRenderableWidget(mainInfo);
        addRenderableWidget(tips);

        // Select default tab
        updateTabContent(null);
    }

    public void setDisplayedItem(ItemStack stack) {
        this.displayedStack = stack.copy();
        updateTabContent(null);
    }

    private void updateTabContent(WeaponStats.AttackType tab) {
        this.tab = tab;
        tabButtons.forEach(a -> a.setSelected(a.getType() == tab));

        WeaponStats.WeaponInfo data = WeaponStats.lookupStats(displayedStack);
        String nee = "basic";
        if (tab != null) {
            nee = tab.name();
        }
        //the gui needs to grab several things:
        //general description, general tips,
        //for each of the 8 states: name, description, tips, a render???
        //wardance:trident.guard_counter.desc
        //so entry.state.desc/tip
//
//
//        String stateKey = tab.name();
//        String moveName = data != null ? data.getMoveName(stateKey, displayedStack) : "Basic " + stateKey;
//
//        String description = data != null ? data.getDescription(stateKey) : "No style data available for this weapon.";
//        String tips = data != null ? data.getTips(stateKey) : "Click other items to compare.";
//
//        mainInfo.get(0).setText(Component.literal("§n" + moveName + "§r\n\n" + description));
//        this.tips.get(0).setText(Component.literal(tips));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Dark semi-transparent background
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xAA000000, 0xCC000000);

        // Main GUI texture
        int panelX = (this.width - 380) / 2;
        int panelY = 30;

        guiGraphics.blit(BACKGROUND, panelX, panelY, 0, 0, 380, 220, 512, 256);

        // Render inventory at bottom (reuse vanilla logic)
        renderInventory(guiGraphics, mouseX, mouseY);

        // Render item in animation slot
        int animX = panelX + 45;
        int animY = panelY + 35;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, animX, animY, 22, 0, 0, player); // Optional player preview
        guiGraphics.renderFakeItem(displayedStack, animX - 8, animY - 12);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderInventory(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Simple bottom inventory bar (you can expand to full 3x9 + hotbar)
        int invY = this.height - 90;
        for (int i = 0; i < 9; i++) {
            int slotX = this.width / 2 - 80 + i * 20;
            ItemStack stack = player.getInventory().items.get(i);
            //guiGraphics.(slotX, invY, 0, 0, stack, null);

            // Click detection is handled in mouseClicked()
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check inventory slots
        int invY = this.height - 90;
        for (int i = 0; i < 9; i++) {
            int slotX = this.width / 2 - 80 + i * 20;
            if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= invY && mouseY <= invY + 16) {
                ItemStack stack = player.getInventory().items.get(i);
                if (!stack.isEmpty()) {
                    setDisplayedItem(stack);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}