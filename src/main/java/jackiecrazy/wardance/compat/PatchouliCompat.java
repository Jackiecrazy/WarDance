package jackiecrazy.wardance.compat;

import net.minecraft.resources.ResourceLocation;
import vazkii.patchouli.api.PatchouliAPI;

public class PatchouliCompat {
    private static final ResourceLocation MANUAL = new ResourceLocation("footwork:combat_manual");
    public static void openManualClient(){
        PatchouliAPI.get().openBookGUI(MANUAL);
    }
}
