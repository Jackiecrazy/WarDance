package jackiecrazy.wardance;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class WarSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, WarDance.MODID);

    public static final Supplier<SoundEvent> OMAE =
            SOUND_EVENTS.register("omae",
                                  () -> SoundEvent.createVariableRangeEvent(
                                          ResourceLocation.tryBuild(WarDance.MODID, "omae")));
    public static final Supplier<SoundEvent> MOU =
            SOUND_EVENTS.register("shindeiru",
                                  () -> SoundEvent.createVariableRangeEvent(
                                          ResourceLocation.tryBuild(WarDance.MODID, "shindeiru")));
}
