package jackiecrazy.wardance.move.actions;

import jackiecrazy.footwork.move.action.Action;
import jackiecrazy.footwork.move.argument.Argument;
import jackiecrazy.footwork.move.argument.stack.EquippedItemArgument;
import jackiecrazy.footwork.move.condition.Condition;
import jackiecrazy.footwork.move.condition.FalseCondition;
import jackiecrazy.footwork.move.utils.ActionContext;
import jackiecrazy.wardance.capability.flyingweapon.FlyingWeaponData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.config.weapon.interactions.WeaponInteractions;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;

public class PlayInteractionAction extends Action {
    private WeaponStats.AttackType move_state;
    private Argument<ItemStack> stack;
    private WeaponInteractions.InteractionGroup interaction;
    private Condition immediate = FalseCondition.INSTANCE;

    @Override
    public int perform(ActionContext actionContext) {
        if (actionContext.performer() instanceof LivingEntity performer) {
            actionContext.wrapper().getData(this);
            ItemStack prevHeld = performer.getMainHandItem();
            int ticks = performer.attackStrengthTicker;
            try {
                ItemStack stack;
                if (actionContext.getContext("itemstack") instanceof ItemStack is)
                    stack = is;
                else stack = this.stack.resolve(actionContext);
                InteractionHand hand = InteractionHand.MAIN_HAND;
                if (actionContext.getContext("hand") instanceof InteractionHand is)
                    hand = is;

                CombatUtils.quickSwap(performer, stack, hand);
                CombatUtils.setHandCooldown(performer, hand, 2, false);
                if (move_state != null) {
                    CombatUtils.setAttackType(performer, move_state);
                }
                if (immediate.resolve(actionContext))
                    FlyingWeaponData.getCap(performer).getWeapon(hand).clearPath();
                FlyingWeaponData.getCap(performer).forceRefreshWeapons();
                if (interaction != null)
                    CombatUtils.processWeaponInteraction(performer, null, hand, performer.getAttributeValue(ForgeMod.ENTITY_REACH.get()), interaction);
                else
                    CombatUtils.processWeaponInteraction(performer, null, hand, performer.getAttributeValue(ForgeMod.ENTITY_REACH.get()));
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
