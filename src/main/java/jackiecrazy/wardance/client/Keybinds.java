package jackiecrazy.wardance.client;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.client.screen.SkillCastScreen;
import jackiecrazy.wardance.client.screen.SkillSelectionScreen;
import jackiecrazy.wardance.networking.CastSkillPacket;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.CombatModePacket;
import jackiecrazy.wardance.skill.Skill;
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

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class Keybinds {
    public static final IKeyConflictContext IN_COMBAT = new IKeyConflictContext() {

        @Override
        public boolean isActive() {
            return CombatData.getCap(Minecraft.getInstance().player).isCombatMode() && !KeyConflictContext.GUI.isActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other != KeyConflictContext.GUI;
        }
    };
    public static final KeyBinding COMBAT = new KeyBinding("wardance.combat", KeyConflictContext.IN_GAME, KeyModifier.SHIFT, InputMappings.Type.KEYSYM, 82, "key.categories.gameplay");
    public static final KeyBinding CAST = new KeyBinding("wardance.skill", IN_COMBAT, InputMappings.Type.KEYSYM, 82, "key.categories.gameplay");
    public static final KeyBinding QUICKCAST = new KeyBinding("wardance.quickSkill", IN_COMBAT, InputMappings.Type.MOUSE, 2, "key.categories.gameplay");
    public static final KeyBinding SELECT = new KeyBinding("wardance.selectSkill", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, 86, "key.categories.gameplay");
    public static Skill quick = null;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleInputEvent(InputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ICombatCapability itsc = CombatData.getCap(mc.player);
        if (COMBAT.getKeyConflictContext().isActive() && COMBAT.isPressed()) {
            mc.player.sendStatusMessage(new TranslationTextComponent("wardance.combat." + (itsc.isCombatMode() ? "off" : "on")), true);
            CombatChannel.INSTANCE.sendToServer(new CombatModePacket());
        }
        if (CAST.getKeyConflictContext().isActive() && CAST.isPressed() && mc.player.isAlive()) {
            mc.displayGuiScreen(new SkillCastScreen(CasterData.getCap(mc.player).getEquippedSkills()));
        }
        if (SELECT.getKeyConflictContext().isActive() && SELECT.isPressed() && mc.player.isAlive()) {
            mc.displayGuiScreen(new SkillSelectionScreen());
        }
        if (quick != null && QUICKCAST.getKeyConflictContext().isActive() && QUICKCAST.isPressed() && mc.player.isAlive()) {
            CombatChannel.INSTANCE.sendToServer(new CastSkillPacket(quick.getRegistryName()));
        }
    }
}
