package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.config.CombatConfig;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.skill.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;

public class IronGuard extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "quickCast", ProcPoints.on_parry, ProcPoints.on_projectile_parry, "countdown", ProcPoints.recharge_parry)));

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return defensivePhysical;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return defensive;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.iron_guard;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 30);
        CombatData.getCap(caster).consumeSpirit(spiritConsumption(caster));
        return true;
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        if (CombatConfig.sneakParry == 0) {
            return super.activeTick(caster, d);
        }
        return false;
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
        if (procPoint instanceof ParryEvent && ((ParryEvent) procPoint).canParry() && ((ParryEvent) procPoint).getPostureConsumption() > 0) {
            if (!CasterData.getCap(((ParryEvent) procPoint).getAttacker()).isCategoryActive(SkillCategories.heavy_blow))
                CombatData.getCap(((ParryEvent) procPoint).getAttacker()).consumePosture(caster, ((ParryEvent) procPoint).getPostureConsumption());
            ((ParryEvent) procPoint).setPostureConsumption(0);
            markUsed(caster);
        }
        if (procPoint instanceof ProjectileParryEvent && getParentCategory() == null) {
            ((ProjectileParryEvent) procPoint).setReturnVec(((ProjectileParryEvent) procPoint).getProjectile().getMotion().inverse());
            ((ProjectileParryEvent) procPoint).setPostureConsumption(0);
            markUsed(caster);
        }
    }
}
