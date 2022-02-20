package jackiecrazy.wardance.skill.guillotine;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.utils.TargetingUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;

import java.awt.*;
import java.util.List;

public class LichtenbergScar extends Guillotine {
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
        if (stack != 3) return;
        DamageSource cds = new CombatDamageSource("lightningBolt", caster).setDamageTyping(CombatDamageSource.TYPE.MAGICAL).setSkillUsed(this).setProcSkillEffects(true).setProxy(target).setDamageBypassesArmor().setDamageIsAbsolute();
        target.attackEntityFrom(cds, target.getHealth() / 5);
        final float radius = 7;
        final List<LivingEntity> list = caster.world.getLoadedEntitiesWithinAABB(LivingEntity.class, caster.getBoundingBox().grow(radius), (a) -> TargetingUtils.isHostile(a, caster));
        //float damage = s.getArbitraryFloat() * (1 + CombatData.getCap(caster).getSpirit());
        for (LivingEntity baddie : list) {
            baddie.attackEntityFrom(cds, baddie.getHealth() / 10);
            LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(target.world);
            lightningboltentity.moveForced(baddie.getPosX(), baddie.getPosY(), baddie.getPosZ());
            lightningboltentity.setEffectOnly(true);
            target.world.addEntity(lightningboltentity);
            if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(baddie, lightningboltentity))
                baddie.causeLightningStrike((ServerWorld) baddie.world, lightningboltentity);
        }
    }
}
