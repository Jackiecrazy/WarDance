package jackiecrazy.wardance.advancement;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.items.DummyItem;
import jackiecrazy.wardance.items.WarItems;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillColors;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WarAdvancements {
    public static final SkillCastTrigger SKILL_CAST_TRIGGER = new SkillCastTrigger();
    public static final SkillChallengeTrigger CHALLENGE_ONLY = new SkillChallengeTrigger();
    private static final ResourceLocation ROOT = new ResourceLocation(WarDance.MODID, "root");
    private static final ResourceLocation SKILL = new ResourceLocation(WarDance.MODID, "skillscroll");
    private static final ResourceLocation STYLE = new ResourceLocation(WarDance.MODID, "stylescroll");
    private static final HashMap<SkillCategory, Advancement> last_in_cat = new HashMap<>();
    private static Advancement style;
    private static Advancement root;

    @SubscribeEvent
    public static void advance(FMLCommonSetupEvent e) {
        //criteria
        e.enqueueWork(() -> CriteriaTriggers.register(SKILL_CAST_TRIGGER));
        e.enqueueWork(() -> CriteriaTriggers.register(CHALLENGE_ONLY));
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                event.includeServer(), new ForgeAdvancementProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper(), List.of(
                        (registries, consumer, fileHelper) -> {
                            root=style=buildRoot().save(consumer, ROOT, fileHelper);
                            //initial categories
                            final Set<SkillCategory> color = WarSkills.SKILLS.getEntries().stream().map(RegistryObject::get).map(Skill::getCategory).collect(Collectors.toSet());
                            for (SkillCategory s : color)
                                if (s != SkillColors.none)
                                    last_in_cat.put(s, root);
                            //hook them to root
                            final Set<Skill> set = WarSkills.SKILLS.getEntries().stream().map(RegistryObject::get).filter(Skill::hasChallenge).collect(Collectors.toSet());
                            for (Skill s : set) {
                                final Advancement next = buildAdvancement(s).save(consumer, s.getRegistryName(), fileHelper);
                                if (s instanceof SkillStyle)
                                    style = next;
                                else last_in_cat.put(s.getCategory(), next);
                            }
                            //cap them
                            style = buildStyles().save(consumer, new ResourceLocation(WarDance.MODID, "styles"), fileHelper);
                            for (SkillCategory s : color)
                                if (s != SkillColors.none)
                                    last_in_cat.put(s, buildBase(s).save(consumer, s.baseName(), fileHelper));

                        }
                ))
        );
    }

    @Nonnull
    private static Advancement.Builder buildAdvancement(Skill s) {
        ItemStack display = DummyItem.makeScroll(false, s);
        return Advancement.Builder.advancement()
                .display(display,
                        Component.translatable("advancements.wardance." + s.getRegistryName().getPath() + ".title"),
                        Component.translatable("advancements.wardance." + s.getRegistryName().getPath() + ".description"),
                        null, FrameType.CHALLENGE, true, false, false)
                .addCriterion("skill", SkillChallengeTrigger.TriggerInstance.skill(s))
                .parent(s instanceof SkillStyle ? style : last_in_cat.get(s.getCategory()))
                .rewards(AdvancementRewards.Builder.loot(s instanceof SkillStyle ? STYLE : SKILL).build());
    }

    @Nonnull
    private static Advancement.Builder buildStyles() {
        return Advancement.Builder.advancement()
                .display(WarItems.MANUAL.get(),
                        Component.translatable("advancements.wardance.styles.title"),
                        Component.translatable("advancements.wardance.styles.description"),
                        null, FrameType.TASK, false, false, false)
                .addCriterion("tick", PlayerTrigger.TriggerInstance.tick())
                .parent(style);
    }

    @Nonnull
    private static Advancement.Builder buildRoot() {
        return Advancement.Builder.advancement()
                .display(WarItems.MANUAL.get(),
                        Component.translatable("advancements.wardance.root.title"),
                        Component.translatable("advancements.wardance.root.description"),
                        new ResourceLocation("minecraft:textures/gui/advancements/backgrounds/stone.png"), FrameType.TASK, false, false, false)
                .addCriterion("tick", PlayerTrigger.TriggerInstance.tick());
    }

    @Nonnull
    private static Advancement.Builder buildBase(SkillCategory s) {
        ItemStack display = DummyItem.makeCategory(s);
        return Advancement.Builder.advancement()
                .display(display,
                        s.name(),
                        s.description(),
                        null, FrameType.TASK, false, false, false)
                .addCriterion("tick", PlayerTrigger.TriggerInstance.tick())
                .parent(last_in_cat.get(s));
    }
}
