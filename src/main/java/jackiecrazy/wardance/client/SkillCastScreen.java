package jackiecrazy.wardance.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.networking.CastSkillPacket;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SkillCastScreen extends Screen {
    private static final ResourceLocation radial = new ResourceLocation(WarDance.MODID, "textures/skill/radialhud.png");
    private static final int[] fixedU = {
            200, 400, 0, 200, 400, 0, 200, 400
    };
    private static final int[] fixedV = {
            0, 0, 200, 200, 200, 400, 400, 400
    };
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
        //RenderSystem.scaled(0.5, 0.5, 0.5);
        mc.getTextureManager().bindTexture(radial);
        selected = index < 0 ? null : elements[index];
        //mc.player.sendStatusMessage(new StringTextComponent(selected == null ? "none" : selected.getRegistryName().toString()), true);
        //mc.ingameGUI.blit(matrixStack, width / 2 - 100, height / 2 - 100, 0, 0, 200, 200, 600, 600);
        for (int a = 0; a < 8; a++) {
            if (elements[a] != null && elements[a].canCast(mc.player)) {
                if (a != index)
                    RenderSystem.color4f(0.6f, 0.6f, 0.6f, 1);
                mc.ingameGUI.blit(matrixStack, width / 2 - 100, height / 2 - 100, fixedU[a], fixedV[a], 200, 200, 600, 600);
                RenderSystem.color4f(1, 1, 1, 1);
            }
        }
        if (index >= 0 && elements[index] != null) {
            String name = elements[index].getRegistryName().toString();
            int yee=mc.fontRenderer.getStringWidth(name);
            mc.ingameGUI.getFontRenderer().drawString(matrixStack, name, (width-yee) / 2f, height / 2f-3, 0);
        }
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
        matrixStack.pop();

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
            Keybinds.quick=selected;
            CombatChannel.INSTANCE.sendToServer(new CastSkillPacket(selected.getRegistryName().toString()));
        }
    }
}