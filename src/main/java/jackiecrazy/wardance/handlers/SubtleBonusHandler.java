package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class SubtleBonusHandler {
    public static boolean update = false;
    private static UUID u = UUID.fromString("1896391d-0d6c-4a3e-a4a5-5e3c9d173b80");

    @SubscribeEvent
    public static void thief(LootingLevelEvent e) {
        if (e.getDamageSource().getEntity() instanceof LivingEntity)
            e.setLootingLevel(e.getLootingLevel() + Math.max(0, (CombatData.getCap((LivingEntity) e.getDamageSource().getEntity()).getComboRank() - 3) / (CombatData.getCap((LivingEntity) e.getDamageSource().getEntity()).halvedAdrenaline()?2:1)));
    }

    @SubscribeEvent
    public static void tank(LivingHealEvent e) {
        e.setAmount(e.getAmount() * (1 + (CombatData.getCap(e.getEntity()).getComboRank() * 0.02f * (CombatData.getCap(e.getEntity()).halvedAdrenaline() ? 0.5f : 1))));
    }

    @SubscribeEvent
    public static void tank(TickEvent.PlayerTickEvent e) {
        if (e.player.tickCount % 60 == 0 || update) {
            SkillUtils.modifyAttribute(e.player, Attributes.MOVEMENT_SPEED, u, 0.02 * CombatData.getCap(e.player).getComboRank() * (CombatData.getCap(e.player).halvedAdrenaline() ? 0.5 : 1), AttributeModifier.Operation.MULTIPLY_BASE);
            SkillUtils.modifyAttribute(e.player, Attributes.ATTACK_SPEED, u, 0.02 * CombatData.getCap(e.player).getComboRank() * (CombatData.getCap(e.player).halvedAdrenaline() ? 0.5 : 1), AttributeModifier.Operation.MULTIPLY_TOTAL);
            update = false;
        }
    }


}
