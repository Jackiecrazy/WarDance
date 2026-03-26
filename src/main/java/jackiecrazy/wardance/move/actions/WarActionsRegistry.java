package jackiecrazy.wardance.move.actions;

import jackiecrazy.footwork.move.action.ActionRegistry;
import jackiecrazy.footwork.move.action.ActionType;
import jackiecrazy.footwork.utils.ActionJsonAdapters;
import jackiecrazy.wardance.WarDance;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class WarActionsRegistry {
    public static final DeferredRegister<ActionType> ACTIONS = DeferredRegister.create(ActionRegistry.REGISTRY_NAME, WarDance.MODID);

    public static final RegistryObject<ActionType> LOAD_ITEM = ACTIONS.register("load_item", () -> (a) -> ActionJsonAdapters.gson.fromJson(a, LoadItemAction.class));
    public static final RegistryObject<ActionType> PLAY_INTERACTION = ACTIONS.register("play_interaction", () -> (a) -> ActionJsonAdapters.gson.fromJson(a, PlayInteractionAction.class));

}
