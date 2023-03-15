package jackiecrazy.wardance.skill.judgment;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;

import java.awt.*;
import java.util.List;

import jackiecrazy.wardance.skill.Skill.STATE;

public class LichtenbergScar extends Judgment {
    @Override
    public Color getColor() {
        return Color.CYAN;
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 1;
    }

    @Override
    protected void performEffect(LivingEntity caster, LivingEntity target, int stack, SkillData sd) {
        DamageSource cds = new CombatDamageSource("lightningBolt", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setSkillUsed(this).setProcSkillEffects(true).setProxy(target).bypassArmor();
        if (stack != 3) {
            target.hurt(cds, 0);
            return;
        }
        final float radius = 30;
        final List<LivingEntity> list = caster.level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius), (a) -> Marks.getCap(a).isMarked(this));
        list.add(target);
        //float damage = s.getArbitraryFloat() * (1 + CombatData.getCap(caster).getSpirit());
        for (LivingEntity baddie : list) {
            if (baddie == target)
                target.hurt(cds, target.getHealth() / 5);
            else baddie.hurt(cds, baddie.getHealth() / 10);
            LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(target.level);
            lightningboltentity.moveTo(baddie.getX(), baddie.getY(), baddie.getZ());
            lightningboltentity.setVisualOnly(true);
            target.level.addFreshEntity(lightningboltentity);
            if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(baddie, lightningboltentity))
                baddie.thunderHit((ServerLevel) baddie.level, lightningboltentity);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING && prev.isCondition()) {
            prev.setDuration(1);
            return true;
        } else return super.onStateChange(caster, prev, from, to);
    }
}
