package jackiecrazy.wardance.skill.kick;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

import java.awt.*;
import java.util.HashSet;

public class Tornado extends Kick {
    private final HashSet<String> tag = makeTag("physical", "melee", "sweep", "boundCast", "normalAttack", "countdown", "rechargeWithAttack");
    private final HashSet<String> no = makeTag("normalAttack", "sweep");

    protected void additionally(LivingEntity caster, LivingEntity target) {

    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE&& cast(caster, -999)) {
                for (Entity t : caster.level.getEntities(caster, caster.getBoundingBox().inflate(caster.getAttributeValue(ForgeMod.ATTACK_RANGE.get())), (a -> !TargetingUtils.isAlly(a, caster))))
                    if (t instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) t;
                        CombatData.getCap(target).consumePosture(caster, 4);
                        if (caster instanceof Player)
                            ((Player) caster).sweepAttack();
                        target.hurt(new CombatDamageSource("fallingBlock", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true), 2);
                        if (target.getLastHurtByMob() == null)
                            target.setLastHurtByMob(caster);
                        caster.level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
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
