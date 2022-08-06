package jackiecrazy.wardance.client;

import com.elenai.elenaidodge2.event.ClientTickEventListener;
import com.mojang.blaze3d.matrix.MatrixStack;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.networking.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class ClientEvents {
    private static final int ALLOWANCE = 7;
    /**
     * left, back, right
     */
    private static final long[] lastTap = {0, 0, 0, 0};
    private static final boolean[] tapped = {false, false, false, false};
    public static int combatTicks = Integer.MAX_VALUE;
    static boolean lastTickParry;
    static boolean weird = false;
    private static HashMap<String, Boolean> rotate;
    private static boolean sneak = false;
    private static double dodgeDecimal;
    private static Entity lastTickLookAt;
    private static boolean rightClick = false;

    static {
        RenderEvents.formatter.setRoundingMode(RoundingMode.DOWN);
        RenderEvents.formatter.setMinimumFractionDigits(1);
        RenderEvents.formatter.setMaximumFractionDigits(1);
    }

    public static void updateList(List<? extends String> pos) {
        rotate = new HashMap<>();
        for (String s : pos) {
            try {
                String[] val = s.split(",");
                rotate.put(val[0], Boolean.parseBoolean(val[1]));
            } catch (Exception e) {
                if (GeneralConfig.debug)
                    WarDance.LOGGER.warn("improperly formatted custom rotation definition " + s + "!");
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void dodge(InputUpdateEvent e) {
        Minecraft mc = Minecraft.getInstance();
        MovementInput mi = e.getMovementInput();
        final ICombatCapability itsc = CombatData.getCap(mc.player);
//        if (itsc.getStaggerTime() > 0) {
//            //no moving while you're rooted!
//            KeyBinding.unPressAllKeys();
//            return;
//        }
        if (itsc.isCombatMode() && mc.level != null) {
            final boolean onSprint = mc.options.keySprint.consumeClick();
            int dir = -1;
            if (!WarCompat.elenaiDodge) {
                if (mi.left && (!tapped[0] || onSprint)) {
                    if (mc.level.getGameTime() - lastTap[0] <= ALLOWANCE || onSprint) {
                        dir = 0;
                    }
                    lastTap[0] = mc.level.getGameTime();
                }
                tapped[0] = mi.left;
                if (mi.down && (!tapped[1] || onSprint)) {
                    if (mc.level.getGameTime() - lastTap[1] <= ALLOWANCE || onSprint) {
                        dir = 1;
                    }
                    lastTap[1] = mc.level.getGameTime();
                }
                tapped[1] = mi.down;
                if (mi.right && (!tapped[2] || onSprint)) {
                    if (mc.level.getGameTime() - lastTap[2] <= ALLOWANCE || onSprint) {
                        dir = 2;
                    }
                    lastTap[2] = mc.level.getGameTime();
                }
                tapped[2] = mi.right;
//            if (mi.forwardKeyDown && (!tapped[3] || onSprint)) {
//                if (mc.world.getGameTime() - lastTap[3] <= ALLOWANCE || onSprint) {
//                    dir = 3;
//                }
//                lastTap[3] = mc.world.getGameTime();
//            }
//            tapped[3] = mi.forwardKeyDown;
            }
            if (mc.player.isSprinting() && mc.options.keySprint.isDown() && mi.shiftKeyDown && !sneak) {
                //if(mc.world.getTotalWorldTime()-lastSneak<=ALLOWANCE){
                dir = 99;
                //}
            }
            sneak = mi.shiftKeyDown;
            if (dir != -1)
                CombatChannel.INSTANCE.sendToServer(new DodgePacket(dir, mi.shiftKeyDown));
        }

        if (itsc.getStaggerTime() > 0) {
            //no moving while you're down! (except for a safety roll)
            KeyBinding.releaseAll();
            return;
        }
    }

    @SubscribeEvent
    public static void alert(LivingAttackEvent e) {
        if (Minecraft.getInstance().player == null) return;
        if ((e.getEntityLiving() == Minecraft.getInstance().player && e.getSource().getEntity() instanceof LivingEntity) || e.getSource().getEntity() == Minecraft.getInstance().player) {
            if (ClientConfig.autoCombat > 0 && combatTicks != Integer.MAX_VALUE) {
                if (!CombatData.getCap(Minecraft.getInstance().player).isCombatMode())
                    CombatChannel.INSTANCE.sendToServer(new CombatModePacket());
                combatTicks = Minecraft.getInstance().player.tickCount;
            }
        }
    }

    @SubscribeEvent
    public static void downTick(LivingEvent.LivingUpdateEvent event) {
        final LivingEntity e = event.getEntityLiving();
        if (e.isAlive()) {
            if (CombatData.getCap(e).getStaggerTime() > 0) {
                boolean reg = (ForgeRegistries.ENTITIES.getKey(e.getType()) != null && rotate.containsKey(ForgeRegistries.ENTITIES.getKey(e.getType()).toString()));
                float height = reg && rotate.getOrDefault(ForgeRegistries.ENTITIES.getKey(e.getType()).toString(), false) ? e.getBbWidth() : e.getBbHeight();
                event.getEntity().level.addParticle(ParticleTypes.CRIT, e.getX() + Math.sin(e.tickCount) * e.getBbWidth() / 2, e.getY() + height, e.getZ() + Math.cos(e.tickCount) * e.getBbWidth() / 2, 0, 0, 0);
            }


        }
    }

    @SubscribeEvent
    public static void down(RenderLivingEvent.Pre event) {
        final LivingEntity e = event.getEntity();
        float width = e.getBbWidth();
        float height = e.getBbHeight();

        if (e.isAlive()) {
            if (CombatData.getCap(event.getEntity()).getStaggerTime() > 0) {
                //System.out.println("yes");
                MatrixStack ms = event.getMatrixStack();
                //ms.push();
                //tall bois become flat bois
                boolean reg = (ForgeRegistries.ENTITIES.getKey(e.getType()) != null && rotate.containsKey(ForgeRegistries.ENTITIES.getKey(e.getType()).toString()));
                boolean rot = reg ? rotate.getOrDefault(ForgeRegistries.ENTITIES.getKey(e.getType()).toString(), false) : width < height;
                if (rot) {
                    ms.mulPose(Vector3f.XN.rotationDegrees(90));
                    ms.mulPose(Vector3f.ZP.rotationDegrees(-e.yBodyRot));
                    ms.mulPose(Vector3f.YP.rotationDegrees(e.yBodyRot));
                    ms.translate(0, -e.getBbHeight() / 2, 0);
                }
                //cube bois become side bois
                //flat bois become flatter bois
                //multi bois do nothing
            }
            if (CombatData.getCap(e).getRollTime() != 0 && e.getPose() == Pose.SLEEPING) {
                MatrixStack ms = event.getMatrixStack();
                ms.mulPose(Vector3f.YN.rotationDegrees(e.yRot - e.getBedOrientation().toYRot()));
//                ms.rotate(Vector3f.ZP.rotationDegrees(-e.renderYawOffset));
//                ms.rotate(Vector3f.YP.rotationDegrees(e.renderYawOffset));
            }
//            if(e.isPotionActive(WarEffects.PETRIFY.get())){
//                event.getRenderer()
//                Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
//            }
        }
    }

    @SubscribeEvent
    public static void handRaising(RenderHandEvent e) {
        if (e.getHand().equals(Hand.MAIN_HAND) || !GeneralConfig.dual) return;
        AbstractClientPlayerEntity p = Minecraft.getInstance().player;
        if (p == null || (!CombatData.getCap(p).isCombatMode() && (p.swingingArm != Hand.OFF_HAND || !p.swinging)) || !e.getItemStack().isEmpty())
            return;
        e.setCanceled(true);
        float cd = CombatUtils.getCooledAttackStrength(p, Hand.OFF_HAND, e.getPartialTicks());
        float f6 = 1 - (cd * cd * cd);
        Minecraft.getInstance().getItemInHandRenderer().renderPlayerArm(e.getMatrixStack(), e.getBuffers(), e.getLight(), f6, e.getSwingProgress(), p.getMainArm() == HandSide.RIGHT ? HandSide.LEFT : HandSide.RIGHT);
    }

    @SubscribeEvent
    public static void tickPlayer(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity p = mc.player;
        if (p != null && !mc.isPaused()) {
            if (e.phase == TickEvent.Phase.START) {
                Entity look = RenderEvents.getEntityLookedAt(p, 32);
                if (look != lastTickLookAt) {
                    lastTickLookAt = look;
                    if (look instanceof LivingEntity && look.isAlive())
                        CombatChannel.INSTANCE.sendToServer(new RequestUpdatePacket(look.getId()));
                    else
                        CombatChannel.INSTANCE.sendToServer(new RequestUpdatePacket(-1));
                }
                if (combatTicks != Integer.MAX_VALUE && combatTicks + ClientConfig.autoCombat == p.tickCount && CombatData.getCap(p).isCombatMode()) {
                    CombatChannel.INSTANCE.sendToServer(new CombatModePacket());
                }
                if (!Keybinds.PARRY.isDown())
                    lastTickParry = false;
            } else {
                if (!mc.options.keyUse.isDown())
                    rightClick = false;
                if (WarCompat.elenaiDodge) {
                    if (GeneralConfig.elenaiP && CombatData.getCap(p).getPostureGrace() > 0) {
                        ClientTickEventListener.regen++;
                    } else if (GeneralConfig.elenaiC) {
                        dodgeDecimal += Math.floor(CombatData.getCap(p).getRank()) / 10;
                        if (dodgeDecimal > 1) {
                            dodgeDecimal--;
                            ClientTickEventListener.regen--;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void noFovChange(FOVUpdateEvent e) {
        if (CombatData.getCap(e.getEntity()).getStaggerTime() > 0)
            e.setNewfov(0.7f);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleInputEvent(InputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (Keybinds.PARRY.getKeyConflictContext().isActive() && !lastTickParry && CombatConfig.sneakParry != 0 && Keybinds.PARRY.consumeClick() && mc.player.isAlive()) {
            if (CombatConfig.sneakParry < 0) {
                mc.player.displayClientMessage(new TranslationTextComponent("wardance.toggleparry." + (CombatData.getCap(mc.player).getParryingTick() == -1 ? "on" : "off")), true);

            }
            CombatChannel.INSTANCE.sendToServer(new ManualParryPacket());
            lastTickParry = true;

        }
    }

    @SubscribeEvent
    public static void sweepSwing(PlayerInteractEvent.LeftClickEmpty e) {
        Entity n = RenderEvents.getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.MAIN_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
        if (n != null)
            CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(true, n));
        CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(true, n));
    }

    @SubscribeEvent
    public static void sweepSwingOff(PlayerInteractEvent.RightClickEmpty e) {
        if (!rightClick && GeneralConfig.dual && e.getHand() == Hand.OFF_HAND && ((CombatUtils.isWeapon(e.getEntityLiving(), e.getItemStack()) || (e.getItemStack().isEmpty() && CombatData.getCap(e.getPlayer()).isCombatMode())))) {
            rightClick = true;
            Entity n = RenderEvents.getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
            e.getPlayer().swing(Hand.OFF_HAND, false);
            if (n != null)
                CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(false, n));
            CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(false, n));
        }
    }

    @SubscribeEvent
    public static void sweepSwingBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (Minecraft.getInstance().gameMode.isDestroying()) return;
        float temp = CombatUtils.getCooledAttackStrength(e.getPlayer(), Hand.MAIN_HAND, 0.5f);
        Entity n = RenderEvents.getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.MAIN_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
        if (n != null)
            CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(true, n));
        CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(true, n));
    }

    @SubscribeEvent
    public static void sweepSwingOffItem(PlayerInteractEvent.RightClickItem e) {
        if (!rightClick && GeneralConfig.dual && e.getHand() == Hand.OFF_HAND && ((CombatUtils.isWeapon(e.getEntityLiving(), e.getItemStack()) || (e.getItemStack().isEmpty() && CombatData.getCap(e.getPlayer()).isCombatMode())))) {
            rightClick = true;
            Entity n = RenderEvents.getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
            e.getPlayer().swing(Hand.OFF_HAND, false);
            if (n != null)
                CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(false, n));
            CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(false, n));
        }
    }

    @SubscribeEvent
    public static void punchy(PlayerInteractEvent.EntityInteract e) {
        if (!rightClick && GeneralConfig.dual && e.getHand() == Hand.OFF_HAND && (e.getItemStack().isEmpty() && CombatData.getCap(e.getPlayer()).isCombatMode())) {
            rightClick = true;
            Entity n = RenderEvents.getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
            e.getPlayer().swing(Hand.OFF_HAND, false);
            if (n != null)
                CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(false, n));
            CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(false, n));
        }
    }

    @SubscribeEvent
    public static void sweepSwingOffItemBlock(PlayerInteractEvent.RightClickBlock e) {
        if (!rightClick && GeneralConfig.dual && e.getHand() == Hand.OFF_HAND && ((CombatUtils.isWeapon(e.getEntityLiving(), e.getItemStack()) || (e.getItemStack().isEmpty() && CombatData.getCap(e.getPlayer()).isCombatMode())))) {
            rightClick = true;
            Entity n = RenderEvents.getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
            e.getPlayer().swing(Hand.OFF_HAND, false);
            if (n != null)
                CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(false, n));
            CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(false, n));
        }
    }

    @SubscribeEvent
    public static void noHit(InputEvent.KeyInputEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        double range = GeneralUtils.getAttributeValueSafe(mc.player, ForgeMod.REACH_DISTANCE.get()) - (mc.player.getMainHandItem().isEmpty() ? 1 : 0);
        Vector3d look = mc.player.getViewVector(1);
        if (mc.crosshairPickEntity != null) {
            if (GeneralUtils.getDistSqCompensated(mc.crosshairPickEntity, mc.player) > range * range) {
                mc.crosshairPickEntity = null;
                Vector3d miss = mc.player.position().add(look.scale(range));
                mc.hitResult = BlockRayTraceResult.miss(miss, Direction.getNearest(look.x, look.y, look.z), new BlockPos(miss));
            }
        } else if (RenderEvents.getEntityLookedAt(mc.player, range) != null) {
            EntityRayTraceResult ertr = ProjectileHelper.getEntityHitResult(mc.player, mc.player.getEyePosition(0.5f), mc.player.getEyePosition(0.5f).add(look.scale(range)), mc.player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D, 1.0D, 1.0D), (p_215312_0_) -> !p_215312_0_.isSpectator() && p_215312_0_.isPickable(), range);
            if (ertr != null) {
                mc.hitResult = ertr;
                mc.crosshairPickEntity = ertr.getEntity();
            }
        }
    }

    @SubscribeEvent
    public static void noHitMouse(InputEvent.MouseInputEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        double range = GeneralUtils.getAttributeValueSafe(mc.player, ForgeMod.REACH_DISTANCE.get()) - (mc.player.getMainHandItem().isEmpty() ? 1 : 0);
        Vector3d look = mc.player.getViewVector(1);
        if (mc.crosshairPickEntity != null) {
            if (GeneralUtils.getDistSqCompensated(mc.crosshairPickEntity, mc.player) > range * range) {
                mc.crosshairPickEntity = null;
                Vector3d miss = mc.player.position().add(look.scale(range));
                mc.hitResult = BlockRayTraceResult.miss(miss, Direction.getNearest(look.x, look.y, look.z), new BlockPos(miss));
            }
        } else if (RenderEvents.getEntityLookedAt(mc.player, range) != null) {
            EntityRayTraceResult ertr = ProjectileHelper.getEntityHitResult(mc.player, mc.player.getEyePosition(0.5f), mc.player.getEyePosition(0.5f).add(look.scale(range)), mc.player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D, 1.0D, 1.0D), (p_215312_0_) -> !p_215312_0_.isSpectator() && p_215312_0_.isPickable(), range);
            if (ertr != null) {
                mc.hitResult = ertr;
                mc.crosshairPickEntity = ertr.getEntity();
            }
        }
    }

    @SubscribeEvent
    public static void pickTarget(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            double range = GeneralUtils.getAttributeValueSafe(mc.player, ForgeMod.REACH_DISTANCE.get()) - (mc.player.getMainHandItem().isEmpty() ? 1 : 0);
            Vector3d look = mc.player.getViewVector(1);
            if (mc.crosshairPickEntity != null) {
                if (GeneralUtils.getDistSqCompensated(mc.crosshairPickEntity, mc.player) > range * range) {
                    mc.crosshairPickEntity = null;
                    Vector3d miss = mc.player.position().add(look.scale(range));
                    mc.hitResult = BlockRayTraceResult.miss(miss, Direction.getNearest(look.x, look.y, look.z), new BlockPos(miss));
                }
            } else if (RenderEvents.getEntityLookedAt(mc.player, range) != null) {
                Entity e = RenderEvents.getEntityLookedAt(mc.player, range);
                mc.hitResult = new EntityRayTraceResult(e, e.position());
                mc.crosshairPickEntity = e;
            }
        }
    }

}
