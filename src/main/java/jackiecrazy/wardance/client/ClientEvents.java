package jackiecrazy.wardance.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.client.screen.dashboard.DashboardScreen;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.client.screen.scroll.ScrollScreen;
import jackiecrazy.wardance.client.screen.skill.SkillSelectionScreen;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.ClientConfig;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.handlers.TwoHandingHandler;
import jackiecrazy.wardance.networking.*;
import jackiecrazy.wardance.networking.combat.*;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
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
    private static final ResourceLocation expose = new ResourceLocation(WarDance.MODID, "textures/hud/exposed.png");
    private static final int ALLOWANCE = 7;
    /**
     * left, back, right
     */
    private static final long[] lastTap = {0, 0, 0, 0};
    private static final boolean[] tapped = {false, false, false, false};
    public static int combatTicks = -999;
    static boolean lastTickParry;
    static boolean weird = false;
    private static HashMap<String, Boolean> rotate;
    private static boolean sneak = false;
    private static double dodgeDecimal;
    private static Entity lastTickLookAt;
    private static boolean rightClick = false;
    private static int lastAttackTick = 0, lastSweepTick = 0;
    private static int meditationTicks=0;

    static {
        RenderUtils.formatter.setRoundingMode(RoundingMode.DOWN);
        RenderUtils.formatter_truncate.setRoundingMode(RoundingMode.DOWN);
        //eurgh
        RenderUtils.formatter.setMaximumFractionDigits(1); //340 = DecimalFormat.DOUBLE_FRACTION_DIGITS
        RenderUtils.formatter.setMinimumFractionDigits(0);
        RenderUtils.formatter.setMaximumFractionDigits(0); //340 = DecimalFormat.DOUBLE_FRACTION_DIGITS
        RenderUtils.formatter.setMinimumFractionDigits(0);
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

    @SubscribeEvent
    public static void skillReading(RenderTooltipEvent.Color e) {
        if (e.getItemStack().isEmpty() && (Minecraft.getInstance().screen instanceof DashboardScreen || Minecraft.getInstance().screen instanceof SkillSelectionScreen || Minecraft.getInstance().screen instanceof ScrollScreen)) {
            e.setBorderEnd(0xffffffff);
            e.setBorderStart(0xffffffff);
        }
        //e.setBackgroundStart(0xffffff);
        //e.setBackgroundEnd(0);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void dodge(MovementInputUpdateEvent e) {
        Minecraft mc = Minecraft.getInstance();
        Input mi = e.getInput();
        final ICombatCapability itsc = CombatData.getCap(mc.player);
//        if (itsc.getStunTime() > 0) {
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
            if (mc.player.isSprinting() && mi.shiftKeyDown && !sneak) {
                //if(mc.world.getTotalWorldTime()-lastSneak<=ALLOWANCE){
                dir = 99;
                //}
            }
            sneak = mi.shiftKeyDown;
            if (dir != -1)
                CombatChannel.INSTANCE.sendToServer(new DodgePacket(dir, mi.shiftKeyDown));
        }

        if (itsc.isExposed()) {
            //no moving while you're down! (except for a safety roll)
            KeyMapping.releaseAll();
            return;
        }
    }

    @SubscribeEvent
    public static void alert(LivingAttackEvent e) {
        if (Minecraft.getInstance().player == null) return;
        if ((e.getEntity() == Minecraft.getInstance().player && e.getSource().getEntity() instanceof LivingEntity) || e.getSource().getEntity() == Minecraft.getInstance().player) {
            if (ClientConfig.autoCombat > 0 && combatTicks != Integer.MAX_VALUE) {
                if (!CombatData.getCap(Minecraft.getInstance().player).isCombatMode())
                    CombatChannel.INSTANCE.sendToServer(new CombatModePacket());
                combatTicks = Minecraft.getInstance().player.tickCount;
            }
        }
    }

    @SubscribeEvent
    public static void downTick(LivingEvent.LivingTickEvent event) {
        final LivingEntity e = event.getEntity();
        if (e.isAlive()) {
            if (CombatData.getCap(e).isExposed()) {
                //boolean reg = (rotate.containsKey(EntityType.getKey(e.getType()).toString()));
                float height = e.getBbHeight();// reg && rotate.getOrDefault(EntityType.getKey(e.getType()).toString(), false) ? e.getBbWidth() : e.getBbHeight();
                if (event.getEntity().tickCount % 10 == 0)
                    event.getEntity().level().addParticle(ParticleTypes.ANGRY_VILLAGER, e.getX() + Math.sin(e.tickCount) * e.getBbWidth() / 2, e.getY() + height / 2, e.getZ() + Math.cos(e.tickCount) * e.getBbWidth() / 2, 0, 0, 0);
            }
            if (CombatData.getCap(e).isKnockedDown()) {
                if (event.getEntity().tickCount % 10 == 0)
                    event.getEntity().level().addParticle(ParticleTypes.MYCELIUM, e.getX() + Math.sin(e.tickCount) * e.getBbHeight() / 2, e.getY(), e.getZ() + Math.cos(e.tickCount) * e.getBbHeight() / 2, 0, 0, 0);
            }else if (CombatData.getCap(e).isStunned()) {
                event.getEntity().level().addParticle(ParticleTypes.CRIT, e.getX() + Math.sin(e.tickCount) * e.getBbWidth() / 2, e.getY() + e.getBbHeight() + 0.4, e.getZ() + Math.cos(e.tickCount) * e.getBbWidth() / 2, 0, 0, 0);
            }



        }
    }

    @SubscribeEvent
    public static void noXP(RenderGuiOverlayEvent.Pre e) {
        if (ClientConfig.hide && Minecraft.getInstance().player != null && CombatData.getCap(Minecraft.getInstance().player).isCombatMode() && e.getOverlay() == VanillaGuiOverlay.EXPERIENCE_BAR.type()) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void down(RenderLivingEvent.Pre event) {
        final LivingEntity e = event.getEntity();
        float width = e.getBbWidth();
        float height = e.getBbHeight();

        if (e.isAlive()) {
            if (CombatData.getCap(event.getEntity()).isKnockedDown()) {
                PoseStack ms = event.getPoseStack();
                //ms.push();
                //tall bois become flat bois
                boolean reg = (ForgeRegistries.ENTITY_TYPES.getKey(e.getType()) != null && rotate.containsKey(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()).toString()));
                boolean rot = reg ? rotate.getOrDefault(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()).toString(), false) : width < height;
                if (rot) {
                    ms.mulPose(Axis.XN.rotationDegrees(90));
                    ms.mulPose(Axis.ZP.rotationDegrees(-e.yBodyRot));
                    ms.mulPose(Axis.YP.rotationDegrees(e.yBodyRot));
                    ms.translate(0, -e.getBbHeight() / 2, 0);
                }
                //cube bois become side bois
                //flat bois become flatter bois
                //multi bois do nothing
            }
            if (CombatData.getCap(e).getRollTime() != 0 && e.getPose() == Pose.SLEEPING) {
                PoseStack ms = event.getPoseStack();
                ms.mulPose(Axis.YN.rotationDegrees(e.getYRot() - e.getBedOrientation().toYRot()));
//                ms.rotate(Vector3f.ZP.rotationDegrees(-e.renderYawOffset));
//                ms.rotate(Vector3f.YP.rotationDegrees(e.renderYawOffset));
            }
//            if(e.isPotionActive(FootworkEffects.PETRIFY.get())){
//                event.getRenderer()
//                Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
//            }
        }
    }

//    @SubscribeEvent
//    public static void buildContents(CreativeModeTabEvent.Register event) {
//        event.registerCreativeModeTab(new ResourceLocation(Gempire.MODID, "gemstones"), builder ->
//                // Set name of tab to display
//                builder.title(Component.translatable("item_group." + Gempire.MODID + ".gemstones"))
//                        // Set icon of creative tab
//                        .icon(() -> new ItemStack(ModItems.RUBY_GEM.get()))
//                        // Add default items to tab
//                        .displayItems((enabledFlags, populator, hasPermissions) -> {
//                            populator.accept(ModItems.AQUAMARINE_GEM.get());
//                            populator.accept(ModItems.NEPHRITE_GEM.get());
//                            populator.accept(ModItems.BISMUTH_GEM.get());
//                        })
//        );
//    }

    @SubscribeEvent
    public static void handRaising(RenderHandEvent e) {
        if (e.getHand().equals(InteractionHand.MAIN_HAND) || !GeneralConfig.dual) return;
        AbstractClientPlayer p = Minecraft.getInstance().player;
        if (p == null || p.isInvisible() || (!CombatData.getCap(p).isCombatMode() && (p.swingingArm != InteractionHand.OFF_HAND || !p.swinging)))
            return;
        if (CombatData.getCap(p).getHandBind(InteractionHand.OFF_HAND) > 0) {
            e.setCanceled(true);
            return;
        }
        if (!e.getItemStack().isEmpty()) return;
        e.setCanceled(true);
        float cd = CombatUtils.getCooledAttackStrength(p, InteractionHand.OFF_HAND, e.getPartialTick());
        float f6 = 1 - (cd * cd * cd);
        Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderPlayerArm(e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight(), f6, e.getSwingProgress(), p.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
    }

    @SubscribeEvent
    public static void tickPlayer(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p != null && !mc.isPaused()) {
            if (e.phase == TickEvent.Phase.START) {
                Entity look = RenderUtils.getEntityLookedAt(p, 32);
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
                //TODO reenable after elenai compat
//                if (WarCompat.elenaiDodge) {
//                    if (GeneralConfig.elenaiP && CombatData.getCap(p).getPostureGrace() > 0) {
//                        ClientTickEventListener.regen++;
//                    } else if (GeneralConfig.elenaiC) {
//                        dodgeDecimal += Math.floor(CombatData.getCap(p).getRank()) / 10;
//                        if (dodgeDecimal > 1) {
//                            dodgeDecimal--;
//                            ClientTickEventListener.regen--;
//                        }
//                    }
//                }
            }
        }
    }

    @SubscribeEvent
    public static void noFovChange(ComputeFovModifierEvent e) {
        if (CombatData.getCap(e.getPlayer()).isExposed())
            e.setNewFovModifier(0.7f);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleInputEvent(InputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (Keybinds.PARRY.getKeyConflictContext().isActive() && !lastTickParry && CombatConfig.parryTime != 0 && Keybinds.PARRY.consumeClick() && mc.player.isAlive()) {
            if (CombatConfig.parryTime < 0) {
                mc.player.displayClientMessage(Component.translatable("wardance.toggleparry." + (CombatData.getCap(mc.player).getParryingTick() == -1 ? "on" : "off")), true);

            }
            CombatChannel.INSTANCE.sendToServer(new ManualParryPacket());
            lastTickParry = true;

        }
    }

    @SubscribeEvent
    public static void sweepSwing(PlayerInteractEvent.LeftClickEmpty e) {
        Entity n = RenderUtils.getEntityLookedAt(e.getEntity(), GeneralUtils.getAttributeValueHandSensitive(e.getEntity(), ForgeMod.ENTITY_REACH.get(), InteractionHand.MAIN_HAND));
//        if (n != null && e.getEntity().tickCount != lastAttackTick) {
//            CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(true, n));
//            lastAttackTick = e.getEntity().tickCount;
//        }
        if (lastSweepTick != e.getEntity().tickCount)
            CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(true, n));
        lastSweepTick = e.getEntity().tickCount;
    }

    @SubscribeEvent
    public static void sweepSwingOff(PlayerInteractEvent.RightClickEmpty e) {
        if (TwoHandingHandler.suppressOffhand(e.getEntity(), e.getEntity().getMainHandItem()) && e.getHand() == InteractionHand.OFF_HAND)
            return;
        if (!rightClick && GeneralConfig.dual && e.getHand() == InteractionHand.OFF_HAND&& CombatData.getCap(e.getEntity()).isCombatMode() && (WeaponStats.isWeapon(e.getEntity(), e.getItemStack()) || e.getItemStack().isEmpty() || WeaponStats.isShield(e.getEntity(), e.getItemStack()))) {
            rightClick = true;
            Entity n = RenderUtils.getEntityLookedAt(e.getEntity(), GeneralUtils.getAttributeValueHandSensitive(e.getEntity(), ForgeMod.ENTITY_REACH.get(), InteractionHand.OFF_HAND));
            if (n == null && WeaponStats.isShield(e.getEntity(), e.getItemStack())) return;
            e.getEntity().swing(InteractionHand.OFF_HAND, false);
            if (n != null && e.getEntity().tickCount != lastAttackTick) {
                CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(false, n));
                lastAttackTick = e.getEntity().tickCount;
            }
            if (lastSweepTick != e.getEntity().tickCount)
                CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(false, n));
            lastSweepTick = e.getEntity().tickCount;
        }
    }

    @SubscribeEvent
    public static void sweepSwingBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (Minecraft.getInstance().gameMode.isDestroying()) return;
        float temp = CombatUtils.getCooledAttackStrength(e.getEntity(), InteractionHand.MAIN_HAND, 0.5f);
        Entity n = RenderUtils.getEntityLookedAt(e.getEntity(), GeneralUtils.getAttributeValueHandSensitive(e.getEntity(), ForgeMod.ENTITY_REACH.get(), InteractionHand.MAIN_HAND));
        if (n != null && e.getEntity().tickCount != lastAttackTick) {
            CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(true, n));
            lastAttackTick = e.getEntity().tickCount;
        }
        if (lastSweepTick != e.getEntity().tickCount)
            CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(true, n));
        lastSweepTick = e.getEntity().tickCount;
    }

    @SubscribeEvent
    public static void sweepSwingOffItem(PlayerInteractEvent.RightClickItem e) {
        if (TwoHandingHandler.suppressOffhand(e.getEntity(), e.getEntity().getMainHandItem()) && e.getHand() == InteractionHand.OFF_HAND)
            return;
        if (!rightClick && GeneralConfig.dual && e.getHand() == InteractionHand.OFF_HAND && CombatData.getCap(e.getEntity()).isCombatMode() && (WeaponStats.isWeapon(e.getEntity(), e.getItemStack()) || e.getItemStack().isEmpty() || WeaponStats.isShield(e.getEntity(), e.getItemStack()))) {
            rightClick = true;
            Entity n = RenderUtils.getEntityLookedAt(e.getEntity(), GeneralUtils.getAttributeValueHandSensitive(e.getEntity(), ForgeMod.ENTITY_REACH.get(), InteractionHand.OFF_HAND));
            if (n == null && WeaponStats.isShield(e.getEntity(), e.getItemStack())) return;
            e.getEntity().swing(InteractionHand.OFF_HAND, false);
            if (n != null && e.getEntity().tickCount != lastAttackTick) {
                CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(false, n));
                lastAttackTick = e.getEntity().tickCount;
            }
            if (lastSweepTick != e.getEntity().tickCount)
                CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(false, n));
            lastSweepTick = e.getEntity().tickCount;
        }
    }

    @SubscribeEvent
    public static void punchy(PlayerInteractEvent.EntityInteract e) {
        if (TwoHandingHandler.suppressOffhand(e.getEntity(), e.getEntity().getMainHandItem()) && e.getHand() == InteractionHand.OFF_HAND)
            return;
        if (!rightClick && GeneralConfig.dual && e.getHand() == InteractionHand.OFF_HAND && CombatData.getCap(e.getEntity()).isCombatMode() && (WeaponStats.isWeapon(e.getEntity(), e.getItemStack()) || e.getItemStack().isEmpty() || WeaponStats.isShield(e.getEntity(), e.getItemStack()))) {
            rightClick = true;
            Entity n = RenderUtils.getEntityLookedAt(e.getEntity(), GeneralUtils.getAttributeValueHandSensitive(e.getEntity(), ForgeMod.ENTITY_REACH.get(), InteractionHand.OFF_HAND));
            e.getEntity().swing(InteractionHand.OFF_HAND, false);
            if (n == null && WeaponStats.isShield(e.getEntity(), e.getItemStack())) return;
            if (n != null && e.getEntity().tickCount != lastAttackTick) {
                CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(false, n));
                lastAttackTick = e.getEntity().tickCount;
            }
            if (lastSweepTick != e.getEntity().tickCount)
                CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(false, n));
            lastSweepTick = e.getEntity().tickCount;
        }
    }

    @SubscribeEvent
    public static void sweepSwingOffItemBlock(PlayerInteractEvent.RightClickBlock e) {
        if (TwoHandingHandler.suppressOffhand(e.getEntity(), e.getEntity().getMainHandItem()) && e.getHand() == InteractionHand.OFF_HAND)
            return;
        if (!rightClick && GeneralConfig.dual && e.getHand() == InteractionHand.OFF_HAND && CombatData.getCap(e.getEntity()).isCombatMode() && (WeaponStats.isWeapon(e.getEntity(), e.getItemStack()) || e.getItemStack().isEmpty() || WeaponStats.isShield(e.getEntity(), e.getItemStack()))) {
            rightClick = true;
            Entity n = RenderUtils.getEntityLookedAt(e.getEntity(), GeneralUtils.getAttributeValueHandSensitive(e.getEntity(), ForgeMod.ENTITY_REACH.get(), InteractionHand.OFF_HAND) - (e.getItemStack().isEmpty() ? 1 : 0));
            e.getEntity().swing(InteractionHand.OFF_HAND, false);
            if (n == null && WeaponStats.isShield(e.getEntity(), e.getItemStack())) return;
            if (n != null && e.getEntity().tickCount != lastAttackTick) {
                CombatChannel.INSTANCE.sendToServer(new RequestAttackPacket(false, n));
                lastAttackTick = e.getEntity().tickCount;
            }
            if (lastSweepTick != e.getEntity().tickCount)
                CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(false, n));
            lastSweepTick = e.getEntity().tickCount;
        }
    }

//    private static void renderDie(LivingEntity passedEntity, float partialTicks, PoseStack poseStack) {
//        double x = passedEntity.xo + (passedEntity.getX() - passedEntity.xo) * partialTicks;
//        double y = passedEntity.yo + (passedEntity.getY() - passedEntity.yo) * partialTicks;
//        double z = passedEntity.zo + (passedEntity.getZ() - passedEntity.zo) * partialTicks;
//
//        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
//        Vec3 renderPos = renderDispatcher.camera.getPosition();
//
//        poseStack.pushPose();
//        poseStack.translate((float) (x - renderPos.x()), (float) (y - renderPos.y() + passedEntity.getBbHeight()), (float) (z - renderPos.z()));
//        RenderSystem.setShaderTexture(0, expose);
//        poseStack.translate(0.0D, (double) 1, 0.0D);
//        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
//        final float size = Mth.clamp(0.002F * CombatData.getCap(passedEntity).getMaxPosture(), 0.015f, 0.1f);
//        poseStack.scale(-size, -size, size);
//        GuiComponent.blit(poseStack, -32, -32, 0, 0, 64, 64, 64, 64);
//        poseStack.popPose();
//
//        //poseStack.translate(0.0D, -(NeatConfig.backgroundHeight + NeatConfig.barHeight + NeatConfig.backgroundPadding), 0.0D);
//    }

    //I think this is no longer necessary, but we'll seal it away for now
    /*@SubscribeEvent
    public static void noHit(InputEvent.KeyInputEvent e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        double range = GeneralUtils.getAttributeValueSafe(mc.player, ForgeMod.ENTITY_REACH.get()) - (mc.player.getMainHandItem().isEmpty() ? 1 : 0);
        Vec3 look = mc.player.getViewVector(1);
        if (mc.crosshairPickEntity != null) {
            if (GeneralUtils.getDistSqCompensated(mc.crosshairPickEntity, mc.player) > range * range) {
                mc.crosshairPickEntity = null;
                Vec3 miss = mc.player.position().add(look.scale(range));
                mc.hitResult = BlockHitResult.miss(miss, Direction.getNearest(look.x, look.y, look.z), new BlockPos(miss));
            }
        } else if (RenderEvents.getEntityLookedAt(mc.player, range) != null) {
            EntityHitResult ertr = ProjectileUtil.getEntityHitResult(mc.player, mc.player.getEyePosition(0.5f), mc.player.getEyePosition(0.5f).add(look.scale(range)), mc.player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D, 1.0D, 1.0D), (p_215312_0_) -> !p_215312_0_.isSpectator() && p_215312_0_.isPickable(), range);
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
        double range = GeneralUtils.getAttributeValueSafe(mc.player, ForgeMod.ENTITY_REACH.get()) - (mc.player.getMainHandItem().isEmpty() ? 1 : 0);
        Vec3 look = mc.player.getViewVector(1);
        if (mc.crosshairPickEntity != null) {
            if (GeneralUtils.getDistSqCompensated(mc.crosshairPickEntity, mc.player) > range * range) {
                mc.crosshairPickEntity = null;
                Vec3 miss = mc.player.position().add(look.scale(range));
                mc.hitResult = BlockHitResult.miss(miss, Direction.getNearest(look.x, look.y, look.z), new BlockPos(miss));
            }
        } else if (RenderEvents.getEntityLookedAt(mc.player, range) != null) {
            EntityHitResult ertr = ProjectileUtil.getEntityHitResult(mc.player, mc.player.getEyePosition(0.5f), mc.player.getEyePosition(0.5f).add(look.scale(range)), mc.player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D, 1.0D, 1.0D), (p_215312_0_) -> !p_215312_0_.isSpectator() && p_215312_0_.isPickable(), range);
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
            double range = GeneralUtils.getAttributeValueSafe(mc.player, ForgeMod.ENTITY_REACH.get()) - (mc.player.getMainHandItem().isEmpty() ? 1 : 0);
            Vec3 look = mc.player.getViewVector(1);
            if (mc.crosshairPickEntity != null) {
                if (GeneralUtils.getDistSqCompensated(mc.crosshairPickEntity, mc.player) > range * range) {
                    mc.crosshairPickEntity = null;
                    Vec3 miss = mc.player.position().add(look.scale(range));
                    mc.hitResult = BlockHitResult.miss(miss, Direction.getNearest(look.x, look.y, look.z), new BlockPos(miss));
                }
            } else if (RenderEvents.getEntityLookedAt(mc.player, range) != null) {
                Entity e = RenderEvents.getEntityLookedAt(mc.player, range);
                mc.hitResult = new EntityHitResult(e, e.position());
                mc.crosshairPickEntity = e;
            }
        }
    }*/

}
