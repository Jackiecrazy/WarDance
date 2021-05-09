package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import jackiecrazy.wardance.skill.coupdegrace.Reinvigorate;
import jackiecrazy.wardance.skill.heavyblow.*;
import jackiecrazy.wardance.skill.ironguard.Backpedal;
import jackiecrazy.wardance.skill.ironguard.IronGuard;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class WarSkills {
    public static DeferredRegister<Skill> SKILLS = DeferredRegister
            .create(Skill.class, WarDance.MODID);

    public static final RegistryObject<Skill> HEAVY_BLOW = SKILLS.register("heavy_blow", HeavyBlow::new);
    public static final RegistryObject<Skill> SHATTER = SKILLS.register("shatter", Shatter::new);
    public static final RegistryObject<Skill> STAGGER = SKILLS.register("stagger", Stagger::new);
    public static final RegistryObject<Skill> POISE = SKILLS.register("poise", Poise::new);
    public static final RegistryObject<Skill> LUNGE = SKILLS.register("lunge", Lunge::new);
    public static final RegistryObject<Skill> BACKSTAB = SKILLS.register("backstab", Backstab::new);
    public static final RegistryObject<Skill> IRONGUARD = SKILLS.register("iron_guard", IronGuard::new);
    public static final RegistryObject<Skill> BACKPEDAL = SKILLS.register("backpedal", Backpedal::new);
    public static final RegistryObject<Skill> COUPDEGRACE = SKILLS.register("coup_de_grace", CoupDeGrace::new);
    public static final RegistryObject<Skill> REINVIGORATE = SKILLS.register("reinvigorate", Reinvigorate::new);
}
