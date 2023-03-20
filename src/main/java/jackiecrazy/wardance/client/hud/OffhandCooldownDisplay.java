package jackiecrazy.wardance.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.client.RenderEvents;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.ForgeMod;

public class OffhandCooldownDisplay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON || player == null) return;
        if (Minecraft.getInstance().options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
            if (mc.getCameraEntity() instanceof Player p) {
                GlStateManager._clearColor(1.0F, 1.0F, 1.0F, 1.0F);
                ItemStack itemstack = p.getOffhandItem();
                HumanoidArm oppositeHand = p.getMainArm().getOpposite();
                int halfOfScreen = width / 2;

                //GlStateManager._enableRescaleNormal();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                //Lighting.turnBackOn();

                if (mc.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
                    float strength = CombatUtils.getCooledAttackStrength(p, InteractionHand.OFF_HAND, 0);
                    if (strength < 1.0F) {
                        int y = height - 20;
                        int x = halfOfScreen + 91 + 6;
                        if (oppositeHand == HumanoidArm.LEFT) {
                            x = halfOfScreen - 91 - 22;
                        }

                        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                        int modStrength = (int) (strength * 19.0F);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        mc.gui.blit(stack, x + 18, y, 0, 94, 18, 18);
                        mc.gui.blit(stack, x + 18, y + 18 - modStrength, 18, 112 - modStrength, 18, modStrength);
                    }
                }

                //Lighting.turnOff();
                RenderSystem.disableBlend();
            }
        } else if (mc.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
            float cooldown = CombatUtils.getCooledAttackStrength(player, InteractionHand.OFF_HAND, 0f);
            boolean hyperspeed = false;

            if (RenderEvents.getEntityLookedAt(player, GeneralUtils.getAttributeValueHandSensitive(player, ForgeMod.ATTACK_RANGE.get(), InteractionHand.OFF_HAND)) != null && cooldown >= 1.0F) {
                hyperspeed = CombatUtils.getCooldownPeriod(player, InteractionHand.OFF_HAND) > 5.0F;
                hyperspeed = hyperspeed & (RenderEvents.getEntityLookedAt(player, GeneralUtils.getAttributeValueHandSensitive(player, ForgeMod.ATTACK_RANGE.get(), InteractionHand.OFF_HAND))).isAlive();
            }

            int y = height / 2 - 7 - 7;
            int x = width / 2 - 8;

            if (hyperspeed) {
                mc.gui.blit(stack, x, y, 68, 94, 16, 16);
            } else if (cooldown < 1.0F) {
                int k = (int) (cooldown * 17.0F);
                mc.gui.blit(stack, x, y, 36, 94, 16, 4);
                mc.gui.blit(stack, x, y, 52, 94, k, 4);
            }
        }
    }
}
