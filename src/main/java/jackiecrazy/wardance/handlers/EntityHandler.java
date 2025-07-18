package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.capability.resources.CombatDataOverride;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.skill.SkillCapability;
import jackiecrazy.wardance.capability.status.Mark;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.capability.stylish.StyleDataOverride;
import jackiecrazy.wardance.compat.ElenaiCompat;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.config.TwohandingStats;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.entity.ai.ExposeGoal;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.sync.SyncSkillPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class EntityHandler {
    public static final HashMap<Player, Entity> mustUpdate = new HashMap<>();
    public static final ConcurrentHashMap<Tuple<Level, BlockPos>, Float> alertTracker = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void start(ServerStartingEvent e) {
        mustUpdate.clear();
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void stop(ServerStoppingEvent e) {
        mustUpdate.clear();
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void caps(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof LivingEntity lb) {
            e.addCapability(new ResourceLocation("wardance:combatinfo"), new CombatDataOverride(lb));
            e.addCapability(new ResourceLocation("wardance:stylishdata"), new StyleDataOverride(lb));
            e.addCapability(new ResourceLocation("wardance:statuseffects"), new Marks(new Mark(lb)));
            if (lb instanceof Player) {
                e.addCapability(new ResourceLocation("wardance:casterinfo"), new CasterData(new SkillCapability(lb)));
                e.addCapability(new ResourceLocation("wardance:permissions"), new PermissionData());
            }
        }
    }

    @SubscribeEvent
    public static void death(LivingEvent.LivingJumpEvent e) {
        if (CombatData.getCap(e.getEntity()).isStunned()) {
            if (!(e.getEntity() instanceof Player))
                e.getEntity().setDeltaMovement(0, 0, 0);
            else{
                //TODO circle sweep up
            }
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            WeaponStats.sendItemData(sp);
            TwohandingStats.sendItemData(sp);
        }
    }

    @SubscribeEvent
    public static void reload(OnDatapackSyncEvent e) {
        //fixme still doesn't sync to server
        for (ServerPlayer sp : e.getPlayerList().getPlayers()) {
            WeaponStats.sendItemData(sp);
            TwohandingStats.sendItemData(sp);
        }
    }

    @SubscribeEvent
    public static void takeThis(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof ServerPlayer) {
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) e.getEntity()), new SyncSkillPacket(CasterData.getCap((LivingEntity) e.getEntity()).write()));
        }
        if (e.getEntity() instanceof Mob mob) {
            mob.goalSelector.addGoal(-1, new ExposeGoal(mob));
            mob.targetSelector.addGoal(-1, new ExposeGoal(mob));
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
        //yare yare daze
        orig.invalidateCaps();
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.player.isAlive() && e.phase == TickEvent.Phase.START) {
            if (e.side != LogicalSide.SERVER) {
                CombatData.getCap(e.player).clientTick();
                return;
            } else CombatData.getCap(e.player).serverTick();
            CasterData.getCap(e.player).update();
            StylishData.getCap(e.player).tick();
            if (WarCompat.elenaiDodge) {
                ElenaiCompat.syncIFrames(e.player);
            }
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
                float nausea = elb instanceof Player || !elb.hasEffect(MobEffects.CONFUSION) ? 0 : (elb.getEffect(MobEffects.CONFUSION).getAmplifier() + 1) * GeneralConfig.nausea;
                if (nausea > 0) cap.consumePosture(null, nausea, false);
            }
        }
    }
}
