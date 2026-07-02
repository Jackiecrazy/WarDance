package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.CombatConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class SubtleBonusHandler {

    @SubscribeEvent
    public static void thief(LootingLevelEvent e) {
        if (e.getDamageSource() == null) return;
        if (CombatConfig.adrenaline < 0) return;
        if (e.getDamageSource().getEntity() instanceof Player elb)
            e.setLootingLevel(e.getLootingLevel() + (int) Math.max(0, (StylishData.getCap(elb).getCombo())));
    }
    @SubscribeEvent
    public static void exp(LivingExperienceDropEvent e) {
        if (e.getAttackingPlayer() == null) return;
        if (CombatConfig.adrenaline < 0) return;
        e.setDroppedExperience((int) (e.getDroppedExperience() * Math.max(1, (StylishData.getCap(e.getAttackingPlayer()).getCombo()))));
    }

//    @SubscribeEvent
//    public static void tank(LivingHealEvent e) {
//        if (CombatConfig.adrenaline < 0) return;
//        e.setAmount(e.getAmount() * (1 + (CombatData.getCap(e.getEntity()).getComboRank() * 0.03f * (CombatData.getCap(e.getEntity()).halvedAdrenaline() ? 0f : 1))));
//    }

//    @SubscribeEvent
//    public static void tank(TickEvent.PlayerTickEvent e) {
//        if (CombatConfig.adrenaline < 0) return;
//        if (e.player.tickCount % 60 == 0 || update) {
//            final boolean adrenaline = CombatData.getCap(e.player).halvedAdrenaline();
//            SkillUtils.modifyAttribute(e.player, Attributes.MOVEMENT_SPEED, u, 0.03 * CombatData.getCap(e.player).getComboRank() * (adrenaline ? 0 : 1), AttributeModifier.Operation.MULTIPLY_BASE);
//            SkillUtils.modifyAttribute(e.player, Attributes.ATTACK_SPEED, u, 0.03 * CombatData.getCap(e.player).getComboRank() * (adrenaline ? 0 : 1), AttributeModifier.Operation.MULTIPLY_TOTAL);
//            update = false;
//        }
//    }


}
