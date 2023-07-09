package jackiecrazy.wardance.skill.styles.two;

import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public class WarCry extends SkillStyle {
    private static final AttributeModifier wrap = new AttributeModifier(UUID.fromString("4b342542-fcfb-47a8-8da8-4f57588f7003"), "bandaging wounds", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private final HashSet<String> procs = makeTag("chant", ProcPoints.on_being_hurt, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep);
    private final HashSet<String> tag = makeTag(SkillTags.chant, SkillTags.melee);

    public WarCry() {
        super(2);
    }

    @Override
    public HashSet<String> getTags() {
        return none;
    }

    @Nonnull
    @Override
    public HashSet<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 2;
    }

    @Nonnull
    @Override
    public SkillArchetype getArchetype() {
        return SkillArchetypes.none;
    }

    protected int getDuration(float might) {
        return Math.max(0, (int) (might * 1.5));
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
//        if (to == STATE.HOLSTERED && cast(caster, getDuration(CombatData.getCap(caster).getMight())))
//            evoke(caster);
//        if (to == STATE.COOLING) {
//            prev.setState(STATE.INACTIVE);
//            prev.setDuration(0);
//        }
        prev.setState(STATE.INACTIVE);
        return instantCast(prev, from, to);
    }
}
