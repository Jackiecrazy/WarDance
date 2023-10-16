package jackiecrazy.wardance.advancement;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.client.SkillModelProvider;
import jackiecrazy.wardance.items.DummyItem;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import jackiecrazy.wardance.skill.styles.SkillStyle;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.FrameType;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = WarDance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WarAdvancements {
    public static final SkillCastTrigger SKILL_CAST_TRIGGER = new SkillCastTrigger();
    public static final SkillChallengeTrigger CHALLENGE_ONLY = new SkillChallengeTrigger();
    private static final ResourceLocation ROOT = new ResourceLocation(WarDance.MODID, "root");
    private static final ResourceLocation SKILL = new ResourceLocation(WarDance.MODID, "skillscroll");
    private static final ResourceLocation STYLE = new ResourceLocation(WarDance.MODID, "stylescroll");

    @SubscribeEvent
    public static void advance(FMLCommonSetupEvent e) {
        //criteria
        e.enqueueWork(() -> CriteriaTriggers.register(SKILL_CAST_TRIGGER));
        e.enqueueWork(() -> CriteriaTriggers.register(CHALLENGE_ONLY));
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                event.includeServer(), new AdvancementProvider(event.getGenerator(), event.getExistingFileHelper()) {
                    @Override
                    protected void registerAdvancements(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper fileHelper) {
                        final Set<Skill> set = WarSkills.SKILLS.getEntries().stream().map(RegistryObject::get).filter(Skill::hasChallenge).collect(Collectors.toSet());
                        for (Skill s : set)
                            buildAdvancement(s).save(consumer, s.getRegistryName(), fileHelper);
                    }
                }
        );
        event.getGenerator().addProvider(
                event.includeClient(), new SkillModelProvider(event.getGenerator(), WarDance.MODID, event.getExistingFileHelper())
        );
    }

    //todo give them icons that match their skill after figuring out bewlr
    @Nonnull
    private static Advancement.Builder buildAdvancement(Skill s) {
        ItemStack display = DummyItem.makeScroll(false, s);
        return Advancement.Builder.advancement()
                .display(display,
                        Component.translatable("advancements.wardance." + s.getRegistryName().getPath() + ".title"),
                        Component.translatable("advancements.wardance." + s.getRegistryName().getPath() + ".description"),
                        null, FrameType.CHALLENGE, true, false, false)
                .addCriterion("skill", SkillChallengeTrigger.TriggerInstance.skill(s))
                .parent(ROOT)
                .rewards(AdvancementRewards.Builder.loot(s instanceof SkillStyle ? STYLE : SKILL).build());
    }
}
