package jackiecrazy.wardance.move.actions;

import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.argument.stack.EquippedItemArgument;
import jackiecrazy.footwork.move.motionframe.MotionFrame;
import jackiecrazy.footwork.move.motionframe.MotionManager;
import jackiecrazy.footwork.move.motionframe.MotionManagers;
import jackiecrazy.footwork.move.utils.ActionContext;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.entity.GhostBlockEntity;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.util.List;

public class PlayInteractionAction extends Action {
    private WeaponStats.AttackType move_state;
    private Argument<ItemStack> stack=new EquippedItemArgument();
    private WeaponInteractions.InteractionGroup interaction;

    @Override
    public int perform(ActionContext actionContext) {
        if (actionContext.performer() instanceof LivingEntity performer) {
            //pickup flourish
            ItemStack prevHeld = performer.getMainHandItem();
            int ticks = performer.attackStrengthTicker;
            try {
                CombatUtils.quickSwap(performer, stack.resolve(actionContext));
                CombatUtils.setHandCooldown(performer, InteractionHand.MAIN_HAND, 2, false);
                if (move_state != null) {
                    CombatUtils.setAttackType(performer, move_state);
                }
                FlyingWeaponData.getCap(performer).getWeapon(InteractionHand.MAIN_HAND).clearPath();
                FlyingWeaponData.getCap(performer).forceRefreshWeapons();
                if (interaction != null)
                    CombatUtils.processWeaponInteraction(performer, null, InteractionHand.MAIN_HAND, performer.getAttributeValue(ForgeMod.ENTITY_REACH.get()), interaction);
                else
                    CombatUtils.processWeaponInteraction(performer, null, InteractionHand.MAIN_HAND, performer.getAttributeValue(ForgeMod.ENTITY_REACH.get()));
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                CombatUtils.quickSwap(performer, prevHeld);
                performer.attackStrengthTicker = ticks;
            }
        }
        return 0;
    }
}
