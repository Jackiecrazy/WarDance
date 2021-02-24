package jackiecrazy.wardance.client;

import net.java.games.input.Controller;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;

public class Keybinds {
    public static final KeyBinding COMBAT=new KeyBinding("wardance.combat", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, 92, "key.categories.gameplay");
}
