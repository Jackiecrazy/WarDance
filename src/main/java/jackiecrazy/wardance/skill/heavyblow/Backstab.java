package jackiecrazy.wardance.skill.heavyblow;

import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.Arrays;
import java.util.HashSet;

public class Backstab extends HeavyBlow {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "boundCast", "normalAttack", "onHurt", "modifyCrit", "rechargeWithAttack", "onBeingParried")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        super.onSuccessfulProc(caster, stats, target, procPoint);
        if (procPoint instanceof LivingAttackEvent && GeneralUtils.isBehindEntity(caster, target, 90)) {
            ((LivingAttackEvent) procPoint).getSource().setDamageBypassesArmor();
        }
    }
}
