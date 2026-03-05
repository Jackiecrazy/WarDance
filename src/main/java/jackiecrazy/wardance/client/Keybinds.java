package jackiecrazy.wardance.client;

import com.mojang.blaze3d.platform.InputConstants;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.stylish.IStyleCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.capability.aerial.IAerialMode;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.client.screen.skill.SkillCastScreen;
import jackiecrazy.wardance.config.QiCosts;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.combat.CombatModePacket;
import jackiecrazy.wardance.networking.combat.DodgePacket;
import jackiecrazy.wardance.networking.combat.GrapplePacket;
import jackiecrazy.wardance.networking.combat.KickPacket;
import jackiecrazy.wardance.networking.skill.EvokeSkillPacket;
import jackiecrazy.wardance.networking.skill.SelectSkillPacket;
import jackiecrazy.wardance.utils.MobilityUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    public static final KeyMapping ALTERNATE_KEY = new KeyMapWrapper("wardance.bindCast", IN_COMBAT, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, "key.categories.wardance");
    public static final KeyMapping DODGE = new KeyMapWrapper("wardance.dodge", IN_COMBAT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.categories.wardance");
    public static final KeyMapping THROW = new KeyMapWrapper("wardance.throw", IN_COMBAT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Q, "key.categories.wardance");
    public static final KeyMapping SWAP = new KeyMapWrapper("wardance.swap", IN_COMBAT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F, "key.categories.wardance");
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
//            if (ClientEvents.heavy(WeaponStats.SWEEPSTATE.SPRINTING)) {
//                side = 3;
//            }
//            else
            {
                if (mc.player.input.left)
                    side = 0;
                if (mc.player.input.right)
                    side = 2;
                if (mc.player.input.up)
                    side = 3;
                if (mc.player.isSprinting()&&mc.player.onGround()) {
                    side = 99;
                    mc.player.setForcedPose(Pose.SLEEPING);
                }
            }
            AerialModeData.getCap(mc.player).setState(IAerialMode.WallState.STICKY);
            MobilityUtils.attemptDodge(mc.player, side);
            CombatChannel.INSTANCE.sendToServer(new DodgePacket(side));
        }
        for (int x = 0; x < SKILL.length; x++) {
            if (SKILL[x].getKeyConflictContext().isActive() && SKILL[x].consumeClick())
                CombatChannel.INSTANCE.sendToServer(new SelectSkillPacket(x));
        }
        if (ALTERNATE_KEY.getKeyConflictContext().isActive() && ALTERNATE_KEY.consumeClick() && mc.player.isAlive()) {
            ALTERNATE_KEY.setDown(false);
            //grapple
            if (Keybinds.THROW.isDown()) {
                Player p = mc.player;
                Vec3 destination = ProjectileUtil.getHitResultOnViewVector(p, EntitySelector.LIVING_ENTITY_STILL_ALIVE, 32).getLocation();
                if (ClientEvents.coyoteTimeID >=0) {
                    destination = ClientEvents.coyoteVector;
                }

                CombatChannel.INSTANCE.sendToServer(new GrapplePacket(destination, ClientEvents.coyoteTimeID));
                p.setDeltaMovement(Vec3.ZERO);
            }
            //skills
            else if (CasterData.getCap(mc.player).getHolsteredSkill() != null) {
                CombatChannel.INSTANCE.sendToServer(new EvokeSkillPacket(ClientEvents.coyoteTimeID));
            } else {
                //kick
                CombatChannel.INSTANCE.sendToServer(new KickPacket(ClientEvents.coyoteTimeID));
                HitResult destination = ProjectileUtil.getHitResultOnViewVector(mc.player, EntitySelector.LIVING_ENTITY_STILL_ALIVE, 3);
                if (destination.getType() != HitResult.Type.MISS&&CombatData.getCap(mc.player).getPosture()>= QiCosts.KICK) {
                    //jump up
                    if (!mc.player.onGround()) {
                        Vec3 vel=mc.player.getDeltaMovement();
                        mc.player.setDeltaMovement(new Vec3(vel.x, 1, vel.z));
                    }
                }
            }
        }
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
