package jackiecrazy.wardance.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.client.Keybinds;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class QuiverDisplay implements IGuiOverlay {
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    public static int listIndex, invIndex;
    private static ItemStack selected = ItemStack.EMPTY;
    private static List<Tuple<Integer, ItemStack>> inventory = new ArrayList<>();

    public static void refreshInventory(Player p, ListTag tag) {
        inventory.clear();
        inventory.add(new Tuple<>(-1, new ItemStack(Items.BARRIER)));
        PlayerEnderChestContainer ender = p.getEnderChestInventory();
        ender.fromTag(tag);
        for (int i = 0; i < ender.getContainerSize(); i++) {
            final ItemStack item = ender.getItem(i);
            if (!item.isEmpty() && WeaponStats.isCombatItem(p, item)) {
                inventory.add(new Tuple<>(i, item));
                if (selected == item) {
                    listIndex = inventory.size() - 1;
                    invIndex = i;
                }
            }
        }
        nextItem(0);
    }

    private static void nextItem(int jump) {
        if (inventory.isEmpty()) return;
        listIndex += jump;
        listIndex %= inventory.size();
        //modulo?
        if (listIndex < 0) {
            listIndex += inventory.size();
        }
        selected = inventory.get(listIndex).getB();
        invIndex = inventory.get(listIndex).getA();
    }

    private static void renderItem(GuiGraphics gfx, ItemStack stack, int x, int y, float scale) {
        final PoseStack pose = gfx.pose();
        pose.pushPose();// Center the scaling on the item
        if(scale!=1) {
            pose.translate(-x, -y, 0);
            pose.scale(scale, scale, 1.0F);
        }
        gfx.renderItem(stack, x - 8, y - 8);          // icon
        gfx.renderItemDecorations(
                Minecraft.getInstance().font,
                stack,
                x - 8,
                y - 8
        );
        pose.popPose();
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (!showQuiver()) return;

        double delta = event.getScrollDelta();
        if (delta == 0) return;

        if (delta > 0) {
            nextItem(-1);
        } else {
            nextItem(1);
        }

        event.setCanceled(true); // prevents hotbar scroll
    }

    private static boolean showQuiver() {
        return Keybinds.THROW.isDown()||Keybinds.SWAP.isDown();
    }

    public void drawSlice(GuiGraphics guiGraphics,
                          float x,
                          float y,
                          float z,
                          float radiusIn,
                          float radiusOut,
                          float startAngle,
                          float endAngle,
                          int r,
                          int g,
                          int b,
                          int a) {
        float angle = endAngle - startAngle;
        int sections = Math.max(1, Mth.ceil(angle / 5f));

        startAngle = (float) Math.toRadians(startAngle);
        endAngle = (float) Math.toRadians(endAngle);
        angle = endAngle - startAngle;

        var buffer = guiGraphics.bufferSource().getBuffer(RenderType.gui());

        for (int i = 0; i < sections; i++) {
            float angle1 = startAngle + (i / (float) sections) * angle;
            float angle2 = startAngle + ((i + 1) / (float) sections) * angle;

            float pos1InX = x + radiusIn * (float) Math.cos(angle1);
            float pos1InY = y + radiusIn * (float) Math.sin(angle1);
            float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
            float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
            float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
            float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
            float pos2InX = x + radiusIn * (float) Math.cos(angle2);
            float pos2InY = y + radiusIn * (float) Math.sin(angle2);

            buffer.vertex(pos1OutX, pos1OutY, z).color(r, g, b, a);
            buffer.vertex(pos1InX, pos1InY, z).color(r, g, b, a);
            buffer.vertex(pos2InX, pos2InY, z).color(r, g, b, a);
            buffer.vertex(pos2OutX, pos2OutY, z).color(r, g, b, a);
        }

        guiGraphics.flush();
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        if (player == null || !showQuiver()) {
            return;
        }
        if (inventory.isEmpty()) return;
        //grab ender chest content
        //find the index stack and 2 before/after it
        //draw them on the screen
        int max = Mth.clamp(inventory.size()/2, 0, 2);
        double angle = -90 - (15 * max);
        for (int a = listIndex - max; a < listIndex + 1 + max; a++) {
            int corrected = a % inventory.size();
            if (corrected < 0) corrected += inventory.size();
            int offset = height / 2;
            int x = (int) (Math.cos(Mth.DEG_TO_RAD * angle) * offset);
            int y = (int) (Math.sin(Mth.DEG_TO_RAD * angle) * offset);
            float scale =1;
            if(corrected==listIndex)scale=2;
            renderItem(guiGraphics, inventory.get(corrected).getB(), width / 2 + x, height + y, scale);
            angle += 15;
        }
//        float step = (float)(2 * Math.PI / 3);
//        float centerAngle = -Mth.HALF_PI; // top
//
//        float a0 = centerAngle - step / 2f;
//        float a1 = centerAngle + step / 2f;
//        drawSlice(guiGraphics, width/2f, height/2f, 10, 200, 400, a0, a1, 256,256,256,180);
        //forward index to packet when needed
    }
}
