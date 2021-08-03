package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class Kick extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "melee", "noDamage", "boundCast", "normalAttack", "countdown", "rechargeWithAttack")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        activate(caster, 40);
        return true;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass()==Kick.class ? null : WarSkills.KICK.get();
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 5);
    }

    protected void additionally(LivingEntity caster, LivingEntity target) {

    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingAttackEvent) {
            procPoint.setCanceled(true);
            if (GeneralUtils.getDistSqCompensated(caster, target) <= distanceSq()) {
                CombatData.getCap(target).consumePosture(caster, 5);
                if(caster instanceof PlayerEntity)
                    ((PlayerEntity) caster).spawnSweepParticles();
                caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR , SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f+WarDance.rand.nextFloat() * 0.5f);
                target.attackEntityFrom(DamageSource.FALLING_BLOCK, 1);
                additionally(caster, target);
                markUsed(caster);
            }
        }
    }

    protected int distanceSq(){
        return 9;
    }

    public static class Backflip extends Kick {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        protected void additionally(LivingEntity caster, LivingEntity target) {
            final Vector3d vec = caster.getPositionVec().subtractReverse(target.getPositionVec());
            final Vector3d noy = new Vector3d(vec.x, 0, vec.z).normalize().scale(-1);
            caster.setMotion(caster.getMotion().add(noy.x, 0.4, noy.z));
            caster.velocityChanged = true;
            final ICombatCapability cap = CombatData.getCap(caster);
            cap.addCombo(0.4f);
            cap.addPosture(0.3f * (cap.getPosture() / cap.getMaxPosture()));
        }
    }
}
