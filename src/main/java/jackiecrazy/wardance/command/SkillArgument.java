package jackiecrazy.wardance.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkillArgument implements ArgumentType<Skill> {

    public static final SuggestionProvider<CommandSourceStack> SKILLS = SuggestionProviders
            .register(new ResourceLocation(WarDance.MODID, "skills"),
                    (context, builder) -> SharedSuggestionProvider.suggestResource(
                            GameRegistry.findRegistry(Skill.class).getValues().stream(),
                            builder, Skill::getRegistryName, (type) -> new TranslatableComponent(
                                    Util.makeDescriptionId("skill", type.getRegistryName()))));
    private static final Collection<String> EXAMPLES = Stream.of(WarSkills.RETURN_TO_SENDER.get(), WarSkills.VITAL_STRIKE.get()).map((worldKey) -> worldKey.getRegistryName().toString()).collect(Collectors.toList());
    private static final DynamicCommandExceptionType INVALID_SKILL_EXCEPTION = new DynamicCommandExceptionType((worldKey) -> new TranslatableComponent("argument.skill.invalid", worldKey));

    public static SkillArgument skill() {
        return new SkillArgument();
    }

    @Override
    public Skill parse(StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final ResourceLocation skill=ResourceLocation.read(reader);
        final Skill result = Skill.getSkill(skill);
        if (result == null) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }
        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_listSuggestions_1_, SuggestionsBuilder p_listSuggestions_2_) {
        return SharedSuggestionProvider.suggestResource(GameRegistry.findRegistry(Skill.class).getKeys(), p_listSuggestions_2_);
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }
}
