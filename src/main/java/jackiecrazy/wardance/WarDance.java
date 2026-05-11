package jackiecrazy.wardance;

import jackiecrazy.footwork.client.render.ItemEntityRenderer;
import jackiecrazy.wardance.capability.permission.IPermission;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.IMark;
import jackiecrazy.wardance.client.GrappleRenderer;
import jackiecrazy.wardance.client.hud.*;
import jackiecrazy.wardance.command.CategoryArgument;
import jackiecrazy.wardance.command.SkillArgument;
import jackiecrazy.wardance.command.WarDanceCommand;
import jackiecrazy.wardance.compat.ElenaiCompat;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.*;
import jackiecrazy.wardance.config.weapon.TwohandingStats;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.items.WarItems;
import jackiecrazy.wardance.loot.ScrollLootModifier;
import jackiecrazy.wardance.move.actions.WarActionsRegistry;
import jackiecrazy.wardance.networking.*;
import jackiecrazy.wardance.networking.combat.*;
import jackiecrazy.wardance.networking.meta.*;
import jackiecrazy.wardance.networking.movement.*;
import jackiecrazy.wardance.networking.skill.EvokeSkillPacket;
import jackiecrazy.wardance.networking.skill.SelectSkillPacket;
import jackiecrazy.wardance.networking.skill.UpdateMarkPacket;
import jackiecrazy.wardance.networking.skill.UpdateSkillSelectionPacket;
import jackiecrazy.wardance.networking.sync.*;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("wardance")
public class WarDance {
    public static final String MODID = "wardance";
    public static final Random rand = new Random();
    public static int lastReport = 0;

    public static final Logger LOGGER = LogManager.getLogger();
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> WARTAB = TABS.register("scrolls", ()->CreativeModeTab.builder().icon(()->new ItemStack(WarItems.SCROLL.get())).title(Component.translatable("itemGroup.wardance.scrolls")).build());
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, "forge");
    private static final RegistryObject<SingletonArgumentInfo<SkillArgument>> WARDANCE_COMMAND_SKI_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("war_skills", () ->
            ArgumentTypeInfos.registerByClass(SkillArgument.class,
                    SingletonArgumentInfo.contextFree(SkillArgument::skill)));
    private static final RegistryObject<SingletonArgumentInfo<CategoryArgument>> WARDANCE_COMMAND_CAT_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("war_categories", () ->
            ArgumentTypeInfos.registerByClass(CategoryArgument.class,
                    SingletonArgumentInfo.contextFree(CategoryArgument::color)));

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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::gui);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::register);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(MODID));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GeneralConfig.CONFIG_SPEC, MODID + "/general.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, StealthConfig.CONFIG_SPEC, MODID + "/stealth.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CombatConfig.CONFIG_SPEC, MODID + "/combat.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LootConfig.CONFIG_SPEC, MODID + "/loot.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SkillConfig.CONFIG_SPEC, MODID + "/skill.toml");
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ItemConfig.CONFIG_SPEC, MODID + "/items.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ResourceConfig.CONFIG_SPEC, MODID + "/resources.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC, MODID + "/client.toml");
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        WarSkills.SUPPLIER = WarSkills.SKILLS.makeRegistry(RegistryBuilder::new);
        WarSkills.SKILLS.register(bus);
        WarEntities.ENTITIES.register(bus);
        WarItems.ITEMS.register(bus);
        WarActionsRegistry.ACTIONS.register(bus);
        TABS.register(bus);
        COMMAND_ARGUMENT_TYPES.register(bus);
        MinecraftForge.EVENT_BUS.addListener(this::commands);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        //packets
        int index = 0;
        CombatChannel.INSTANCE.registerMessage(index++, UpdateClientResourcePacket.class, new UpdateClientResourcePacket.Encoder(), new UpdateClientResourcePacket.Decoder(), new UpdateClientResourcePacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateClientStylePacket.class, new UpdateClientStylePacket.Encoder(), new UpdateClientStylePacket.Decoder(), new UpdateClientStylePacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateMarkPacket.class, new UpdateMarkPacket.Encoder(), new UpdateMarkPacket.Decoder(), new UpdateMarkPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateAttackCooldownPacket.class, new UpdateAttackCooldownPacket.Encoder(), new UpdateAttackCooldownPacket.Decoder(), new UpdateAttackCooldownPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, DodgePacket.class, new DodgePacket.Encoder(), new DodgePacket.Decoder(), new DodgePacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, RequestUpdatePacket.class, new RequestUpdatePacket.Encoder(), new RequestUpdatePacket.Decoder(), new RequestUpdatePacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, CombatModePacket.class, new CombatModePacket.Encoder(), new CombatModePacket.Decoder(), new CombatModePacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, RequestSweepPacket.class, new RequestSweepPacket.Encoder(), new RequestSweepPacket.Decoder(), new RequestSweepPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, HeavyPacket.class, new HeavyPacket.Encoder(), new HeavyPacket.Decoder(), new HeavyPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateWeaponRenderPacket.class, new UpdateWeaponRenderPacket.Encoder(), new UpdateWeaponRenderPacket.Decoder(), new UpdateWeaponRenderPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, RequestAttackPacket.class, new RequestAttackPacket.Encoder(), new RequestAttackPacket.Decoder(), new RequestAttackPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, SelectSkillPacket.class, new SelectSkillPacket.Encoder(), new SelectSkillPacket.Decoder(), new SelectSkillPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, EvokeSkillPacket.class, new EvokeSkillPacket.Encoder(), new EvokeSkillPacket.Decoder(), new EvokeSkillPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateSkillSelectionPacket.class, new UpdateSkillSelectionPacket.Encoder(), new UpdateSkillSelectionPacket.Decoder(), new UpdateSkillSelectionPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, SyncSkillPacket.class, new SyncSkillPacket.Encoder(), new SyncSkillPacket.Decoder(), new SyncSkillPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateTargetPacket.class, new UpdateTargetPacket.Encoder(), new UpdateTargetPacket.Decoder(), new UpdateTargetPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, SyncItemDataPacket.class, new SyncItemDataPacket.Encoder(), new SyncItemDataPacket.Decoder(), new SyncItemDataPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, SyncTagDataPacket.class, new SyncTagDataPacket.Encoder(), new SyncTagDataPacket.Decoder(), new SyncTagDataPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateClientPermissionPacket.class, new UpdateClientPermissionPacket.Encoder(), new UpdateClientPermissionPacket.Decoder(), new UpdateClientPermissionPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, LearnScrollPacket.class, new LearnScrollPacket.Encoder(), new LearnScrollPacket.Decoder(), new LearnScrollPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, OpenScrollScreenPacket.class, new OpenScrollScreenPacket.Encoder(), new OpenScrollScreenPacket.Decoder(), new OpenScrollScreenPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, OpenManualScreenPacket.class, new OpenManualScreenPacket.Encoder(), new OpenManualScreenPacket.Decoder(), new OpenManualScreenPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, ManualizePacket.class, new ManualizePacket.Encoder(), new ManualizePacket.Decoder(), new ManualizePacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, LearnManualPacket.class, new LearnManualPacket.Encoder(), new LearnManualPacket.Decoder(), new LearnManualPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, TwoHandItemDataPacket.class, new TwoHandItemDataPacket.Encoder(), new TwoHandItemDataPacket.Decoder(), new TwoHandItemDataPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, TwoHandTagDataPacket.class, new TwoHandTagDataPacket.Encoder(), new TwoHandTagDataPacket.Decoder(), new TwoHandTagDataPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, ThrowPacket.class, new ThrowPacket.Encoder(), new ThrowPacket.Decoder(), new ThrowPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, GrapplePacket.class, new GrapplePacket.Encoder(), new GrapplePacket.Decoder(), new GrapplePacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UnhookPacket.class, new UnhookPacket.Encoder(), new UnhookPacket.Decoder(), new UnhookPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateWeaponFramePacket.class, new UpdateWeaponFramePacket.Encoder(), new UpdateWeaponFramePacket.Decoder(), new UpdateWeaponFramePacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, KickPacket.class, new KickPacket.Encoder(), new KickPacket.Decoder(), new KickPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, SyncQuiverPacket.class, new SyncQuiverPacket.Encoder(), new SyncQuiverPacket.Decoder(), new SyncQuiverPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, SwapAttackPacket.class, new SwapAttackPacket.Encoder(), new SwapAttackPacket.Decoder(), new SwapAttackPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateWallPacket.class, new UpdateWallPacket.Encoder(), new UpdateWallPacket.Decoder(), new UpdateWallPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, ResetAirJumpPacket.class, new ResetAirJumpPacket.Encoder(), new ResetAirJumpPacket.Decoder(), new ResetAirJumpPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateAirPacket.class, new UpdateAirPacket.Encoder(), new UpdateAirPacket.Decoder(), new UpdateAirPacket.Handler());
        CombatChannel.INSTANCE.registerMessage(index++, AerialModePacket.class, new AerialModePacket.Encoder(), new AerialModePacket.Decoder(), new AerialModePacket.Handler());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        ClientConfig.bake();
        EntityRenderers.register(WarEntities.WEAPON.get(), ItemEntityRenderer::new);
        EntityRenderers.register(WarEntities.GRAPPLE.get(), GrappleRenderer::new);
        EntityRenderers.register(WarEntities.FLYING_BLOCK.get(), ItemEntityRenderer::new);
        EntityRenderers.register(WarEntities.THROWN_WEAPON.get(), ItemEntityRenderer::new);
    }


    @SubscribeEvent
    public void onJsonListener(AddReloadListenerEvent event) {
        WeaponStats.register(event);
        TwohandingStats.register(event);
        MobSpecs.register(event);
    }

    public void register(RegisterEvent e) {
        if (e.getForgeRegistry() == (Object) ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get()) {
            e.getForgeRegistry().register("scrolls", ScrollLootModifier.CODEC);
        }
    }

    private void gui(final RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "offhandcd", new OffhandCooldownDisplay());
        event.registerAboveAll("pwdresources", new ResourceDisplay());
        event.registerAboveAll("pwdskills", new SkillCoolDisplay());
        event.registerAboveAll("pwdmarks", new MarkDisplay());
        event.registerAboveAll("pwdthrowingquiver", new QuiverDisplay());
        event.registerAboveAll("pwdeath", new DeathDoorDisplay());
    }

    private void caps(final RegisterCapabilitiesEvent event) {
        event.register(IMark.class);
        event.register(ISkillCapability.class);
        event.register(IPermission.class);
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
