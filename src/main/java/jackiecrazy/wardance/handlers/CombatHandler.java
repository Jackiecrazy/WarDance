package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class CombatHandler {


    @SubscribeEvent
    public static void projectileParry(final ProjectileImpactEvent e){

    }

    @SubscribeEvent
    public static void parry(final LivingAttackEvent e){

    }

    @SubscribeEvent
    public static void offhandAttack(final PlayerInteractEvent.EntityInteract e){

    }
}
