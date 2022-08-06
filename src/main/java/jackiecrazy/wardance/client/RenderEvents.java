package jackiecrazy.wardance.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.ResourceConfig;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import jackiecrazy.wardance.utils.StealthUtils;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class RenderEvents {
    static final DecimalFormat formatter = new DecimalFormat("#.#");
    private static final Cache<LivingEntity, Tuple<StealthUtils.Awareness, Double>> cache = CacheBuilder.newBuilder().weakKeys().expireAfterWrite(1, TimeUnit.SECONDS).build();
    private static final ResourceLocation amo = new ResourceLocation(WarDance.MODID, "textures/hud/amo.png");
    private static final ResourceLocation darkmega = new ResourceLocation(WarDance.MODID, "textures/hud/dark.png");
    private static final ResourceLocation raihud = new ResourceLocation(WarDance.MODID, "textures/hud/thanksrai.png");
    private static final ResourceLocation stealth = new ResourceLocation(WarDance.MODID, "textures/hud/stealth.png");
    static float currentComboLevel = 0;
    private static float currentMightLevel = 0;
    private static float currentSpiritLevel = 0;
    private static boolean flip = false;

    /**
     * @Author Vazkii
     */
    @SubscribeEvent
    public static void down(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();

        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        MatrixStack poseStack = event.getMatrixStack();
        float partialTicks = event.getPartialTicks();
        Entity cameraEntity = camera.getEntity() != null ? camera.getEntity() : mc.player;

        Vector3d cameraPos = camera.getPosition();
        final ClippingHelper frustum = new ClippingHelper(poseStack.last().pose(), event.getProjectionMatrix());
        frustum.prepare(cameraPos.x(), cameraPos.y(), cameraPos.z());

        ClientWorld client = mc.level;
        if (client != null && ClientConfig.dodomeki) {
            Entity look = getEntityLookedAt(Minecraft.getInstance().player, 32);
            for (Entity entity : client.entitiesForRendering()) {
                if (entity != null && (entity != look || !ClientConfig.CONFIG.stealth.enabled || !CombatData.getCap(mc.player).isCombatMode()) && entity instanceof LivingEntity && entity != cameraEntity && entity.isAlive() && !entity.getIndirectPassengers().iterator().hasNext() && entity.shouldRender(cameraPos.x(), cameraPos.y(), cameraPos.z()) && (entity.noCulling || frustum.isVisible(entity.getBoundingBox()))) {
                    renderEye((LivingEntity) entity, partialTicks, poseStack);
                }
            }
        }

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
                    GameSettings gamesettings = mc.options;

                    if (gamesettings.getCameraType() == PointOfView.FIRST_PERSON) {

                        int width = sr.getGuiScaledWidth();
                        int height = sr.getGuiScaledHeight();

                        ClientPlayerEntity player = mc.player;
                        if (player == null) return;
                        if (!gamesettings.renderDebug || gamesettings.hideGui || player.isReducedDebugInfo() || gamesettings.reducedDebugInfo) {
                            if (mc.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                                GlStateManager._enableAlphaTest();
                                float cooldown = CombatUtils.getCooledAttackStrength(player, Hand.OFF_HAND, 0f);
                                boolean hyperspeed = false;

                                if (getEntityLookedAt(player, GeneralUtils.getAttributeValueHandSensitive(player, ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND)) != null && cooldown >= 1.0F) {
                                    hyperspeed = CombatUtils.getCooldownPeriod(player, Hand.OFF_HAND) > 5.0F;
                                    hyperspeed = hyperspeed & (getEntityLookedAt(player, GeneralUtils.getAttributeValueHandSensitive(player, ForgeMod.REACH_DISTANCE.get(), Hand.OFF_HAND))).isAlive();
                                }

                                int y = height / 2 - 7 - 7;
                                int x = width / 2 - 8;

                                if (hyperspeed) {
                                    mc.gui.blit(stack, x, y, 68, 94, 16, 16);
                                } else if (cooldown < 1.0F) {
                                    int k = (int) (cooldown * 17.0F);
                                    mc.gui.blit(stack, x, y, 36, 94, 16, 4);
                                    mc.gui.blit(stack, x, y, 52, 94, k, 4);
                                }
                            }
                        }
                    }
                }
            }
            if (event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR)) {
                //draw offhand cooldown, hotbar type
                if (mc.getCameraEntity() instanceof PlayerEntity) {
                    GlStateManager._clearColor(1.0F, 1.0F, 1.0F, 1.0F);
                    PlayerEntity p = (PlayerEntity) mc.getCameraEntity();
                    ItemStack itemstack = p.getOffhandItem();
                    HandSide oppositeHand = p.getMainArm().getOpposite();
                    int halfOfScreen = sr.getGuiScaledWidth() / 2;

                    GlStateManager._enableRescaleNormal();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderHelper.turnBackOn();

                    if (mc.options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
                        float strength = CombatUtils.getCooledAttackStrength(p, Hand.OFF_HAND, 0);
                        if (strength < 1.0F) {
                            int y = sr.getGuiScaledHeight() - 20;
                            int x = halfOfScreen + 91 + 6;
                            if (oppositeHand == HandSide.LEFT) {
                                x = halfOfScreen - 91 - 22;
                            }

                            mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
                            int modStrength = (int) (strength * 19.0F);
                            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                            mc.gui.blit(stack, x + 18, y, 0, 94, 18, 18);
                            mc.gui.blit(stack, x + 18, y + 18 - modStrength, 18, 112 - modStrength, 18, modStrength);
                        }
                    }

                    RenderHelper.turnOff();
                    RenderSystem.disableBlend();
                }
            }
        }

        if (event.getType().equals(RenderGameOverlayEvent.ElementType.ALL))
            if (mc.getCameraEntity() instanceof PlayerEntity) {
                ClientPlayerEntity player = mc.player;
                ICombatCapability cap = CombatData.getCap(player);
                int width = sr.getGuiScaledWidth();
                int height = sr.getGuiScaledHeight();
                mc.getTextureManager().bind(raihud);
                currentSpiritLevel = updateValue(currentSpiritLevel, cap.getSpirit());
                currentMightLevel = updateValue(currentMightLevel, cap.getMight());
                currentComboLevel = cap.getRank() > currentComboLevel ? updateValue(currentComboLevel, cap.getRank()) : cap.getRank();
                //yourCurrentPostureLevel = updateValue(yourCurrentPostureLevel, cap.getPosture());
                if (cap.isCombatMode()) {
                    stack.pushPose();
                    RenderSystem.enableBlend();
                    RenderSystem.enableAlphaTest();
                    Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.might, width, height);
                    int x = Math.max(pair.getFirst() - 16, 0);
                    int y = Math.min(pair.getSecond() - 16, height - 32);
                    int fillHeight = (int) (Math.min(1, currentMightLevel / cap.getMaxMight()) * 32);
                    if (ClientConfig.CONFIG.might.enabled) {
                        //might circle
                        RenderSystem.color4f(1, 1, 1, 1);
                        stack.pushPose();
                        stack.pushPose();
                        mc.gui.blit(stack, x, y, 0, 64, 32, 32);
                        stack.popPose();
                        //might circle filling
                        stack.pushPose();
                        mc.gui.blit(stack, x, y + 32 - fillHeight, 0, 96 - fillHeight, 32, fillHeight);
                        stack.popPose();
                        fillHeight += Math.min(fillHeight, 3);
                        fillHeight = Math.min(fillHeight, 32);
                        //might base
                        stack.pushPose();
                        mc.gui.blit(stack, x, y, 32, 64, 32, 32);
                        stack.popPose();
                        //might illumination
                        stack.pushPose();
                        mc.gui.blit(stack, x, y + 32 - fillHeight, 64, 96 - fillHeight, 32, fillHeight);
                        stack.popPose();
                        stack.popPose();
                    }
                    pair = translateCoords(ClientConfig.CONFIG.spirit, width, height);
                    x = MathHelper.clamp(pair.getFirst() - 16, 0, width - 32);
                    y = MathHelper.clamp(pair.getSecond() - 16, 0, height - 32);
                    fillHeight = (int) (Math.min(1, currentSpiritLevel / cap.getMaxSpirit()) * 32);
                    String display = formatter.format(currentSpiritLevel) + "/" + formatter.format(cap.getMaxSpirit());
                    //spirit circle
                    stack.pushPose();
                    if (ClientConfig.CONFIG.spirit.enabled) {
                        stack.pushPose();
                        mc.gui.blit(stack, x, y, 0, 96, 32, 32);
                        stack.popPose();
                        //spirit circle filling
                        stack.pushPose();
                        mc.gui.blit(stack, x, y + 32 - fillHeight, 0, 128 - fillHeight, 32, fillHeight);
                        stack.popPose();
                        fillHeight += Math.min(fillHeight, 3);
                        fillHeight = Math.min(fillHeight, 32);
                        //spirit base
                        stack.pushPose();
                        mc.gui.blit(stack, x, y + 1, 32, 96, 32, 32);
                        stack.popPose();
                        //spirit illumination
                        stack.pushPose();
                        mc.gui.blit(stack, x, y + 33 - fillHeight, 64, 128 - fillHeight, 32, fillHeight);
                        stack.popPose();
                    }
                    if (ClientConfig.CONFIG.spiritNumber.enabled) {
                        pair = translateCoords(ClientConfig.CONFIG.spiritNumber, width, height);
                        mc.font.drawShadow(event.getMatrixStack(), display, pair.getFirst() - mc.font.width(display) / 2f, pair.getSecond() - 2, ClientConfig.spiritColor);
                    }
                    if (ClientConfig.CONFIG.mightNumber.enabled) {
                        pair = translateCoords(ClientConfig.CONFIG.mightNumber, width, height);
                        display = formatter.format(currentMightLevel) + "/" + formatter.format(cap.getMaxMight());
                        mc.font.drawShadow(event.getMatrixStack(), display, pair.getFirst() - mc.font.width(display) / 2f, pair.getSecond() - 2, ClientConfig.mightColor);
                    }
                    stack.popPose();

                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                    stack.popPose();
                    //combo bar at 224,20 to 229, 121. Grace at 222,95 to 224, 121
                    //initial bar
                    RenderSystem.enableBlend();
                    stack.pushPose();
                    if (ClientConfig.CONFIG.combo.enabled) {
                        mc.getTextureManager().bind(raihud);
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
                        mc.gui.blit(stack, x, y, comboU, 0, combowidth, 32);
                        //fancy fill percentage
                        mc.gui.blit(stack, x, y + 33 - fillHeight, comboU, 65 - fillHeight, combowidth, fillHeight - 2);
                    }

                    stack.popPose();
                    RenderSystem.disableBlend();
                }
                mc.getTextureManager().bind(amo);
                //render posture bar if not full, displayed even out of combat mode because it's pretty relevant to not dying
                if (cap.isCombatMode() || cap.getPosture() < cap.getMaxPosture() || cap.getStaggerTime() > 0 || cap.getShatterCooldown() < Math.floor(GeneralUtils.getAttributeValueSafe(player, WarAttributes.SHATTER.get())) || cap.getBarrier() < cap.getMaxBarrier())
                    drawPostureBarAt(true, stack, player, width, height);
                Entity look = getEntityLookedAt(player, 32);
                if (look instanceof LivingEntity) {
                    LivingEntity looked = (LivingEntity) look;
                    List<SkillData> afflict = new ArrayList<>();
                    final ISkillCapability skill = CasterData.getCap(player);
                    if (ClientConfig.CONFIG.enemyAfflict.enabled) {
                        //coup de grace
                        final Skill variant = skill.getEquippedVariation(SkillCategories.coup_de_grace);
                        if (look != player && variant instanceof CoupDeGrace && skill.isSkillUsable(variant)) {
                            CoupDeGrace cdg = (CoupDeGrace) variant;
                            if (cdg.willKillOnCast(player, looked)) {
                                afflict.add(new SkillData(cdg, 0, 0));
                            }
                        }
                        //marks
                        afflict.addAll(Marks.getCap(looked).getActiveMarks().values().stream().filter(a->a.getSkill().showsMark(a, looked)).collect(Collectors.toList()));
                        Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.enemyAfflict, width, height);
                        for (int index = 0; index < afflict.size(); index++) {
                            SkillData s = afflict.get(index);
                            mc.getTextureManager().bind(s.getSkill().icon());
                            Color c = s.getSkill().getColor();
                            RenderSystem.color4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
                            AbstractGui.blit(stack, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 - 8, pair.getSecond(), 0, 0, 16, 16, 16, 16);
                            if(s.getMaxDuration()!=0) {
                                String display = formatter.format(s.getDuration());
                                mc.font.drawShadow(event.getMatrixStack(), display, pair.getFirst() - (afflict.size() - 1 - index) * 16 + (afflict.size() - 1) * 8 - 8, pair.getSecond() - 2, 0);
                            }

                        }
                    }
                    RenderSystem.color4f(1, 1, 1, 1);
                    stealth:
                    {
                        if (ClientConfig.CONFIG.stealth.enabled && cap.isCombatMode()) {
                            Pair<Integer, Integer> pair = translateCoords(ClientConfig.CONFIG.stealth, width, height);
                            final Tuple<StealthUtils.Awareness, Double> info = stealthInfo(looked);
                            double dist = info.getB();
                            int shift = 0;
                            switch (info.getA()) {
                                case ALERT:
                                    break stealth;
                                case DISTRACTED:
                                    shift = 1;
                                    break;
                                case UNAWARE:
                                    if (Minecraft.getInstance().player != null)
                                        shift = looked.distanceToSqr(Minecraft.getInstance().player) < dist * dist ? 2 : 3;
                                    break;
                            }
                            if (info.getB()<0)
                                shift = 0;
                            mc.getTextureManager().bind(stealth);
                            AbstractGui.blit(stack, pair.getFirst() - 16, pair.getSecond() - 8, 0, shift * 16, 32, 16, 64, 64);
                        }
                    }
                    if (ClientConfig.CONFIG.enemyPosture.enabled && (cap.isCombatMode() || CombatData.getCap((LivingEntity) look).getPosture() < CombatData.getCap((LivingEntity) look).getMaxPosture() || CombatData.getCap((LivingEntity) look).getStaggerTime() > 0 || cap.getShatterCooldown() < GeneralUtils.getAttributeValueSafe(player, WarAttributes.SHATTER.get()) || cap.getBarrier() < cap.getMaxBarrier()))
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
        mc.getTextureManager().bind(amo);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().push("postureBar");
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
            mc.gui.blit(ms, left - 1, atY - 1, 0, 74, filled, 7);
            RenderSystem.color3f(1, 1, 1);
            //base
            mc.gui.blit(ms, left, atY, 0, 64, barWidth, 5);
            filled = (int) (posPerc * (float) (barWidth));
            RenderSystem.color3f(1 - posPerc, posPerc, 30f / 255);
            //bar on top
            mc.gui.blit(ms, left, atY, 0, 69, filled, 5);
            //fatigue
            float fatigue = itsc.getMaxPosture() / trueMaxPosture;
            fatigue = Float.isFinite(fatigue) ? fatigue : 0;
            filled = (int) (fatigue * (float) (barWidth));
            RenderSystem.color3f(1, 0.1f, 0.1f);
            mc.gui.blit(ms, left + filled, atY, filled, 69, barWidth - filled, 5);
            if (itsc.getStaggerTime() > 0) {
                int invulTime = (int) (MathHelper.clamp((float) itsc.getStaggerTime() / (float) CombatConfig.staggerDuration, 0, 1) * (float) (barWidth));//apparently this is synced to the client?
                RenderSystem.color3f(0, 0, 0);//, ((float) itsc.getPosInvulTime()) / (float) CombatConfig.ssptime);
                mc.gui.blit(ms, left, atY, 0, 69, invulTime, 5);
            }

        }
        mc.getProfiler().pop();
        mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
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
        mc.getTextureManager().bind(amo);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().push("postureBar");
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
            mc.gui.blit(ms, atX + flexBarWidth, atY - 1, 235, 0, 5, barHeight);
            mc.gui.blit(ms, atX - flexBarWidth - 5, atY - 1, 0, 0, 5, barHeight);
            //reduce length by fatigue and draw working bracket
            flexBarWidth -= (itsc.getFatigue() * halfBarWidth / itsc.getTrueMaxPosture());
            mc.gui.blit(ms, atX + flexBarWidth, atY - 1, 235, 40, 5, barHeight);
            mc.gui.blit(ms, atX - flexBarWidth - 5, atY - 1, 0, 40, 5, barHeight);
            //grayscale and change width if staggered
            if (itsc.getStaggerTime() > 0) {
                int count = (int) ((itsc.getMaxStaggerCount() - itsc.getStaggerCount()) * flexBarWidth / (float) itsc.getMaxStaggerCount()) + 3;
                int time = (int) ((itsc.getMaxStaggerTime() - itsc.getStaggerTime()) * flexBarWidth / (float) itsc.getMaxStaggerTime()) + 3;
                flexBarWidth = Math.max(count, time);
                mc.gui.blit(ms, atX, atY, 238 - flexBarWidth, 13, flexBarWidth, barHeight - 1);
                mc.gui.blit(ms, atX - flexBarWidth, atY, 0, 13, flexBarWidth, barHeight - 1);
            } else {
                //reduce length by posture percentage
                flexBarWidth = (int) (itsc.getPosture() * halfBarWidth / itsc.getTrueMaxPosture()) + 4;
                mc.gui.blit(ms, atX, atY, 238 - flexBarWidth, 7, flexBarWidth, barHeight - 1);
                mc.gui.blit(ms, atX - flexBarWidth - 1, atY, 0, 7, flexBarWidth + 1, barHeight - 1);
                //determine bar color by shatter
                if (shatter > 0 && shatter != mshatter) {//gold that rapidly fades
                    RenderSystem.color4f(1, 1, 1, (float) shatter / mshatter);
                    mc.gui.blit(ms, atX, atY, 238 - flexBarWidth, 54, flexBarWidth, barHeight - 1);
                    mc.gui.blit(ms, atX - flexBarWidth - 1, atY, 0, 54, flexBarWidth + 1, barHeight - 1);
                    RenderSystem.color4f(1, 1, 1, 1);
                }
                // draw barrier if eligible
                final float barrier = itsc.getBarrier();
                flexBarWidth = (int) (barrier * halfBarWidth / itsc.getTrueMaxPosture()) + 5;
                int vOff = itsc.consumeBarrier(flip ? -.0001f : 0.0001f) == 0 ? 26 : 19;
                mc.gui.blit(ms, atX, atY - 1, 239 - flexBarWidth, vOff, flexBarWidth + 1, barHeight);
                mc.gui.blit(ms, atX - flexBarWidth, atY - 1, 0, vOff, flexBarWidth + 1, barHeight);
                //otherwise draw nothing
            }
            //draw the center bar as the last step
        }
        mc.getProfiler().pop();
        mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
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
        mc.getTextureManager().bind(darkmega);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        ICombatCapability itsc = CombatData.getCap(elb);
        mc.getProfiler().push("postureBar");
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
            mc.gui.blit(ms, atX, atY - 2, 240 - flexBarWidth - 10, 0, flexBarWidth + 10, barHeight);
            mc.gui.blit(ms, atX - flexBarWidth - 10, atY - 2, 0, 0, flexBarWidth + 10, barHeight);
            //reduce length by fatigue and draw working bracket
            flexBarWidth -= (itsc.getFatigue() * halfBarWidth / itsc.getTrueMaxPosture());
            int temp = flexBarWidth;
            mc.gui.blit(ms, atX + flexBarWidth, atY - 2, 232, 50, 10, barHeight);
            mc.gui.blit(ms, atX - flexBarWidth - 9, atY - 2, 0, 50, 12, barHeight);
            //grayscale and change width if staggered
            if (itsc.getStaggerTime() > 0) {
                int count = (int) ((itsc.getMaxStaggerCount() - itsc.getStaggerCount()) * flexBarWidth / (float) itsc.getMaxStaggerCount()) + 3;
                int time = (int) ((itsc.getMaxStaggerTime() - itsc.getStaggerTime()) * flexBarWidth / (float) itsc.getMaxStaggerTime()) + 3;
                flexBarWidth = Math.max(count, time);
                mc.gui.blit(ms, atX, atY - 2, 238 - flexBarWidth, 20, flexBarWidth, barHeight);
                mc.gui.blit(ms, atX - flexBarWidth, atY - 2, 0, 20, flexBarWidth, barHeight);
            } else {
                //reduce length by posture percentage
                flexBarWidth = (int) (itsc.getPosture() * halfBarWidth / itsc.getTrueMaxPosture()) + 4;
                mc.gui.blit(ms, atX, atY - 2, 238 - flexBarWidth, 10, flexBarWidth, barHeight);
                mc.gui.blit(ms, atX - flexBarWidth, atY - 2, 2, 10, flexBarWidth, barHeight);
                // draw barrier if eligible
                flexBarWidth = (int) (itsc.getBarrier() * halfBarWidth / itsc.getTrueMaxPosture()) + 5;
                // hacky flip for usage
                flip = !flip;
                final int vOffset = itsc.consumeBarrier(flip ? -0.001f : 0.001f) == 0 ? 40 : 30;
                mc.gui.blit(ms, atX - flexBarWidth, atY - 2, 0, vOffset, flexBarWidth + 1, barHeight);
                mc.gui.blit(ms, atX, atY - 2, 239 - flexBarWidth, vOffset, flexBarWidth + 1, barHeight);
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
                        mc.gui.blit(ms, atX, atY - 2, 240 - fini - 10, 70, fini + 10, barHeight);
                        mc.gui.blit(ms, atX - fini - 9, atY - 2, 0, 70, fini + 10, barHeight);
                        RenderSystem.color4f(1, 1, 1, -(otemp - 2));
                        RenderSystem.color4f(1, 1, 1, 1);
                        insigniaV = 90;
                    }
                    //insignia
                    mc.gui.blit(ms, atX - insigniaW / 2, atY - 2, insigniaU, insigniaV, insigniaW, barHeight - 1);
                    //shatter bracket
                    mc.gui.blit(ms, atX - temp - 8, atY - 2, 0, 80, 10, barHeight);
                    //shatter bracket
                    mc.gui.blit(ms, atX + temp, atY - 2, 232, 80, 10, barHeight);
                }
            }
        }
        mc.getProfiler().pop();
        mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
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
        Vector3d positionVector = e.position();

        if (e instanceof PlayerEntity)
            positionVector = positionVector.add(0, e.getEyeHeight(e.getPose()), 0);

        if (pos != null)
            distance = pos.getLocation().distanceTo(positionVector);

        Vector3d lookVector = e.getLookAngle();
        Vector3d reachVector = positionVector.add(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance);

        Entity lookedEntity = null;
        List<Entity> entitiesInBoundingBox = e.getCommandSenderWorld().getEntities(e, e.getBoundingBox().inflate(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance).expandTowards(1F, 1F, 1F));
        double minDistance = distance;

        for (Entity entity : entitiesInBoundingBox) {
            if (entity.isPickable()) {
                AxisAlignedBB collisionBox = entity.getBoundingBoxForCulling();
                Optional<Vector3d> interceptPosition = collisionBox.clip(positionVector, reachVector);

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
        Vector3d vec = new Vector3d(e.getX(), e.getY(), e.getZ());
        if (e instanceof PlayerEntity)
            vec = vec.add(new Vector3d(0, e.getEyeHeight(e.getPose()), 0));

        Vector3d look = e.getLookAngle();
        if (look == null)
            return null;

        return raycast(vec, look, e, len);
    }

    public static RayTraceResult raycast(Vector3d origin, Vector3d ray, Entity e, double len) {
        Vector3d next = origin.add(ray.normalize().scale(len));
        return e.level.clip(new RayTraceContext(origin, next, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, e));
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

    private static Tuple<StealthUtils.Awareness, Double> stealthInfo(LivingEntity at) {
        try {
            return cache.get(at, () -> {
                StealthUtils.Awareness a = StealthUtils.getAwareness(Minecraft.getInstance().player, at);
                double mult = Minecraft.getInstance().player.getVisibilityPercent(at);
                return new Tuple<>(a, mult * CombatData.getCap(at).visionRange());
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new Tuple<>(StealthUtils.Awareness.ALERT, 1d);
    }

    private static Pair<Integer, Integer> translateCoords(ClientConfig.DisplayData dd, int width, int height) {
        return translateCoords(dd.anchorPoint, dd.numberX, dd.numberY, width, height);
    }

    private static void renderEye(LivingEntity passedEntity, float partialTicks, MatrixStack poseStack) {
        final Tuple<StealthUtils.Awareness, Double> info = stealthInfo(passedEntity);
        double dist = info.getB();
        int shift = 0;
        switch (info.getA()) {
            case ALERT:
                return;
            case DISTRACTED:
                shift = 1;
                break;
            case UNAWARE:
                if (Minecraft.getInstance().player != null)
                    shift = passedEntity.distanceToSqr(Minecraft.getInstance().player) < dist * dist ? 2 : 3;
                break;
        }
        if(info.getB()<0)
            shift = 0;
        double x = passedEntity.xo + (passedEntity.getX() - passedEntity.xo) * partialTicks;
        double y = passedEntity.yo + (passedEntity.getY() - passedEntity.yo) * partialTicks;
        double z = passedEntity.zo + (passedEntity.getZ() - passedEntity.zo) * partialTicks;

        EntityRendererManager renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Vector3d renderPos = renderDispatcher.camera.getPosition();

        poseStack.pushPose();
        poseStack.translate((float) (x - renderPos.x()), (float) (y - renderPos.y() + passedEntity.getBbHeight()), (float) (z - renderPos.z()));
        Minecraft.getInstance().getTextureManager().bind(stealth);
        poseStack.translate(0.0D, (double) 0.5, 0.0D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        final float size = MathHelper.clamp(0.002F * CombatData.getCap(passedEntity).getTrueMaxPosture(), 0.015f, 0.1f);
        poseStack.scale(-size, -size, size);
        AbstractGui.blit(poseStack, -16, -8, 0, shift * 16, 32, 16, 64, 64);
        poseStack.popPose();

        //poseStack.translate(0.0D, -(NeatConfig.backgroundHeight + NeatConfig.barHeight + NeatConfig.backgroundPadding), 0.0D);
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
