package jackiecrazy.wardance.client.screen;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SkillCategoryButton extends ImageButton {
    private SkillSelectionScreen parent;
    public SkillCategoryButton(SkillSelectionScreen screen, int p_94242_, int p_94243_, int p_94244_, int p_94245_, int p_94246_, int p_94247_, int p_94248_, ResourceLocation p_94249_, int p_94250_, int p_94251_, OnPress p_94252_, OnTooltip p_94253_, Component p_94254_) {
        super(p_94242_, p_94243_, p_94244_, p_94245_, 0, 0, 0, p_94249_, 16, 16, p_94252_, p_94253_, p_94254_);
        parent=screen;

    }
}
