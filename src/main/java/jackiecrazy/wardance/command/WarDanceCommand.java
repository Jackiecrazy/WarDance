package jackiecrazy.wardance.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class WarDanceCommand {

    public static final SimpleCommandExceptionType MISSING_ARGUMENT = new SimpleCommandExceptionType(Component.translatable("wardance.command.missing"));

    public static int missingArgument(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        throw MISSING_ARGUMENT.create();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("wardance")
                .requires(s -> s.hasPermission(2))
                .executes(WarDanceCommand::missingArgument)
                .then(Commands.literal("skill")
                        .executes(WarDanceCommand::missingArgument)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(WarDanceCommand::missingArgument)
                                .then(Commands.argument("skill", SkillArgument.skill())
                                        .executes(WarDanceCommand::getSkill)
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(WarDanceCommand::setSkill)))
                                .then(Commands.literal("reset")
                                        .executes(WarDanceCommand::resetSkills))))
                .then(Commands.literal("might")
                        .executes(WarDanceCommand::missingArgument)
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(WarDanceCommand::getMight)
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(WarDanceCommand::missingArgument)
                                        .then(Commands.literal("add")
                                                .executes(WarDanceCommand::addMight))
                                        .then(Commands.literal("consume")
                                                .executes(WarDanceCommand::consumeMight))
                                        .then(Commands.literal("set")
                                                .executes(WarDanceCommand::setMight)))))
                .then(Commands.literal("spirit")
                        .executes(WarDanceCommand::missingArgument)
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(WarDanceCommand::getSpirit)
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(WarDanceCommand::missingArgument)
                                        .then(Commands.literal("add")
                                                .executes(WarDanceCommand::addSpirit))
                                        .then(Commands.literal("consume")
                                                .executes(WarDanceCommand::consumeSpirit))
                                        .then(Commands.literal("set")
                                                .executes(WarDanceCommand::setSpirit)))))
                .then(Commands.literal("posture")
                        .executes(WarDanceCommand::missingArgument)
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(WarDanceCommand::getPosture)
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(WarDanceCommand::missingArgument)
                                        .then(Commands.literal("add")
                                                .executes(WarDanceCommand::addPosture))
                                        .then(Commands.literal("consume")
                                                .executes(WarDanceCommand::consumePosture))
                                        .then(Commands.literal("set")
                                                .executes(WarDanceCommand::setPosture)))))
                .then(Commands.literal("stagger")
                        .executes(WarDanceCommand::missingArgument)
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(WarDanceCommand::missingArgument)
                                .then(Commands.argument("time", IntegerArgumentType.integer(0))
                                        .executes(WarDanceCommand::defaultStagger)
                                        .then(Commands.argument("count", IntegerArgumentType.integer())
                                                .executes(WarDanceCommand::stagger)))));
        dispatcher.register(builder);
    }

    private static int stagger(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        int time = IntegerArgumentType.getInteger(ctx, "time");
        int count = IntegerArgumentType.getInteger(ctx, "count");
        CombatData.getCap((LivingEntity) player).stun(time);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.command.stagger", player.getDisplayName(), time, count), false);
        return Command.SINGLE_SUCCESS;
    }

    //TODO expose command
    private static int defaultStagger(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        int time = IntegerArgumentType.getInteger(ctx, "time");
        CombatData.getCap((LivingEntity) player).stun(time);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.staggerDefault", player.getDisplayName(), time), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setMight(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).setMight(i);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.setMight", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int consumeMight(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).consumeMight(i);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.conMight", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addMight(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addMight(i);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.addMight", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getMight(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float might = CombatData.getCap((LivingEntity) player).getMight();
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.getMight", player.getDisplayName(), might), false);
        return Math.round(might);
    }

    private static int setSpirit(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).setSpirit(i);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.setSpirit", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int consumeSpirit(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).consumeSpirit(i);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.conSpirit", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addSpirit(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addSpirit(i);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.addSpirit", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getSpirit(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float spirit = CombatData.getCap((LivingEntity) player).getSpirit();
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.getSpirit", player.getDisplayName(), spirit), false);
        return Math.round(spirit);
    }

    private static int setPosture(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).setPosture(i);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.setPosture", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int consumePosture(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).consumePosture(i);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.conPosture", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addPosture(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addPosture(i);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.addPosture", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getPosture(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float posture = CombatData.getCap((LivingEntity) player).getPosture();
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.getPosture", player.getDisplayName(), posture), false);
        return Math.round(posture);
    }

    private static int setSkill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        final Skill skill = ctx.getArgument("skill", Skill.class);
        final boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        CasterData.getCap(player).setSkillSelectable(skill, enabled);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.setSkill"+(CasterData.getCap(player).isSkillSelectable(skill)), player.getDisplayName(), skill.getDisplayName(null)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getSkill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        final Skill skill = ctx.getArgument("skill", Skill.class);
        final boolean enabled = CasterData.getCap(player).isSkillSelectable(skill);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.getSkill"+(CasterData.getCap(player).isSkillSelectable(skill)), player.getDisplayName(), skill.getDisplayName(null)), false);
        return enabled ? 1 : 0;
    }

    private static int resetSkills(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        CasterData.getCap(player).getSelectableList().clear();
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.clearSkill"+(player.level.getGameRules().getBoolean(WarDance.GATED_SKILLS)), player.getDisplayName()), false);
        return Command.SINGLE_SUCCESS;
    }

}