package jackiecrazy.wardance.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public class NewSkillSelectionScreen extends Screen {
    protected NewSkillSelectionScreen(ITextComponent titleIn) {
        super(titleIn);
    }

    @Override
    public void init() {
        //stuff you need here
    }


    @Override
    public void tick() {
        //per tick things
    }

    //all rectangles should have a white trim
    @Override
    public void render(MatrixStack mStack, int mouseX, int mouseY, float partialTicks) {
        //draw base rectangle. Translucent dark and cover the left half of the screen with some padding
        //draw category rectangle. Opaque dark and cover the left of the base
        //fill category rectangle with octagons. Render their category icons
        //add text to top right base rectangle, skill name header and base descriptions

        //draw variation rectangle. Opaque dark and somewhat shorter and thinner than category rectangle
        //fill category rectangle with hexagons. Render their tinted icons
        //draw variation description rectangle. Opaque dark and fills the bottom gap between variation and end of base
        //add text to var desc rectangle, var name header and desc

        //draw skill grid quad to the side
    }


    @Override
    public void resize(Minecraft mc, int width, int height) {
        //squish hexagons and octagons into more scrolling
    }

    @Override
    public void closeScreen() {
        //finalize skill selection and send packet to server
    }
}

