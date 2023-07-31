package jackiecrazy.wardance.client;

import com.mojang.blaze3d.platform.InputConstants;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.client.screen.SkillCastScreen;
import jackiecrazy.wardance.client.screen.SkillSelectionScreen;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.CombatModePacket;
import jackiecrazy.wardance.networking.EvokeSkillPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
    public static final KeyMapping COMBAT = new KeyMapping("wardance.combat", KeyConflictContext.IN_GAME, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.gameplay");
    public static final KeyMapping CAST = new KeyMapping("wardance.skill", IN_COMBAT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.gameplay");
    public static final KeyMapping BINDCAST = new KeyMapping("wardance.bindCast", IN_COMBAT, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, "key.categories.gameplay");
    public static final KeyMapping SELECT = new KeyMapping("wardance.selectSkill", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.gameplay");
    public static final KeyMapping PARRY = new KeyMapping("wardance.parry", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.gameplay");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleInputEvent(InputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ICombatCapability itsc = CombatData.getCap(mc.player);
        if (COMBAT.getKeyConflictContext().isActive() && COMBAT.consumeClick()) {
            ClientEvents.combatTicks = Integer.MAX_VALUE;
            mc.player.displayClientMessage(Component.translatable("wardance.combat." + (itsc.isCombatMode() ? "off" : "on")), true);
            CombatChannel.INSTANCE.sendToServer(new CombatModePacket());
        }
        if (CAST.getKeyConflictContext().isActive() && CAST.consumeClick() && mc.player.isAlive()) {
            mc.setScreen(new SkillCastScreen(CasterData.getCap(mc.player).getEquippedSkills()));
        }
        if (SELECT.getKeyConflictContext().isActive() && SELECT.consumeClick() && mc.player.isAlive()) {
            //hard no go
            if(!PermissionData.getCap(mc.player).canSelectSkills()) {
                return;
            }
            mc.setScreen(new SkillSelectionScreen());
        }
        if (BINDCAST.getKeyConflictContext().isActive() && BINDCAST.consumeClick() && mc.player.isAlive()) {
            CombatChannel.INSTANCE.sendToServer(new EvokeSkillPacket());
        }
    }
}
