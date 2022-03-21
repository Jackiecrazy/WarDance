package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import jackiecrazy.wardance.skill.crownchampion.CrownChampion;
import jackiecrazy.wardance.skill.judgment.*;
import jackiecrazy.wardance.skill.feint.Feint;
import jackiecrazy.wardance.skill.grapple.Clinch;
import jackiecrazy.wardance.skill.grapple.Grapple;
import jackiecrazy.wardance.skill.grapple.Submission;
import jackiecrazy.wardance.skill.heavyblow.*;
import jackiecrazy.wardance.skill.hex.Hex;
import jackiecrazy.wardance.skill.hex.ItchyCurse;
import jackiecrazy.wardance.skill.hex.Petrify;
import jackiecrazy.wardance.skill.ironguard.*;
import jackiecrazy.wardance.skill.kick.*;
import jackiecrazy.wardance.skill.mementomori.DeathDenial;
import jackiecrazy.wardance.skill.mementomori.MementoMori;
import jackiecrazy.wardance.skill.mementomori.PoundOfFlesh;
import jackiecrazy.wardance.skill.mementomori.ShadowDive;
import jackiecrazy.wardance.skill.regenspirit.*;
import jackiecrazy.wardance.skill.shieldbash.*;
import jackiecrazy.wardance.skill.warcry.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class WarSkills {
    public static DeferredRegister<Skill> SKILLS = DeferredRegister
            .create(Skill.class, WarDance.MODID);

    public static final RegistryObject<Skill> VITAL_STRIKE = SKILLS.register("vital_strike", HeavyBlow::new);
    public static final RegistryObject<Skill> MOMENTUM = SKILLS.register("momentum", Momentum::new);
    public static final RegistryObject<Skill> STAGGER = SKILLS.register("stagger", Stagger::new);
    public static final RegistryObject<Skill> POISE = SKILLS.register("poise", Poise::new);
    public static final RegistryObject<Skill> LEVERAGE = SKILLS.register("leverage", HeavyBlow.Leverage::new);
    public static final RegistryObject<Skill> SILENCER = SKILLS.register("silencer", Silencer::new);
    public static final RegistryObject<Skill> MIKIRI = SKILLS.register("mikiri", Mikiri::new);
    public static final RegistryObject<Skill> RETURN_TO_SENDER = SKILLS.register("return_to_sender", ReturnToSender::new);
    public static final RegistryObject<Skill> AFTERIMAGE = SKILLS.register("afterimage", Afterimage::new);
    public static final RegistryObject<Skill> BACKPEDAL = SKILLS.register("backpedal", Backpedal::new);
    public static final RegistryObject<Skill> OVERPOWER = SKILLS.register("overpower", Overpower::new);
    public static final RegistryObject<Skill> RECOVERY = SKILLS.register("recovery", Recovery::new);
    public static final RegistryObject<Skill> DECAPITATE = SKILLS.register("decapitate", CoupDeGrace::new);
    public static final RegistryObject<Skill> REINVIGORATE = SKILLS.register("reinvigorate", CoupDeGrace.Reinvigorate::new);
    public static final RegistryObject<Skill> DANSE_MACABRE = SKILLS.register("danse_macabre", CoupDeGrace.DanseMacabre::new);
    public static final RegistryObject<Skill> FRENZY = SKILLS.register("frenzy", CoupDeGrace.Frenzy::new);
    public static final RegistryObject<Skill> REAPING = SKILLS.register("reaping", CoupDeGrace.ReapersLaugh::new);
    public static final RegistryObject<Skill> RUPTURE = SKILLS.register("rupture", CoupDeGrace.Rupture::new);
    public static final RegistryObject<Skill> TRAMPLE = SKILLS.register("trample", Kick::new);
    public static final RegistryObject<Skill> SABATON_SMASH = SKILLS.register("sabaton_smash", SabatonSmash::new);
    public static final RegistryObject<Skill> TACKLE = SKILLS.register("tackle", Tackle::new);
    public static final RegistryObject<Skill> BACKFLIP = SKILLS.register("backflip", Kick.Backflip::new);
    public static final RegistryObject<Skill> TORNADO = SKILLS.register("tornado", Tornado::new);
    public static final RegistryObject<Skill> SHADOWLESS_KICK = SKILLS.register("shadowless_kick", ShadowlessKick::new);
    public static final RegistryObject<Skill> THROW = SKILLS.register("throw", Grapple::new);
    public static final RegistryObject<Skill> CLINCH = SKILLS.register("clinch", Clinch::new);
    public static final RegistryObject<Skill> REVERSAL = SKILLS.register("reverse_grip", Grapple.ReverseGrip::new);
    public static final RegistryObject<Skill> SUBMISSION = SKILLS.register("submission", Submission::new);
    public static final RegistryObject<Skill> SUPLEX = SKILLS.register("suplex", Grapple.Suplex::new);
    public static final RegistryObject<Skill> PUMMEL = SKILLS.register("pummel", Pummel::new);
    public static final RegistryObject<Skill> RIM_PUNCH = SKILLS.register("rim_punch", ShieldBash.RimPunch::new);
    public static final RegistryObject<Skill> ARM_LOCK = SKILLS.register("lockdown", ArmLock::new);
    public static final RegistryObject<Skill> FOOT_SLAM = SKILLS.register("foot_slam", ShieldBash.FootSlam::new);
    public static final RegistryObject<Skill> OVERBEAR = SKILLS.register("overbear", Overbear::new);
    public static final RegistryObject<Skill> BERSERK = SKILLS.register("berserk", Berserk::new);
    public static final RegistryObject<Skill> REJUVENATE = SKILLS.register("rejuvenate", WarCry::new);
    public static final RegistryObject<Skill> BOULDER_BRACE = SKILLS.register("boulder_brace", BoulderBrace::new);
    public static final RegistryObject<Skill> WIND_SCAR = SKILLS.register("wind_scar", WindScar::new);
    public static final RegistryObject<Skill> FLAME_DANCE = SKILLS.register("flame_dance", FlameDance::new);
    public static final RegistryObject<Skill> FROST_FANG = SKILLS.register("frost_fang", FrostFang::new);
    public static final RegistryObject<Skill> TIMBERFALL = SKILLS.register("timberfall", Timberfall::new);
    public static final RegistryObject<Skill> CROWN_CHAMPION = SKILLS.register("crown_champion", CrownChampion::new);
    public static final RegistryObject<Skill> VENGEFUL_MIGHT = SKILLS.register("vengeful_might", CrownChampion.VengefulMight::new);
    public static final RegistryObject<Skill> HIDDEN_MIGHT = SKILLS.register("hidden_might", CrownChampion.HiddenMight::new);
    public static final RegistryObject<Skill> PRIDEFUL_MIGHT = SKILLS.register("prideful_might", CrownChampion.PridefulMight::new);
    public static final RegistryObject<Skill> ELEMENTAL_MIGHT = SKILLS.register("elemental_might", CrownChampion.ElementalMight::new);
    public static final RegistryObject<Skill> FOLLOWUP = SKILLS.register("followup", Feint::new);
    public static final RegistryObject<Skill> SPIRIT_RESONANCE = SKILLS.register("spirit_resonance", Feint.SpiritBomb::new);
    public static final RegistryObject<Skill> SMIRKING_SHADOW = SKILLS.register("smirking_shadow", Feint.SmirkingShadow::new);
    public static final RegistryObject<Skill> SCORPION_STING = SKILLS.register("scorpion_sting", Feint.ScorpionSting::new);
    public static final RegistryObject<Skill> UPPER_HAND = SKILLS.register("upper_hand", Feint.UpperHand::new);
    public static final RegistryObject<Skill> CAPRICIOUS_STRIKE = SKILLS.register("capricious_strike", Feint.CapriciousStrike::new);
    public static final RegistryObject<Skill> AMPUTATION = SKILLS.register("amputation", Judgment::new);
    public static final RegistryObject<Skill> CROWD_PLEASER = SKILLS.register("combo_breaker", ComboBreaker::new);
    public static final RegistryObject<Skill> LICHTENBERG_SCAR = SKILLS.register("lichtenberg_scar", LichtenbergScar::new);
    public static final RegistryObject<Skill> FEVER_DREAM = SKILLS.register("fever_dream", FeverDream::new);
    public static final RegistryObject<Skill> VIRAL_DECAY = SKILLS.register("viral_decay", ViralDecay::new);
    public static final RegistryObject<Skill> BRUTALIZE = SKILLS.register("brutalize", Brutalize::new);
    public static final RegistryObject<Skill> BLOODLUST = SKILLS.register("bloodlust", MementoMori::new);
    public static final RegistryObject<Skill> RAPID_CLOTTING = SKILLS.register("rapid_clotting", MementoMori.RapidClotting::new);
    public static final RegistryObject<Skill> SHADOW_DIVE = SKILLS.register("shadow_dive", ShadowDive::new);
    public static final RegistryObject<Skill> DEATH_DENIAL = SKILLS.register("death_denial", DeathDenial::new);
    public static final RegistryObject<Skill> STATIC_DISCHARGE = SKILLS.register("static_discharge", MementoMori.StaticDischarge::new);
    public static final RegistryObject<Skill> POUND_OF_FLESH = SKILLS.register("pound_of_flesh", PoundOfFlesh::new);
    public static final RegistryObject<Skill> CURSE_OF_MISFORTUNE = SKILLS.register("curse_of_misfortune", Hex::new);
    public static final RegistryObject<Skill> CURSE_OF_ECHOES = SKILLS.register("curse_of_echoes", Hex.CurseOfEchoes::new);
    public static final RegistryObject<Skill> ITCHY_CURSE = SKILLS.register("itchy_curse", ItchyCurse::new);
    public static final RegistryObject<Skill> UNRAVEL = SKILLS.register("unravel", Hex.Unravel::new);
    public static final RegistryObject<Skill> GANGRENE = SKILLS.register("gangrene", Hex.Gangrene::new);
    public static final RegistryObject<Skill> BLACK_MARK = SKILLS.register("black_mark", Hex.BlackMark::new);
    public static final RegistryObject<Skill> PETRIFY = SKILLS.register("petrify", Petrify::new);
    public static final RegistryObject<Skill> BACK_AND_FORTH = SKILLS.register("back_and_forth", Morale::new);
    public static final RegistryObject<Skill> ARCHERS_PARADOX = SKILLS.register("archers_paradox", ArchersParadox::new);
    public static final RegistryObject<Skill> APATHY = SKILLS.register("confidence", Confidence::new);
    public static final RegistryObject<Skill> LADY_LUCK = SKILLS.register("lady_luck", LadyLuck::new);
    public static final RegistryObject<Skill> NATURAL_SPRINTER = SKILLS.register("natural_sprinter", NaturalSprinter::new);
    public static final RegistryObject<Skill> SPEED_DEMON = SKILLS.register("speed_demon", SpeedDemon::new);
    //public static final RegistryObject<Skill> WEAPON_THROW = SKILLS.register("weapon_throw", HeavyBlow::new);

}
