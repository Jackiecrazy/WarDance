package jackiecrazy.wardance.skill.guillotine;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.status.Marks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;

import java.awt.*;
import java.util.List;

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
    protected void performEffect(LivingEntity caster, LivingEntity target, int stack) {
        DamageSource cds = new CombatDamageSource("lightningBolt", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setSkillUsed(this).setProcSkillEffects(true).setProxy(target).setDamageBypassesArmor().setDamageIsAbsolute();
        if (stack != 3) {
            target.attackEntityFrom(cds, 0);
            return;
        }
        final float radius = 30;
        final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(radius), (a) -> Marks.getCap(a).isMarked(this));
        list.add(target);
        //float damage = s.getArbitraryFloat() * (1 + CombatData.getCap(caster).getSpirit());
        for (LivingEntity baddie : list) {
            if(baddie==target)
                target.attackEntityFrom(cds, target.getHealth() / 5);
            else baddie.attackEntityFrom(cds, baddie.getHealth() / 10);
            LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(target.world);
            lightningboltentity.moveForced(baddie.getPosX(), baddie.getPosY(), baddie.getPosZ());
            lightningboltentity.setEffectOnly(true);
            target.world.addEntity(lightningboltentity);
            if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(baddie, lightningboltentity))
                baddie.causeLightningStrike((ServerWorld) baddie.world, lightningboltentity);
        }
    }
}
