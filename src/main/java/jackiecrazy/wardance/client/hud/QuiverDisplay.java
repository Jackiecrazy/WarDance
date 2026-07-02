package jackiecrazy.wardance.client.hud;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
    private static Color c = Color.WHITE;
    private static ItemStack selected = ItemStack.EMPTY;
    private static ItemStackHandler inventory = null;
    private static List<Tuple<Integer, ItemStack>> filledSlots = new ArrayList<>();
    private static QuiverData d;
    private double prevX = 0, prevY = 0, prevAngle;

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
        c = QuiverData.ORDER[d.getSelectedQuiver()].getColor();
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
            if (invIndex < -1) invIndex += (d.getVisibleSlots(d.getSelectedQuiver())) + 2;
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

    private double getMouseAngle() {
        Minecraft mc = Minecraft.getInstance();
        final Window window = mc.getWindow();
        if (mc.player == null) return 0;

        if (Double.isNaN(prevX) || Double.isNaN(prevY)) {
            WarDance.LOGGER.warn("invalid angles have been discarded");
            prevX = prevY = 0;
        }
        // Get current mouse position in screen coordinates
        double mouseX = mc.mouseHandler.xpos();//Mth.lerp(0.8, prevX, mc.mouseHandler.xpos());
        double mouseY = mc.mouseHandler.ypos();//Mth.lerp(0.8, prevY, mc.mouseHandler.ypos());
        double dx = mouseX - prevX;
        double dy = mouseY - prevY;
        double epsilon = 5;

        //comment out these two to swap to alt scheme
//        prevX = mouseX;
//        prevY = mouseY;
        if (Math.abs(dx) < epsilon && Math.abs(dy) < epsilon) return prevAngle;

        // Angle in degrees, adjusted so 0° is top (like your current rendering)
        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90; // +90 to make top = 0
        if (angle < 0) angle += 360;

        return angle;
    }

    private void updateSelectionFromMouse(double angle) {
        if (inventory == null || filledSlots.isEmpty()) return;

        int size = filledSlots.size(); // exclude the barrier maybe? or keep
        // Each slot gets equal angle slice
        double sliceAngle = 360.0 / size;

        // Find closest slot
        int newIndex = (int) Math.floor((angle+sliceAngle/2) / sliceAngle) % size;

        // Map to filledSlots
        Tuple<Integer, ItemStack> selectedTuple = filledSlots.get(newIndex);
        invIndex = selectedTuple.getA();
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
        float start = (float) Math.toRadians(startAngle);
        float end = (float) Math.toRadians(endAngle);
        float angleDiff = end - start;

        int sections = Math.max(8, Mth.ceil(Math.abs(angleDiff) / 6f)); // smooth curve

        VertexConsumer buffer = guiGraphics.bufferSource().getBuffer(RenderType.gui());

        for (int i = 0; i < sections; i++) {
            float t1 = i / (float) sections;
            float t2 = (i + 1) / (float) sections;

            float a1 = start + t1 * angleDiff;
            float a2 = start + t2 * angleDiff;

            float x1i = x + radiusIn * Mth.cos(a1);
            float y1i = y + radiusIn * Mth.sin(a1);
            float x1o = x + radiusOut * Mth.cos(a1);
            float y1o = y + radiusOut * Mth.sin(a1);

            float x2i = x + radiusIn * Mth.cos(a2);
            float y2i = y + radiusIn * Mth.sin(a2);
            float x2o = x + radiusOut * Mth.cos(a2);
            float y2o = y + radiusOut * Mth.sin(a2);

            // Draw quad (counter-clockwise order)
            buffer.vertex(x1o, y1o, z).color(r, g, b, a).endVertex();
            buffer.vertex(x1i, y1i, z).color(r, g, b, a).endVertex();
            buffer.vertex(x2i, y2i, z).color(r, g, b, a).endVertex();
            buffer.vertex(x2o, y2o, z).color(r, g, b, a).endVertex();
        }
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        mc.mouseHandler.cursorEntered();
        Player player = mc.player;
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        float mouseAngle = (float) getMouseAngle();
        if (player == null || !showQuiver()) {
            prevX = mc.mouseHandler.xpos();//Mth.lerp(0.8, prevX, mc.mouseHandler.xpos());
            prevY = mc.mouseHandler.ypos();
            return;
        }
        if (inventory == null || d == null) return;
        prevAngle = mouseAngle;
        updateSelectionFromMouse(mouseAngle);
        //find the index stack and 2 before/after it
        //draw them on the screen
        int size = filledSlots.size();
        int max = size;//Mth.clamp(size, 0, 2);
        float angle = -90;
        for (Tuple<Integer, ItemStack> is : filledSlots) {
            int offset = height / 5;
            int x = (int) (Math.cos(Mth.DEG_TO_RAD * angle) * offset);
            int y = (int) (Math.sin(Mth.DEG_TO_RAD * angle) * offset);
            float scale = 1;
            ItemStack stack = is.getB();
            if (is.getA() == invIndex) {
                //RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);{
                //distance indicator
                double xx = mc.mouseHandler.xpos() - prevX;
                double yy = mc.mouseHandler.ypos() - prevY;
                float what = Mth.sqrt((float) (xx * xx + yy * yy));
                float selectorAngle=30;
                drawSlice(guiGraphics,
                          width / 2f,
                          height / 2f,
                          1f,           // z-level (higher = on top)
                          0f,           // inner radius
                          Mth.clamp(what / 15, 0, 15),
                          (float) (prevAngle - 90-selectorAngle/2),
                          (float) (prevAngle - 90+selectorAngle/2),
                          255, 255, 255, 180);

                drawSlice(guiGraphics,
                          width / 2f,
                          height / 2f,
                          1f,           // z-level (higher = on top)
                          25f,           // inner radius
                          70f,          // outer radius
                          angle - 30,
                          angle + 30,
                          c.getRed(), c.getGreen(), c.getBlue(), 180);  // nice visible color + some transparency
                //GuiComponent.blit(guiGraphics.pose(), CIRCLE, width / 2 + x - 33, height / 2 + y - 33, 0, 0, 64, 64, 64, 64);
                //RenderSystem.setShaderColor(1, 1, 1, 1);
                scale = 2;
            }
            renderItem(guiGraphics, stack, width / 2 + x, height / 2 + y, scale);
            angle += (360f / (size));
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
