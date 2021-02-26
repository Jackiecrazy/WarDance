package jackiecrazy.wardance.client;

import com.elenai.elenaidodge2.event.ClientTickEventListener;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarCompat;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.capability.ICombatCapability;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.CombatPacket;
import jackiecrazy.wardance.networking.DodgePacket;
import jackiecrazy.wardance.networking.RequestUpdatePacket;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class ClientEvents {
    private static HashMap<String, Boolean> rotate;
    private static final int ALLOWANCE = 7;
    private static final ResourceLocation hud = new ResourceLocation(WarDance.MODID, "textures/hud/yeet.png");
    private static final ResourceLocation hood = new ResourceLocation(WarDance.MODID, "textures/hud/icons.png");
    /**
     * left, back, right
     */
    private static final long[] lastTap = {0, 0, 0, 0};
    private static final boolean[] tapped = {false, false, false, false};
    private static boolean sneak = false;
    private static float currentQiLevel = 0;
    private static float currentComboLevel = 0;
    private static DecimalFormat formatter = new DecimalFormat("#.#");

    static {
        formatter.setRoundingMode(RoundingMode.DOWN);
    }

    public static void updateList(List<? extends String> pos) {
        rotate = new HashMap<>();
        for (String s : pos) {
            try {
                String[] val = s.split(",");
                rotate.put(val[0], Boolean.parseBoolean(val[2]));
            } catch (Exception e) {
                WarDance.LOGGER.warn("improperly formatted custom rotation definition " + s + "!");
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void dodge(InputUpdateEvent e) {
        Minecraft mc = Minecraft.getInstance();
        MovementInput mi = e.getMovementInput();
        final ICombatCapability itsc = CombatData.getCap(mc.player);
        if (Keybinds.COMBAT.getKeyConflictContext().isActive() && Keybinds.COMBAT.isPressed()) {
            mc.player.sendStatusMessage(new TranslationTextComponent("taoism.combat." + (itsc.isCombatMode() ? "off" : "on")), true);
            CombatChannel.INSTANCE.sendToServer(new CombatPacket());
        }
//        if (itsc.getStaggerTime() > 0) {
//            //no moving while you're rooted!
//            KeyBinding.unPressAllKeys();
//            return;
//        }
        if (itsc.isCombatMode()&& (!WarCompat.elenaiDodge || itsc.getStaggerTime() != 0)) {
            final boolean onSprint = mc.gameSettings.keyBindSprint.isPressed();
            int dir = -1;
            if (mi.leftKeyDown && (!tapped[0] || onSprint)) {
                if (mc.world.getGameTime() - lastTap[0] <= ALLOWANCE || onSprint) {
                    dir = 0;
                }
                lastTap[0] = mc.world.getGameTime();
            }
            tapped[0] = mi.leftKeyDown;
            if (mi.backKeyDown && (!tapped[1] || onSprint)) {
                if (mc.world.getGameTime() - lastTap[1] <= ALLOWANCE || onSprint) {
                    dir = 1;
                }
                lastTap[1] = mc.world.getGameTime();
            }
            tapped[1] = mi.backKeyDown;
            if (mi.rightKeyDown && (!tapped[2] || onSprint)) {
                if (mc.world.getGameTime() - lastTap[2] <= ALLOWANCE || onSprint) {
                    dir = 2;
                }
                lastTap[2] = mc.world.getGameTime();
            }
            tapped[2] = mi.rightKeyDown;
//            if (mi.forwardKeyDown && (!tapped[3] || onSprint)) {
//                if (mc.world.getGameTime() - lastTap[3] <= ALLOWANCE || onSprint) {
//                    dir = 3;
//                }
//                lastTap[3] = mc.world.getGameTime();
//            }
//            tapped[3] = mi.forwardKeyDown;
            if (dir != -1)
                CombatChannel.INSTANCE.sendToServer(new DodgePacket(dir, mi.sneaking));
        }

        if (itsc.getStaggerTime() > 0) {
            //no moving while you're down! (except for a safety roll)
            KeyBinding.unPressAllKeys();
            return;
        }

        if (itsc.isCombatMode()) {
//            if (mi.jump && !jump) {
//                //if(mc.world.getTotalWorldTime()-lastSneak<=ALLOWANCE){
//                Taoism.net.sendToServer(new PacketJump());
//                //}
//            }
//            jump = mi.jump;

            if (mc.player.isSprinting() && mi.sneaking && !sneak) {
                //if(mc.world.getTotalWorldTime()-lastSneak<=ALLOWANCE){
                CombatChannel.INSTANCE.sendToServer(new DodgePacket(99, true));
                //}
            }
            sneak = mi.sneaking;
        }
    }

    @SubscribeEvent
    public static void down(RenderLivingEvent.Pre event) {
        final LivingEntity e = event.getEntity();
        float width = e.getWidth();
        float height = e.getHeight();
        if (e.isAlive()) {
            if (CombatData.getCap(event.getEntity()).getStaggerTime() > 0) {
                //System.out.println("yes");
                MatrixStack ms = event.getMatrixStack();
                //ms.push();
                //tall bois become flat bois
                boolean reg = (ForgeRegistries.ENTITIES.getKey(e.getType()) != null && rotate.containsKey(ForgeRegistries.ENTITIES.getKey(e.getType()).toString()));
                boolean rot = reg ? rotate.getOrDefault(ForgeRegistries.ENTITIES.getKey(e.getType()).toString(), false) : width < height;
                if (rot) {
                    ms.rotate(Vector3f.XN.rotationDegrees(90));
                    ms.rotate(Vector3f.ZP.rotationDegrees(-e.renderYawOffset));
                    ms.rotate(Vector3f.YP.rotationDegrees(e.renderYawOffset));
                    ms.translate(0, -e.getHeight() / 2, 0);
                }
                //cube bois become side bois
                //flat bois become flatter bois
                else {
                    ms.translate(0, -e.getHeight() / 2, 0);
                }
                //multi bois do nothing
            }
        }
    }

    @SubscribeEvent
    public static void handRaising(RenderHandEvent e) {
        if (e.getHand().equals(Hand.MAIN_HAND)) return;
        AbstractClientPlayerEntity p = Minecraft.getInstance().player;
        if (p == null || (!CombatData.getCap(p).isCombatMode() && (p.swingingHand != Hand.OFF_HAND || !p.isSwingInProgress)) || !e.getItemStack().isEmpty())
            return;
        e.setCanceled(true);
        float cd = CombatUtils.getCooledAttackStrength(p, Hand.OFF_HAND, e.getPartialTicks());
        float f6 = 1 - (cd * cd * cd);
        Minecraft.getInstance().getFirstPersonRenderer().renderArmFirstPerson(e.getMatrixStack(), e.getBuffers(), e.getLight(), f6, e.getSwingProgress(), p.getPrimaryHand() == HandSide.RIGHT ? HandSide.LEFT : HandSide.RIGHT);
    }

    @SubscribeEvent
    public static void displayCoolie(RenderGameOverlayEvent.Post event) {
        MainWindow sr = event.getWindow();
        final Minecraft mc = Minecraft.getInstance();
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.CROSSHAIRS)) {
            //draw offhand cooldown, crosshair type
            {
                GameSettings gamesettings = mc.gameSettings;

                if (gamesettings.getPointOfView() == PointOfView.FIRST_PERSON) {

                    int width = sr.getScaledWidth();
                    int height = sr.getScaledHeight();

                    ClientPlayerEntity player = mc.player;
                    if (!gamesettings.showDebugInfo || gamesettings.hideGUI || player.hasReducedDebug() || gamesettings.reducedDebugInfo) {
                        if (mc.gameSettings.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                            GlStateManager.enableAlphaTest();
                            float cooldown = CombatUtils.getCooledAttackStrength(player, Hand.OFF_HAND, 0f);
                            boolean hyperspeed = false;

                            if (mc.pointedEntity instanceof LivingEntity && cooldown >= 1.0F) {
                                hyperspeed = CombatUtils.getCooldownPeriod(player, Hand.OFF_HAND) > 5.0F;
                                hyperspeed = hyperspeed & (mc.pointedEntity).isAlive();
                            }

                            int y = height / 2 - 7 - 7;
                            int x = width / 2 - 8;

                            if (hyperspeed) {
                                mc.ingameGUI.blit(event.getMatrixStack(), x, y, 68, 94, 16, 16);
                            } else if (cooldown < 1.0F) {
                                int k = (int) (cooldown * 17.0F);
                                mc.ingameGUI.blit(event.getMatrixStack(), x, y, 36, 94, 16, 4);
                                mc.ingameGUI.blit(event.getMatrixStack(), x, y, 52, 94, k, 4);
                            }
                        }
                    }
                }
            }
        }
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR)) {
            //draw offhand cooldown, hotbar type
            if (mc.getRenderViewEntity() instanceof PlayerEntity) {
                GlStateManager.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
                PlayerEntity p = (PlayerEntity) mc.getRenderViewEntity();
                ItemStack itemstack = p.getHeldItemOffhand();
                HandSide oppositeHand = p.getPrimaryHand().opposite();
                int halfOfScreen = sr.getScaledWidth() / 2;

                GlStateManager.enableRescaleNormal();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderHelper.enableStandardItemLighting();

                if (mc.gameSettings.attackIndicator == AttackIndicatorStatus.HOTBAR) {
                    float strength = CombatUtils.getCooledAttackStrength(p, Hand.OFF_HAND, 0);
                    if (strength < 1.0F) {
                        int y = sr.getScaledHeight() - 20;
                        int x = halfOfScreen + 91 + 6;
                        if (oppositeHand == HandSide.LEFT) {
                            x = halfOfScreen - 91 - 22;
                        }

                        mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
                        int modStrength = (int) (strength * 19.0F);
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                        mc.ingameGUI.blit(event.getMatrixStack(), x + 18, y, 0, 94, 18, 18);
                        mc.ingameGUI.blit(event.getMatrixStack(), x + 18, y + 18 - modStrength, 18, 112 - modStrength, 18, modStrength);
                    }
                }

                RenderHelper.disableStandardItemLighting();
                RenderSystem.disableBlend();
            }
        }


        if (event.getType().equals(RenderGameOverlayEvent.ElementType.ALL))
            if (mc.getRenderViewEntity() instanceof PlayerEntity) {
                GameSettings gamesettings = mc.gameSettings;
                ClientPlayerEntity player = mc.player;
                ICombatCapability cap = CombatData.getCap(player);
                int width = sr.getScaledWidth();
                int height = sr.getScaledHeight();
                mc.getTextureManager().bindTexture(hud);
                float targetQiLevel = cap.getMight();
                boolean closeEnough = true;
                if (targetQiLevel > currentQiLevel) {
                    currentQiLevel += Math.min(0.1, (targetQiLevel - currentQiLevel) / 20);
                    closeEnough = false;
                }
                if (targetQiLevel < currentQiLevel) {
                    currentQiLevel -= Math.min(0.1, (currentQiLevel - targetQiLevel) / 20);
                    closeEnough = !closeEnough;
                }
                if (closeEnough)
                    currentQiLevel = targetQiLevel;
                int qi = (int) (currentQiLevel);
                float qiExtra = currentQiLevel - qi;
                if (qiExtra < 0.1) qiExtra = 0;
                if (cap.isCombatMode()) {
                    if (qi != 0 || qiExtra != 0f) {
                        //render qi bar
                        event.getMatrixStack().push();
                        //bar
                        event.getMatrixStack().push();
                        RenderSystem.enableBlend();
                        RenderSystem.enableAlphaTest();
                        RenderSystem.blendColor(1, 1, 1, qi > 0 ? 1 : qiExtra);
                        mc.ingameGUI.blit(event.getMatrixStack(), Math.min(ClientConfig.qiX, width - 64), Math.min(ClientConfig.qiY, height - 64), 0, 0, 64, 64);//+(int)(qiExtra*32)
                        event.getMatrixStack().pop();

                        if (qi > 0) {
                            //overlay
                            event.getMatrixStack().push();
                            RenderSystem.color4f(qiExtra, qiExtra, qiExtra, qiExtra);
                            mc.ingameGUI.blit(event.getMatrixStack(), Math.min(ClientConfig.qiX, width - 64), Math.min(ClientConfig.qiY, height - 64), ((qi + 1) * 64) % 256, Math.floorDiv((qi + 1), 4) * 64, 64, 64);
                            event.getMatrixStack().pop();

                            //overlay layer 2
                            event.getMatrixStack().push();
                            RenderSystem.color3f(1f, 1f, 1f);
                            mc.ingameGUI.blit(event.getMatrixStack(), Math.min(ClientConfig.qiX, width - 64), Math.min(ClientConfig.qiY, height - 64), (qi * 64) % 256, Math.floorDiv(qi, 4) * 64, 64, 64);
                            event.getMatrixStack().pop();
                        }
                        RenderSystem.disableAlphaTest();
                        RenderSystem.disableBlend();
                        event.getMatrixStack().pop();
                    }
                    //combo bar at 224,20 to 229, 121. Grace at 222,95 to 224, 121
                    //initial bar
                    RenderSystem.enableBlend();
                    mc.getTextureManager().bindTexture(hood);
                    int barHeight = 101;
                    event.getMatrixStack().push();
                    RenderSystem.defaultBlendFunc();
                    mc.ingameGUI.blit(event.getMatrixStack(), width - 8 + ClientConfig.comboX, Math.min(height / 2 - barHeight / 2 + ClientConfig.comboY, height - barHeight), 220, 20, 8, barHeight);
                    event.getMatrixStack().pop();
                    //combo
                    float targetCombo = cap.getCombo();
                    closeEnough = true;
                    if (targetCombo > currentComboLevel) {
                        currentComboLevel += Math.min(0.1, (targetCombo - currentComboLevel) / 20);
                        closeEnough = false;
                    }
                    if (targetCombo < currentComboLevel) {
                        currentComboLevel += Math.min(0.1, (targetCombo - currentComboLevel) / 20);
                        closeEnough = !closeEnough;
                    }
                    if (closeEnough)
                        currentComboLevel = targetCombo;
                    int emptyPerc = (int) ((Math.ceil(currentComboLevel) - currentComboLevel) * barHeight);
                    if (emptyPerc != 0) {
                        event.getMatrixStack().push();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.color3f(0.15f, 0.2f, 1f);
                        mc.ingameGUI.blit(event.getMatrixStack(), width - 3 + ClientConfig.comboX, Math.min(height / 2 - barHeight / 2 + ClientConfig.comboY, height - barHeight) + emptyPerc, 224, 20 + emptyPerc, 3, barHeight - emptyPerc);
                        event.getMatrixStack().pop();
                    }
                    //grace
                    barHeight = 26;
                    event.getMatrixStack().push();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.color3f(1 - cap.getComboGrace() / (float) CombatConfig.comboGrace, cap.getComboGrace() / (float) CombatConfig.comboGrace, 0);
                    emptyPerc = (int) ((CombatConfig.comboGrace - cap.getComboGrace()) / (float) CombatConfig.comboGrace * barHeight);
                    mc.ingameGUI.blit(event.getMatrixStack(), width - 7 + ClientConfig.comboX, Math.min(height / 2 - barHeight / 2 + ClientConfig.comboY, height - barHeight) + 38 + emptyPerc, 220, 95 + emptyPerc, 4, barHeight - emptyPerc);
                    event.getMatrixStack().pop();
                    RenderSystem.disableBlend();
                    //multiplier
                    String combo = formatter.format(1 + Math.floor(currentComboLevel) / 10) + "X";
                    mc.fontRenderer.drawString(event.getMatrixStack(), combo, width - 28 + ClientConfig.comboX, Math.min(height / 2 - barHeight / 2 + ClientConfig.comboY, height - barHeight) + 60, 0);
                }
                //render posture bar if not full, displayed even out of combat mode because it's pretty relevant to not dying
                if (cap.getPosture() < cap.getMaxPosture() || cap.getStaggerTime() > 0)
                    drawPostureBarreAt(event.getMatrixStack(), player, width / 2 + ClientConfig.yourPostureX, height - ClientConfig.yourPostureY);
                Entity look = getEntityLookedAt(player);
                if (look instanceof LivingEntity && ClientConfig.displayEnemyPosture && (CombatData.getCap((LivingEntity) look).getPosture() < CombatData.getCap((LivingEntity) look).getMaxPosture() || CombatData.getCap((LivingEntity) look).getStaggerTime() > 0)) {
                    drawPostureBarreAt(event.getMatrixStack(), (LivingEntity) look, width / 2 + ClientConfig.theirPostureX, ClientConfig.theirPostureY);//Math.min(HudConfig.client.enemyPosture.x, width - 64), Math.min(HudConfig.client.enemyPosture.y, height - 64));
                }
            }
    }

    /**
     * Draws it with the coord as its center
     *
     * @param elb
     * @param atX
     * @param atY
     */
    private static void drawPostureBarreAt(MatrixStack ms, LivingEntity elb, int atX, int atY) {
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(hood);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().startSection("postureBar");
        float cap = itsc.getMaxPosture();
        int left = atX - 91;
        float posPerc = MathHelper.clamp(itsc.getPosture() / itsc.getTrueMaxPosture(), 0, 1);
        if (cap > 0) {
            short barWidth = 182;
            int filled = (int) (itsc.getPosture() / itsc.getTrueMaxPosture() * (float) (barWidth));
            //int invulTime = (int) ((float) itsc.getPosInvulTime() / (float) CombatConfig.ssptime * (float) (barWidth));
            //base
            mc.ingameGUI.blit(ms, left, atY, 0, 64, barWidth, 5);
            RenderSystem.color3f(1 - posPerc, posPerc, 30f / 255);
            //bar on top
            mc.ingameGUI.blit(ms, left, atY, 0, 69, filled, 5);
            //fatigue
            filled = (int) (itsc.getMaxPosture() / itsc.getTrueMaxPosture() * (float) (barWidth));
            RenderSystem.color3f(1, 0.1f, 0.1f);
            mc.ingameGUI.blit(ms, left + filled, atY, filled, 69, barWidth - filled, 5);
            if (itsc.getStaggerTime() > 0) {
                int invulTime = (int) (MathHelper.clamp((float) itsc.getStaggerTime() / (float) CombatConfig.staggerDuration, 0, 1) * (float) (barWidth));//apparently this is synced to the client?
                RenderSystem.color3f(0, 0, 0);//, ((float) itsc.getPosInvulTime()) / (float) CombatConfig.ssptime);
                mc.ingameGUI.blit(ms, left, atY, 0, 69, invulTime, 5);
            }

        }
        mc.getProfiler().endSection();
//        mc.mcProfiler.startSection("postureNumber");
//        float postureNumber = ((int) (itsc.getPosture() * 100)) / 100f;
//        String text = "" + postureNumber;
//        int x = atX - (mc.fontRenderer.getStringWidth(text) / 2);
//        int y = atY - 1;
//        mc.fontRenderer.drawString(text, x + 1, y, 0);
//        mc.fontRenderer.drawString(text, x - 1, y, 0);
//        mc.fontRenderer.drawString(text, x, y + 1, 0);
//        mc.fontRenderer.drawString(text, x, y - 1, 0);
//        mc.fontRenderer.drawString(text, x, y, c.getRGB());
//        mc.mcProfiler.endSection();
        mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
        RenderSystem.disableBlend();
        RenderSystem.color3f(1, 1, 1);
        //RenderSystem.enableLighting();
    }

    public static Entity getEntityLookedAt(Entity e) {
        Entity foundEntity = null;
        final double finalDistance = 32;
        double distance = finalDistance;
        RayTraceResult pos = raycast(e, finalDistance);
        Vector3d positionVector = e.getPositionVec();

        if (e instanceof PlayerEntity)
            positionVector = positionVector.add(0, e.getEyeHeight(e.getPose()), 0);

        if (pos != null)
            distance = pos.getHitVec().distanceTo(positionVector);

        Vector3d lookVector = e.getLookVec();
        Vector3d reachVector = positionVector.add(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance);

        Entity lookedEntity = null;
        List<Entity> entitiesInBoundingBox = e.getEntityWorld().getEntitiesWithinAABBExcludingEntity(e, e.getBoundingBox().grow(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance).expand(1F, 1F, 1F));
        double minDistance = distance;

        for (Entity entity : entitiesInBoundingBox) {
            if (entity.canBeCollidedWith()) {
                AxisAlignedBB collisionBox = entity.getRenderBoundingBox();
                Optional<Vector3d> interceptPosition = collisionBox.rayTrace(positionVector, reachVector);

                if (collisionBox.contains(positionVector)) {
                    if (0.0D < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = 0.0D;
                    }
                } else if (interceptPosition.isPresent()) {
                    double distanceToEntity = positionVector.distanceTo(interceptPosition.get());

                    if (distanceToEntity < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = distanceToEntity;
                    }
                }
            }

            if (lookedEntity != null && (minDistance < distance || pos == null))
                foundEntity = lookedEntity;
        }

        return foundEntity;
    }

    public static RayTraceResult raycast(Entity e, double len) {
        Vector3d vec = new Vector3d(e.getPosX(), e.getPosY(), e.getPosZ());
        if (e instanceof PlayerEntity)
            vec = vec.add(new Vector3d(0, e.getEyeHeight(e.getPose()), 0));

        Vector3d look = e.getLookVec();
        if (look == null)
            return null;

        return raycast(vec, look, e, len);
    }

    public static RayTraceResult raycast(Vector3d origin, Vector3d ray, Entity e, double len) {
        Vector3d next = origin.add(ray.normalize().scale(len));
        return e.world.rayTraceBlocks(new RayTraceContext(origin, next, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, e));
    }

    private static double dodgeDecimal;

    @SubscribeEvent
    public static void tickPlayer(TickEvent.ClientTickEvent e) {
        PlayerEntity p = Minecraft.getInstance().player;
        if (p != null && !Minecraft.getInstance().isGamePaused()) {
            if (e.phase == TickEvent.Phase.START) {
                if (Minecraft.getInstance().pointedEntity instanceof LivingEntity && Minecraft.getInstance().pointedEntity.isAlive()) {
                    CombatChannel.INSTANCE.sendToServer(new RequestUpdatePacket(Minecraft.getInstance().pointedEntity.getEntityId()));
                }
            } else {
                if (WarCompat.elenaiDodge) {
                    if (CombatConfig.elenaiP && CombatData.getCap(p).getPostureGrace() > 0) {
                        ClientTickEventListener.regen++;
                    } else if (CombatConfig.elenaiC) {
                        dodgeDecimal += Math.floor(CombatData.getCap(p).getCombo()) / 10;
                        if (dodgeDecimal > 1) {
                            dodgeDecimal--;
                            ClientTickEventListener.regen--;
                        }
                    }
                }
            }
        }
    }

}
