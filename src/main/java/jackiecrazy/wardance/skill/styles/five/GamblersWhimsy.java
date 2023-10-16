package jackiecrazy.wardance.skill.styles.five;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GamblersWhimsy extends SkillStyle {

    public GamblersWhimsy() {
        super(5);
    }

    @Override
    public boolean canCast(LivingEntity caster, Skill s) {
        if (!s.isPassive(caster)) {
            //find the skill
            int index = CasterData.getCap(caster).getEquippedSkills().indexOf(s);
            int validity = 1 << index;
            int allowed = 0b11111;
            Optional<SkillData> sd = CasterData.getCap(caster).getSkillData(this);
            if (sd.isPresent())
                allowed = (int) sd.get().getDuration();
            return (allowed & validity) != 0;
        }
        return true;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof SkillCastEvent sre && sre.getPhase() == EventPriority.LOWEST) {

            //slots that can be cast
            int allowed = 0b00000;
            int indices = (int) (CombatData.getCap(caster).getComboRank() * SkillUtils.getSkillEffectiveness(caster) / 2d + 1.5);
            while (indices > 0) {
                int index = WarDance.rand.nextInt(indices);
                allowed = unban(allowed, index);
                indices--;
            }
            stats.setDuration(allowed);
            sre.setEffectiveness(WarDance.rand.nextFloat() * (caster.getAttributeValue(Attributes.LUCK) + sre.getEffectiveness() - (sre.getTarget() == null ? 0 : sre.getTarget().getAttributeValue(Attributes.LUCK))));
        }
    }

    private int unban(int orig, int index) {
        int op = 1;
        //special casing: index is 0 and 0 is banned
        if ((orig & 1) == 0 && index == 0)
            return orig | 1;
        //otherwise look for the next banned index
        while (index > 0) {
            //index goes up
            op <<= 1;
            //if index is not unbanned, this be a valid index
            if ((orig & op) == 0) {
                index--;
            }
        }
        return orig | op;
    }

}
