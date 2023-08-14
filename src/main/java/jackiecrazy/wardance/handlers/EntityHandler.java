package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.capability.resources.CombatDataOverride;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.skill.SkillCapability;
import jackiecrazy.wardance.capability.status.Mark;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.WeaponStats;
import jackiecrazy.wardance.entity.ai.ExposeGoal;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.SyncSkillPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
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
            e.addCapability(new ResourceLocation("wardance:statuseffects"), new Marks(new Mark(lb)));
            if (lb instanceof Player) {
                e.addCapability(new ResourceLocation("wardance:casterinfo"), new CasterData(new SkillCapability(lb)));
                e.addCapability(new ResourceLocation("wardance:permissions"), new PermissionData());
            }
        }
    }

    @SubscribeEvent
    public static void death(LivingEvent.LivingJumpEvent e) {
        if (!(e.getEntity() instanceof Player) && CombatData.getCap(e.getEntity()).getExposeTime() > 0) {
            e.getEntity().setDeltaMovement(0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void reload(OnDatapackSyncEvent e){
        for(ServerPlayer p: e.getPlayerList().getPlayers()){
            WeaponStats.sendItemData(p);
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
        cap.setStyle(ocap.getStyle());
        cap.setEquippedSkills(ocap.getEquippedSkills());
        cap.getEquippedSkillsAndStyle().stream().filter(Objects::nonNull).forEach(a->cap.replaceSkill(a, a));
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
        }
    }

    @SubscribeEvent
    public static void tickMobs(LivingEvent.LivingTickEvent e) {
        LivingEntity elb = e.getEntity();
        if (!elb.level.isClientSide) {
            Marks.getCap(elb).update();
            if (!(elb instanceof Player)) {
                        /*
                        The battle circle AI basically works like this (from an enemy's perspective):
First, walk towards the player until I get within a "danger" radius
While in "danger" mode, don't get too close to another enemy, unless I am given permission to attack the player.
Also while in "danger" mode, try to approach the player. If there are too many enemies in my way, I will effectively not be able to reach the player until the enemies move or the player moves.
When the player is in my "attack" radius (roughly the maximum range of my attack) ask the player if I'm allowed to attack. If so, add me to the list of current attackers on the player object.
If there are already the maximum allowed number of attackers on the list, I'm denied permission.
If I'm denied permission, try strafing for a second or two in a random direction until I'm given permission.
If the player moves out of attack range—even if I'm attacking—remove me from the attacker list.
If I die, or am stunned or otherwise unable to attack, remove me from the attacker list.
The maximum allowed number of simultaneous attackers is critical in balancing your battle circle. A higher number causes an exponential increase in pressure. In the example demo I have it set at 2; less twitchy and more "cinematic" games set it at 1. If you put this number too high, you defeat the purpose of the circle, because large groups of enemies become unassailable or can only be defeated with uninteresting poke-and-run tactics.
Of similar importance is the enemy attack rate. This is not the fastest possible attack rate of the enemy, but how often they will choose to attack when given permission.
As you would expect, a lower number increases pressure, but you should generally have this be several times higher than the real attack rate. You can make this rate a bit more unpredictable (and thus the amount of pressure slightly less predictable) by increasing attackRateFluctuation, which will increase or decrease the attack rate after each attack.
Mobs should move into a position that is close to the player, far from allies, and close to them.
                         */
                //staggered mobs bypass update interval
                ICombatCapability cap = CombatData.getCap(elb);
                if (cap.isVulnerable() || mustUpdate.containsValue(e.getEntity()))
                    cap.serverTick();
            }
        }
    }
}
