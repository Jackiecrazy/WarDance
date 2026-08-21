package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.capability.timeslow.ITimeChange;
import jackiecrazy.footwork.capability.timeslow.TimeSlowData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.permission.PermissionData;
import jackiecrazy.wardance.capability.aerial.AerialModeData;
import jackiecrazy.wardance.capability.charging.ChargingData;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.capability.quiver.QuiverData;
import jackiecrazy.wardance.capability.quiver.QuiverMenu;
import jackiecrazy.wardance.capability.resources.CombatDataOverride;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.skill.SkillCapability;
import jackiecrazy.wardance.capability.status.Mark;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.capability.stylish.StyleDataOverride;
import jackiecrazy.wardance.client.ClientEvents;
import jackiecrazy.wardance.compat.ElenaiCompat;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.weapon.TwohandingStats;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.entity.ai.ExposeGoal;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.SyncSkillPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class EntityHandler {
    public static final HashMap<Player, Entity> mustUpdate = new HashMap<>();
    public static final HashMap<Player, Double> fasterUse = new HashMap<>();

    public static Level getWorld() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return ClientEvents.getClientWorld();
        } else {
            return server.getLevel(Level.OVERWORLD);
        }
    }

    // DANGER REMEMBER TO REMOVE
//    @SubscribeEvent
//    public static void lagMachine(TickEvent.ServerTickEvent event) {
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException ignored) {}
//    }

    public static final HashMap<ResourceLocation, Integer> LAST_TICK_SPAWN_FAIL=new HashMap<>();
    public static final HashMap<ResourceLocation, Integer> LAST_TICK_DESPAWN=new HashMap<>();
    public static final HashMap<ResourceLocation, Integer> HISTORICAL_SPAWN_SUCCESS=new HashMap<>();
//    @SubscribeEvent
//    public static void trackSpawns(TickEvent.ServerTickEvent event) {
//        if(!LAST_TICK_SPAWN_FAIL.isEmpty()) {
//            WarDance.LOGGER.debug("in the last tick, the following mobs attempted and failed to spawn:");
//            WarDance.LOGGER.debug(LAST_TICK_SPAWN_FAIL.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).toList());
//            AtomicReference<Integer> fails= new AtomicReference<>(0);
//            AtomicReference<Integer> success= new AtomicReference<>(0);
//            LAST_TICK_SPAWN_FAIL.forEach((k,v)-> fails.updateAndGet(v1 -> v1 + v));
//            HISTORICAL_SPAWN_SUCCESS.forEach((k,v)-> success.updateAndGet(v1 -> v1 + v));
//            WarDance.LOGGER.debug("spawn fail rate: "+fails.get()+"/"+(fails.get()+success.get()));
//            WarDance.LOGGER.debug("in addition the following mobs despawned:");
//            WarDance.LOGGER.debug(LAST_TICK_DESPAWN.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).toList());
//            LAST_TICK_SPAWN_FAIL.clear();
//            HISTORICAL_SPAWN_SUCCESS.clear();
//            LAST_TICK_DESPAWN.clear();
//        }
//    }

    @SubscribeEvent
    public static void start(ServerStartingEvent e) {
        mustUpdate.clear();
        fasterUse.clear();
        WeaponStats.DESPERATION = BuiltInRegistries.ITEM.stream()
                .filter(item -> item.builtInRegistryHolder().is(WeaponStats.DESPERATE_THROW))
                .toList();
        WeaponStats.GUARDIANS = BuiltInRegistries.ITEM.stream()
                .filter(item -> item.builtInRegistryHolder().is(WeaponStats.GUARDIAN_WEAPONS))
                .toList();
    }

    @SubscribeEvent
    public static void stop(ServerStoppingEvent e) {
        mustUpdate.clear();
        fasterUse.clear();
    }

    @SubscribeEvent
    public static void sonic(LivingEntityUseItemEvent.Start e) {
        if (e.getEntity() instanceof Player p) {
            if (CombatData.getCap(p).alreadyProc("speedItemUseTimer")) {
                double spd = CombatData.getCap(p).getProc("speedItemUseTick");
                fasterUse.put(p, spd);
            }
        }
    }

    @SubscribeEvent
    public static void sonic(LivingEntityUseItemEvent.Tick e) {
        if (e.getEntity() instanceof Player p) {
            int ticked = ChargingData.getCap(p).tick(e.getItem());
            if (ticked != 1)
                e.setDuration(e.getDuration() + 1 - ticked);
        }
    }


    @SubscribeEvent
    public static void fall(LivingFallEvent e) {
        if (e.getEntity() instanceof Player p && TimeSlowData.getCap(p).getEffectiveSpeed() < 1)
            e.setCanceled(true);
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void what(AttachCapabilitiesEvent<ItemStack> e) {
//        if (e.getObject().isStackable() && !e.getCapabilities().isEmpty()) {
//            WarDance.LOGGER.fatal(e.getObject().getItem()+" has "+e.getCapabilities()+" attached to it, this is probably bad.");
//        }
    }

    @SubscribeEvent
    public static void caps(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof LivingEntity lb) {
            e.addCapability(new ResourceLocation("wardance:combatinfo"), new CombatDataOverride(lb));
            e.addCapability(new ResourceLocation("wardance:stylishdata"), new StyleDataOverride(lb));
            e.addCapability(new ResourceLocation("wardance:statuseffects"), new Marks(new Mark(lb)));
            e.addCapability(new ResourceLocation("wardance:aerialmode"), new AerialModeData(lb));
            if (lb instanceof Player p) {
                e.addCapability(new ResourceLocation("wardance:casterinfo"), new CasterData(new SkillCapability(lb)));
                e.addCapability(new ResourceLocation("wardance:permissions"), new PermissionData(p));
                e.addCapability(new ResourceLocation("wardance:flyingweapon"), new FlyingWeaponData(p));
                e.addCapability(new ResourceLocation("wardance:fasterusing"), new ChargingData());
                e.addCapability(new ResourceLocation(WarDance.MODID, "quiver_data"), new QuiverData());
            }
        }
    }

    @SubscribeEvent
    public static void death(LivingEvent.LivingJumpEvent e) {
        if (CombatData.getCap(e.getEntity()).isStunned()) {
            if (!(e.getEntity() instanceof Player))
                e.getEntity().setDeltaMovement(0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void login(EntityJoinLevelEvent e) {
        if (!e.getLevel().isClientSide && e.getEntity() instanceof ServerPlayer sp) {
            WeaponStats.sendItemData(sp);
            TwohandingStats.sendItemData(sp);
            QuiverData.getData(sp).sync(sp);
        }
        //steve time extensions
        if (e.getEntity() instanceof OwnableEntity o && o.getOwner() != null) {
            ITimeChange itc = TimeSlowData.getCap(o.getOwner());
            if (itc.getEffectiveSpeed() != 1)
                TimeSlowData.getCap(e.getEntity()).alterSpeed(itc.getTimeRemaining(), itc.getEffectiveSpeed());
        }
        if (e.getEntity() instanceof TraceableEntity o && o.getOwner() != null) {
            ITimeChange itc = TimeSlowData.getCap(o.getOwner());
            if (itc.getEffectiveSpeed() != 1)
                TimeSlowData.getCap(e.getEntity()).alterSpeed(itc.getTimeRemaining(), itc.getEffectiveSpeed());

        }
    }



    @SubscribeEvent
    public static void takeThis(EntityJoinLevelEvent e) {
        if(e.getEntity() instanceof Projectile p && p.getOwner() instanceof LivingEntity le){
            if(CombatData.getCap(le).getHandBind(InteractionHand.MAIN_HAND)>0)
                e.setCanceled(true);
        }
        if (e.getEntity() instanceof ServerPlayer) {
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) e.getEntity()), new SyncSkillPacket(CasterData.getCap((LivingEntity) e.getEntity()).write()));
        }
        if (e.getEntity() instanceof Mob mob) {
            mob.goalSelector.addGoal(-1, new ExposeGoal(mob));
            mob.targetSelector.addGoal(-1, new ExposeGoal(mob));
        }
    }

    @SubscribeEvent
    public static void inventory(PlayerContainerEvent.Close e) {
        if (e.getContainer() instanceof QuiverMenu menu) {
            QuiverData.getData(e.getEntity()).sync(e.getEntity());
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.Clone e) {
        final Player orig = e.getOriginal();
        final Player p = e.getEntity();
        orig.reviveCaps();
        if (!e.isWasDeath()) {
            final ICombatCapability icc = CombatData.getCap(p);
            icc.read(CombatData.getCap(e.getOriginal()).write());
        }
        //CasterData.getCap(e.getPlayer()).read(CasterData.getCap(e.getOriginal()).write());
        ISkillCapability cap = CasterData.getCap(p);
        ISkillCapability ocap = CasterData.getCap(orig);
        cap.read(ocap.write());
        cap.setStyle(ocap.getStyle());
        cap.setEquippedSkills(ocap.getEquippedSkills());
        cap.getEquippedSkillsAndStyle().stream().filter(Objects::nonNull).forEach(a -> cap.replaceSkill(a, a));
        e.getOriginal().getCapability(QuiverData.QUIVER_CAP).ifPresent(oldCap -> e.getEntity().getCapability(QuiverData.QUIVER_CAP).ifPresent(newCap -> {
            // Copy data
            newCap.deserializeNBT(oldCap.serializeNBT());
        }));
        //yare yare daze
        orig.invalidateCaps();
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if(!e.player.isAlive() || e.player.isSpectator())return;
        if (e.phase == TickEvent.Phase.START) {
            if (e.side != LogicalSide.SERVER) {
                CombatData.getCap(e.player).clientTick();
                AerialModeData.getCap(e.player).tick();
                //FlyingWeaponData.getCap(e.player).tick();
                return;
            } else CombatData.getCap(e.player).serverTick();
            CasterData.getCap(e.player).update();
            StylishData.getCap(e.player).tick();
            FlyingWeaponData.getCap(e.player).tick();
            AerialModeData.getCap(e.player).tick();
            if (WarCompat.elenaiDodge) {
                ElenaiCompat.syncIFrames(e.player);
            }
        } else if (TimeSlowData.getCap(e.player).getEffectiveSpeed() < 1) {
            e.player.resetFallDistance();
        }
    }

    @SubscribeEvent
    public static void tickMobs(LivingEvent.LivingTickEvent e) {
        LivingEntity elb = e.getEntity();
        if (!elb.level().isClientSide) {
            Marks.getCap(elb).update();
            if (!(elb instanceof Player)) {
                //staggered mobs bypass update interval
                ICombatCapability cap = CombatData.getCap(elb);
                if (cap.isStunned() || mustUpdate.containsValue(e.getEntity()))
                    cap.serverTick();
                MobEffectInstance nau = elb.getEffect(MobEffects.CONFUSION);
                float nausea = nau==null ? 0 : (nau.getAmplifier() + 1) * GeneralConfig.nausea;
                if (nausea > 0) cap.consumePosture(null, nausea, 0, false);
            }
        }
    }
}
