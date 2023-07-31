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
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillColors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CategoryArgument implements ArgumentType<SkillCategory> {

    public static final SuggestionProvider<CommandSourceStack> COLORS = SuggestionProviders
            .register(new ResourceLocation(WarDance.MODID, "skill_categories"),
                    (context, builder) -> SharedSuggestionProvider.suggest(
                            Skill.categoryMap.keySet().stream().map(SkillCategory::rawName),
                            builder));//, Skill::getRegistryName, (type) -> Component.translatable(Util.makeDescriptionId("skill", type))));
    private static final Collection<String> EXAMPLES = Stream.of(SkillColors.green.rawName(), SkillColors.red.rawName()).toList();
    private static final DynamicCommandExceptionType INVALID_SKILL_EXCEPTION = new DynamicCommandExceptionType((worldKey) -> Component.translatable("argument.category.invalid", worldKey));

    public static CategoryArgument color() {
        return new CategoryArgument();
    }

    @Override
    public SkillCategory parse(StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        String str = reader.readString();
        SkillCategory result = SkillCategory.fromString(str);
        if (result == null) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }
        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_listSuggestions_1_, SuggestionsBuilder p_listSuggestions_2_) {
        return SharedSuggestionProvider.suggest(Skill.categoryMap.keySet().stream().map(SkillCategory::rawName), p_listSuggestions_2_);
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }
}
