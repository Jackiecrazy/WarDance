package jackiecrazy.wardance.skill.misc;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.weapon.WeaponStats;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Switchup extends Skill {
    @SubscribeEvent
    public static void changeup(LivingEquipmentChangeEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && e.getSlot().getType() == EquipmentSlot.Type.HAND) {
            CasterData.getCap(p).getSkillData(WarSkills.QUICKSWITCH.get()).ifPresent(a -> quickSwitch(p, e, a));
        }
    }

    private static void quickSwitch(LivingEntity caster, LivingEquipmentChangeEvent event, SkillData stats) {
        //block obvious cheaties
        if (event.getFrom().equals(event.getTo())) return;
        if (!event.getFrom().isEmpty()&&(event.getFrom().equals(caster.getMainHandItem()) || event.getFrom().equals(caster.getOffhandItem()))) return;
        WeaponStats.WeaponInfo previnfo = WeaponStats.lookupStats(event.getFrom());
        WeaponStats.WeaponInfo newinfo = WeaponStats.lookupStats(event.getTo());
        if (previnfo != newinfo)
        if(event.getSlot()==EquipmentSlot.MAINHAND)
            stats.setDuration(1);
        else stats.setArbitraryFloat(1);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if(stats.getDuration()>0) {
            CombatUtils.setHandCooldown(caster, InteractionHand.MAIN_HAND, 1, true);
            stats.setDuration(0);
        }
        if(stats.getArbitraryFloat()>0) {
            CombatUtils.setHandCooldown(caster, InteractionHand.OFF_HAND, 1, true);
            stats.setArbitraryFloat(0);
        }
        return false;
    }

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        prev.setState(STATE.INACTIVE);
        return false;
    }

}
