package jackiecrazy.wardance.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Button.class)
public interface YouTestMyPatienceAccessor {
    @Accessor
    static Button.CreateNarration getDEFAULT_NARRATION() {throw new UnsupportedOperationException();}

    @Invoker("<init>")
    static Button createButton(int p_259075_, int p_259271_, int p_260232_, int p_260028_, Component p_259351_, Button.OnPress p_260152_, Button.CreateNarration p_259552_) {throw new UnsupportedOperationException();}
}
