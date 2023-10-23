package jackiecrazy.wardance.utils;

import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
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

    public static boolean hasAttribute(LivingEntity to, Attribute a, AttributeModifier am) {
        return hasAttribute(to, a, am.getId());
    }

    public static boolean hasAttribute(LivingEntity to, Attribute a, UUID am) {
        final AttributeInstance atr = to.getAttribute(a);
        if (atr == null) return false;
        return atr.getModifier(am) != null;
    }

    public static boolean removeAttribute(LivingEntity to, Attribute a, AttributeModifier am) {
        return removeAttribute(to, a, am.getId());
    }

    public static boolean removeAttribute(LivingEntity to, Attribute a, UUID am) {
        final AttributeInstance atr = to.getAttribute(a);
        if (atr == null) return false;
        atr.removeModifier(am);
        return true;
    }

    public static void createCloud(Level world, Entity entityIn, double x, double y, double z, float size, ParticleOptions type) {
        AreaEffectCloud areaeffectcloudentity = new AreaEffectCloud(world, x, y, z);
        if (entityIn instanceof LivingEntity)
            areaeffectcloudentity.setOwner((LivingEntity) entityIn);
        areaeffectcloudentity.setParticle(type);
        areaeffectcloudentity.setRadius(size);
        areaeffectcloudentity.setDuration(1);
        world.addFreshEntity(areaeffectcloudentity);
    }

    public static Entity aimEntity(LivingEntity caster) {
        return GeneralUtils.raytraceEntity(caster.level(), caster, caster.getAttributeValue(ForgeMod.ENTITY_REACH.get()));
    }

    public static LivingEntity aimLiving(LivingEntity caster) {
        return GeneralUtils.raytraceLiving(caster.level(), caster, caster.getAttributeValue(ForgeMod.ENTITY_REACH.get()));
    }

    public static Entity aimEntity(LivingEntity caster, double range) {
        return GeneralUtils.raytraceEntity(caster.level(), caster, range);
    }

    public static LivingEntity aimLiving(LivingEntity caster, double range) {
        return GeneralUtils.raytraceLiving(caster.level(), caster, range);
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

    /**
     * use {@link SkillData#getEffectiveness()} for any skills that invoke {@link Skill#cast(LivingEntity, float)}
     */
    public static float getSkillEffectiveness(@Nullable LivingEntity caster) {
        if (caster == null) return 1;
        return (float) caster.getAttributeValue(FootworkAttributes.SKILL_EFFECTIVENESS.get());
    }

    public static void updateTetheringVelocity(LivingEntity moveTowards, LivingEntity toBeMoved, double maxDist) {
        if (toBeMoved != null && moveTowards != null) {
            double distsq = toBeMoved.distanceToSqr(moveTowards);
            Vec3 point = moveTowards.position();

            if (maxDist * maxDist < distsq) {
                toBeMoved.push((point.x - toBeMoved.getX()) * 0.05, (point.y - toBeMoved.getY()) * 0.05, (point.z - toBeMoved.getZ()) * 0.05);
            }
            toBeMoved.hurtMarked = true;
        }

    }


}
