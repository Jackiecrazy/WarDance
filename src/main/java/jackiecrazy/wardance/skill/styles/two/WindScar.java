package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.capability.stylish.StylishData;
import jackiecrazy.footwork.entity.flyingweapon.FlyingItemEntity;
import jackiecrazy.footwork.event.DodgeEvent;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.entity.WindBladeEntity;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.HashSet;
import java.util.UUID;

public class WindScar extends WarCry {
    private static final AttributeModifier reach = new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "wind scar bonus", 1.5, AttributeModifier.Operation.ADDITION);
    private final HashSet<String> tag = makeTag("chant", ProcPoints.on_being_hurt, ProcPoints.melee, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> no = makeTag((("sweep")));

    @Override
    public void onEquip(LivingEntity caster) {
        SkillUtils.addAttribute(caster, ForgeMod.ENTITY_REACH.get(), reach);
        super.onEquip(caster);
    }

    @Override
    public void onUnequip(LivingEntity caster, SkillData stats) {
        SkillUtils.removeAttribute(caster, ForgeMod.ENTITY_REACH.get(), reach);
        super.onUnequip(caster, stats);
    }

    protected boolean cast(LivingEntity caster, float duration) {
        return cast(caster, null, duration, false, 1);
    }

    @Override
    protected int getDuration(float might) {
        return (int) (3 * might);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent hurt && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.ACTIVE && hurt.getEntity() == target) {
            stats.addTarget(target);
            mark(caster, target, 60);
            if (hurt.getSource().getDirectEntity() instanceof Projectile)
                windPressure(caster, stats, 1);
        }
        if (procPoint instanceof DodgeEvent && !caster.level().isClientSide) {
            windPressure(caster, stats, 1);
        }
        if (procPoint instanceof PlayInteractionEvent.Pre p) {
            switch (p.getMoveState()) {
                case AERIAL, SPRINTING:
                    windPressure(caster, stats, 1);
                case THROW, PICKUP_FLOURISH,DRAW_ATTACK:
                    windPressure(caster, stats, 2);
            }
        }
        super.onProc(caster, procPoint, state, stats, target);
    }

    private void windPressure(LivingEntity player, SkillData d, int amount) {
        d.addDuration(amount);
        if (d.getDuration() >= 1) {
            //create wind blades
            final float numofBlades = StylishData.getCap(player).getCombo() * 1.5f;
            //make a fan shape
            Vec3 up = new Vec3(0, 1, 0);
            Vec3 forward = player.getLookAngle();
            //cross two different ways to get two corners
            Vec3 firstHalf = up.cross(forward);
            Vec3 secondHalf = forward.cross(up);
            for (Vec3 v : new Vec3[]{firstHalf, secondHalf})
                for (int x = 0; x < numofBlades; x++) {
                    WindBladeEntity fwe = new WindBladeEntity(WarEntities.WIND_BLADE.get(), player.level());
                    fwe.setOwner(player);
                    Vec3 look = player.getLookAngle().reverse();
                    Vec3 interpol = v.lerp(up, x / numofBlades);
                    fwe.setPosRaw(player.getX() + look.x + interpol.x, player.getEyeY() + look.y + interpol.y, player.getZ() + look.z + interpol.z);
                    fwe.yeet(player.getEyePosition().add(look).add(interpol.scale(3)), 2.5);
                    fwe.setState(FlyingItemEntity.STATE.THROW_TRACK);
                    fwe.addTargets(d.getTargets());
                    fwe.setInteractionRange(1f);
                    player.level().addFreshEntity(fwe);
                }
            d.clearTargets();
            d.setDuration(0);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return super.onStateChange(caster, prev, from, to);
    }
}
