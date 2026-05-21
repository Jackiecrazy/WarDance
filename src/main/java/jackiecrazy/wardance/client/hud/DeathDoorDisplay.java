package jackiecrazy.wardance.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.client.GuiComponent;
import jackiecrazy.wardance.WarDance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

public class DeathDoorDisplay implements IGuiOverlay {
    private static final ResourceLocation DEATHDOOR = new ResourceLocation(WarDance.MODID, "textures/hud/deathdoor.png");
    private static final ResourceLocation RECOVERY = new ResourceLocation(WarDance.MODID, "textures/hud/deathdoor_recovery.png");
    private double alpha;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (StylishData.getCap(player).isDeathDoor())
            alpha += 0.1;
        else alpha -= 0.01;
        if (alpha > 1) alpha =1;
        if (alpha < 0) alpha = 0;
        float dangerzone = StylishData.getCap(player).isDyingFast()? Mth.sin(player.tickCount)/3:0;
        if (alpha > 0) {
            guiGraphics.pose().pushPose();
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, (float) alpha+dangerzone);
            //RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.ZERO);
            GuiComponent.blit(guiGraphics.pose(), DEATHDOOR, 0, 0, 0, 0, width, height, width, height);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            guiGraphics.pose().popPose();
        }
        if(!StylishData.getCap(player).canTrigger()){
            guiGraphics.pose().pushPose();
            RenderSystem.enableBlend();
            GuiComponent.blit(guiGraphics.pose(), RECOVERY, 0, 0, 0, 0, width, height, width, height);
            guiGraphics.pose().popPose();
        }
    }
}
