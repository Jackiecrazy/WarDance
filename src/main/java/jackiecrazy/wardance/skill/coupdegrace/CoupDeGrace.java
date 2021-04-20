package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.handlers.CombatHandler;
import jackiecrazy.wardance.networking.CombatModePacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.skill.heavyblow.HeavyBlow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;

public class CoupDeGrace extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "afterArmor", "noRecharge", "execution")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

    private static double getLife(LivingEntity e) {
        if (e instanceof PlayerEntity) return 3;
        return e.getMaxHealth() / Math.max(1, Math.cbrt(e.getMaxHealth()) - 2);
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == CoupDeGrace.class ? null : WarSkills.COUPDEGRACE.get();
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        if (CasterData.getCap(caster).isTagActive("execution"))
            CasterData.getCap(caster).removeActiveTag("execution");
        else activate(caster, 1);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {

    }

    protected void additionally(LivingEntity caster, LivingEntity target) {

    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingDamageEvent) {
            LivingDamageEvent e = (LivingDamageEvent) procPoint;
            if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0 && !CombatHandler.downingHit) {
                e.setAmount(e.getAmount() + (float) CoupDeGrace.getLife(target));
                CombatData.getCap(target).setStaggerCount(0);
                if (e.getAmount() > target.getHealth()) {
                    CombatData.getCap(caster).addMight(1);
                    additionally(caster, target);
                }
            }
        }
    }
}
