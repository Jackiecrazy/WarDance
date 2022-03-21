package jackiecrazy.wardance.client;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.client.screen.SkillCastScreen;
import jackiecrazy.wardance.client.screen.SkillSelectionScreen;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.CombatModePacket;
import jackiecrazy.wardance.networking.EvokeSkillPacket;
import jackiecrazy.wardance.networking.ShoutPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class Keybinds {
    public static final IKeyConflictContext IN_COMBAT = new IKeyConflictContext() {

        @Override
        public boolean isActive() {
            return Minecraft.getInstance().player != null && CombatData.getCap(Minecraft.getInstance().player).isCombatMode() && !KeyConflictContext.GUI.isActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other != KeyConflictContext.GUI;
        }
    };
    public static final KeyBinding COMBAT = new KeyBinding("wardance.combat", KeyConflictContext.IN_GAME, KeyModifier.SHIFT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.gameplay");
    public static final KeyBinding CAST = new KeyBinding("wardance.skill", IN_COMBAT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.gameplay");
    public static final KeyBinding BINDCAST = new KeyBinding("wardance.bindCast", IN_COMBAT, InputMappings.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, "key.categories.gameplay");
    public static final KeyBinding SELECT = new KeyBinding("wardance.selectSkill", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.gameplay");
    public static final KeyBinding PARRY = new KeyBinding("wardance.parry", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.gameplay");
    public static final KeyBinding SHOUT = new KeyBinding("wardance.shout", IN_COMBAT, KeyModifier.SHIFT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_T, "key.categories.gameplay");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleInputEvent(InputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ICombatCapability itsc = CombatData.getCap(mc.player);
        if (COMBAT.getKeyConflictContext().isActive() && COMBAT.consumeClick()) {
            ClientEvents.combatTicks = Integer.MAX_VALUE;
            mc.player.displayClientMessage(new TranslationTextComponent("wardance.combat." + (itsc.isCombatMode() ? "off" : "on")), true);
            CombatChannel.INSTANCE.sendToServer(new CombatModePacket());
        }
        if (CAST.getKeyConflictContext().isActive() && CAST.consumeClick() && mc.player.isAlive()) {
            mc.setScreen(new SkillCastScreen(CasterData.getCap(mc.player).getEquippedSkills()));
        }
        if (SELECT.getKeyConflictContext().isActive() && SELECT.consumeClick() && mc.player.isAlive()) {
            mc.setScreen(new SkillSelectionScreen());
        }
        if (BINDCAST.getKeyConflictContext().isActive() && BINDCAST.consumeClick() && mc.player.isAlive()) {
            CombatChannel.INSTANCE.sendToServer(new EvokeSkillPacket());
        }
        if (SHOUT.getKeyConflictContext().isActive() && SHOUT.consumeClick() && mc.player.isAlive()) {
            CombatChannel.INSTANCE.sendToServer(new ShoutPacket(ClientConfig.shout));
        }
    }
}
