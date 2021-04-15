package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.heavyblow.HeavyBlow;
import jackiecrazy.wardance.skill.heavyblow.Shatter;
import jackiecrazy.wardance.skill.heavyblow.Stagger;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;

public class WarSkills {
    public static DeferredRegister<Skill> SKILLS = DeferredRegister
            .create(Skill.class, WarDance.MODID);

    public static final RegistryObject<Skill> HEAVY_BLOW = SKILLS.register("heavy_blow", HeavyBlow::new);
    public static final RegistryObject<Skill> SHATTER = SKILLS.register("shatter", Shatter::new);
    public static final RegistryObject<Skill> STAGGER = SKILLS.register("stagger", Stagger::new);
}
