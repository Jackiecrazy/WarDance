package jackiecrazy.wardance.skill.descend;

import jackiecrazy.wardance.event.EntityAwarenessEvent;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.eventbus.api.Event;

import java.awt.*;

public class PhantomDive extends Descend {
    @Override
    public Color getColor() {
        return Color.LIGHT_GRAY;
    }

    @Override
    protected boolean canCast(LivingEntity caster) {
        return true;
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof EntityAwarenessEvent && ((EntityAwarenessEvent) procPoint).getAttacker() == caster) {
            CombatUtils.Awareness awareness = ((EntityAwarenessEvent) procPoint).getAwareness();
            double posDiff = stats.getDuration() - caster.getPosY();
            int length = 0;
            while (posDiff > 0) {
                switch (awareness) {
                    case ALERT:
                        awareness = CombatUtils.Awareness.DISTRACTED;
                        break;
                    case DISTRACTED:
                        awareness = CombatUtils.Awareness.UNAWARE;
                        break;
                    case UNAWARE:
                        length += 60;
                }
                posDiff -= 7;
            }
            if (length > 0) target.addPotionEffect(new EffectInstance(WarEffects.PARALYSIS.get(), length));
            ((EntityAwarenessEvent) procPoint).setAwareness(awareness);
        }
        super.onSuccessfulProc(caster, stats, target, procPoint);
    }
}
