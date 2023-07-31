package jackiecrazy.wardance.utils;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class SkillUtils {

    public static final EntityDataSerializer<Skill> SKILLSERIALIZER = new EntityDataSerializer<Skill>() {
        public void write(FriendlyByteBuf buf, Skill value) {
            buf.writeResourceLocation(value.getRegistryName());
        }

        public Skill read(FriendlyByteBuf buf) {
            return Skill.getSkill(buf.readResourceLocation());
        }

        public Skill copy(Skill value) {
            return value;
        }
    };

    public static void modifyAttribute(LivingEntity caster, Attribute a, UUID id, double amount, AttributeModifier.Operation op) {
        final AttributeInstance atr = caster.getAttribute(a);
        if (atr == null) return;
        final AttributeModifier modifier = atr.getModifier(id);
        if (modifier != null) {
            if (modifier.getAmount() == amount && modifier.getOperation() == op)
                return;//saves operations and prevents race conditions.
        }
        atr.removeModifier(id);
        if (amount != 0) {
            AttributeModifier am = new AttributeModifier(id, "skill modifier", amount, op);
            atr.addPermanentModifier(am);
        }
    }

    public static void addAttribute(LivingEntity to, Attribute a, AttributeModifier am) {
        final AttributeInstance atr = to.getAttribute(a);
        if (atr == null) return;
        atr.removeModifier(am.getId());
        atr.addPermanentModifier(am);
    }

    public static void removeAttribute(LivingEntity to, Attribute a, AttributeModifier am) {
        removeAttribute(to, a, am.getId());
    }

    public static void removeAttribute(LivingEntity to, Attribute a, UUID am) {
        final AttributeInstance atr = to.getAttribute(a);
        if (atr == null) return;
        atr.removeModifier(am);
    }

    public static void createCloud(Level world, Entity entityIn, double x, double y, double z, float size, ParticleOptions type) {
        AreaEffectCloud areaeffectcloudentity = new AreaEffectCloud(world, x, y, z);
        if (entityIn instanceof LivingEntity)
            areaeffectcloudentity.setOwner((LivingEntity) entityIn);
        areaeffectcloudentity.setParticle(type);
        areaeffectcloudentity.setRadius(size);
        areaeffectcloudentity.setDuration(0);
        world.addFreshEntity(areaeffectcloudentity);
    }

    public static Entity aimEntity(LivingEntity caster) {
        return GeneralUtils.raytraceEntity(caster.level, caster, caster.getAttributeValue(ForgeMod.ATTACK_RANGE.get()));
    }

    public static LivingEntity aimLiving(LivingEntity caster) {
        return GeneralUtils.raytraceLiving(caster.level, caster, caster.getAttributeValue(ForgeMod.ATTACK_RANGE.get()));
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

    public static double getSkillEffectiveness(LivingEntity caster){
        return
    }

}
