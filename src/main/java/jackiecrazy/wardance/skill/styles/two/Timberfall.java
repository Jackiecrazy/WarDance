package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.footwork.event.StunEvent;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.entity.skill.TimberfallEntity;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.event.PlayInteractionEvent;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.HashSet;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = FORGE)
public class Timberfall extends WarCry {
    private static final Color c = new Color(71, 48, 9);
    private final HashSet<String> tag = makeTag("chant", ProcPoints.melee, ProcPoints.modify_crit, ProcPoints.on_hurt, ProcPoints.attack_might, ProcPoints.on_being_hurt, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> no = none;//.getTagFromContents(new HashSet<>(Collections.emptyList()));

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void targeted(LivingChangeTargetEvent e) {
        LivingEntity target = e.getNewTarget();
        if (target != null && e.getEntity() instanceof Mob m && m.getTarget() != target && CasterData.getCap(target).isSkillEquipped(WarSkills.TIMBERFALL.get())){
            plantTree(target);
        }
    }

    private static void plantTree(LivingEntity caster) {
        TimberfallEntity fwe = new TimberfallEntity(WarEntities.TIMBER.get(), caster.level());
        fwe.setSkillUsed(WarSkills.TIMBERFALL.get()).setOwner(caster);
        Vec3 look = Vec3.ZERO.add(WarDance.rand.nextDouble() - 0.5, 0, WarDance.rand.nextDouble() - 0.5).scale(10).add(new Vec3(0, 7, 0));
        fwe.moveTo(caster.getX() + look.x, caster.getEyeY() + look.y, caster.getZ() + look.z);
        //fwe.yeet(caster.getEyePosition().add(look.scale(2)), 1);
        fwe.setInteractionRange(1f);
        caster.level().addFreshEntity(fwe);
    }

    @Override
    protected int getDuration(float might) {
        return (int) might * 2 - 2;
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.ACTIVE) {
            prev.setState(STATE.ACTIVE);
            return true;
        }
        if (to == STATE.COOLING) {
            prev.setState(STATE.INACTIVE);
        }
        return super.onStateChange(caster, prev, from, to);
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return activeTick(stats);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint.getPhase() != EventPriority.LOWEST) return;
        if (procPoint instanceof StunEvent cpe && state == STATE.ACTIVE && cpe.getEntity() == target) {
            plantTree(caster);
        } else if (procPoint instanceof SkillCastEvent sce && sce.getEntity() == caster) {
            plantTree(caster);
        } else if (procPoint instanceof PlayInteractionEvent.Interaction se && state == STATE.ACTIVE) {
            //se.setColor(Color.ORANGE);
            se.getInteraction().tags();
        }
        super.onProc(caster, procPoint, state, stats, target);
    }
}
