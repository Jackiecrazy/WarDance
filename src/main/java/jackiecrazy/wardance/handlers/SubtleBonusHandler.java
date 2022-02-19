package jackiecrazy.wardance.handlers;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class SubtleBonusHandler {
    private static UUID u = UUID.fromString("1896391d-0d6c-4a3e-a4a5-5e3c9d173b80");

    @SubscribeEvent
    public static void thief(LootingLevelEvent e) {
        if (e.getDamageSource().getTrueSource() instanceof LivingEntity)
            e.setLootingLevel(e.getLootingLevel() + Math.max(0, (CombatData.getCap((LivingEntity) e.getDamageSource().getTrueSource()).getComboRank() - 3) / 2));
    }

    @SubscribeEvent
    public static void tank(LivingHealEvent e) {
        e.setAmount(e.getAmount() * (1 + (CombatData.getCap(e.getEntityLiving()).getComboRank() * 0.02f)));
    }

    @SubscribeEvent
    public static void tank(LivingEvent.LivingUpdateEvent e) {
        if (e.getEntityLiving().ticksExisted % 60 == 0) {
            SkillUtils.modifyAttribute(e.getEntityLiving(), Attributes.MOVEMENT_SPEED, u, 0.02 * CombatData.getCap(e.getEntityLiving()).getComboRank(), AttributeModifier.Operation.ADDITION);
            SkillUtils.modifyAttribute(e.getEntityLiving(), Attributes.ATTACK_SPEED, u, 0.02 * CombatData.getCap(e.getEntityLiving()).getComboRank(), AttributeModifier.Operation.ADDITION);
        }
    }


}
