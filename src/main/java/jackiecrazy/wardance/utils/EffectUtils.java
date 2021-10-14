package jackiecrazy.wardance.utils;

import jackiecrazy.wardance.entity.FearEntity;
import jackiecrazy.wardance.entity.WarEntities;
import jackiecrazy.wardance.potion.WarEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;

import java.util.Map;

public class EffectUtils {
    //so you think immunity to my potions is clever, eh?

    /**
     * Attempts to add the potion effect. If it fails, the function will *permanently* apply all the attribute modifiers, with the option to stack them as well
     * Take that, wither!
     */
    public static boolean attemptAddPot(LivingEntity elb, EffectInstance pot, boolean stackWhenFailed) {
        Effect p = pot.getPotion();
        elb.addPotionEffect(pot);
        if (!elb.isPotionActive(p)) {
            //I'm gonna do it anyways, take that.
            for (Map.Entry<Attribute, AttributeModifier> e : p.getAttributeModifierMap().entrySet()) {
                final ModifiableAttributeInstance attribute = elb.getAttribute(e.getKey());
                if (attribute != null) {
                    if (stackWhenFailed) {
                        AttributeModifier am = attribute.getModifier(e.getValue().getID());
                        if (am != null && am.getOperation() == e.getValue().getOperation()) {
                            AttributeModifier apply = new AttributeModifier(e.getValue().getID(), e.getValue().getName(), am.getAmount() + e.getValue().getAmount(), am.getOperation());
                            attribute.removeModifier(e.getValue().getID());
                            attribute.applyNonPersistentModifier(apply);
                        } else attribute.applyNonPersistentModifier(e.getValue());
                    } else {
                        attribute.removeModifier(e.getValue().getID());
                        attribute.applyNonPersistentModifier(e.getValue());
                    }
                }
            }
            elb.getActivePotionMap().put(pot.getPotion(), pot);
            return false;
        } else {
            elb.getActivePotionEffect(pot.getPotion()).combine(pot);
        }
        return true;
    }

    /**
     * increases the potion amplifier on the entity, with options on the duration
     */
    public static EffectInstance stackPot(LivingEntity elb, EffectInstance toAdd, StackingMethod method) {
        Effect p = toAdd.getPotion();
        EffectInstance pe = elb.getActivePotionEffect(p);
        if (pe == null || method == StackingMethod.NONE) {
            //System.out.println("beep1");
            return toAdd;
        }
        //System.out.println(pe);
        int length = pe.getDuration();
        int potency = pe.getAmplifier() + 1 + toAdd.getAmplifier();
        //System.out.println(length);
        //System.out.println(potency);

        switch (method) {
            case ADD:
                length = toAdd.getDuration() + pe.getDuration();
                break;
            case MAXDURATION:
                length = Math.max(pe.getDuration(), toAdd.getDuration());
                break;
            case MAXPOTENCY:
                length = pe.getAmplifier() == toAdd.getAmplifier() ? Math.max(pe.getDuration(), toAdd.getDuration()) : pe.getAmplifier() > toAdd.getAmplifier() ? pe.getDuration() : toAdd.getDuration();
                break;
            case MINDURATION:
                length = Math.min(pe.getDuration(), toAdd.getDuration());
                break;
            case MINPOTENCY:
                length = pe.getAmplifier() == toAdd.getAmplifier() ? Math.min(pe.getDuration(), toAdd.getDuration()) : pe.getAmplifier() < toAdd.getAmplifier() ? pe.getDuration() : toAdd.getDuration();
                break;
            case ONLYADD:
                potency = toAdd.getAmplifier();
                length = toAdd.getDuration() + pe.getDuration();
                break;
        }
        //System.out.println(ret);
        return new EffectInstance(p, length, potency, pe.isAmbient(), pe.doesShowParticles(), pe.isShowIcon());
    }

    public static double getEffectiveLevel(LivingEntity elb, Effect p, Attribute workOff) {
        if (elb.getActivePotionEffect(p) != null)
            return elb.getActivePotionEffect(p).getAmplifier() + 1;
        return 0;
    }

    public static void causeFear(LivingEntity elb, LivingEntity applier, int duration) {
        attemptAddPot(elb, new EffectInstance(WarEffects.FEAR.get(), duration, 0), false);
        if (elb instanceof MobEntity) {
            MobEntity el = (MobEntity) elb;
            el.getNavigator().clearPath();
            el.setAttackTarget(null);
        }
        if (!elb.world.isRemote) {
            //PigEntity f=new PigEntity(EntityType.PIG, elb.world);
            FearEntity f = new FearEntity(WarEntities.fear, elb.world);
            f.setFearSource(applier);
            f.setTetheringEntity(elb);
            f.setPositionAndUpdate(elb.getPosX(), elb.getPosY(), elb.getPosZ());
            elb.world.addEntity(f);
        }
    }

    public enum StackingMethod {
        NONE,
        ADD,
        MAXDURATION,
        MAXPOTENCY,
        MINDURATION,
        MINPOTENCY,
        ONLYADD
    }
}
