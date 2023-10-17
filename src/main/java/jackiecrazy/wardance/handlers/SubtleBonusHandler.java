package jackiecrazy.wardance.handlers;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.config.CombatConfig;
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
    private static final UUID u = UUID.fromString("1896391d-0d6c-4a3e-a4a5-5e3c9d173b80");
    public static boolean update = false;

    @SubscribeEvent
    public static void thief(LootingLevelEvent e) {
        if (e.getDamageSource() == null) return;
        if (CombatConfig.adrenaline < 0) return;
        if (e.getDamageSource().getEntity() instanceof LivingEntity elb)
            e.setLootingLevel(e.getLootingLevel() + Math.max(0, (CombatData.getCap(elb).getComboRank() - 3) * (CombatData.getCap(elb).halvedAdrenaline() ? 0 : 1)));
    }

    @SubscribeEvent
    public static void tank(LivingHealEvent e) {
        if (CombatConfig.adrenaline < 0) return;
        e.setAmount(e.getAmount() * (1 + (CombatData.getCap(e.getEntity()).getComboRank() * 0.03f * (CombatData.getCap(e.getEntity()).halvedAdrenaline() ? 0f : 1))));
    }

    @SubscribeEvent
    public static void tank(TickEvent.PlayerTickEvent e) {
        if (CombatConfig.adrenaline < 0) return;
        if (e.player.tickCount % 60 == 0 || update) {
            final boolean adrenaline = CombatData.getCap(e.player).halvedAdrenaline();
            SkillUtils.modifyAttribute(e.player, Attributes.MOVEMENT_SPEED, u, 0.03 * CombatData.getCap(e.player).getComboRank() * (adrenaline ? 0 : 1), AttributeModifier.Operation.MULTIPLY_BASE);
            SkillUtils.modifyAttribute(e.player, Attributes.ATTACK_SPEED, u, 0.03 * CombatData.getCap(e.player).getComboRank() * (adrenaline ? 0 : 1), AttributeModifier.Operation.MULTIPLY_TOTAL);
            update = false;
        }
    }


}
