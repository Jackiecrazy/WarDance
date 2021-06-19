package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.client.Keybinds;
import jackiecrazy.wardance.networking.CastSkillPacket;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SkillCastScreen extends Screen {
    private static final ResourceLocation radial = new ResourceLocation(WarDance.MODID, "textures/skill/radialhud.png");
    private static final ResourceLocation cooldown = new ResourceLocation(WarDance.MODID, "textures/skill/blank.png");
    private static final int[] fixedU = {
            200, 400, 0, 200, 400, 0, 200, 400
    };
    private static final int[] fixedV = {
            0, 0, 200, 200, 200, 400, 400, 400
    };
    private static final int[] iconX = {
            80,
            135,
            156,
            135,
            80,
            35,
            15,
            35
    };
    private static final int[] iconY = {
            15,
            35,
            79,
            132,
            156,
            133,
            79,
            35
    };
    private static final DecimalFormat formatter = new DecimalFormat("#.#");
    protected final Skill[] elements;
    protected Skill selected;

    public SkillCastScreen(List<Skill> skills) {
        super(new StringTextComponent("yeet"));
        this.passEvents = true;
        this.elements = new Skill[8];
        for (int a = 0; a < 8; a++) {
            elements[a] = skills.size() > a ? skills.get(a) : null;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        //draw background, then slice, then each skill at determined points
        //don't draw slices that cannot be used (so they appear to be greyed out)
        //highlight a slice based on distance from center (to allow cancel leeway) and direction
        //listen to key release to send cast message to server
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getMainWindow().getScaledWidth(), height = mc.getMainWindow().getScaledHeight();
        //get distance
        float centeredx = mouseX - width / 2f;
        float centeredy = mouseY - height / 2f;
        boolean distance = centeredy * centeredy + centeredx * centeredx > 1100;
        //get direction
        double angle = Math.toDegrees(MathHelper.atan2(centeredx, -centeredy));
        if (angle < 0) angle += 360;
        int index = distance ? (int) Math.floor(((angle + 22.5) / 45) % 8) : -1;
        matrixStack.push();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        mc.getTextureManager().bindTexture(radial);
        selected = index < 0 ? null : elements[index];
        //mc.player.sendStatusMessage(new StringTextComponent(selected == null ? "none" : selected.getRegistryName().toString()), true);
        int x = width / 2 - 100;
        int y = height / 2 - 100;
        RenderSystem.color4f(0.6f, 0.6f, 0.6f, 1);
        blit(matrixStack, x, y, 0, 0, 200, 200, 600, 600);
        RenderSystem.color4f(1, 1, 1, 1);
        for (int a = 0; a < 8; a++) {
            if (elements[a] != null) {
                Skill s = elements[a];

                //radial slice for highlighting
                matrixStack.push();
                mc.textureManager.bindTexture(radial);
                if (CasterData.getCap(mc.player).isSkillActive(elements[a]))
                    RenderSystem.color4f(0.4f, 0.7f, 0.4f, 1);
                else if (s.castingCheck(mc.player) != Skill.CastStatus.ALLOWED)
                    RenderSystem.color4f(0.4f, 0.4f, 0.4f, 1);
                else if (a != index)
                    RenderSystem.color4f(0.6f, 0.6f, 0.6f, 1);
                blit(matrixStack, x, y, fixedU[a], fixedV[a], 200, 200, 600, 600);
                matrixStack.pop();

                //skill icon
                matrixStack.push();
                mc.textureManager.bindTexture(s.icon());
                Color c = s.getColor();
                RenderSystem.color4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                AbstractGui.blit(matrixStack, x + iconX[a], y + iconY[a], 0, 0, 32, 32, 32, 32);
                RenderSystem.color4f(1, 1, 1, 1);
                matrixStack.pop();

                //cooldown overlay
                if (CasterData.getCap(mc.player).isSkillCoolingDown(s)) {
                    matrixStack.push();
                    float cd = CasterData.getCap(mc.player).getSkillCooldown(s);
                    float cdPerc = cd / CasterData.getCap(mc.player).getMaxSkillCooldown(s);
                    mc.textureManager.bindTexture(cooldown);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    drawCooldownCircle(matrixStack, x + iconX[a], y + iconY[a], cdPerc);

                    //cooldown number
                    String num = String.valueOf((int) cd);
                    if (Math.ceil(cd) != cd)
                        num = formatter.format(cd);
                    matrixStack.push();
                    mc.textureManager.bindTexture(cooldown);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.6F);
                    //AbstractGui.blit(matrixStack, x + iconX[a], y + iconY[a], 0, 0, 32, 32, 32, 32);
                    mc.fontRenderer.drawString(matrixStack, num, x + iconX[a] + 16 - mc.fontRenderer.getStringWidth(num) / 2f, y + iconY[a] + 12, 0xFFFFFF);
                    matrixStack.pop();
                    matrixStack.pop();
                }

            }
        }
        if (index >= 0 && selected != null) {
            String print = selected.getDisplayName().getString();
            int yee = mc.fontRenderer.getStringWidth(print);
            mc.ingameGUI.getFontRenderer().drawString(matrixStack, print, (width - yee) / 2f, height / 2f - 3, selected.getColor().getRGB());
            final Skill.CastStatus castStatus = selected.castingCheck(mc.player);
            if (castStatus != Skill.CastStatus.ALLOWED) {
                switch (castStatus) {
                    case COOLDOWN:
                        print = new TranslationTextComponent("wardance.skill.cooldown").getString();
                        break;
                    case CONFLICT:
                        print = new TranslationTextComponent("wardance.skill.conflict").getString();
                        break;
                    case OTHER:
                        print = new TranslationTextComponent(elements[index].getRegistryName().toString() + ".requirement").getString();
                        break;
                }
                yee = mc.fontRenderer.getStringWidth(print);
                mc.ingameGUI.getFontRenderer().drawString(matrixStack, print, (width - yee) / 2f, height / 2f + 3, Color.RED.getRGB());
            }

        }
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
        matrixStack.pop();

    }

    private void drawCooldownCircle(MatrixStack ms, int x, int y, float v) {
        ms.push();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        Minecraft.getInstance().textureManager.bindTexture(cooldown);
        if (v <= 0) return; // nothing to be drawn
        int x2 = x + 32, y2 = y + 32; // bottom-right corner
        if (v >= 1) {
            RenderSystem.color4f(0.125F, 0.125F, 0.125F, 0.6F);
            AbstractGui.blit(ms, x, y, 0, 0, 32, 32, 32, 32);
            ms.pop();
            // entirely filled
            return;
        }
        int xm = (x + x2) / 2, ym = (y + y2) / 2; // middle point
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(9, DefaultVertexFormats.POSITION_TEX_COLOR);
        drawVertex(bufferbuilder, xm, ym);
        drawVertex(bufferbuilder, xm, y);
// draw corners:
        if (v >= 0.125) drawVertex(bufferbuilder, x, y);
        if (v >= 0.375) drawVertex(bufferbuilder, x, y2);
        if (v >= 0.625) drawVertex(bufferbuilder, x2, y2);
        if (v >= 0.875) drawVertex(bufferbuilder, x2, y);
// calculate angle & vector from value:
        double vd = Math.PI * (v * 2 - 0.5);
        double vx = -Math.cos(vd);
        double vy = Math.sin(vd);
// normalize the vector, so it hits -1+1 at either side:
        double vl = Math.max(Math.abs(vx), Math.abs(vy));
        if (vl < 1) {
            vx /= vl;
            vy /= vl;
        }
        drawVertex(bufferbuilder, (int) (xm + vx * (x2 - x) / 2), (int) (ym + vy * (y2 - y) / 2));
        Tessellator.getInstance().draw();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1F);
        ms.pop();
    }

    private void drawVertex(BufferBuilder bufferbuilder, int x, int y) {
        bufferbuilder.pos(x, y, 0.0D).tex(0, 0).color(32, 32, 32, 205).endVertex();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.minecraft
                != null && this.minecraft
                .player != null) {
            if (!this.minecraft
                    .player.isAlive()) {
                this.minecraft
                        .player.closeScreen();
            } else {
                this.minecraft
                        .player.movementInput.tickMovement(this.minecraft
                        .player.isForcedDown()); //shouldRenderSneaking
            }
        }
    }

    @Override
    public boolean keyReleased(int key, int scancode, int modifiers) {
        if (!isKeyBindingStillPressed()) {
            this.selectAndClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        return false;
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        if (!isKeyBindingStillPressed()) {
            this.selectAndClose();
            return true;
        }
        return false;
    }

    @Override
    public void init() {
        super.init();
        ForgeIngameGui.renderCrosshairs = false;
    }

    @Override
    public void onClose() {
        super.onClose();
        //GLFW.glfwSetInputMode(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        ForgeIngameGui.renderCrosshairs = true;
    }

    @Override
    public void closeScreen() {
        super.closeScreen();
        ForgeIngameGui.renderCrosshairs = true;
    }

    @Override
    public boolean isPauseScreen() { //isPauseScreen
        return false;
    }

    protected boolean isKeyBindingStillPressed() {
        return Keybinds.CAST.isKeyDown();
    }

    protected void selectAndClose() {
        closeScreen();
        if (selected != null) {
            Keybinds.quick = selected;
            CombatChannel.INSTANCE.sendToServer(new CastSkillPacket(selected.getRegistryName()));
        }
    }
}