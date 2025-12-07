package jackiecrazy.wardance.client;

import com.mojang.blaze3d.platform.InputConstants;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.IStyleCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.client.screen.skill.SkillCastScreen;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.combat.CombatModePacket;
import jackiecrazy.wardance.networking.combat.DodgePacket;
import jackiecrazy.wardance.networking.combat.HeavyPacket;
import jackiecrazy.wardance.networking.skill.EvokeSkillPacket;
import jackiecrazy.wardance.networking.skill.SelectSkillPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class Keybinds {
    public static final IKeyConflictContext IN_COMBAT = new IKeyConflictContext() {

        @Override
        public boolean isActive() {
            return Minecraft.getInstance().player != null && StylishData.getCap(Minecraft.getInstance().player).isCombatMode() && !KeyConflictContext.GUI.isActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other != KeyConflictContext.GUI;
        }
    };
    public static final List<KeyMapping> ALL = new ArrayList<>();
    public static final KeyMapping COMBAT = new KeyMapWrapper("wardance.combat", KeyConflictContext.IN_GAME, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.wardance");
    public static final KeyMapping CAST = new KeyMapWrapper("wardance.skill", IN_COMBAT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.wardance");
    public static final KeyMapping BINDCAST = new KeyMapWrapper("wardance.bindCast", IN_COMBAT, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, "key.categories.wardance");
    public static final KeyMapping DODGE = new KeyMapWrapper("wardance.dodge", IN_COMBAT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.wardance");
    public static final KeyMapping FINISHER = new KeyMapWrapper("wardance.finisher", IN_COMBAT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Q, "key.categories.wardance");
    public static final KeyMapping EVOKE = new KeyMapWrapper("wardance.evoke", IN_COMBAT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.wardance");
    //center, top right, down clockwise
    public static final KeyMapping[] SKILL = {
            new KeyMapWrapper("wardance.skill1", IN_COMBAT, InputConstants.UNKNOWN, "key.categories.wardance"),
            new KeyMapWrapper("wardance.skill2", IN_COMBAT, InputConstants.UNKNOWN, "key.categories.wardance"),
            new KeyMapWrapper("wardance.skill3", IN_COMBAT, InputConstants.UNKNOWN, "key.categories.wardance"),
            new KeyMapWrapper("wardance.skill4", IN_COMBAT, InputConstants.UNKNOWN, "key.categories.wardance"),
            new KeyMapWrapper("wardance.skill5", IN_COMBAT, InputConstants.UNKNOWN, "key.categories.wardance")
    };

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleInputEvent(InputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        IStyleCapability itsc = StylishData.getCap(mc.player);
        if (COMBAT.getKeyConflictContext().isActive() && COMBAT.consumeClick()) {
            ClientEvents.combatTicks = itsc.isCombatMode() ? -999 : Integer.MAX_VALUE;
            mc.player.displayClientMessage(Component.translatable("wardance.combat." + (itsc.isCombatMode() ? "off" : "on")), true);
            itsc.toggleCombatMode(!itsc.isCombatMode());
            CombatChannel.INSTANCE.sendToServer(new CombatModePacket());
        }
        if (CAST.getKeyConflictContext().isActive() && CAST.consumeClick() && mc.player.isAlive()) {
            mc.setScreen(new SkillCastScreen(CasterData.getCap(mc.player).getEquippedSkills()));
        }
        if (DODGE.getKeyConflictContext().isActive() && DODGE.consumeClick() && mc.player.isAlive() && CombatData.getCap(mc.player).canDodge()) {

            //slide>front>side>back(default)
            //left back right forward
            int side = 1;
            if (ClientEvents.heavy(WeaponStats.SWEEPSTATE.SPRINTING)) {
                side = 3;
            }
            else {
                if (mc.player.input.left)
                    side = 0;
                if (mc.player.input.right)
                    side = 2;
                if (mc.player.input.up)
                    side = 3;
                if (mc.player.isSprinting()) {
                    side = 99;
                    mc.player.setForcedPose(Pose.SLEEPING);
                }
            }
            CombatChannel.INSTANCE.sendToServer(new DodgePacket(side));
        }
        for (int x = 0; x < SKILL.length; x++) {
            if (SKILL[x].getKeyConflictContext().isActive() && SKILL[x].consumeClick())
                CombatChannel.INSTANCE.sendToServer(new SelectSkillPacket(x));
        }
        if (BINDCAST.getKeyConflictContext().isActive() && BINDCAST.consumeClick() && mc.player.isAlive()) {
            //I think this cancels pick block?
            BINDCAST.setDown(false);
            CombatChannel.INSTANCE.sendToServer(new EvokeSkillPacket());
        }
        FINISHER.consumeClick();
    }

    public static class KeyMapWrapper extends KeyMapping {
        public KeyMapWrapper(String p_90821_, int p_90822_, String p_90823_) {
            super(p_90821_, p_90822_, p_90823_);
            ALL.add(this);
        }

        public KeyMapWrapper(String p_90825_, InputConstants.Type p_90826_, int p_90827_, String p_90828_) {
            super(p_90825_, p_90826_, p_90827_, p_90828_);
            ALL.add(this);
        }

        public KeyMapWrapper(String description,
                             IKeyConflictContext keyConflictContext,
                             InputConstants.Type inputType,
                             int keyCode,
                             String category) {
            super(description, keyConflictContext, inputType, keyCode, category);
            ALL.add(this);
        }

        public KeyMapWrapper(String description,
                             IKeyConflictContext keyConflictContext,
                             InputConstants.Key keyCode,
                             String category) {
            super(description, keyConflictContext, keyCode, category);
            ALL.add(this);
        }

        public KeyMapWrapper(String description,
                             IKeyConflictContext keyConflictContext,
                             KeyModifier keyModifier,
                             InputConstants.Type inputType,
                             int keyCode,
                             String category) {
            super(description, keyConflictContext, keyModifier, inputType, keyCode, category);
            ALL.add(this);
        }

        public KeyMapWrapper(String description,
                             IKeyConflictContext keyConflictContext,
                             KeyModifier keyModifier,
                             InputConstants.Key keyCode,
                             String category) {
            super(description, keyConflictContext, keyModifier, keyCode, category);
            ALL.add(this);
        }
    }
}
