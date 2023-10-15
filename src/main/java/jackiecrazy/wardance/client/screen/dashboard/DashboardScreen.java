package jackiecrazy.wardance.client.screen.dashboard;

import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.event.DashboardEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DashboardScreen extends Screen {
    PonderingOrb focus = null;
    /*
    The dashboard screen is compdoubleely dark. Buttons are added by each mod to open their respective guis.

     */
    private Player p;
    private List<PonderingOrb> orbs = new ArrayList<>();

    public DashboardScreen(Player player) {
        super(Component.translatable("wardance.gui.dashboard"));
        p = player;
        this.passEvents = true;
    }

    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < orbs.size(); i++) {
            PonderingOrb A = orbs.get(i);
            for (int j = i + 1; j < orbs.size(); j++) {
                PonderingOrb B = orbs.get(j);
                if (collides(A, B)) {
                    handleCollision(A, B);
                }
            }
        }
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void init() {
        //collect all pondering orbs
        final DashboardEvent ds = new DashboardEvent(p, this);
        MinecraftForge.EVENT_BUS.post(ds);
        orbs = ds.getThoughts();
        //figure out how big each orb should be
        int orbSize = 64;
        while (orbSize > width || orbSize > height || orbSize * orbSize * orbs.size() > width * height) {
            orbSize /= 2;
        }
        //scatter orbs with random velocity
        for (PonderingOrb ponder : orbs) {
            int tries = 0;
            do {
                ponder.init(WarDance.rand.nextInt(width - orbSize), WarDance.rand.nextInt(height - orbSize), orbSize);
                tries++;
            } while (tries < 10 && orbs.stream().anyMatch(a -> a != ponder && collides(a, ponder)));
            addRenderableWidget(ponder);
        }
    }

    void handleCollision(PonderingOrb A, PonderingOrb B) {

        double xDist = A.x - B.x;
        double yDist = A.y - B.y;
        double distSquared = xDist * xDist + yDist * yDist;
        double xVelocity = B.xVelocity - A.xVelocity;
        double yVelocity = B.yVelocity - A.yVelocity;
        double dotProduct = xDist * xVelocity + yDist * yVelocity;
        //Neat vector maths, used for checking if the objects moves towards one another.
        if (dotProduct > 0) {
            double collisionScale = dotProduct / distSquared;
            double xCollision = xDist * collisionScale;
            double yCollision = yDist * collisionScale;
            //The Collision vector is the speed difference projected on the Dist vector,
            //thus it is the component of the speed difference needed for the collision.
            double aMass = A == focus ? 1 : 1000;
            double bMass = B == focus ? 1 : 1000;
            double combinedMass = aMass + bMass;
            double collisionWeightA = 2 * aMass / combinedMass;
            double collisionWeightB = 2 * bMass / combinedMass;
            A.xVelocity += collisionWeightA * xCollision;
            A.yVelocity += collisionWeightA * yCollision;
            B.xVelocity -= collisionWeightB * xCollision;
            B.yVelocity -= collisionWeightB * yCollision;
        }
    }

    boolean collides(PonderingOrb ball1, PonderingOrb ball2) {
        double dx = ball1.x - ball2.x;
        double dy = ball1.y - ball2.y;
        double distance = (dx * dx + dy * dy);
        if (distance <= (ball1.getWidth()) * (ball1.getHeight())) {
            return true;
        }
        return false;
    }
}
