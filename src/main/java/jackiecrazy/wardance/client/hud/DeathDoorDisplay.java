package jackiecrazy.wardance.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.client.GuiComponent;
import jackiecrazy.wardance.WarDance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

public class DeathDoorDisplay implements IGuiOverlay {
    private static final ResourceLocation DEATHDOOR = new ResourceLocation(WarDance.MODID,"textures/hud/deathdoor.png");

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(StylishData.getCap(player).isDeathDoor()){
            guiGraphics.pose().pushPose();
            RenderSystem.enableBlend();
            //RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.ZERO);
            GuiComponent.blit(guiGraphics.pose(), DEATHDOOR, 0, 0, 0, 0, width, height, width, height);
            guiGraphics.pose().popPose();
        }
    }
}
