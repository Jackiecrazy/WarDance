package jackiecrazy.wardance.utils;

import jackiecrazy.wardance.skill.Skill;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.particles.IParticleData;
import net.minecraft.world.World;

import java.util.UUID;

public class SkillUtils {

    public static final IDataSerializer<Skill> SKILLSERIALIZER = new IDataSerializer<Skill>() {
        public void write(PacketBuffer buf, Skill value) {
            buf.writeResourceLocation(value.getRegistryName());
        }

        public Skill read(PacketBuffer buf) {
            return Skill.getSkill(buf.readResourceLocation());
        }

        public Skill copyValue(Skill value) {
            return value;
        }
    };

    public static void modifyAttribute(LivingEntity caster, Attribute a, UUID id, float amount, AttributeModifier.Operation op) {
        final ModifiableAttributeInstance atr = caster.getAttribute(a);
        if (atr == null) return;
        if (atr.getModifier(id) != null) {
            if (atr.getModifier(id).getAmount() == amount && atr.getModifier(id).getOperation() == op)
                return;//I think this is a bit more efficient.
        }
        atr.removeModifier(id);
        if (amount != 0) {
            AttributeModifier am = new AttributeModifier(id, "skill modifier", amount, op);
            atr.applyNonPersistentModifier(am);
        }
    }

    public static void createCloud(World world, Entity entityIn, double x, double y, double z, float size, IParticleData type) {
        AreaEffectCloudEntity areaeffectcloudentity = new AreaEffectCloudEntity(world, x, y, z);
        if (entityIn instanceof LivingEntity)
            areaeffectcloudentity.setOwner((LivingEntity) entityIn);
        areaeffectcloudentity.setParticleData(type);
        areaeffectcloudentity.setRadius(size);
        areaeffectcloudentity.setDuration(0);
        world.addEntity(areaeffectcloudentity);
    }

}
