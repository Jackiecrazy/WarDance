package jackiecrazy.wardance;

import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.IMark;
import jackiecrazy.wardance.client.Keybinds;
import jackiecrazy.wardance.client.hud.OffhandCooldownDisplay;
import jackiecrazy.wardance.client.hud.ResourceDisplay;
import jackiecrazy.wardance.command.WarDanceCommand;
import jackiecrazy.wardance.compat.ElenaiCompat;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.*;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.networking.*;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("wardance")
public class WarDance {
    public static final String MODID = "wardance";
    public static final Random rand = new Random();

    public static final Logger LOGGER = LogManager.getLogger();
    public static final GameRules.Key<GameRules.BooleanValue> GATED_SKILLS = GameRules.register("lockWarSkills", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false)); //Blessed be the TF

    public WarDance() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::caps);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::keys);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(MODID), MODID);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GeneralConfig.CONFIG_SPEC, MODID + "/general.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, StealthConfig.CONFIG_SPEC, MODID + "/stealth.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CombatConfig.CONFIG_SPEC, MODID + "/combat.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ItemConfig.CONFIG_SPEC, MODID + "/items.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ResourceConfig.CONFIG_SPEC, MODID + "/resources.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC, MODID + "/client.toml");
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        WarSkills.SUPPLIER = WarSkills.SKILLS.makeRegistry(RegistryBuilder::new);
        WarSkills.SKILLS.register(bus);
        WarEntities.ENTITIES.register(bus);
        MinecraftForge.EVENT_BUS.addListener(this::commands);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        int index = 0;
        CombatChannel.INSTANCE.registerMessage(index++, UpdateClientPacket.class, new UpdateClientPacket.UpdateClientEncoder(), new UpdateClientPacket.UpdateClientDecoder(), new UpdateClientPacket.UpdateClientHandler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateAfflictionPacket.class, new UpdateAfflictionPacket.UpdateClientEncoder(), new UpdateAfflictionPacket.UpdateClientDecoder(), new UpdateAfflictionPacket.UpdateClientHandler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateAttackPacket.class, new UpdateAttackPacket.UpdateAttackEncoder(), new UpdateAttackPacket.UpdateAttackDecoder(), new UpdateAttackPacket.UpdateAttackHandler());
        CombatChannel.INSTANCE.registerMessage(index++, DodgePacket.class, new DodgePacket.DodgeEncoder(), new DodgePacket.DodgeDecoder(), new DodgePacket.DodgeHandler());
        CombatChannel.INSTANCE.registerMessage(index++, RequestUpdatePacket.class, new RequestUpdatePacket.RequestUpdateEncoder(), new RequestUpdatePacket.RequestUpdateDecoder(), new RequestUpdatePacket.RequestUpdateHandler());
        CombatChannel.INSTANCE.registerMessage(index++, CombatModePacket.class, new CombatModePacket.CombatEncoder(), new CombatModePacket.CombatDecoder(), new CombatModePacket.CombatHandler());
        CombatChannel.INSTANCE.registerMessage(index++, RequestSweepPacket.class, new RequestSweepPacket.RequestSweepEncoder(), new RequestSweepPacket.RequestSweepDecoder(), new RequestSweepPacket.RequestSweepHandler());
        CombatChannel.INSTANCE.registerMessage(index++, RequestAttackPacket.class, new RequestAttackPacket.RequestAttackEncoder(), new RequestAttackPacket.RequestAttackDecoder(), new RequestAttackPacket.RequestAttackHandler());
        CombatChannel.INSTANCE.registerMessage(index++, SelectSkillPacket.class, new SelectSkillPacket.CombatEncoder(), new SelectSkillPacket.CombatDecoder(), new SelectSkillPacket.CombatHandler());
        CombatChannel.INSTANCE.registerMessage(index++, EvokeSkillPacket.class, new EvokeSkillPacket.EvokeEncoder(), new EvokeSkillPacket.EvokeDecoder(), new EvokeSkillPacket.EvokeHandler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateSkillSelectionPacket.class, new UpdateSkillSelectionPacket.UpdateSkillEncoder(), new UpdateSkillSelectionPacket.UpdateSkillDecoder(), new UpdateSkillSelectionPacket.UpdateSkillHandler());
        CombatChannel.INSTANCE.registerMessage(index++, SyncSkillPacket.class, new SyncSkillPacket.SyncSkillEncoder(), new SyncSkillPacket.SyncSkillDecoder(), new SyncSkillPacket.SyncSkillHandler());
        CombatChannel.INSTANCE.registerMessage(index++, ManualParryPacket.class, new ManualParryPacket.ParryEncoder(), new ManualParryPacket.ParryDecoder(), new ManualParryPacket.ParryHandler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateTargetPacket.class, new UpdateTargetPacket.UpdateTargetEncoder(), new UpdateTargetPacket.UpdateTargetDecoder(), new UpdateTargetPacket.UpdateTargetHandler());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        ClientConfig.bake();
    }

    private void keys(final RegisterKeyMappingsEvent event) {
        event.register(Keybinds.COMBAT);
        event.register(Keybinds.CAST);
        event.register(Keybinds.SELECT);
        event.register(Keybinds.BINDCAST);
        event.register(Keybinds.PARRY);
    }

    private void gui(final RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "offhandcd", new OffhandCooldownDisplay());
        event.registerAboveAll("pwdresources", new ResourceDisplay());
    }

    private void caps(final RegisterCapabilitiesEvent event) {
        event.register(IMark.class);
        event.register(ISkillCapability.class);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        //InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        WarCompat.checkCompatStatus();
        if (WarCompat.elenaiDodge)
            MinecraftForge.EVENT_BUS.register(ElenaiCompat.class);
    }

    private void commands(final RegisterCommandsEvent event) {
        WarDanceCommand.register(event.getDispatcher());
    }
}
