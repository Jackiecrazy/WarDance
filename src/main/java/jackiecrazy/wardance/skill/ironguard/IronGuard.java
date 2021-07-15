package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.skill.SkillTags;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;

public class IronGuard extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "quickCast", "onParry", "countdown", SkillTags.recharge_parry)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("onParry")));

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
        return this.getClass() == IronGuard.class ? null : WarSkills.IRON_GUARD.get();
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 30);
        return true;
    }

    @Override
    public void onCooledDown(LivingEntity caster, float overflow) {
        if (CasterData.getCap(caster).isSkillUsable(WarSkills.MIKIRI.get()))
            WarSkills.MIKIRI.get().onCast(caster);
        super.onCooledDown(caster, overflow);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 5);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, @Nullable LivingEntity target, Event procPoint) {
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).getPostureConsumption() > 0) {
            if (!CasterData.getCap(((ParryEvent) procPoint).getAttacker()).isSkillActive(WarSkills.HEAVY_BLOW.get()))
                CombatData.getCap(((ParryEvent) procPoint).getAttacker()).consumePosture(caster, ((ParryEvent) procPoint).getPostureConsumption());
            ((ParryEvent) procPoint).setPostureConsumption(0);
            markUsed(caster);
        }
        if (procPoint instanceof ProjectileParryEvent) {
            ((ProjectileParryEvent) procPoint).setReturnVec(((ProjectileParryEvent) procPoint).getProjectile().getMotion().inverse());
            ((ProjectileParryEvent) procPoint).setPostureConsumption(0);
            markUsed(caster);
        }
    }
}
