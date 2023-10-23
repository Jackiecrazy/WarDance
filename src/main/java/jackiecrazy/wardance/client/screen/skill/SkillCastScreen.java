package jackiecrazy.wardance.client.screen.skill;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.client.Keybinds;
import jackiecrazy.wardance.client.RenderUtils;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.SelectSkillPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.List;

import static jackiecrazy.wardance.client.RenderUtils.formatter;

@OnlyIn(Dist.CLIENT)
public class SkillCastScreen extends Screen {
    private static final ResourceLocation radial = new ResourceLocation(WarDance.MODID, "textures/skill/radialhud.png");
    private static final int[] fixedU = {
            200, 0, 200, 400, 400
    };
    private static final int[] fixedV = {
            0, 200, 200, 200, 0
    };
    private static final int[] iconX = {
            84,
            117,
            117,
            52,
            52
    };
    private static final int[] iconY = {
            84,
            52,
            117,
            117,
            52
    };
    protected final Skill[] elements;
    protected int exIndex = -1;

    public SkillCastScreen(List<Skill> skills) {
        super(Component.empty());
        this.elements = new Skill[5];
        for (int a = 0; a < elements.length; a++) {
            elements[a] = skills.size() > a ? skills.get(a) : null;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        //draw background, then slice, then each skill at determined points
        //don't draw slices that cannot be used (so they appear to be greyed out)
        //highlight a slice based on distance from center (to allow cancel leeway) and direction
        //listen to key release to send cast message to server
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth(), height = mc.getWindow().getGuiScaledHeight();
        //get distance
        float centeredx = mouseX - width / 2f;
        float centeredy = mouseY - height / 2f;
        double angle = Math.toDegrees(Mth.atan2(centeredx, -centeredy));
        if (angle < 45) angle += 720;
        //at 45/135/215/305 deg, the distance cutoff should be 430, otherwise 700
        double cutoffy = centeredx > 0 ? 37 - centeredx : centeredx + 37;
        boolean distance = centeredy > cutoffy;
        if (centeredy < 0) {
            cutoffy = centeredx < 0 ? -37 - centeredx : centeredx - 37;
            distance = cutoffy > centeredy;
        }
        int index = distance ? (int) Math.floor((angle / 90) % 4) + 1 : 0;
        graphics.pose().pushPose();
        //RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //RenderSystem.setShaderTexture(0, radial);
        this.exIndex = index;
        //mc.player.sendStatusMessage(new StringTextComponent(selected == null ? "none" : selected.getRegistryName().toString()), true);
        int x = width / 2 - 100;
        int y = height / 2 - 100;
        RenderSystem.setShaderColor(0.6f, 0.6f, 0.6f, 1);
        graphics.blit(radial, x, y, 0, 0, 200, 200, 600, 600);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        for (int a = 0; a < elements.length; a++) {
            if (elements[a] != null) {
                Skill s = elements[a];

                //radial slice for highlighting
                graphics.pose().pushPose();
                //RenderSystem.setShaderTexture(0, radial);
                //holstered is green
                final ISkillCapability cap = CasterData.getCap(mc.player);
                if (cap.getHolsteredSkill() == elements[a])
                    RenderSystem.setShaderColor(0.4f, 0.7f, 0.4f, 1);
                    //active is blue
                else if (cap.getSkillState(elements[a]) == Skill.STATE.ACTIVE)
                    RenderSystem.setShaderColor(0.4f, 0.4f, 0.9f, 1);
                    //not allowed is dark gray
                else if (s.castingCheck(mc.player, cap.getSkillData(s).orElse(SkillData.DUMMY)) != Skill.CastStatus.ALLOWED)
                    RenderSystem.setShaderColor(0.4f, 0.4f, 0.4f, 1);
                    //allowed is light gray, selected is white
                else if (a != index)
                    RenderSystem.setShaderColor(0.6f, 0.6f, 0.6f, 1);
                graphics.blit(radial, x, y, fixedU[a], fixedV[a], 200, 200, 600, 600);
                graphics.pose().popPose();

                //skill icon
                graphics.pose().pushPose();
                //RenderSystem.setShaderTexture(0, s.icon());
                Color c = s.getColor();
                RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                graphics.blit(s.icon(), x + iconX[a], y + iconY[a], 0, 0, 32, 32, 32, 32);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                graphics.pose().popPose();

                //cooldown overlay and mask
                if (cap.getSkillState(s) == Skill.STATE.COOLING) {
                    graphics.pose().pushPose();
                    //overlay mask
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.ZERO);
                    graphics.blit(s.icon(), x + iconX[a], y + iconY[a], 0, 0, 32, 32, 32, 32);
                    if (cap.getSkillData(s).orElse(SkillData.DUMMY).getMaxDuration() != 0) {
                        //cooldown spinny
                        float cd = cap.getSkillData(s).orElse(SkillData.DUMMY).getDuration();
                        float cdPerc = cd / cap.getSkillData(s).orElse(SkillData.DUMMY).getMaxDuration();
                        //RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                        RenderUtils.drawCooldownCircle(graphics.pose(), x + iconX[a], y + iconY[a], 32, cdPerc, false);
                        RenderSystem.disableBlend();

                        //cooldown number
                        String num = String.valueOf((int) cd);
                        if (Math.ceil(cd) != cd)
                            num = formatter.format(cd);
                        graphics.pose().pushPose();
                        RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
                        //AbstractGui.blit(matrixStack, x + iconX[a], y + iconY[a], 0, 0, 32, 32, 32, 32);
                        graphics.drawString(mc.font, String.valueOf(num), (int) (x + iconX[a] + 16 - mc.font.width(num) / 2f), y + iconY[a] + 12, 0xFFFFFF);
                        graphics.pose().popPose();
                    }
                    graphics.pose().popPose();
                } else if (cap.getSkillState(s) == Skill.STATE.ACTIVE) {
                    graphics.pose().pushPose();
                    int finalA = a;
                    cap.getSkillData(s).ifPresent((sd) -> {
                        RenderSystem.enableBlend();
                        //active mask
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.ZERO);
                        graphics.blit(s.icon(), x + iconX[finalA], y + iconY[finalA], 0, 0, 32, 32, 32, 32);
                        if (sd.getMaxDuration() != 0) {
                            //active spinny
                            float cdPerc = sd.getDuration() / sd.getMaxDuration();
                            RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                            float cd = sd.getDuration();
                            RenderSystem.setShaderColor(0.4f, 0.7f, 0.4f, 1);
                            RenderUtils.drawCooldownCircle(graphics.pose(), x + iconX[finalA], y + iconY[finalA], 32, cdPerc, false);

                            //active number
                            String num = String.valueOf((int) cd);
                            if (Math.ceil(cd) != cd)
                                num = formatter.format(cd);
                            graphics.pose().pushPose();
                            RenderSystem.setShaderTexture(0, RenderUtils.cooldown);
                            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
                            //AbstractGui.blit(matrixStack, x + iconX[a], y + iconY[a], 0, 0, 32, 32, 32, 32);
                            graphics.drawString(mc.font, String.valueOf(num), (int) (x + iconX[finalA] + 16 - mc.font.width(num) / 2f), y + iconY[finalA] + 12, 0xFFFFFF);
                            graphics.pose().popPose();
                        }
                        RenderSystem.disableBlend();
                    });
                    graphics.pose().popPose();
                }
            }
        }
        if (index >= 0 && elements[index] != null) {
            Skill selected = elements[index];
            String print = selected.getDisplayName(mc.player).getString();
            int yee = mc.font.width(print);
            graphics.drawString(mc.font, String.valueOf(print), (width - yee) / 2, 4, selected.getColor().getRGB());
            final Skill.CastStatus castStatus = selected.castingCheck(mc.player, CasterData.getCap(mc.player).getSkillData(selected).orElse(SkillData.DUMMY));
            if (castStatus != Skill.CastStatus.ALLOWED && castStatus != Skill.CastStatus.HOLSTERED && castStatus != Skill.CastStatus.ACTIVE) {
                switch (castStatus) {
                    case COOLDOWN:
                        print = Component.translatable("wardance.skill.cooldown").getString();
                        break;
                    case CONFLICT:
                        print = Component.translatable("wardance.skill.conflict").getString();
                        break;
                    case SILENCE:
                        print = Component.translatable("wardance.skill.silence").getString();
                        break;
                    case STYLE:
                        print = Component.translatable("wardance.skill.style").getString();
                        break;
                    case SPIRIT:
                        print = Component.translatable("wardance.skill.spirit", selected.spiritConsumption(mc.player)).getString();
                        break;
                    case MIGHT:
                        print = Component.translatable("wardance.skill.might", selected.mightConsumption(mc.player)).getString();
                        break;
                    case OTHER:
                        final ResourceLocation registryName = elements[index].getRegistryName();
                        print = Component.translatable(registryName.getNamespace()+"."+registryName.getPath() + ".requirement").getString();
                        break;
                }
                yee = mc.font.width(print);
                graphics.drawString(mc.font, String.valueOf(print), (width - yee) / 2, 12, Color.RED.getRGB());
            }

        }
        //RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
        graphics.pose().popPose();

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
                        .player.closeContainer();
            } else {
                this.minecraft.player.input.tick(this.minecraft.player.isMovingSlowly(), 1); //shouldRenderSneaking
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
        //ForgeIngameGui.renderCrosshairs = false;
    }

    @Override
    public void removed() {
        super.removed();
        //GLFW.glfwSetInputMode(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        //ForgeIngameGui.renderCrosshairs = true;
    }

    @Override
    public void onClose() {
        super.onClose();
        //ForgeIngameGui.renderCrosshairs = true;
    }

    @Override
    public boolean isPauseScreen() { //isPauseScreen
        return false;
    }

    protected boolean isKeyBindingStillPressed() {
        return Keybinds.CAST.isDown();
    }

    protected void selectAndClose() {
        onClose();
        if (exIndex >= 0 && elements[exIndex] != null)
            CombatChannel.INSTANCE.sendToServer(new SelectSkillPacket(exIndex));
    }
}