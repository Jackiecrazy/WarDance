package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import jackiecrazy.wardance.skill.crownchampion.CrownChampion;
import jackiecrazy.wardance.skill.fightingspirit.*;
import jackiecrazy.wardance.skill.grapple.Clinch;
import jackiecrazy.wardance.skill.grapple.Grapple;
import jackiecrazy.wardance.skill.grapple.Submission;
import jackiecrazy.wardance.skill.heavyblow.*;
import jackiecrazy.wardance.skill.ironguard.*;
import jackiecrazy.wardance.skill.kick.*;
import jackiecrazy.wardance.skill.shieldbash.ArmLock;
import jackiecrazy.wardance.skill.shieldbash.Berserk;
import jackiecrazy.wardance.skill.shieldbash.Overbear;
import jackiecrazy.wardance.skill.shieldbash.ShieldBash;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class WarSkills {
    public static DeferredRegister<Skill> SKILLS = DeferredRegister
            .create(Skill.class, WarDance.MODID);

    public static final RegistryObject<Skill> HEAVY_BLOW = SKILLS.register("heavy_blow", HeavyBlow::new);
    public static final RegistryObject<Skill> SHATTER = SKILLS.register("shatter", Shatter::new);
    public static final RegistryObject<Skill> STAGGER = SKILLS.register("stagger", Stagger::new);
    public static final RegistryObject<Skill> POISE = SKILLS.register("poise", Poise::new);
    public static final RegistryObject<Skill> VAULT = SKILLS.register("vault", Vault::new);
    public static final RegistryObject<Skill> BACKSTAB = SKILLS.register("backstab", Backstab::new);
    public static final RegistryObject<Skill> IRON_GUARD = SKILLS.register("iron_guard", IronGuard::new);
    public static final RegistryObject<Skill> BACKPEDAL = SKILLS.register("backpedal", Backpedal::new);
    public static final RegistryObject<Skill> BIND = SKILLS.register("bind", Bind::new);
    public static final RegistryObject<Skill> MIKIRI = SKILLS.register("mikiri", Mikiri::new);
    public static final RegistryObject<Skill> OVERPOWER = SKILLS.register("overpower", Overpower::new);
    public static final RegistryObject<Skill> RECOVERY = SKILLS.register("recovery", Recovery::new);
    public static final RegistryObject<Skill> COUP_DE_GRACE = SKILLS.register("coup_de_grace", CoupDeGrace::new);
    public static final RegistryObject<Skill> REINVIGORATE = SKILLS.register("reinvigorate", CoupDeGrace.Reinvigorate::new);
    public static final RegistryObject<Skill> SILENCER = SKILLS.register("silencer", CoupDeGrace.Silencer::new);
    public static final RegistryObject<Skill> FRENZY = SKILLS.register("frenzy", CoupDeGrace.Frenzy::new);
    public static final RegistryObject<Skill> WARNING = SKILLS.register("i_warned_you", CoupDeGrace.Warning::new);
    public static final RegistryObject<Skill> KICK = SKILLS.register("kick", Kick::new);
    public static final RegistryObject<Skill> IRON_KNEE = SKILLS.register("iron_knee", IronKnee::new);
    public static final RegistryObject<Skill> TACKLE = SKILLS.register("tackle", Tackle::new);
    public static final RegistryObject<Skill> BACKFLIP = SKILLS.register("backflip", Kick.Backflip::new);
    public static final RegistryObject<Skill> LOW_SWEEP = SKILLS.register("low_sweep", LowSweep::new);
    public static final RegistryObject<Skill> TRIP = SKILLS.register("trip", Trip::new);
    public static final RegistryObject<Skill> GRAPPLE = SKILLS.register("grapple", Grapple::new);
    public static final RegistryObject<Skill> CLINCH = SKILLS.register("clinch", Clinch::new);
    public static final RegistryObject<Skill> REVERSAL = SKILLS.register("reversal", Grapple.Reversal::new);
    public static final RegistryObject<Skill> SUBMISSION = SKILLS.register("submission", Submission::new);
    public static final RegistryObject<Skill> SUPLEX = SKILLS.register("suplex", Grapple.Suplex::new);
    public static final RegistryObject<Skill> SHIELD_BASH = SKILLS.register("shield_bash", ShieldBash::new);
    public static final RegistryObject<Skill> RIM_PUNCH = SKILLS.register("rim_punch", ShieldBash.RimPunch::new);
    public static final RegistryObject<Skill> ARM_LOCK = SKILLS.register("arm_lock", ArmLock::new);
    public static final RegistryObject<Skill> FOOT_SLAM = SKILLS.register("foot_slam", ShieldBash.FootSlam::new);
    public static final RegistryObject<Skill> OVERBEAR = SKILLS.register("overbear", Overbear::new);
    public static final RegistryObject<Skill> BERSERK = SKILLS.register("berserk", Berserk::new);
    public static final RegistryObject<Skill> FIGHTINGSPIRIT = SKILLS.register("fighting_spirit", FightingSpirit::new);
    public static final RegistryObject<Skill> BOULDERBRACE = SKILLS.register("boulder_brace", BoulderBrace::new);
    public static final RegistryObject<Skill> WINDSCAR = SKILLS.register("wind_scar", WindScar::new);
    public static final RegistryObject<Skill> FLAMEDANCE = SKILLS.register("flame_dance", FlameDance::new);
    public static final RegistryObject<Skill> FROSTFANG = SKILLS.register("frost_fang", FrostFang::new);
    public static final RegistryObject<Skill> TIMBERFALL = SKILLS.register("timberfall", Timberfall::new);
    public static final RegistryObject<Skill> CROWNCHAMPION = SKILLS.register("crown_champion", CrownChampion::new);
    public static final RegistryObject<Skill> VENGEFULMIGHT = SKILLS.register("vengeful_might", CrownChampion.VengefulMight::new);
    public static final RegistryObject<Skill> HIDDENMIGHT = SKILLS.register("hidden_might", CrownChampion.HiddenMight::new);
    public static final RegistryObject<Skill> PRIDEFULMIGHT = SKILLS.register("prideful_might", CrownChampion.PridefulMight::new);
    public static final RegistryObject<Skill> ELEMENTALMIGHT = SKILLS.register("elemental_might", CrownChampion.ElementalMight::new);
}
