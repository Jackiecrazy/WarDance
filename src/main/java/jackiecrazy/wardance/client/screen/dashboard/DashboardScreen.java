package jackiecrazy.wardance.client.screen.dashboard;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class DashboardScreen extends Screen {
    /*
    The dashboard screen is completely dark. Buttons are added by each mod to open their respective guis.

     */
    private Player p;
    public DashboardScreen(Player player) {
        super(Component.translatable("wardance.gui.dashboard"));
        p = player;
        this.passEvents = true;
    }

    @Override
    protected void init() {
        //collect all pondering orbs
        //figure out how big each orb should be
        //scatter orbs with random velocity
    }

    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
    }
}
