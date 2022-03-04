package jackiecrazy.wardance.client;

import com.elenai.elenaidodge2.event.ClientTickEventListener;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.networking.*;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
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

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class ClientEvents {
    private static final int ALLOWANCE = 7;
    private static final ResourceLocation posture = new ResourceLocation(WarDance.MODID, "textures/hud/amo.png");
    private static final ResourceLocation darkmega = new ResourceLocation(WarDance.MODID, "textures/hud/dark.png");
    private static final ResourceLocation goodhud = new ResourceLocation(WarDance.MODID, "textures/hud/thanksrai.png");
    /**
     * left, back, right
     */
    private static final long[] lastTap = {0, 0, 0, 0};
    private static final boolean[] tapped = {false, false, false, false};
    private static final DecimalFormat formatter = new DecimalFormat("#.#");
    public static int combatTicks = Integer.MAX_VALUE;
    static boolean lastTickParry;
    static boolean weird = false;
    private static HashMap<String, Boolean> rotate;
    private static boolean sneak = false;
    private static float currentMightLevel = 0;
    private static float currentComboLevel = 0;
    private static float currentSpiritLevel = 0;
    private static double dodgeDecimal;
    private static Entity lastTickLookAt;
    private static boolean rightClick = false;
    private static boolean flip = false;

    static {
        formatter.setRoundingMode(RoundingMode.DOWN);
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(1);
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
        if (itsc.isCombatMode() && mc.world != null) {
            final boolean onSprint = mc.gameSettings.keyBindSprint.isPressed();
            int dir = -1;
            if (!WarCompat.elenaiDodge) {
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
            }
            if (mc.player.isSprinting() && mc.gameSettings.keyBindSprint.isKeyDown() && mi.sneaking && !sneak) {
                //if(mc.world.getTotalWorldTime()-lastSneak<=ALLOWANCE){
                dir = 99;
                //}
            }
            sneak = mi.sneaking;
            if (dir != -1)
                CombatChannel.INSTANCE.sendToServer(new DodgePacket(dir, mi.sneaking));
        }

        if (itsc.getStaggerTime() > 0) {
            //no moving while you're down! (except for a safety roll)
            KeyBinding.unPressAllKeys();
            return;
        }
    }

    @SubscribeEvent
    public static void alert(LivingAttackEvent e) {
        if (Minecraft.getInstance().player == null) return;
        if ((e.getEntityLiving() == Minecraft.getInstance().player && e.getSource().getTrueSource() instanceof LivingEntity) || e.getSource().getTrueSource() == Minecraft.getInstance().player) {
            if (ClientConfig.autoCombat > 0 && combatTicks != Integer.MAX_VALUE) {
                if (!CombatData.getCap(Minecraft.getInstance().player).isCombatMode())
                    CombatChannel.INSTANCE.sendToServer(new CombatModePacket());
                combatTicks = Minecraft.getInstance().player.ticksExisted;
            }
        }
    }

    @SubscribeEvent
    public static void downTick(LivingEvent.LivingUpdateEvent event) {
        final LivingEntity e = event.getEntityLiving();
        if (e.isAlive()) {
            if (CombatData.getCap(e).getStaggerTime() > 0) {
                boolean reg = (ForgeRegistries.ENTITIES.getKey(e.getType()) != null && rotate.containsKey(ForgeRegistries.ENTITIES.getKey(e.getType()).toString()));
                float height = reg && rotate.getOrDefault(ForgeRegistries.ENTITIES.getKey(e.getType()).toString(), false) ? e.getWidth() : e.getHeight();
                event.getEntity().world.addParticle(ParticleTypes.CRIT, e.getPosX() + Math.sin(e.ticksExisted) * e.getWidth() / 2, e.getPosY() + height, e.getPosZ() + Math.cos(e.ticksExisted) * e.getWidth() / 2, 0, 0, 0);
            }


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
                //multi bois do nothing
            }
            if (CombatData.getCap(e).getRollTime() != 0 && e.getPose() == Pose.SLEEPING) {
                MatrixStack ms = event.getMatrixStack();
                ms.rotate(Vector3f.YN.rotationDegrees(e.rotationYaw - e.getBedDirection().getHorizontalAngle()));
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
        final MatrixStack stack = event.getMatrixStack();
        if (GeneralConfig.dual) {
            if (event.getType().equals(RenderGameOverlayEvent.ElementType.CROSSHAIRS)) {
                //draw offhand cooldown, crosshair type
                {
                    GameSettings gamesettings = mc.gameSettings;

                    if (gamesettings.getPointOfView() == PointOfView.FIRST_PERSON) {

                        int width = sr.getScaledWidth();
                        int height = sr.getScaledHeight();

                        ClientPlayerEntity player = mc.player;
                        if (player == null) return;
                        if (!gamesettings.showDebugInfo || gamesettings.hideGUI || player.hasReducedDebug() || gamesettings.reducedDebugInfo) {
                            if (mc.gameSettings.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                                GlStateManager.enableAlphaTest();
                                float cooldown = CombatUtils.getCooledAttackStrength(player, Hand.OFF_HAND, 0f);
                                boolean hyperspeed = false;

                                if (getEntityLookedAt(player, GeneralUtils.getAttributeValueHandSensitive(player, ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND)) != null && cooldown >= 1.0F) {
                                    hyperspeed = CombatUtils.getCooldownPeriod(player, Hand.OFF_HAND) > 5.0F;
                                    hyperspeed = hyperspeed & (getEntityLookedAt(player, GeneralUtils.getAttributeValueHandSensitive(player, ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND))).isAlive();
                                }

                                int y = height / 2 - 7 - 7;
                                int x = width / 2 - 8;

                                if (hyperspeed) {
                                    mc.ingameGUI.blit(stack, x, y, 68, 94, 16, 16);
                                } else if (cooldown < 1.0F) {
                                    int k = (int) (cooldown * 17.0F);
                                    mc.ingameGUI.blit(stack, x, y, 36, 94, 16, 4);
                                    mc.ingameGUI.blit(stack, x, y, 52, 94, k, 4);
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
                            mc.ingameGUI.blit(stack, x + 18, y, 0, 94, 18, 18);
                            mc.ingameGUI.blit(stack, x + 18, y + 18 - modStrength, 18, 112 - modStrength, 18, modStrength);
                        }
                    }

                    RenderHelper.disableStandardItemLighting();
                    RenderSystem.disableBlend();
                }
            }
        }

        if (event.getType().equals(RenderGameOverlayEvent.ElementType.ALL))
            if (mc.getRenderViewEntity() instanceof PlayerEntity) {
                ClientPlayerEntity player = mc.player;
                ICombatCapability cap = CombatData.getCap(player);
                int width = sr.getScaledWidth();
                int height = sr.getScaledHeight();
                mc.getTextureManager().bindTexture(goodhud);
                currentSpiritLevel = updateValue(currentSpiritLevel, cap.getSpirit());
                currentMightLevel = updateValue(currentMightLevel, cap.getMight());
                ClientEvents.currentComboLevel = cap.getRank() > currentComboLevel ? updateValue(currentComboLevel, cap.getRank()) : cap.getRank();
                //yourCurrentPostureLevel = updateValue(yourCurrentPostureLevel, cap.getPosture());
                if (cap.isCombatMode()) {
                    stack.push();
                    RenderSystem.enableBlend();
                    RenderSystem.enableAlphaTest();
                    //bar
//                        event.getMatrixStack().push();
//                        RenderSystem.blendColor(1, 1, 1, qi > 0 ? 1 : qiExtra);
//                        mc.ingameGUI.blit(event.getMatrixStack(), Math.min(ClientConfig.mightX, width - 64), Math.min(ClientConfig.mightY, height - 64), 0, 0, 64, 64);//+(int)(qiExtra*32)
//                        event.getMatrixStack().pop();
//
//                        if (qi > 0) {
//                            //overlay
//                            event.getMatrixStack().push();
//                            RenderSystem.color4f(qiExtra, qiExtra, qiExtra, qiExtra);
//                            mc.ingameGUI.blit(event.getMatrixStack(), Math.min(ClientConfig.mightX, width - 64), Math.min(ClientConfig.mightY, height - 64), ((qi + 1) * 64) % 256, Math.floorDiv((qi + 1), 4) * 64, 64, 64);
//                            event.getMatrixStack().pop();
//
//                            //overlay layer 2
//                            event.getMatrixStack().push();
//                            RenderSystem.color3f(1f, 1f, 1f);
//                            mc.ingameGUI.blit(event.getMatrixStack(), Math.min(ClientConfig.mightX, width - 64), Math.min(ClientConfig.mightY, height - 64), (qi * 64) % 256, Math.floorDiv(qi, 4) * 64, 64, 64);
//                            event.getMatrixStack().pop();
//                        }
                    Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.might, width, height);
                    int x = Math.max(pair.getFirst() - 16, 0);
                    int y = Math.min(pair.getSecond() - 16, height - 32);
                    int fillHeight = (int) (Math.min(1, currentMightLevel / cap.getMaxMight()) * 32);
                    if (ClientConfig.CONFIG.might.enabled) {
                        //might circle
                        RenderSystem.color4f(1, 1, 1, 1);
                        stack.push();
                        stack.push();
                        mc.ingameGUI.blit(stack, x, y, 0, 64, 32, 32);
                        stack.pop();
                        //might circle filling
                        stack.push();
                        mc.ingameGUI.blit(stack, x, y + 32 - fillHeight, 0, 96 - fillHeight, 32, fillHeight);
                        stack.pop();
                        fillHeight += Math.min(fillHeight, 3);
                        fillHeight = Math.min(fillHeight, 32);
                        //might base
                        stack.push();
                        mc.ingameGUI.blit(stack, x, y, 32, 64, 32, 32);
                        stack.pop();
                        //might illumination
                        stack.push();
                        mc.ingameGUI.blit(stack, x, y + 32 - fillHeight, 64, 96 - fillHeight, 32, fillHeight);
                        stack.pop();
                        stack.pop();
                    }
                    pair = translateCoords(ClientConfig.CONFIG.spirit, width, height);
                    x = MathHelper.clamp(pair.getFirst() - 16, 0, width - 32);
                    y = MathHelper.clamp(pair.getSecond() - 16, 0, height - 32);
                    fillHeight = (int) (Math.min(1, currentSpiritLevel / cap.getMaxSpirit()) * 32);
                    String display = formatter.format(currentSpiritLevel) + "/" + formatter.format(cap.getMaxSpirit());
                    //spirit circle
                    stack.push();
                    if (ClientConfig.CONFIG.spirit.enabled) {
                        stack.push();
                        mc.ingameGUI.blit(stack, x, y, 0, 96, 32, 32);
                        stack.pop();
                        //spirit circle filling
                        stack.push();
                        mc.ingameGUI.blit(stack, x, y + 32 - fillHeight, 0, 128 - fillHeight, 32, fillHeight);
                        stack.pop();
                        fillHeight += Math.min(fillHeight, 3);
                        fillHeight = Math.min(fillHeight, 32);
                        //spirit base
                        stack.push();
                        mc.ingameGUI.blit(stack, x, y + 1, 32, 96, 32, 32);
                        stack.pop();
                        //spirit illumination
                        stack.push();
                        mc.ingameGUI.blit(stack, x, y + 33 - fillHeight, 64, 128 - fillHeight, 32, fillHeight);
                        stack.pop();
                    }
                    if (ClientConfig.CONFIG.spiritNumber.enabled) {
                        pair = translateCoords(ClientConfig.CONFIG.spiritNumber, width, height);
                        mc.fontRenderer.drawStringWithShadow(event.getMatrixStack(), display, pair.getFirst() - mc.fontRenderer.getStringWidth(display) / 2f, pair.getSecond() - 2, ClientConfig.spiritColor);
                    }
                    if (ClientConfig.CONFIG.mightNumber.enabled) {
                        pair = translateCoords(ClientConfig.CONFIG.mightNumber, width, height);
                        display = formatter.format(currentMightLevel) + "/" + formatter.format(cap.getMaxMight());
                        mc.fontRenderer.drawStringWithShadow(event.getMatrixStack(), display, pair.getFirst() - mc.fontRenderer.getStringWidth(display) / 2f, pair.getSecond() - 2, ClientConfig.mightColor);
                    }
                    stack.pop();

                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                    stack.pop();
                    //combo bar at 224,20 to 229, 121. Grace at 222,95 to 224, 121
                    //initial bar
                    RenderSystem.enableBlend();
                    stack.push();
                    if (ClientConfig.CONFIG.combo.enabled) {
                        mc.getTextureManager().bindTexture(goodhud);
                        int combowidth = 32;
                        float workingCombo = currentComboLevel;
                        int comboU = (int) (MathHelper.clamp(Math.floor(workingCombo), 0, 4)) * 32;
                        int divisor = 1;
                        if (workingCombo >= 4)//S
                            divisor = 2;
                        if (workingCombo >= 6) {//SS
                            combowidth = 33;
                            comboU = 159;
                            divisor = 3;
                        }
                        if (workingCombo >= 9) {//SSS
                            combowidth = 64;
                            comboU = 192;
                            fillHeight = (int) ((workingCombo - 9) * 32f);
                        } else if (divisor > 1)
                            fillHeight = (int) ((workingCombo - divisor * 2) / divisor * 32f);
                        else
                            fillHeight = (int) ((workingCombo - Math.floor(workingCombo)) * 32f);
                        pair = translateCoords(ClientConfig.CONFIG.combo, width, height);
                        x = MathHelper.clamp(pair.getFirst() - combowidth / 2, 0, width - combowidth);
                        y = MathHelper.clamp(pair.getSecond() - 23, 0, height - 46);
                        mc.ingameGUI.blit(stack, x, y, comboU, 0, combowidth, 32);
                        //fancy fill percentage
                        mc.ingameGUI.blit(stack, x, y + 33 - fillHeight, comboU, 65 - fillHeight, combowidth, fillHeight - 2);
                        //TRIANGLE!
//                    BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
//                    bufferbuilder.begin(4, DefaultVertexFormats.POSITION_TEX);
//                    bufferbuilder.pos(stack.getLast().getMatrix(), (float)x+combowidth, (float)y+46, (float)mc.ingameGUI.getBlitOffset()).tex(minU, maxV).endVertex();
//                    bufferbuilder.pos(stack.getLast().getMatrix(), (float)x, (float)y, (float)mc.ingameGUI.getBlitOffset()).tex(maxU, maxV).endVertex();
//                    bufferbuilder.pos(stack.getLast().getMatrix(), (float)x+combowidth, (float)y+46, (float)mc.ingameGUI.getBlitOffset()).tex(maxU, minV).endVertex();
//                    bufferbuilder.finishDrawing();
//                    RenderSystem.enableAlphaTest();
//                    WorldVertexBufferUploader.draw(bufferbuilder);
                        //NO TRIANGLE YET!
                    }

                    stack.pop();
                    RenderSystem.disableBlend();
                    //multiplier
                    //String combo = formatter.format(1 + Math.floor(currentComboLevel) / 10) + "X";
                    //mc.fontRenderer.drawString(event.getMatrixStack(), combo, width - 28 + ClientConfig.comboX, Math.min(height / 2 - barHeight / 2 + ClientConfig.comboY, height - barHeight) + 60, 0);
                }
                mc.getTextureManager().bindTexture(posture);
                //render posture bar if not full, displayed even out of combat mode because it's pretty relevant to not dying
                if (cap.getPosture() < cap.getMaxPosture() || cap.getStaggerTime() > 0 || cap.getShatterCooldown() < Math.floor(GeneralUtils.getAttributeValueSafe(player, WarAttributes.SHATTER.get())) || cap.getBarrier() < cap.getMaxBarrier())
                    drawPostureBarAt(true, stack, player, width, height);
                Entity look = getEntityLookedAt(player, 32);
                if (look instanceof LivingEntity) {
                    LivingEntity looked = (LivingEntity) look;
                    List<Skill> afflict = new ArrayList<>();
                    final ISkillCapability skill = CasterData.getCap(player);
                    final Skill variant = skill.getEquippedVariation(SkillCategories.coup_de_grace);
                    if (look != player && skill.isSkillUsable(variant)) {
                        CoupDeGrace cdg = (CoupDeGrace) variant;
                        if (cdg.willKillOnCast(player, looked)) {
                            afflict.add(cdg);
                        }
                    }
                    afflict.addAll(Marks.getCap(looked).getActiveMarks().keySet());
                    Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.enemyAfflict, width, height);
                    for (int index = 0; index < afflict.size(); index++) {
                        Skill s = afflict.get(index);
                        mc.getTextureManager().bindTexture(s.icon());
                        Color c = s.getColor();
                        RenderSystem.color4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                        mc.ingameGUI.blit(stack, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 - 8, pair.getSecond(), 0, 0, 16, 16, 16, 16);
                    }
                    RenderSystem.color4f(1, 1, 1, 1);
                    if (ClientConfig.CONFIG.enemyPosture.enabled && (CombatData.getCap((LivingEntity) look).getPosture() < CombatData.getCap((LivingEntity) look).getMaxPosture() || CombatData.getCap((LivingEntity) look).getStaggerTime() > 0 || cap.getShatterCooldown() < GeneralUtils.getAttributeValueSafe(player, WarAttributes.SHATTER.get()) || cap.getBarrier() < cap.getMaxBarrier()))
                        drawPostureBarAt(false, stack, looked, width, height);//Math.min(HudConfig.client.enemyPosture.x, width - 64), Math.min(HudConfig.client.enemyPosture.y, height - 64));
                }
            }
    }

    private static void drawPostureBarAt(boolean you, MatrixStack ms, LivingEntity elb, int width, int height) {
        ClientConfig.BarType b = ClientConfig.CONFIG.enemyPosture.bar;
        if (you) {
            b = ClientConfig.CONFIG.playerPosture.bar;
        }
        switch (b) {
            case AMO:
                drawAmoPostureBarAt(you, ms, elb, width, height);
                break;
            case DARKMEGA:
                drawDarkPostureBarAt(you, ms, elb, width, height);
                break;
            case CLASSIC:
                drawOldPostureBarAt(you, ms, elb, width, height);
                break;
        }
    }

    /**
     * Draws it with the coord as its center
     */
    private static void drawOldPostureBarAt(boolean you, MatrixStack ms, LivingEntity elb, int width, int height) {
        Pair<Integer, Integer> pair = you ? translateCoords(ClientConfig.CONFIG.playerPosture, width, height) : translateCoords(ClientConfig.CONFIG.enemyPosture, width, height);
        int atX = pair.getFirst();
        int atY = pair.getSecond();
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(posture);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().startSection("postureBar");
        float cap = itsc.getMaxPosture();
        short barWidth = 182;
        int left = atX - (barWidth / 2);
        float posture = itsc.getPosture();
        final float trueMaxPosture = Float.isFinite(itsc.getTrueMaxPosture()) ? itsc.getTrueMaxPosture() : 1f;
        float posPerc = posture / Math.max(0.1f, trueMaxPosture);
        posPerc = Float.isFinite(posPerc) ? posPerc : 0;
        posPerc = MathHelper.clamp(posPerc, 0, 1);
        double shatter = MathHelper.clamp(itsc.getBarrier() / itsc.getMaxBarrier(), 0, 1);
        if (cap > 0) {
            //shatter ticks
            if (shatter <= 0) {
                shatter = (double) (ResourceConfig.shatterCooldown + itsc.getShatterCooldown()) / ResourceConfig.shatterCooldown;
                RenderSystem.color3f(0, 0, 0);
            }
            int filled = (int) (shatter * (barWidth + 2));
            mc.ingameGUI.blit(ms, left - 1, atY - 1, 0, 74, filled, 7);
            RenderSystem.color3f(1, 1, 1);
            //base
            mc.ingameGUI.blit(ms, left, atY, 0, 64, barWidth, 5);
            filled = (int) (posPerc * (float) (barWidth));
            RenderSystem.color3f(1 - posPerc, posPerc, 30f / 255);
            //bar on top
            mc.ingameGUI.blit(ms, left, atY, 0, 69, filled, 5);
            //fatigue
            float fatigue = itsc.getMaxPosture() / trueMaxPosture;
            fatigue = Float.isFinite(fatigue) ? fatigue : 0;
            filled = (int) (fatigue * (float) (barWidth));
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

    /**
     * Draws it with the coord as its center
     */
    private static void drawAmoPostureBarAt(boolean you, MatrixStack ms, LivingEntity elb, int width, int height) {
        Pair<Integer, Integer> pair = you ? translateCoords(ClientConfig.CONFIG.playerPosture, width, height) : translateCoords(ClientConfig.CONFIG.enemyPosture, width, height);
        int atX = pair.getFirst();
        int atY = pair.getSecond();
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(posture);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().startSection("postureBar");
        float cap = itsc.getMaxPosture();
        //182
        //so we want the full size to be 240, 25 to be 125, and every 5 max posture changes this by 20 pixels
        int halfBarWidth = Math.min(240, (int) (Math.sqrt(itsc.getTrueMaxPosture()) * 25)) / 2;
        //divvy by 2 for two-pronged approach
        int flexBarWidth = halfBarWidth;
        final int barHeight = 7;
        //double shatter = MathHelper.clamp(itsc.getBarrier() / itsc.getMaxBarrier(), 0, 1);
        if (cap > 0) {
            final int shatter = itsc.getShatterCooldown();
            final int mshatter = (int) GeneralUtils.getAttributeValueSafe(elb, WarAttributes.SHATTER.get());
            //take a two-way approach to draw this:
            //first draw the cap brackets
            mc.ingameGUI.blit(ms, atX + flexBarWidth, atY - 1, 235, 0, 5, barHeight);
            mc.ingameGUI.blit(ms, atX - flexBarWidth - 5, atY - 1, 0, 0, 5, barHeight);
            //reduce length by fatigue and draw working bracket
            flexBarWidth -= (itsc.getFatigue() * halfBarWidth / itsc.getTrueMaxPosture());
            mc.ingameGUI.blit(ms, atX + flexBarWidth, atY - 1, 235, 40, 5, barHeight);
            mc.ingameGUI.blit(ms, atX - flexBarWidth - 5, atY - 1, 0, 40, 5, barHeight);
            //grayscale and change width if staggered
            if (itsc.getStaggerTime() > 0) {
                flexBarWidth = (int) ((itsc.getMaxStaggerTime() - itsc.getStaggerTime()) * flexBarWidth / (float) itsc.getMaxStaggerTime()) + 3;
                mc.ingameGUI.blit(ms, atX, atY, 238 - flexBarWidth, 13, flexBarWidth, barHeight - 1);
                mc.ingameGUI.blit(ms, atX - flexBarWidth, atY, 0, 13, flexBarWidth, barHeight - 1);
            } else {
                //reduce length by posture percentage
                flexBarWidth = (int) (itsc.getPosture() * halfBarWidth / itsc.getTrueMaxPosture()) + 4;
                mc.ingameGUI.blit(ms, atX, atY, 238 - flexBarWidth, 7, flexBarWidth, barHeight - 1);
                mc.ingameGUI.blit(ms, atX - flexBarWidth - 1, atY, 0, 7, flexBarWidth + 1, barHeight - 1);
                //determine bar color by shatter
                if (shatter > 0 && shatter != mshatter) {//gold that rapidly fades
                    RenderSystem.color4f(1, 1, 1, (float) shatter / mshatter);
                    mc.ingameGUI.blit(ms, atX, atY, 238 - flexBarWidth, 54, flexBarWidth, barHeight - 1);
                    mc.ingameGUI.blit(ms, atX - flexBarWidth - 1, atY, 0, 54, flexBarWidth + 1, barHeight - 1);
                    RenderSystem.color4f(1, 1, 1, 1);
                }
                // draw barrier if eligible
                final float barrier = itsc.getBarrier();
                flexBarWidth = (int) (barrier * halfBarWidth / itsc.getTrueMaxPosture()) + 5;
                int vOff = itsc.consumeBarrier(flip ? -.0001f : 0.0001f) == 0 ? 26 : 19;
                mc.ingameGUI.blit(ms, atX, atY - 1, 239 - flexBarWidth, vOff, flexBarWidth + 1, barHeight);
                mc.ingameGUI.blit(ms, atX - flexBarWidth, atY - 1, 0, vOff, flexBarWidth + 1, barHeight);
                //otherwise draw nothing
            }
            //draw the center bar as the last step
        }
        mc.getProfiler().endSection();
        mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
        RenderSystem.disableBlend();
        RenderSystem.color3f(1, 1, 1);
    }

    /**
     * Draws it with the coord as its center
     */
    private static void drawDarkPostureBarAt(boolean you, MatrixStack ms, LivingEntity elb, int width, int height) {
        Pair<Integer, Integer> pair = you ? translateCoords(ClientConfig.CONFIG.playerPosture, width, height) : translateCoords(ClientConfig.CONFIG.enemyPosture, width, height);
        int atX = pair.getFirst();
        int atY = pair.getSecond();
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(darkmega);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().startSection("postureBar");
        float cap = itsc.getMaxPosture();
        //182
        //so we want the full size to be 240, 25 to be 125, and every 5 max posture changes this by 20 pixels
        int halfBarWidth = Math.min(240, (int) (Math.sqrt(itsc.getTrueMaxPosture()) * 25)) / 2;
        //divvy by 2 for two-pronged approach
        int flexBarWidth = halfBarWidth;
        final int barHeight = 10;
        //double shatter = MathHelper.clamp(itsc.getBarrier() / itsc.getMaxBarrier(), 0, 1);
        if (cap > 0) {
            final int shatter = itsc.getShatterCooldown();
            final int mshatter = (int) GeneralUtils.getAttributeValueSafe(elb, WarAttributes.SHATTER.get());
            //take a two-way approach to draw this:

            //first draw the ending brackets
            mc.ingameGUI.blit(ms, atX, atY - 2, 240 - flexBarWidth - 10, 0, flexBarWidth + 10, barHeight);
            mc.ingameGUI.blit(ms, atX - flexBarWidth - 10, atY - 2, 0, 0, flexBarWidth + 10, barHeight);
            //reduce length by fatigue and draw working bracket
            flexBarWidth -= (itsc.getFatigue() * halfBarWidth / itsc.getTrueMaxPosture());
            int temp = flexBarWidth;
            mc.ingameGUI.blit(ms, atX + flexBarWidth, atY - 2, 232, 50, 10, barHeight);
            mc.ingameGUI.blit(ms, atX - flexBarWidth - 9, atY - 2, 0, 50, 12, barHeight);
            //grayscale and change width if staggered
            if (itsc.getStaggerTime() > 0) {
                flexBarWidth = (int) ((itsc.getMaxStaggerTime() - itsc.getStaggerTime()) * flexBarWidth / (float) itsc.getMaxStaggerTime()) + 3;
                mc.ingameGUI.blit(ms, atX, atY - 2, 238 - flexBarWidth, 20, flexBarWidth, barHeight);
                mc.ingameGUI.blit(ms, atX - flexBarWidth, atY - 2, 0, 20, flexBarWidth, barHeight);
            } else {
                //reduce length by posture percentage
                flexBarWidth = (int) (itsc.getPosture() * halfBarWidth / itsc.getTrueMaxPosture()) + 4;
                mc.ingameGUI.blit(ms, atX, atY - 2, 238 - flexBarWidth, 10, flexBarWidth, barHeight);
                mc.ingameGUI.blit(ms, atX - flexBarWidth, atY - 2, 2, 10, flexBarWidth, barHeight);
                // draw barrier if eligible
                flexBarWidth = (int) (itsc.getBarrier() * halfBarWidth / itsc.getTrueMaxPosture()) + 5;
                // hacky flip for usage
                flip = !flip;
                final int vOffset = itsc.consumeBarrier(flip ? -0.001f : 0.001f) == 0 ? 40 : 30;
                mc.ingameGUI.blit(ms, atX - flexBarWidth, atY - 2, 0, vOffset, flexBarWidth + 1, barHeight);
                mc.ingameGUI.blit(ms, atX, atY - 2, 239 - flexBarWidth, vOffset, flexBarWidth + 1, barHeight);
                // render shatter overlay if present
                if (shatter > 0) {
                    int insigniaU = 115, insigniaV = 80, insigniaW = 11;
                    if (shatter != mshatter) {//shattering
                        float otemp = 1 - (float) shatter / mshatter;
                        otemp *= 2;//will go over 1 if shatter is less than half
                        if (otemp > 1) {
                            RenderSystem.color4f(1, 1, 1, -(otemp - 2));
                            otemp = 1;
                        }
                        int fini = (int) (otemp * temp);
                        //gold that stretches out to the edges before disappearing
                        mc.ingameGUI.blit(ms, atX, atY - 2, 240 - fini - 10, 70, fini + 10, barHeight);
                        mc.ingameGUI.blit(ms, atX - fini - 9, atY - 2, 0, 70, fini + 10, barHeight);
                        RenderSystem.color4f(1, 1, 1, -(otemp - 2));
                        RenderSystem.color4f(1, 1, 1, 1);
                        insigniaV = 90;
                    }
                    //insignia
                    mc.ingameGUI.blit(ms, atX - insigniaW / 2, atY - 2, insigniaU, insigniaV, insigniaW, barHeight - 1);
                    //shatter bracket
                    mc.ingameGUI.blit(ms, atX - temp - 8, atY - 2, 0, 80, 10, barHeight);
                    //shatter bracket
                    mc.ingameGUI.blit(ms, atX + temp, atY - 2, 232, 80, 10, barHeight);
                }
            }
        }
        mc.getProfiler().endSection();
        mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
        RenderSystem.disableBlend();
        RenderSystem.color3f(1, 1, 1);
    }

    /**
     * @author Vazkii
     */
    public static Entity getEntityLookedAt(Entity e, double finalDistance) {
        Entity foundEntity = null;
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

    @SubscribeEvent
    public static void tickPlayer(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity p = mc.player;
        if (p != null && !mc.isGamePaused()) {
            if (e.phase == TickEvent.Phase.START) {
                Entity look = getEntityLookedAt(p, 32);
                if (look != lastTickLookAt) {
                    lastTickLookAt = look;
                    if (look instanceof LivingEntity && look.isAlive())
                        CombatChannel.INSTANCE.sendToServer(new RequestUpdatePacket(look.getEntityId()));
                    else
                        CombatChannel.INSTANCE.sendToServer(new RequestUpdatePacket(-1));
                }
                if (combatTicks != Integer.MAX_VALUE && combatTicks + ClientConfig.autoCombat == p.ticksExisted && CombatData.getCap(p).isCombatMode()) {
                    CombatChannel.INSTANCE.sendToServer(new CombatModePacket());
                }
                if (!Keybinds.PARRY.isKeyDown())
                    lastTickParry = false;
            } else {
                if (!mc.gameSettings.keyBindUseItem.isKeyDown())
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
        if (Keybinds.PARRY.getKeyConflictContext().isActive() && !lastTickParry && CombatConfig.sneakParry != 0 && Keybinds.PARRY.isPressed() && mc.player.isAlive()) {
            if (CombatConfig.sneakParry < 0) {
                mc.player.sendStatusMessage(new TranslationTextComponent("wardance.toggleparry." + (CombatData.getCap(mc.player).getParryingTick() == -1 ? "on" : "off")), true);

            }
            CombatChannel.INSTANCE.sendToServer(new ManualParryPacket());
            lastTickParry = true;

        }
    }

    @SubscribeEvent
    public static void sweepSwing(PlayerInteractEvent.LeftClickEmpty e) {
        Entity n = getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.MAIN_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
        if (n != null)
            CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(true, n));
        CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(true, n));
    }

    @SubscribeEvent
    public static void sweepSwingOff(PlayerInteractEvent.RightClickEmpty e) {
        if (!rightClick && GeneralConfig.dual && e.getHand() == Hand.OFF_HAND && ((CombatUtils.isWeapon(e.getEntityLiving(), e.getItemStack()) || (e.getItemStack().isEmpty() && CombatData.getCap(e.getPlayer()).isCombatMode())))) {
            rightClick = true;
            Entity n = getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
            e.getPlayer().swing(Hand.OFF_HAND, false);
            if (n != null)
                CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(false, n));
            CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(false, n));
        }
    }

    @SubscribeEvent
    public static void sweepSwingBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (Minecraft.getInstance().playerController.getIsHittingBlock()) return;
        float temp = CombatUtils.getCooledAttackStrength(e.getPlayer(), Hand.MAIN_HAND, 0.5f);
        Entity n = getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.MAIN_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
        if (n != null)
            CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(true, n));
        CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(true, n));
    }

    @SubscribeEvent
    public static void sweepSwingOffItem(PlayerInteractEvent.RightClickItem e) {
        if (!rightClick && GeneralConfig.dual && e.getHand() == Hand.OFF_HAND && ((CombatUtils.isWeapon(e.getEntityLiving(), e.getItemStack()) || (e.getItemStack().isEmpty() && CombatData.getCap(e.getPlayer()).isCombatMode())))) {
            rightClick = true;
            Entity n = getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
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
            Entity n = getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
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
            Entity n = getEntityLookedAt(e.getPlayer(), GeneralUtils.getAttributeValueHandSensitive(e.getPlayer(), ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
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
        double range = GeneralUtils.getAttributeValueSafe(mc.player, ForgeMod.REACH_DISTANCE.get()) - (mc.player.getHeldItemMainhand().isEmpty() ? 1 : 0);
        Vector3d look = mc.player.getLook(1);
        if (mc.pointedEntity != null) {
            if (GeneralUtils.getDistSqCompensated(mc.pointedEntity, mc.player) > range * range) {
                mc.pointedEntity = null;
                Vector3d miss = mc.player.getPositionVec().add(look.scale(range));
                mc.objectMouseOver = BlockRayTraceResult.createMiss(miss, Direction.getFacingFromVector(look.x, look.y, look.z), new BlockPos(miss));
            }
        } else if (getEntityLookedAt(mc.player, range) != null) {
            EntityRayTraceResult ertr = ProjectileHelper.rayTraceEntities(mc.player, mc.player.getEyePosition(0.5f), mc.player.getEyePosition(0.5f).add(look.scale(range)), mc.player.getBoundingBox().expand(look.scale(range)).grow(1.0D, 1.0D, 1.0D), (p_215312_0_) -> !p_215312_0_.isSpectator() && p_215312_0_.canBeCollidedWith(), range);
            if (ertr != null) {
                mc.objectMouseOver = ertr;
                mc.pointedEntity = ertr.getEntity();
            }
        }
    }

    @SubscribeEvent
    public static void noHitMouse(InputEvent.MouseInputEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        double range = GeneralUtils.getAttributeValueSafe(mc.player, ForgeMod.REACH_DISTANCE.get()) - (mc.player.getHeldItemMainhand().isEmpty() ? 1 : 0);
        Vector3d look = mc.player.getLook(1);
        if (mc.pointedEntity != null) {
            if (GeneralUtils.getDistSqCompensated(mc.pointedEntity, mc.player) > range * range) {
                mc.pointedEntity = null;
                Vector3d miss = mc.player.getPositionVec().add(look.scale(range));
                mc.objectMouseOver = BlockRayTraceResult.createMiss(miss, Direction.getFacingFromVector(look.x, look.y, look.z), new BlockPos(miss));
            }
        } else if (getEntityLookedAt(mc.player, range) != null) {
            EntityRayTraceResult ertr = ProjectileHelper.rayTraceEntities(mc.player, mc.player.getEyePosition(0.5f), mc.player.getEyePosition(0.5f).add(look.scale(range)), mc.player.getBoundingBox().expand(look.scale(range)).grow(1.0D, 1.0D, 1.0D), (p_215312_0_) -> !p_215312_0_.isSpectator() && p_215312_0_.canBeCollidedWith(), range);
            if (ertr != null) {
                mc.objectMouseOver = ertr;
                mc.pointedEntity = ertr.getEntity();
            }
        }
    }

    @SubscribeEvent
    public static void zTarget(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            double range = GeneralUtils.getAttributeValueSafe(mc.player, ForgeMod.REACH_DISTANCE.get()) - (mc.player.getHeldItemMainhand().isEmpty() ? 1 : 0);
            Vector3d look = mc.player.getLook(1);
            if (mc.pointedEntity != null) {
                if (GeneralUtils.getDistSqCompensated(mc.pointedEntity, mc.player) > range * range) {
                    mc.pointedEntity = null;
                    Vector3d miss = mc.player.getPositionVec().add(look.scale(range));
                    mc.objectMouseOver = BlockRayTraceResult.createMiss(miss, Direction.getFacingFromVector(look.x, look.y, look.z), new BlockPos(miss));
                }
            } else if (getEntityLookedAt(mc.player, range) != null) {
                Entity e = getEntityLookedAt(mc.player, range);
                mc.objectMouseOver = new EntityRayTraceResult(e, e.getPositionVec());
                mc.pointedEntity = e;
            }
        }
    }

    private static float updateValue(float f, float to) {
        if (f == -1) return to;
        boolean close = true;
        float temp = f;
        if (to > f) {
            f += MathHelper.clamp((to - temp) / 20, 0.01, 0.1);
            close = false;
        }
        if (to < f) {
            f += MathHelper.clamp((to - temp) / 20, -0.1, -0.01);
            close = !close;
        }
        if (close)
            f = to;
        return f;
    }

    private static Pair<Integer, Integer> translateCoords(ClientConfig.DisplayData dd, int width, int height) {
        return translateCoords(dd.anchorPoint, dd.numberX, dd.numberY, width, height);
    }

    private static Pair<Integer, Integer> translateCoords(ClientConfig.AnchorPoint ap, int x, int y, int width, int height) {
        int retx, rety;
        switch (ap) {
            case TOPLEFT:
                retx = 0;
                rety = 0;
                break;
            case TOPRIGHT:
                retx = 0;
                rety = width;
                break;
            case CROSSHAIR:
                retx = width / 2;
                rety = height / 2;
                break;
            case TOPCENTER:
                retx = width / 2;
                rety = 0;
                break;
            case BOTTOMLEFT:
                retx = 0;
                rety = height;
                break;
            case MIDDLELEFT:
                retx = 0;
                rety = height / 2;
                break;
            case BOTTOMRIGHT:
                retx = width;
                rety = height;
                break;
            case MIDDLERIGHT:
                retx = width;
                rety = height / 2;
                break;
            case BOTTOMCENTER:
                retx = width / 2;
                rety = height;
                break;
            default:
                retx = rety = 0;
        }
        retx = MathHelper.clamp(retx + x, 0, width);
        rety = MathHelper.clamp(rety + y, 0, height);
        return Pair.of(retx, rety);
    }

}
