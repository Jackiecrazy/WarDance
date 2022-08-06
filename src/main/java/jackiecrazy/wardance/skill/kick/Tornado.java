package jackiecrazy.wardance.skill.kick;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
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

public class Tornado extends Kick {
    private final Tag<String> tag = Tag.create(new HashSet<>(Arrays.asList("physical", "melee", "sweep", "boundCast", "normalAttack", "countdown", "rechargeWithAttack")));
    private final Tag<String> no = Tag.create(new HashSet<>(Arrays.asList("normalAttack", "sweep")));

    protected void additionally(LivingEntity caster, LivingEntity target) {

    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE&& cast(caster, -999)) {
                for (Entity t : caster.level.getEntities(caster, caster.getBoundingBox().inflate(caster.getAttributeValue(ForgeMod.REACH_DISTANCE.get())), (a -> !TargetingUtils.isAlly(a, caster))))
                    if (t instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) t;
                        CombatData.getCap(target).consumePosture(caster, 4);
                        if (caster instanceof PlayerEntity)
                            ((PlayerEntity) caster).sweepAttack();
                        target.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), 2);
                        if (target.getLastHurtByMob() == null)
                            target.setLastHurtByMob(caster);
                        caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                        additionally(caster, target);
                    }
        }
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, 4);
            return true;
        }
        return boundCast(prev, from, to);
    }

    @Override
    public Color getColor() {
        return Color.CYAN;
    }
}
