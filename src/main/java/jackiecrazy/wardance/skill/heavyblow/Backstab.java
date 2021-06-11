package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Backstab extends HeavyBlow {

    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }

    @SubscribeEvent
    public static void spooketh(CriticalHitEvent e) {
        final Skill back = WarSkills.BACKSTAB.get();
        if (CasterData.getCap(e.getPlayer()).getEquippedSkills().contains(back) && !CasterData.getCap(e.getPlayer()).isSkillCoolingDown(back) && e.getTarget() instanceof LivingEntity && CombatUtils.getAwareness(e.getPlayer(), (LivingEntity) e.getTarget()) == CombatUtils.AWARENESS.UNAWARE) {
            back.onSuccessfulProc(e.getPlayer(), null, (LivingEntity) e.getTarget(), e);
            CasterData.getCap(e.getPlayer()).setSkillCooldown(back, 3);
        }
    }
}
