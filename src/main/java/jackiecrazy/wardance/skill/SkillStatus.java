package jackiecrazy.wardance.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It's literally just potion effects that I don't want you to remove.
 */
public abstract class SkillStatus {
    protected HashMap<Attribute, List<AttributeModifier>> dummy =new HashMap<>();
    protected int timer;
    public SkillStatus(int timer){
        this.timer=timer;
    }
    public SkillStatus(CompoundNBT tag){
        timer=tag.getInt("timer");
    }
    public void writeToTag(CompoundNBT tag){
        tag.putString("name", this.getClass().getName());
        tag.putInt("timer", timer);
    }
    public int getTimer(){
        return timer;
    }
    public abstract void tick(LivingEntity target);
    @SuppressWarnings("all")
    @Nullable
    public static SkillStatus readFromTag(CompoundNBT tag){
        try {
            Class<? extends SkillStatus> c= (Class<? extends SkillStatus>) Class.forName(tag.getString("name"));
            return c.getConstructor(CompoundNBT.class).newInstance(tag);
        }catch (Exception ignored){

        }
        return null;
    }
    public HashMap<Attribute, List<AttributeModifier>> getAttributeModifiers(LivingEntity target){
        return dummy;
    }
    public void removeAttributesModifiersFromEntity(LivingEntity livingEntity, AttributeModifierManager attributeMapIn, int amplifier) {
        for(Map.Entry<Attribute, List<AttributeModifier>> entry : getAttributeModifiers(livingEntity).entrySet()) {
            ModifiableAttributeInstance modifiableattributeinstance = attributeMapIn.createInstanceIfAbsent(entry.getKey());
            if (modifiableattributeinstance != null) {
                for(AttributeModifier am:entry.getValue())
                modifiableattributeinstance.removeModifier(am);
            }
        }

    }

    public void applyAttributesModifiersToEntity(LivingEntity livingEntity, AttributeModifierManager attributeMapIn, int amplifier) {
        for(Map.Entry<Attribute, List<AttributeModifier>> entry : getAttributeModifiers(livingEntity).entrySet()) {
            ModifiableAttributeInstance modifiableattributeinstance = attributeMapIn.createInstanceIfAbsent(entry.getKey());
            if (modifiableattributeinstance != null) {
                for(AttributeModifier attributemodifier:entry.getValue()) {
                    modifiableattributeinstance.removeModifier(attributemodifier);
                    modifiableattributeinstance.applyPersistentModifier(attributemodifier);
                }
            }
        }

    }
}
