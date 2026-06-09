package jackiecrazy.wardance.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.footwork.client.GuiComponent;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.client.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemStackHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class QuiverDisplay implements IGuiOverlay {
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private static final ResourceLocation CIRCLE = new ResourceLocation(WarDance.MODID, "textures/hud/quiver_highlight.png");
    public static int invIndex = 0;
    private static Color c=Color.WHITE;
    private static ItemStack selected = ItemStack.EMPTY;
    private static ItemStackHandler inventory = null;
    private static List<Tuple<Integer, ItemStack>> filledSlots = new ArrayList<>();
    private static QuiverData d;

    public static void refreshInventory(Player p) {
        ItemStackHandler prev = inventory;
        d = QuiverData.getData(p);
        inventory = d.getSelectedQuiverInventory();
        if (inventory != prev)
            invIndex = 0;
        filledSlots.clear();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty())
                filledSlots.add(new Tuple<>(i, inventory.getStackInSlot(i)));
        }
        filledSlots.add(new Tuple<>(-1, new ItemStack(Items.BARRIER)));
        c=QuiverData.ORDER[d.getSelectedQuiver()].getColor();
        nextItem(0);
    }

    private static void nextItem(int jump) {
        if (inventory == null) return;
        invIndex += jump;
        int forcedJump = jump < 0 ? -1 : 1;
        int tries = QuiverData.SLOTS_PER_QUIVER + 1;
        while (filledSlots.stream().noneMatch(a -> a.getA() == invIndex)) {
            if (tries < 0) {
                WarDance.LOGGER.error("too many tries to find usable item, resetting!");
                invIndex = -1;
                break;
            }
            invIndex += forcedJump;
            if (invIndex >= (d.getVisibleSlots(d.getSelectedQuiver())))
                invIndex = -1;
            if (invIndex < -1) invIndex += (d.getVisibleSlots(d.getSelectedQuiver()))+2;
            tries--;
        }
    }

    private static void renderItem(GuiGraphics gfx, ItemStack stack, int x, int y, float scale) {
        final PoseStack pose = gfx.pose();
        pose.pushPose();// Center the scaling on the item
        if (scale != 1) {
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
        return Keybinds.THROW.isDown() || Keybinds.SWAP.isDown();
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
        if (inventory == null || d == null) return;
        //grab ender chest content
        //find the index stack and 2 before/after it
        //draw them on the screen
        int size = filledSlots.size();
        int max = size;//Mth.clamp(size, 0, 2);
        double angle = -90;
        for (Tuple<Integer, ItemStack> is : filledSlots) {
            int offset = height / 5;
            int x = (int) (Math.cos(Mth.DEG_TO_RAD * angle) * offset);
            int y = (int) (Math.sin(Mth.DEG_TO_RAD * angle) * offset);
            float scale = 1;
            ItemStack stack = is.getB();
            if (is.getA() == invIndex){
                RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                GuiComponent.blit(guiGraphics.pose(), CIRCLE, width/2+x-33, height/2+y-33, 0, 0, 64, 64, 64, 64);
                RenderSystem.setShaderColor(1,1,1,1);
                scale = 2;
            }
            renderItem(guiGraphics, stack, width / 2 + x, height / 2 + y, scale);
            angle += (360d / (size));
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
