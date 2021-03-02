package jackiecrazy.wardance.compat;

import jackiecrazy.wardance.config.CombatConfig;
import net.minecraftforge.fml.ModList;

public class WarCompat {
    public static boolean elenaiDodge;

    public static void checkCompatStatus() {
        elenaiDodge = ModList.get().isLoaded("elenaidodge2") && CombatConfig.elenai;
    }
}
