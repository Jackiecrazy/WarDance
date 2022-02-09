package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.TargetingUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.ForgeMod;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class LowSweep extends Kick {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", "melee", "sweep", "boundCast", "normalAttack", "countdown", "rechargeWithAttack")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("normalAttack", "sweep")));

    protected void additionally(LivingEntity caster, LivingEntity target) {

    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE) {
            if (CombatData.getCap(caster).consumeSpirit(spiritConsumption(caster))) {
                for (Entity t : caster.world.getEntitiesInAABBexcluding(caster, caster.getBoundingBox().grow(caster.getAttributeValue(ForgeMod.REACH_DISTANCE.get())), (a -> !TargetingUtils.isAlly(a, caster))))
                    if (t instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) t;
                        CombatData.getCap(target).consumePosture(caster, 4);
                        if (caster instanceof PlayerEntity)
                            ((PlayerEntity) caster).spawnSweepParticles();
                        target.attackEntityFrom(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), 2);
                        if (target.getRevengeTarget() == null)
                            target.setRevengeTarget(caster);
                        caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                        additionally(caster, target);
                    }
                markUsed(caster);
            }
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, 4);
            return true;
        }
        return boundCast(prev, from, to);
    }

    @Override
    public Color getColor() {
        return Color.CYAN;
    }
}
