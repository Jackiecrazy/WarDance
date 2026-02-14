package jackiecrazy.wardance.mixin;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.client.ClientEvents;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.combat.RequestSweepPacket;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class AttackCanceler {
    @Inject(method = "attack", at = @At(value = "HEAD"), cancellable = true)
    private void combatModeOverride(Player p, Entity e, CallbackInfo ci) {
        if(StylishData.getCap(p).isCombatMode()){//schedule guard counter here
            //cancel direct attack for the guard counter
            if(ClientEvents.heavy(WeaponStats.AttackType.GUARD_COUNTER)){
                ci.cancel();
                return;
            }
            //cancel direct attack for anything that is not a normal sweep
            final WeaponStats.AttackType state = CombatUtils.getAttackState(p);
            if(state!= WeaponStats.AttackType.UNDEFINED){
                if(WeaponStats.getSweepInfo(p.getMainHandItem(), p, state).getInteractionType()!= WeaponInteractions.WeaponInteraction.TYPE.SWEEP){
                    ci.cancel();
                    CombatChannel.INSTANCE.sendToServer(new RequestSweepPacket(true, null));
                    //CombatUtils.processWeaponInteraction(p, null, InteractionHand.MAIN_HAND, p.getAttributeValue(ForgeMod.ENTITY_REACH.get()));
                    return;
                    //the amount of shit I put up with to keep compat...
                }
            }

        }
    }
}
