package jackiecrazy.wardance.potion;

import jackiecrazy.wardance.WarDance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class WarEffects {
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, WarDance.MODID);

    //slows posture regeneration by 0.2 per level
    public static final RegistryObject<Effect> EXHAUSTION = EFFECTS.register("exhaustion", () -> new WarEffect(EffectType.HARMFUL, 0xa9a9a9));
    //next attack against a distracted creature applies the distracted bonus and removes distraction
    public static final RegistryObject<Effect> DISTRACTION = EFFECTS.register("distraction", () -> new WarEffect(EffectType.HARMFUL, 0xc98fff));
    //identical to distraction, but not removed on attack
    public static final RegistryObject<Effect> CONFUSION = EFFECTS.register("confusion", () -> new WarEffect(EffectType.HARMFUL, 0xc98fff));
    //identical to distraction, the target will attempt to run away, invoke with EffectUtils.causeFear()
    public static final RegistryObject<Effect> FEAR = EFFECTS.register("fear", () -> new WarEffect(EffectType.HARMFUL, 0xfcfc00));
    //attacks against paralyzed targets apply the unaware bonus, paralyzed targets cannot move
    public static final RegistryObject<Effect> PARALYSIS = EFFECTS.register("paralysis", () -> new WarEffect(EffectType.HARMFUL, 0xc98fff).addAttributesModifier(Attributes.MOVEMENT_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -1, AttributeModifier.Operation.MULTIPLY_TOTAL));
    //identical to paralysis, but adds 4 armor
    public static final RegistryObject<Effect> PETRIFY = EFFECTS.register("petrify", () -> new WarEffect(EffectType.HARMFUL, 0xc98fff).addAttributesModifier(Attributes.MOVEMENT_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -1, AttributeModifier.Operation.MULTIPLY_TOTAL).addAttributesModifier(Attributes.KNOCKBACK_RESISTANCE, "55FCED67-E92A-486E-9800-B47F202C4386", 100, AttributeModifier.Operation.ADDITION).addAttributesModifier(Attributes.ARMOR, "55FCED67-E92A-486E-9800-B47F202C4386", 4, AttributeModifier.Operation.ADDITION));
    //identical to paralysis, but only works once
    public static final RegistryObject<Effect> SLEEP = EFFECTS.register("sleep", () -> new WarEffect(EffectType.HARMFUL, 0xc98fff).addAttributesModifier(Attributes.MOVEMENT_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -1, AttributeModifier.Operation.MULTIPLY_TOTAL));
    //increases incoming non-combat damage by potency
    public static final RegistryObject<Effect> VULNERABLE = EFFECTS.register("vulnerable", () -> new WarEffect(EffectType.HARMFUL, 0xc98fff));
    //reduces armor by 2 per level
    public static final RegistryObject<Effect> CORROSION = EFFECTS.register("corrosion", () -> new WarEffect(EffectType.HARMFUL, 0xc98fff).addAttributesModifier(Attributes.ARMOR, "55FCED67-E92A-486E-9800-B47F202C4386", -2, AttributeModifier.Operation.ADDITION));
    //increases armor by 2 per level
    public static final RegistryObject<Effect> FORTIFICATION = EFFECTS.register("fortification", () -> new WarEffect(EffectType.BENEFICIAL, 0xc98fff).addAttributesModifier(Attributes.ARMOR, "55FCED67-E92A-486E-9800-B47F202C4386", 2, AttributeModifier.Operation.ADDITION));
    //increases posture damage by 20% per level
    public static final RegistryObject<Effect> ENFEEBLE = EFFECTS.register("enfeeble", () -> new WarEffect(EffectType.HARMFUL, 0xc98fff));

}
