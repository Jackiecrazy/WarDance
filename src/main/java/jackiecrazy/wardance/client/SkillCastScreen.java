package jackiecrazy.wardance.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public abstract class SkillCastScreen extends Screen {
    protected final ArrayList<Skill> elements;
    protected float angle;

    public SkillCastScreen(ITextComponent title) {
        super(title);
        this.passEvents = true;
        this.elements = new ArrayList<>();
    }

    public void tick() {
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        //draw background, then slice, then each skill at determined points
        //don't draw slices that cannot be used (so they appear to be greyed out)
        //highlight a slice based on distance from center (to allow cancel leeway) and direction
        //listen to key release to send cast message to server
    }

    @Override
    protected void init() {
        super.init();
    }
}