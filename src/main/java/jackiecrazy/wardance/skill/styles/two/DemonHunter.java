package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.event.ProjectileParryEvent;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.skill.styles.ColorRestrictionStyle;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.DamageUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

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
        if (e.getEntity() instanceof Player player) {
            if (!(e.getItem().getItem() instanceof ProjectileWeaponItem)) return;
            if (CasterData.getCap(player).getStyle() != WarSkills.DEMON_HUNTER.get()) return;
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60));
            if (player.tickCount % 2 == 0) e.setDuration(e.getDuration() - 1);
        }
    }

    @SubscribeEvent()
    public static void pain(ProjectileParryEvent e) {
        //more pain for marked
        Marks.getCap(e.getEntity()).getActiveMark(WarSkills.DEMON_HUNTER.get()).ifPresent(a -> {
            LivingEntity marker = a.getCaster(e.getEntity().level);
            CombatData.getCap(e.getEntity()).consumePosture(marker, 2);
            a.decrementDuration();
        });
    }
    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent a && DamageUtils.isMeleeAttack(a.getSource()) && target != null && a.getEntity() != caster && a.getPhase() == EventPriority.HIGHEST) {
            if (Marks.getCap(target).isMarked(this))
                CombatUtils.knockBack(caster, target, 1, true, true);
            mark(caster, target, 3);
            caster.getAttribute(ForgeMod.ATTACK_RANGE.get()).removeModifier(reach);
        }
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        CasterData.getCap(caster).getSkillData(this).ifPresent(a -> caster.getAttribute(ForgeMod.ATTACK_RANGE.get()).addTransientModifier(reach));
    }

    @Nullable
    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            sd.setDuration(Math.min(sd.getDuration() + existing.getDuration(), 5));
            sd.setMaxDuration(5);
        }
        return sd;
    }
}
