package jackiecrazy.wardance.compat;

import jackiecrazy.wardance.config.GeneralConfig;
import net.minecraftforge.fml.ModList;

public class WarCompat {
    public static boolean elenaiDodge;
    public static boolean patchouli;

    public static void checkCompatStatus() {
        elenaiDodge = ModList.get().isLoaded("elenaidodge2") && GeneralConfig.elenai;
        patchouli = ModList.get().isLoaded("patchouli");
    }
}
