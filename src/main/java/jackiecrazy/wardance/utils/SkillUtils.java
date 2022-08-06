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
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class SkillUtils {

    public static final IDataSerializer<Skill> SKILLSERIALIZER = new IDataSerializer<Skill>() {
        public void write(PacketBuffer buf, Skill value) {
            buf.writeResourceLocation(value.getRegistryName());
        }

        public Skill read(PacketBuffer buf) {
            return Skill.getSkill(buf.readResourceLocation());
        }

        public Skill copy(Skill value) {
            return value;
        }
    };

    public static void modifyAttribute(LivingEntity caster, Attribute a, UUID id, double amount, AttributeModifier.Operation op) {
        final ModifiableAttributeInstance atr = caster.getAttribute(a);
        if (atr == null) return;
        if (atr.getModifier(id) != null) {
            if (atr.getModifier(id).getAmount() == amount && atr.getModifier(id).getOperation() == op)
                return;//I think this is a bit more efficient.
        }
        atr.removeModifier(id);
        if (amount != 0) {
            AttributeModifier am = new AttributeModifier(id, "skill modifier", amount, op);
            atr.addTransientModifier(am);
        }
    }

    public static void createCloud(World world, Entity entityIn, double x, double y, double z, float size, IParticleData type) {
        AreaEffectCloudEntity areaeffectcloudentity = new AreaEffectCloudEntity(world, x, y, z);
        if (entityIn instanceof LivingEntity)
            areaeffectcloudentity.setOwner((LivingEntity) entityIn);
        areaeffectcloudentity.setParticle(type);
        areaeffectcloudentity.setRadius(size);
        areaeffectcloudentity.setDuration(0);
        world.addFreshEntity(areaeffectcloudentity);
    }

    public static Entity aimEntity(LivingEntity caster) {
        return GeneralUtils.raytraceEntity(caster.level, caster, caster.getAttributeValue(ForgeMod.REACH_DISTANCE.get()));
    }

    public static LivingEntity aimLiving(LivingEntity caster) {
        return GeneralUtils.raytraceLiving(caster.level, caster, caster.getAttributeValue(ForgeMod.REACH_DISTANCE.get()));
    }

    public static boolean auxAttack(LivingEntity caster, LivingEntity target, DamageSource s, float dmg, float posdmg, Runnable onHit, Runnable onDamage) {
        CombatData.getCap(target).consumePosture(posdmg);
        onHit.run();
        if (dmg > 0) {
            if (target.hurt(s, dmg))
                onDamage.run();
            return true;
        }
        return false;
    }

    public static boolean auxAttack(LivingEntity caster, LivingEntity target, DamageSource s, float dmg, float posdmg, Runnable run, boolean onHit) {
        return auxAttack(caster, target, s, dmg, posdmg, onHit ? run : () -> {}, onHit ? () -> {} : run);
    }

    public static boolean auxAttack(LivingEntity caster, LivingEntity target, DamageSource s, float dmg, float posdmg) {
        return auxAttack(caster, target, s, dmg, posdmg, () -> {}, () -> {});
    }

}
