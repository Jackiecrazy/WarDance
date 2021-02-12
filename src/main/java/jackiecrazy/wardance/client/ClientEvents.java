package jackiecrazy.wardance.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WarDance.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void down(RenderLivingEvent.Pre event) {
        final LivingEntity e = event.getEntity();
        float width = e.getWidth();
        float height = e.getHeight();
        if (e.isAlive()) {
            if (CombatData.getCap(event.getEntity()).getStaggerTime() > 0) {
                //System.out.println("yes");
                MatrixStack ms = event.getMatrixStack();
                ms.push();
                //tall bois become flat bois
                if (width < height) {
                    ms.rotate(Vector3f.XN.rotationDegrees(90));
                    ms.rotate(Vector3f.ZP.rotationDegrees(-e.renderYawOffset));
                    ms.rotate(Vector3f.YP.rotationDegrees(e.renderYawOffset));
                    ms.translate(0, -e.getHeight() / 2, 0);
//                    GlStateManager.rotate(180f, 0, 0, 0);
//                    GlStateManager.rotate(90f, 1, 0, 0);
//                    GlStateManager.rotate(event.getEntity().renderYawOffset, 0, 0, 1);
//                    GlStateManager.rotate(event.getEntity().renderYawOffset, 0, 1, 0);
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
    public static void downEnd(RenderLivingEvent.Post event) {
        if (event.getEntity().isAlive()) {
            if (CombatData.getCap(event.getEntity()).getStaggerTime() > 0) {//TaoCasterData.getTaoCap(event.getEntity()).getDownTimer()>0
                event.getMatrixStack().pop();
            }
        }
    }

    @SubscribeEvent
    public static void handRaising(RenderHandEvent e) {
        if (e.getHand().equals(Hand.MAIN_HAND)) return;
        AbstractClientPlayerEntity p = Minecraft.getInstance().player;
        //cancel event so two handed weapons give a visual cue to their two-handedness
//        if (p.getHeldItemMainhand().getItem() instanceof ITwoHanded) {
//            if (((TaoWeapon) p.getHeldItemMainhand().getItem()).isTwoHanded(p.getHeldItemMainhand())) {
//                e.setCanceled(true);
//
//                return;
//            }
//        }
        //force offhand to have some semblance of cooldown
        if (!CombatUtils.isWeapon(p, e.getItemStack()) && !CombatUtils.isShield(p, e.getItemStack()))
            return;
        e.setCanceled(true);
        ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
        float f1 = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * e.getPartialTicks();
        //MathHelper.clamp((!requipM ? f * f * f : 0.0F) - this.equippedProgressMainHand, -0.4F, 0.4F);//mainhand add per
        float cd = CombatUtils.getCooledAttackStrength(p, Hand.OFF_HAND, e.getPartialTicks());
        float f6 = 1 - (cd * cd * cd);
        Minecraft.getInstance().getFirstPersonRenderer().renderItemInFirstPerson(p, e.getPartialTicks(), f1, Hand.OFF_HAND, e.getSwingProgress(), p.getHeldItemOffhand(), f6, e.getMatrixStack(), e.getBuffers(), e.getLight());
    }
//
//    @SubscribeEvent
//    public static void displayCoolie(RenderGameOverlayEvent.Post event) {
//        MainWindow sr = event.getWindow();
//        final Minecraft mc = Minecraft.getInstance();
//        if (event.getType().equals(RenderGameOverlayEvent.ElementType.CROSSHAIRS)) {
//            //draw offhand cooldown, crosshair type
//            {
//                GameSettings gamesettings = mc.gameSettings;
//
//                if (gamesettings.getPointOfView() == PointOfView.FIRST_PERSON) {
//
//                    int width = sr.getScaledWidth();
//                    int height = sr.getScaledHeight();
//
//                    ClientPlayerEntity player = mc.player;
//                    if (!gamesettings.showDebugInfo || gamesettings.hideGUI || player.hasReducedDebug() || gamesettings.reducedDebugInfo) {
//                        if (mc.gameSettings.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
//                            GlStateManager.enableAlphaTest();
//                            float cooldown = CombatUtils.getCooledAttackStrength(player, Hand.OFF_HAND, 0f);
//                            boolean hyperspeed = false;
//
//                            if (mc.pointedEntity instanceof LivingEntity && cooldown >= 1.0F) {
//                                hyperspeed = CombatUtils.getCooldownPeriod(player, Hand.OFF_HAND) > 5.0F;
//                                hyperspeed = hyperspeed & (mc.pointedEntity).isAlive();
//                            }
//
//                            int y = height / 2 - 7 - 7;
//                            int x = width / 2 - 8;
//
//                            if (hyperspeed) {
//                                mc.ingameGUI.drawTexturedModalRect(x, y, 68, 94, 16, 16);
//                            } else if (cooldown < 1.0F) {
//                                int k = (int) (cooldown * 17.0F);
//                                mc.ingameGUI.drawTexturedModalRect(x, y, 36, 94, 16, 4);
//                                mc.ingameGUI.drawTexturedModalRect(x, y, 52, 94, k, 4);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if (event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR)) {
//            //draw offhand cooldown, hotbar type
//            if (mc.getRenderViewEntity() instanceof EntityPlayer) {
//                GlStateManager.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
//                EntityPlayer p = (EntityPlayer) mc.getRenderViewEntity();
//                ItemStack itemstack = p.getHeldItemOffhand();
//                EnumHandSide oppositeHand = p.getPrimaryHand().opposite();
//                int halfOfScreen = sr.getScaledWidth() / 2;
//
//                GlStateManager.enableRescaleNormal();
//                GlStateManager.enableBlend();
//                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//                RenderHelper.enableGUIStandardItemLighting();
//
//                if (mc.gameSettings.attackIndicator == 2) {
//                    float strength = TaoCombatUtils.getCooledAttackStrengthOff(p, 0);
//
//                    if (strength < 1.0F) {
//                        int y = sr.getScaledHeight() - 20;
//                        int x = halfOfScreen + 91 + 6;
//
//                        if (oppositeHand == EnumHandSide.LEFT) {
//                            x = halfOfScreen - 91 - 22;
//                        }
//
//                        mc.getTextureManager().bindTexture(Gui.ICONS);
//                        int modStrength = (int) (strength * 19.0F);
//                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//                        mc.ingameGUI.drawTexturedModalRect(x, y, 0, 94, 18, 18);
//                        mc.ingameGUI.drawTexturedModalRect(x, y + 18 - modStrength, 18, 112 - modStrength, 18, modStrength);
//                    }
//                }
//
//                RenderHelper.disableStandardItemLighting();
//                GlStateManager.disableRescaleNormal();
//                GlStateManager.disableBlend();
//            }
//        }
//
//
//        if (event.getType().equals(RenderGameOverlayEvent.ElementType.ALL))
//            if (mc.getRenderViewEntity() instanceof EntityPlayer) {
//                GameSettings gamesettings = mc.gameSettings;
//                EntityPlayerSP player = mc.player;
//                ITaoStatCapability cap = TaoCasterData.getTaoCap(player);
//                int width = sr.getScaledWidth();
//                int height = sr.getScaledHeight();
//                //if (gamesettings.thirdPersonView == 0) {
//                mc.getTextureManager().bindTexture(hud);
//                float targetQiLevel = cap.getQi();
//                boolean closeEnough = true;
//                if (targetQiLevel > currentQiLevel) {
//                    currentQiLevel += Math.min(0.1, (targetQiLevel - currentQiLevel) / 20);
//                    closeEnough = false;
//                }
//                if (targetQiLevel < currentQiLevel) {
//                    currentQiLevel -= Math.min(0.1, (currentQiLevel - targetQiLevel) / 20);
//                    closeEnough = !closeEnough;
//                }
//                if (closeEnough)
//                    currentQiLevel = targetQiLevel;
//                int qi = (int) (currentQiLevel);
//                float qiExtra = currentQiLevel - qi;
//                //System.out.println(currentQiLevel);
//                //System.out.println(qi);
//                if (qi != 0 || qiExtra != 0f) {
//                    //render qi bar
//                    GlStateManager.pushMatrix();
//                    //GlStateManager.bindTexture(mc.renderEngine.getTexture(qibar).getGlTextureId());
//                    //bar
//                    GlStateManager.pushMatrix();
//                    GlStateManager.enableBlend();
//                    GlStateManager.enableAlpha();
//                    //int c = GRADIENTE[MathHelper.clamp((int) (qiExtra *(GRADIENTE.length)), 0, GRADIENTE.length - 1)];
//                    //GlStateManager.color(red(c), green(c), blue(c));
//                    GlStateManager.color(1, 1, 1, qi > 0 ? 1 : qiExtra);
//                    mc.ingameGUI.drawTexturedModalRect(Math.min(HudConfig.client.qi.x, width - 64), Math.min(HudConfig.client.qi.y, height - 64), 0, 0, 64, 64);//+(int)(qiExtra*32)
//                    GlStateManager.popMatrix();
//
//                    if (qi > 0) {
//                        //overlay
//                        GlStateManager.pushMatrix();
//                        GlStateManager.color(qiExtra, qiExtra, qiExtra, qiExtra);
//                        //GlStateManager.bindTexture(mc.renderEngine.getTexture(qihud[qi]).getGlTextureId());
//                        mc.ingameGUI.drawTexturedModalRect(Math.min(HudConfig.client.qi.x, width - 64), Math.min(HudConfig.client.qi.y, height - 64), ((qi + 1) * 64) % 256, Math.floorDiv((qi + 1), 4) * 64, 64, 64);
//                        //GlStateManager.resetColor();
//                        //mc.renderEngine.bindTexture();
//                        GlStateManager.popMatrix();
//
//                        //overlay layer 2
//                        GlStateManager.pushMatrix();
//                        GlStateManager.color(1f, 1f, 1f);
//                        //GlStateManager.bindTexture(mc.renderEngine.getTexture(qihud[qi]).getGlTextureId());
//                        mc.ingameGUI.drawTexturedModalRect(Math.min(HudConfig.client.qi.x, width - 64), Math.min(HudConfig.client.qi.y, height - 64), (qi * 64) % 256, Math.floorDiv(qi, 4) * 64, 64, 64);
//                        //GlStateManager.resetColor();
//                        //mc.renderEngine.bindTexture();
//                        GlStateManager.popMatrix();
//                    }
//                    GlStateManager.disableAlpha();
//                    GlStateManager.disableBlend();
//                    GlStateManager.popMatrix();
//                }
//
//                //render posture bar if not full
//                if (cap.getPosture() < cap.getMaxPosture() || cap.getDownTimer() > 0)
//                    drawPostureBarreAt(player, width / 2, height - 57);
//                Entity look = getEntityLookedAt(player);
//                if (look instanceof EntityLivingBase && HudConfig.client.displayEnemyPosture && (TaoCasterData.getTaoCap((EntityLivingBase) look).getPosture() < TaoCasterData.getTaoCap((EntityLivingBase) look).getMaxPosture() || TaoCasterData.getTaoCap((EntityLivingBase) look).getDownTimer() > 0)) {
//                    drawPostureBarreAt((EntityLivingBase) look, width / 2, 20);//Math.min(HudConfig.client.enemyPosture.x, width - 64), Math.min(HudConfig.client.enemyPosture.y, height - 64));
//                }
//                //}
//            }
//    }
//
//    /**
//     * Draws it with the coord as its center
//     *
//     * @param elb
//     * @param atX
//     * @param atY
//     */
//    private static void drawPostureBarreAt(LivingEntity elb, int atX, int atY) {
//        Minecraft mc = Minecraft.getInstance();
//        mc.getTextureManager().bindTexture(hood);
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        GlStateManager.enableBlend();
//        GlStateManager.enableAlpha();
//        ITaoStatCapability itsc = TaoCasterData.getTaoCap(elb);
//        mc.mcProfiler.startSection("postureBar");
//        float cap = itsc.getMaxPosture();
//        int left = atX - 91;
//        float posPerc = MathHelper.clamp(itsc.getPosture() / itsc.getMaxPosture(), 0, 1);
//        int c = GRADIENTDOWN[(int) (posPerc * (GRADIENTDOWN.length - 1))];
//        if (itsc.isProtected()) c = GRADIENTSSP[(int) (posPerc * (GRADIENTSSP.length - 1))];
//        if (cap > 0) {
//            short barWidth = 182;
//            int filled = (int) (itsc.getPosture() / itsc.getMaxPosture() * (float) (barWidth));
//            int invulTime = (int) ((float) itsc.getPosInvulTime() / (float) CombatConfig.ssptime * (float) (barWidth));
//            mc.ingameGUI.drawTexturedModalRect(left, atY, 0, 64, barWidth, 5);
//            if (filled > invulTime) {
//                GlStateManager.color(red(c), green(c), blue(c));
//                mc.ingameGUI.drawTexturedModalRect(left, atY, 0, 69, filled, 5);
//            }
//            if (itsc.getDownTimer() > 0) {
//                invulTime = (int) (MathHelper.clamp((float) itsc.getDownTimer() / (float) TaoStatCapability.MAXDOWNTIME, 0, 1) * (float) (barWidth));
//                GlStateManager.color(0, 0, 0);//, ((float) itsc.getPosInvulTime()) / (float) CombatConfig.ssptime);
//                mc.ingameGUI.drawTexturedModalRect(left, atY, 0, 69, invulTime, 5);
//            } else {
//                GlStateManager.color(1, 225f / 255f, 0);//, ((float) itsc.getPosInvulTime()) / (float) CombatConfig.ssptime);
//                mc.ingameGUI.drawTexturedModalRect(left, atY, 0, 69, invulTime, 5);
//            }
//            if (filled <= invulTime) {
//                GlStateManager.color(red(c), green(c), blue(c));
//                mc.ingameGUI.drawTexturedModalRect(left, atY, 0, 69, filled, 5);
//            }
//        }
//        mc.mcProfiler.endSection();
////        mc.mcProfiler.startSection("postureNumber");
////        float postureNumber = ((int) (itsc.getPosture() * 100)) / 100f;
////        String text = "" + postureNumber;
////        int x = atX - (mc.fontRenderer.getStringWidth(text) / 2);
////        int y = atY - 1;
////        mc.fontRenderer.drawString(text, x + 1, y, 0);
////        mc.fontRenderer.drawString(text, x - 1, y, 0);
////        mc.fontRenderer.drawString(text, x, y + 1, 0);
////        mc.fontRenderer.drawString(text, x, y - 1, 0);
////        mc.fontRenderer.drawString(text, x, y, c.getRGB());
////        mc.mcProfiler.endSection();
//        mc.getTextureManager().bindTexture(Gui.ICONS);
//        GlStateManager.disableBlend();
//        GlStateManager.disableAlpha();
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//    }
//
//    public static Entity getEntityLookedAt(Entity e) {
//        Entity foundEntity = null;
//
//        final double finalDistance = 16;
//        double distance = finalDistance;
//        RayTraceResult pos = raycast(e, finalDistance);
//
//        Vec3d positionVector = e.getPositionVector();
//        if (e instanceof EntityPlayer)
//            positionVector = positionVector.addVector(0, e.getEyeHeight(), 0);
//
//        if (pos != null)
//            distance = pos.hitVec.distanceTo(positionVector);
//
//        Vec3d lookVector = e.getLookVec();
//        Vec3d reachVector = positionVector.addVector(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance);
//
//        Entity lookedEntity = null;
//        List<Entity> entitiesInBoundingBox = e.getEntityWorld().getEntitiesWithinAABBExcludingEntity(e, e.getEntityBoundingBox().grow(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance).expand(1F, 1F, 1F));
//        double minDistance = distance;
//
//        for (Entity entity : entitiesInBoundingBox) {
//            if (entity.canBeCollidedWith()) {
//                float collisionBorderSize = entity.getCollisionBorderSize();
//                AxisAlignedBB hitbox = entity.getEntityBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
//                RayTraceResult interceptPosition = hitbox.calculateIntercept(positionVector, reachVector);
//
//                if (hitbox.contains(positionVector)) {
//                    if (0.0D < minDistance || minDistance == 0.0D) {
//                        lookedEntity = entity;
//                        minDistance = 0.0D;
//                    }
//                } else if (interceptPosition != null) {
//                    double distanceToEntity = positionVector.distanceTo(interceptPosition.hitVec);
//
//                    if (distanceToEntity < minDistance || minDistance == 0.0D) {
//                        lookedEntity = entity;
//                        minDistance = distanceToEntity;
//                    }
//                }
//            }
//
//            if (lookedEntity != null && (minDistance < distance || pos == null))
//                foundEntity = lookedEntity;
//        }
//
//        return foundEntity;
//    }
//
//    private static float red(int a) {
//        return BinaryMachiavelli.getInteger(a, 16, 23) / 255f;
//    }
//
//    private static float green(int a) {
//        return BinaryMachiavelli.getInteger(a, 8, 15) / 255f;
//    }
//
//	/*@SubscribeEvent
//	public static void colorize(ColorHandlerEvent event){
//		Taoism.logger.debug("this is being called");
//	}*/
//
//    /*public static void draw(int x, int y, int textureX, int textureY, int width, int height) {
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder bufferbuilder = tessellator.getBuffer();
//        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//        bufferbuilder.pos(x, y + height, (double)this.zLevel).tex((float)(textureX + 0) * 0.00390625F, (float)(textureY + height) * 0.00390625F).endVertex();
//        bufferbuilder.pos(x + width, y + height, (double)this.zLevel).tex((float)(textureX + width) * 0.00390625F, (float)(textureY + height) * 0.00390625F).endVertex();
//        bufferbuilder.pos(x + width, y + 0, (double)this.zLevel).tex((float)(textureX + width) * 0.00390625F, (float)(textureY + 0) * 0.00390625F).endVertex();
//        bufferbuilder.pos(x, y + 0, (double)this.zLevel).tex((float)(textureX + 0) * 0.00390625F, (float)(textureY + 0) * 0.00390625F).endVertex();
//        tessellator.draw();
//    }*/
//
//    private static float blue(int a) {
//        return BinaryMachiavelli.getInteger(a, 0, 7) / 255f;
//    }
//
//    public static RayTraceResult raycast(Entity e, double len) {
//        Vec3d vec = new Vec3d(e.posX, e.posY, e.posZ);
//        if (e instanceof EntityPlayer)
//            vec = vec.add(new Vec3d(0, e.getEyeHeight(), 0));
//
//        Vec3d look = e.getLookVec();
//        if (look == null)
//            return null;
//
//        return raycast(e.getEntityWorld(), vec, look, len);
//    }
//
//    public static RayTraceResult raycast(World world, Vec3d origin, Vec3d ray, double len) {
//        Vec3d end = origin.add(ray.normalize().scale(len));
//        RayTraceResult pos = world.rayTraceBlocks(origin, end);
//        return pos;
//    }
}
