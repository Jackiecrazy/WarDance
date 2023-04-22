package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.coupdegrace.BiteTheDust;
import jackiecrazy.wardance.skill.coupdegrace.CoupDeGrace;
import jackiecrazy.wardance.skill.coupdegrace.ShieldCrush;
import jackiecrazy.wardance.skill.crownchampion.CrownChampion;
import jackiecrazy.wardance.skill.feint.Feint;
import jackiecrazy.wardance.skill.fiveelementfist.*;
import jackiecrazy.wardance.skill.flurry.Flurry;
import jackiecrazy.wardance.skill.flurry.Montante;
import jackiecrazy.wardance.skill.grapple.*;
import jackiecrazy.wardance.skill.heavyblow.HeavyBlow;
import jackiecrazy.wardance.skill.heavyblow.Momentum;
import jackiecrazy.wardance.skill.heavyblow.Silencer;
import jackiecrazy.wardance.skill.heavyblow.Stagger;
import jackiecrazy.wardance.skill.hex.Hex;
import jackiecrazy.wardance.skill.hex.ItchyCurse;
import jackiecrazy.wardance.skill.hex.Petrify;
import jackiecrazy.wardance.skill.ironguard.*;
import jackiecrazy.wardance.skill.judgment.ViralDecay;
import jackiecrazy.wardance.skill.kick.*;
import jackiecrazy.wardance.skill.mementomori.MementoMori;
import jackiecrazy.wardance.skill.mementomori.Necrosis;
import jackiecrazy.wardance.skill.mementomori.PoundOfFlesh;
import jackiecrazy.wardance.skill.mementomori.ShadowDive;
import jackiecrazy.wardance.skill.regenspirit.ArchersParadox;
import jackiecrazy.wardance.skill.regenspirit.LadyLuck;
import jackiecrazy.wardance.skill.regenspirit.NaturalSprinter;
import jackiecrazy.wardance.skill.shieldbash.*;
import jackiecrazy.wardance.skill.styles.one.Survivor;
import jackiecrazy.wardance.skill.styles.two.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class WarSkills {

    public static ResourceLocation REGISTRY_NAME = new ResourceLocation(WarDance.MODID, "skills");
    public static Supplier<IForgeRegistry<Skill>> SUPPLIER;
    public static DeferredRegister<Skill> SKILLS = DeferredRegister.create(REGISTRY_NAME, WarDance.MODID);

    //===========================================================================================================//
    //                                                      Styles
    //===========================================================================================================//
    public static final RegistryObject<SkillStyle> SURVIVOR = SKILLS.register("survivor", Survivor::new);
    //public static final RegistryObject<Skill> REJUVENATE = SKILLS.register("rejuvenate", () -> new WarCry().setCategory(SkillColors.white));
    public static final RegistryObject<Skill> BOULDER_BRACE = SKILLS.register("boulder_brace", () -> new BoulderBrace().setColor(Color.GREEN));
    public static final RegistryObject<Skill> WIND_SCAR = SKILLS.register("wind_scar", () -> new WindScar().setColor(Color.CYAN));
    public static final RegistryObject<Skill> FLAME_DANCE = SKILLS.register("flame_dance", () -> new FlameDance().setColor(Color.ORANGE));
    public static final RegistryObject<Skill> FROST_FANG = SKILLS.register("frost_fang", () -> new FrostFang().setColor(Color.LIGHT_GRAY));
    public static final RegistryObject<Skill> TIMBERFALL = SKILLS.register("timberfall", () -> new Timberfall().setColor(Color.RED));


    //===========================================================================================================//
    //                                                      Skills
    //===========================================================================================================//
    public static final RegistryObject<Skill> VITAL_STRIKE = SKILLS.register("vital_strike", () -> new HeavyBlow().setCategory(SkillColors.white));
    public static final RegistryObject<Skill> MOMENTUM = SKILLS.register("momentum", () -> new Momentum().setCategory(SkillColors.orange));
    public static final RegistryObject<Skill> STAGGER = SKILLS.register("stagger", () -> new Stagger().setCategory(SkillColors.red));
    //public static final RegistryObject<Skill> POISE = SKILLS.register("poise", () -> new Poise().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> LEVERAGE = SKILLS.register("leverage", () -> new HeavyBlow.Leverage().setCategory(SkillColors.cyan));
    public static final RegistryObject<Skill> SILENCER = SKILLS.register("silencer", () -> new Silencer().setCategory(SkillColors.gray));
    public static final RegistryObject<Skill> MIKIRI = SKILLS.register("mikiri", () -> new Mikiri().setCategory(SkillColors.orange));
    public static final RegistryObject<Skill> RETURN_TO_SENDER = SKILLS.register("return_to_sender", () -> new ReturnToSender().setCategory(SkillColors.white));
    public static final RegistryObject<Skill> AFTERIMAGE = SKILLS.register("afterimage", () -> new Afterimage().setCategory(SkillColors.gray));
    public static final RegistryObject<Skill> BACKPEDAL = SKILLS.register("backpedal", () -> new Backpedal().setCategory(SkillColors.cyan));
    public static final RegistryObject<Skill> OVERPOWER = SKILLS.register("overpower", () -> new Overpower().setCategory(SkillColors.red));
    //public static final RegistryObject<Skill> RECOVERY = SKILLS.register("recovery", () -> new Recovery().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> DECAPITATE = SKILLS.register("decapitate", () -> new CoupDeGrace().setCategory(SkillColors.white));
    public static final RegistryObject<Skill> REINVIGORATE = SKILLS.register("reinvigorate", () -> new BiteTheDust().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> DANSE_MACABRE = SKILLS.register("danse_macabre", () -> new CoupDeGrace.DanseMacabre().setCategory(SkillColors.gray));
    public static final RegistryObject<Skill> FRENZY = SKILLS.register("frenzy", () -> new CoupDeGrace.Frenzy().setCategory(SkillColors.orange));
    public static final RegistryObject<Skill> REAPING = SKILLS.register("reaping", () -> new CoupDeGrace.ReapersLaugh().setCategory(SkillColors.red));
    //public static final RegistryObject<Skill> RUPTURE = SKILLS.register("rupture", () -> new CoupDeGrace.Rupture().setCategory(SkillColors.cyan));
    public static final RegistryObject<Skill> TRAMPLE = SKILLS.register("trample", () -> new Kick().setCategory(SkillColors.white));
    public static final RegistryObject<Skill> SABATON_SMASH = SKILLS.register("sabaton_smash", () -> new SabatonSmash().setCategory(SkillColors.red));
    public static final RegistryObject<Skill> TACKLE = SKILLS.register("tackle", () -> new Tackle().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> BACKFLIP = SKILLS.register("backflip", () -> new Kick.Backflip().setCategory(SkillColors.gray));
    public static final RegistryObject<Skill> TORNADO = SKILLS.register("tornado", () -> new Tornado().setCategory(SkillColors.cyan));
    public static final RegistryObject<Skill> SHADOWLESS_KICK = SKILLS.register("shadowless_kick", () -> new ShadowlessKick().setCategory(SkillColors.orange));
    public static final RegistryObject<Skill> THROW = SKILLS.register("throw", () -> new Throw().setCategory(SkillColors.cyan));
    public static final RegistryObject<Skill> WRESTLE = SKILLS.register("wrestle", () -> new Wrestle().setCategory(SkillColors.orange));
    //public static final RegistryObject<Skill> REVERSAL = SKILLS.register("reverse_grip", () -> new Grapple.ReverseGrip().setCategory(SkillColors.gray));
    public static final RegistryObject<Skill> SUBMISSION = SKILLS.register("submission", () -> new Submission().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> CURSED_PALMS = SKILLS.register("cursed_palms", () -> new CursedPalms().setCategory(SkillColors.purple));
    public static final RegistryObject<Skill> SUPLEX = SKILLS.register("suplex", () -> new Grapple.Suplex().setCategory(SkillColors.red));
    public static final RegistryObject<Skill> PUMMEL = SKILLS.register("pummel", () -> new Pummel().setCategory(SkillColors.white));
    public static final RegistryObject<Skill> RIM_PUNCH = SKILLS.register("rim_punch", () -> new ShieldBash.RimPunch().setCategory(SkillColors.cyan));
    public static final RegistryObject<Skill> ARM_LOCK = SKILLS.register("lockdown", () -> new ArmLock().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> FOOT_SLAM = SKILLS.register("foot_slam", () -> new ShieldBash.FootSlam().setCategory(SkillColors.gray));
    public static final RegistryObject<Skill> OVERBEAR = SKILLS.register("overbear", () -> new Overbear().setCategory(SkillColors.red));
    public static final RegistryObject<Skill> BERSERK = SKILLS.register("berserk", () -> new Berserk().setCategory(SkillColors.orange));
    public static final RegistryObject<Skill> CROWN_CHAMPION = SKILLS.register("crown_champion", () -> new CrownChampion().setCategory(SkillColors.white));
    public static final RegistryObject<Skill> VENGEFUL_MIGHT = SKILLS.register("vengeful_might", () -> new CrownChampion.VengefulMight().setCategory(SkillColors.green));
    //public static final RegistryObject<Skill> HIDDEN_MIGHT = SKILLS.register("hidden_might", () -> new CrownChampion.HiddenMight().setCategory(SkillColors.gray));
    public static final RegistryObject<Skill> PRIDEFUL_MIGHT = SKILLS.register("prideful_might", () -> new CrownChampion.PridefulMight().setCategory(SkillColors.orange));
    //public static final RegistryObject<Skill> ELEMENTAL_MIGHT = SKILLS.register("elemental_might", () -> new CrownChampion.ElementalMight().setCategory(SkillColors.purple));
    public static final RegistryObject<Skill> FOLLOWUP = SKILLS.register("followup", () -> new Feint().setCategory(SkillColors.white));
    public static final RegistryObject<Skill> SPIRIT_RESONANCE = SKILLS.register("spirit_resonance", () -> new Feint().setCategory(SkillColors.orange));
    public static final RegistryObject<Skill> SMIRKING_SHADOW = SKILLS.register("smirking_shadow", () -> new Feint().setCategory(SkillColors.gray));
    //public static final RegistryObject<Skill> SCORPION_STING = SKILLS.register("scorpion_sting", () -> new Feint.ScorpionSting().setCategory(SkillColors.purple));
    public static final RegistryObject<Skill> UPPER_HAND = SKILLS.register("upper_hand", () -> new Feint.UpperHand().setCategory(SkillColors.green));
    //public static final RegistryObject<Skill> CAPRICIOUS_STRIKE = SKILLS.register("capricious_strike", () -> new Feint().setCategory(SkillColors.orange));
    //public static final RegistryObject<Skill> AMPUTATION = SKILLS.register("amputation", ()-> new  Judgment().setCategory(SkillColors.));
    //public static final RegistryObject<Skill> CROWD_PLEASER = SKILLS.register("combo_breaker", ()-> new  ComboBreaker().setCategory(SkillColors.));
    //public static final RegistryObject<Skill> LICHTENBERG_SCAR = SKILLS.register("lichtenberg_scar", ()-> new  LichtenbergScar().setCategory(SkillColors.));
    //public static final RegistryObject<Skill> FEVER_DREAM = SKILLS.register("fever_dream", ()-> new  FeverDream().setCategory(SkillColors.));
    public static final RegistryObject<Skill> VIRAL_DECAY = SKILLS.register("viral_decay", () -> new ViralDecay().setCategory(SkillColors.purple));
    //public static final RegistryObject<Skill> BRUTALIZE = SKILLS.register("brutalize", ()-> new  Brutalize().setCategory(SkillColors.));
    //public static final RegistryObject<Skill> BLOODLUST = SKILLS.register("bloodlust", () -> new MementoMori().setCategory(SkillColors.white));
    public static final RegistryObject<Skill> RAPID_CLOTTING = SKILLS.register("rapid_clotting", () -> new MementoMori.RapidClotting().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> SHADOW_DIVE = SKILLS.register("shadow_dive", () -> new ShadowDive().setCategory(SkillColors.gray));
    public static final RegistryObject<Skill> NECROSIS = SKILLS.register("necrosis", () -> new Necrosis().setCategory(SkillColors.orange));
    public static final RegistryObject<Skill> STATIC_DISCHARGE = SKILLS.register("static_discharge", () -> new MementoMori.StaticDischarge().setCategory(SkillColors.cyan));
    public static final RegistryObject<Skill> POUND_OF_FLESH = SKILLS.register("pound_of_flesh", () -> new PoundOfFlesh().setCategory(SkillColors.red));
    public static final RegistryObject<Skill> CURSE_OF_MISFORTUNE = SKILLS.register("curse_of_misfortune", () -> new Hex().setCategory(SkillColors.purple));
    public static final RegistryObject<Skill> CURSE_OF_ECHOES = SKILLS.register("curse_of_echoes", () -> new Hex.CurseOfEchoes().setCategory(SkillColors.purple));
    public static final RegistryObject<Skill> ITCHY_CURSE = SKILLS.register("itchy_curse", () -> new ItchyCurse().setCategory(SkillColors.purple));
    public static final RegistryObject<Skill> UNRAVEL = SKILLS.register("unravel", () -> new Hex.Unravel().setCategory(SkillColors.purple));
    public static final RegistryObject<Skill> GANGRENE = SKILLS.register("gangrene", () -> new Hex.Gangrene().setCategory(SkillColors.purple));
    //public static final RegistryObject<Skill> BLACK_MARK = SKILLS.register("black_mark", () -> new Hex().setCategory(SkillColors.purple));
    public static final RegistryObject<Skill> PETRIFY = SKILLS.register("petrify", () -> new Petrify().setCategory(SkillColors.purple));
    //public static final RegistryObject<Skill> BACK_AND_FORTH = SKILLS.register("back_and_forth", () -> new Morale().setCategory(SkillColors.white));
    public static final RegistryObject<Skill> ARCHERS_PARADOX = SKILLS.register("archers_paradox", () -> new ArchersParadox().setCategory(SkillColors.cyan));
    //public static final RegistryObject<Skill> APATHY = SKILLS.register("confidence", () -> new Confidence().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> LADY_LUCK = SKILLS.register("lady_luck", () -> new LadyLuck().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> NATURAL_SPRINTER = SKILLS.register("natural_sprinter", () -> new NaturalSprinter().setCategory(SkillColors.red));
    //public static final RegistryObject<Skill> SPEED_DEMON = SKILLS.register("speed_demon", () -> new SpeedDemon().setCategory(SkillColors.gray));
    //public static final RegistryObject<Skill> WEAPON_THROW = SKILLS.register("weapon_throw", ()-> new  HeavyBlow().setCategory(SkillColors.));
    public static final RegistryObject<Skill> FLURRY = SKILLS.register("flurry", () -> new Flurry().setCategory(SkillColors.orange));
    public static final RegistryObject<Skill> MONTANTE = SKILLS.register("montante", () -> new Montante().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> SHIELD_CRUSH = SKILLS.register("shield_crush", () -> new ShieldCrush().setCategory(SkillColors.green));
    public static final RegistryObject<Skill> IRON_CHOP = SKILLS.register("iron_chop", () -> new IronChop().setCategory(SkillColors.gray));
    public static final RegistryObject<Skill> WATER_UPPERCUT = SKILLS.register("water_uppercut", () -> new WaterUppercut().setCategory(SkillColors.cyan));
    public static final RegistryObject<Skill> WOODEN_JAB = SKILLS.register("wooden_jab", () -> new WoodenJab().setCategory(SkillColors.orange));
    public static final RegistryObject<Skill> FIERY_LUNGE = SKILLS.register("fiery_lunge", () -> new FieryLunge().setCategory(SkillColors.red));
    public static final RegistryObject<Skill> EARTHEN_SWEEP = SKILLS.register("earthen_sweep", () -> new EarthenSweep().setCategory(SkillColors.green));

}
