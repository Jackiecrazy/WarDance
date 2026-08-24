// com/yourmod/client/gui/StudyTheBladeScreen.java
package jackiecrazy.wardance.client.screen.ponder;

import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.client.screen.InfoPanel;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.SweepAttack;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class StudyTheBladeScreen extends AbstractContainerScreen<StudyTheBlade> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.tryBuild(WarDance.MODID, "textures/gui/weapon_style.png");
    private static final int TAB_WIDTH = 80;
    private static final int TAB_HEIGHT = 20;
    private static final ResourceLocation TEXTURE = new ResourceLocation("wardance", "textures/gui/quiver_gui.png");
    private final Player player;
    private final List<StudyStateButton> tabButtons = new ArrayList<>();
    private InfoPanel mainInfo; // Main description area
    private InfoPanel tips; // Small right box
    private ItemStack displayedStack = null;
    private WeaponStats.AttackState tab = null; // null = Summary?

    public StudyTheBladeScreen(StudyTheBlade menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        player = playerInventory.player;
        this.imageWidth = 176; // Extra space for overflow + labels
        this.imageHeight = 30 + 8 * 18 + 100;
    }

    private static String orElse(String key, String fallback) {
        String ret = Component.translatable(key).getString();
        if (ret.equals(key))
            ret = Component.translatable(fallback, key).getString();
        return ret;
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
        super.init();
        int mainWidth = 180, mainHeight = 130;
        mainInfo = new InfoPanel(this, minecraft, mainWidth, mainHeight, (width - mainWidth) / 2, 20);
        tips = new InfoPanel(this, minecraft, 100, mainHeight, width-100, (height-mainHeight)/2);
        tabButtons.clear();
        mainInfo.clearInfo();
        tips.clearInfo();

        int tabX = 20;
        int tabY = (height-(WeaponStats.AttackState.values().length*TAB_HEIGHT))/2;


        for (WeaponStats.AttackState t : WeaponStats.AttackState.values()) {
            StudyStateButton tab = new StudyStateButton(this, tabX, tabY + t.ordinal() * TAB_HEIGHT, TAB_WIDTH, TAB_HEIGHT, t, t.name());
            tabButtons.add(tab);
            addRenderableWidget(tab);
        }

        // Animation / Item Render Area (top left of info panel)
        // We'll handle rendering in render()

        // Text areas are custom widgets

        addRenderableWidget(tips);
        addRenderableWidget(mainInfo);

        // Select default tab
        updateTabContent(tab);
    }

    public void setDisplayedItem(ItemStack stack) {
        this.displayedStack = stack;
        updateTabContent(WeaponStats.AttackState.UNDEFINED);
    }

    public void updateTabContent(WeaponStats.AttackState tab) {
        if (displayedStack == null) {
            mainInfo.setInfo(Component.translatable("wardance.weeb.info"));
            tips.setInfo(Component.translatable("wardance.weeb.tips"));
            tabButtons.forEach(a -> a.visible = false);

            return;
        }
        this.tab = tab;
        tabButtons.forEach(a -> {
            a.visible = true;
            a.setSelected(a.getType() == tab);
        });

        WeaponStats.WeaponInfo data = WeaponStats.lookupStats(displayedStack);
        if (data == null) {
            mainInfo.setInfo(Component.translatable("wardance.weeb.notaweapon"));
            tips.setInfo(Component.translatable("wardance.weeb.notaweapon.tips"));
            return;
        }
        String nee = "";
        String fallbackDesc = "wardance:wip.desc";
        if (tab != null && tab != WeaponStats.AttackState.UNDEFINED) {
            nee = tab.name().toLowerCase(Locale.ROOT) + ".";
            final List<WeaponInteractions.WeaponInteraction> RULESOFNATURE = WeaponStats.getSweepInfo(displayedStack, player, tab, true, InteractionHand.MAIN_HAND).getInteractions();
            if (!RULESOFNATURE.isEmpty()) {
                final WeaponInteractions.WeaponInteraction interact = RULESOFNATURE.get(0);
                if (interact instanceof SweepAttack sa) {
                    fallbackDesc = "wardance:wip." + sa.getType().name().toLowerCase(Locale.ROOT);
                } else fallbackDesc = "wardance:wip." + interact.getInteractionType().name().toLowerCase(Locale.ROOT);
            }
        }
        //the gui needs to grab several things:
        //general description, general tips,does
        //for each of the 8 states: name, description, tips, a render???
        //wardance:trident.guard_counter.desc
        //so entry.state.desc/tip/name
        String base = data.getName() + "." + nee;
        List<String> main = new ArrayList<>();
        final String name = orElse(base + "name", "");
        main.add("{" + name + ";GOLD}");
        String tags = "";
        if (tab != null && tab != WeaponStats.AttackState.UNDEFINED)
            for (String str : data.getTags(tab))
                tags = tags.concat((tags.isEmpty() ? "" : " • ") + Component.translatable("wardance.attack.tag." + str).getString());
        main.add(tags);
        main.add(" ");
        final String desc = orElse(base + "desc", fallbackDesc);
        main.add(desc);


        mainInfo.setInfo(main, null);
        List<String> tip = new ArrayList<>();
        tip.add(Component.translatable("wardance.weeb.tip").getString());
        tip.add(orElse(base + "tips", "wardance:wip.tips"));
        tips.setInfo(tip, null);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick); // Renders all slots + labels

        int x = 20;
        int y = 10;

        // Draw custom background over the slots
        //guiGraphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, 512, 512);

        // Animation / Item Display Area (top left)
        if (displayedStack != null)
            guiGraphics.renderFakeItem(displayedStack, x + 32, y + 0);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if click is over any slot
        for (Slot slot : menu.slots) {
            if (slot.isActive() && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                if (slot.hasItem() || displayedStack == null || displayedStack.isEmpty()) {
                    setDisplayedItem(slot.getItem() == displayedStack ? null : slot.getItem());
                    return true; // Block default pickup / drag behavior
                }
            }
        }

        // Let tabs and widgets still work
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
        //no labels here, nope
    }
}