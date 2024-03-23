package jackiecrazy.wardance.skill.projectile;

import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.client.particle.FootworkParticles;
import jackiecrazy.footwork.utils.ParticleUtils;
import jackiecrazy.footwork.utils.TargetingUtils;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class WindShot extends Skill {
    //shooting an arrow will consume evasion to give it 3 levels of piercing and pull all enemies in a 4 block radius towards point of impact
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void wind(ProjectileImpactEvent e) {
        if (e.getProjectile().getOwner() instanceof LivingEntity shooter && CasterData.getCap(shooter).getSkillState(WarSkills.WIND_SHOT.get()) == STATE.HOLSTERED && CombatData.getCap(shooter).consumeEvade()) {
            if (e.getProjectile() instanceof Arrow arrow) arrow.setPierceLevel((byte) (arrow.getPierceLevel() + 3));
            ParticleUtils.playSweepParticle(FootworkParticles.CIRCLE.get(), shooter, e.getProjectile().position(), 0, 4, Color.CYAN, 0.1);
            int radius = 4;

            List<LivingEntity> list = shooter.level().getEntitiesOfClass(LivingEntity.class, AABB.unitCubeFromLowerCorner(e.getProjectile().position()).inflate(radius));

            for (LivingEntity entity : list) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(entity.position().vectorTo(e.getProjectile().position()).scale(0.18)));
            }
        }
    }

    @Override
    public HashSet<String> getTags() {
        return passive;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
            return true;
        }
        return boundCast(prev, from, to);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
    }
}
