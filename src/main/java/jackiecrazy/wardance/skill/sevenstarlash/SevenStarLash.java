package jackiecrazy.wardance.skill.sevenstarlash;

import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;

public class SevenStarLash extends Skill {
    /*
    seven-star lash: your next attack deals no (posture) damage until you attack again, at which point it detonates for 1.5x the original amount
blinding speed: this attack will not modify the enemy's alertness status towards you, you will automatically follow up with the other hand if possible
organ rupture:  deal no extra damage but splash a 2 block area on detonation
mirror curse: incoming damage will be redirected to the target and can also detonate lash
death mark: lash detonates after 5 seconds and will additionally deal 50% of the damage taken by the target during the time
binding chains: trying to leave the attack range of the lashing weapon will detonate lash and drag the target back
     */
    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return null;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return null;
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        return false;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {

    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {

    }
}
