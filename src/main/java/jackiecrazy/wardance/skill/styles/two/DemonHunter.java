package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.SweepEvent;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.skill.styles.ColorRestrictionStyle;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class DemonHunter extends ColorRestrictionStyle {
    private static final AttributeModifier reach = new AttributeModifier(UUID.fromString("abe24c38-73e3-4191-9df4-e06e117699c1"), "demon hunter bonus", 3, AttributeModifier.Operation.ADDITION);

    public DemonHunter() {
        super(2, false, SkillColors.cyan);
    }

    @SubscribeEvent()
    public static void slow(LivingEntityUseItemEvent e) {
        //charge faster
        if (e.getEntity() instanceof Player player && !player.level().isClientSide && !player.onGround()) {
            if (!(e.getItem().getItem() instanceof ProjectileWeaponItem) && !(e.getItem().getItem() instanceof TridentItem))
                return;
            if (CasterData.getCap(player).getStyle() != WarSkills.DEMON_HUNTER.get()) return;
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, true, false, false));
            if (player.tickCount % 2 == 0) e.setDuration(e.getDuration() - 1);
        }
    }

    @SubscribeEvent()
    public static void pain(ProjectileImpactEvent e) {
        if (e.getRayTraceResult().getType() == HitResult.Type.ENTITY && e.getRayTraceResult() instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity uke) {

            //more pain for marked
            Marks.getCap(uke).getActiveMark(WarSkills.DEMON_HUNTER.get()).ifPresent(a -> {
                LivingEntity marker = a.getCaster(e.getEntity().level());
                CombatData.getCap(uke).consumePosture(marker, 2 * SkillUtils.getSkillEffectiveness(marker));
                a.decrementDuration();
                if (uke.level() instanceof ServerLevel server)
                    server.sendParticles(ParticleTypes.CRIT, uke.getX(), uke.getY(), uke.getZ(), (int) 20, uke.getBbWidth(), uke.getBbHeight(), uke.getBbWidth(), 0f);

            });
        }
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent a && DamageUtils.isMeleeAttack(a.getSource()) && target != null && a.getEntity() != caster && a.getPhase() == EventPriority.LOWEST) {
            if (Marks.getCap(target).isMarked(this)) {
                if (!caster.onGround()) {
                    CombatUtils.knockBack(caster, target, 1, true, true);
                    Vec3 vec = caster.getDeltaMovement();
                    caster.lerpMotion(vec.x, vec.y + 1, vec.z);
                    if(SkillUtils.hasAttribute(caster, ForgeMod.ENTITY_REACH.get(), reach))
                        completeChallenge(caster);
                }
            } else mark(caster, target, 3);
            SkillUtils.removeAttribute(caster, ForgeMod.ENTITY_REACH.get(), reach);
        }
        if(procPoint instanceof SweepEvent se){
            if (!caster.onGround()||caster.getAttribute(ForgeMod.ENTITY_REACH.get()).hasModifier(reach)) {
                se.setColor(Color.CYAN);
            }
        }
    }

    @Nullable
    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        return sd;
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        CasterData.getCap(caster).getSkillData(this).ifPresent(a -> SkillUtils.addAttribute(caster, ForgeMod.ENTITY_REACH.get(), reach));
    }
}
