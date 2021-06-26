package jackiecrazy.wardance.utils;

import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.particles.IParticleData;
import net.minecraft.world.World;

import java.util.UUID;

public class SkillUtils {
    public static void modifyAttribute(LivingEntity caster, Attribute a, UUID id, float amount, AttributeModifier.Operation op, SkillData d){
        if (d.getArbitraryFloat() == 0 || d.getArbitraryFloat() != amount) {
            caster.getAttribute(a).removeModifier(id);
            if (amount > 0) {
                AttributeModifier am=new AttributeModifier(id, "skill modifier", amount, op);
                caster.getAttribute(a).applyNonPersistentModifier(am);
            }
            d.setArbitraryFloat(amount);
        }
    }

    public static void createCloud(World world, Entity entityIn, double x, double y, double z, float size, IParticleData type){
        AreaEffectCloudEntity areaeffectcloudentity = new AreaEffectCloudEntity(world, x, y, z);
        if (entityIn instanceof LivingEntity)
            areaeffectcloudentity.setOwner((LivingEntity) entityIn);
        areaeffectcloudentity.setParticleData(type);
        areaeffectcloudentity.setRadius(size);
        areaeffectcloudentity.setDuration(0);
        world.addEntity(areaeffectcloudentity);
    }

}
