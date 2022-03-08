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
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.skill.Skill;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntityOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class WarDanceCommand {

    public static final SimpleCommandExceptionType MISSING_ARGUMENT = new SimpleCommandExceptionType(new TranslationTextComponent("wardance.command.missing"));

    public static int missingArgument(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        throw MISSING_ARGUMENT.create();
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("wardance")
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
                .then(Commands.literal("wounding")
                        .executes(WarDanceCommand::missingArgument)
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(WarDanceCommand::getWounding)
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(WarDanceCommand::missingArgument)
                                        .then(Commands.literal("add")
                                                .executes(WarDanceCommand::addWounding))
                                        .then(Commands.literal("consume")
                                                .executes(WarDanceCommand::consumeWounding))
                                        .then(Commands.literal("set")
                                                .executes(WarDanceCommand::setWounding)))))
                .then(Commands.literal("fatigue")
                        .executes(WarDanceCommand::missingArgument)
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(WarDanceCommand::getFatigue)
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(WarDanceCommand::missingArgument)
                                        .then(Commands.literal("add")
                                                .executes(WarDanceCommand::addFatigue))
                                        .then(Commands.literal("consume")
                                                .executes(WarDanceCommand::consumeFatigue))
                                        .then(Commands.literal("set")
                                                .executes(WarDanceCommand::setFatigue)))))
                .then(Commands.literal("burnout")
                        .executes(WarDanceCommand::missingArgument)
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(WarDanceCommand::getBurnout)
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(WarDanceCommand::missingArgument)
                                        .then(Commands.literal("add")
                                                .executes(WarDanceCommand::addBurnout))
                                        .then(Commands.literal("consume")
                                                .executes(WarDanceCommand::consumeBurnout))
                                        .then(Commands.literal("set")
                                                .executes(WarDanceCommand::setBurnout)))))
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

    private static int stagger(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        int time = IntegerArgumentType.getInteger(ctx, "time");
        int count = IntegerArgumentType.getInteger(ctx, "count");
        CombatData.getCap((LivingEntity) player).setStaggerTime(time);
        CombatData.getCap((LivingEntity) player).setStaggerCount(count);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.command.stagger", player.getDisplayName(), time, count), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int defaultStagger(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        int time = IntegerArgumentType.getInteger(ctx, "time");
        CombatData.getCap((LivingEntity) player).setStaggerTime(time);
        CombatData.getCap((LivingEntity) player).setStaggerCount(0);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.staggerDefault", player.getDisplayName(), time), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setMight(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).setMight(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.setMight", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int consumeMight(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).consumeMight(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.conMight", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addMight(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addMight(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.addMight", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getMight(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float might = CombatData.getCap((LivingEntity) player).getMight();
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.getMight", player.getDisplayName(), might), false);
        return Math.round(might);
    }

    private static int setSpirit(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).setSpirit(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.setSpirit", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int consumeSpirit(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).consumeSpirit(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.conSpirit", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addSpirit(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addSpirit(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.addSpirit", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getSpirit(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float spirit = CombatData.getCap((LivingEntity) player).getSpirit();
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.getSpirit", player.getDisplayName(), spirit), false);
        return Math.round(spirit);
    }

    private static int setPosture(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).setPosture(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.setPosture", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int consumePosture(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).consumePosture(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.conPosture", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addPosture(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addPosture(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.addPosture", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getPosture(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float posture = CombatData.getCap((LivingEntity) player).getPosture();
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.getPosture", player.getDisplayName(), posture), false);
        return Math.round(posture);
    }

    private static int setWounding(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).setWounding(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.setWounding", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int consumeWounding(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addWounding(-i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.conWounding", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addWounding(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addWounding(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.addWounding", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getWounding(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float wounding = CombatData.getCap((LivingEntity) player).getWounding();
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.getWounding", player.getDisplayName(), wounding), false);
        return Math.round(wounding);
    }

    private static int setFatigue(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).setFatigue(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.setFatigue", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int consumeFatigue(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addFatigue(-i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.conFatigue", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addFatigue(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addFatigue(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.addFatigue", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getFatigue(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float fatigue = CombatData.getCap((LivingEntity) player).getFatigue();
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.getFatigue", player.getDisplayName(), fatigue), false);
        return Math.round(fatigue);
    }

    private static int setBurnout(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).setBurnout(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.setBurnout", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int consumeBurnout(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addBurnout(-i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.conBurnout", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addBurnout(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float i = FloatArgumentType.getFloat(ctx, "amount");
        CombatData.getCap((LivingEntity) player).addBurnout(i);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.addBurnout", player.getDisplayName(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getBurnout(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Entity player = EntityArgument.getEntity(ctx, "entity");
        if (!(player instanceof LivingEntity)) throw EntityOptions.ERROR_INAPPLICABLE_OPTION.create(player);
        float burnout = CombatData.getCap((LivingEntity) player).getBurnout();
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.getBurnout", player.getDisplayName(), burnout), false);
        return Math.round(burnout);
    }

    private static int setSkill(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        final Skill skill = ctx.getArgument("skill", Skill.class);
        final boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        CasterData.getCap(player).setSkillSelectable(skill, enabled);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.setSkill"+(CasterData.getCap(player).isSkillSelectable(skill)), player.getDisplayName(), skill.getDisplayName(null)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getSkill(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        final Skill skill = ctx.getArgument("skill", Skill.class);
        final boolean enabled = CasterData.getCap(player).isSkillSelectable(skill);
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.getSkill"+(CasterData.getCap(player).isSkillSelectable(skill)), player.getDisplayName(), skill.getDisplayName(null)), false);
        return enabled ? 1 : 0;
    }

    private static int resetSkills(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        CasterData.getCap(player).getSelectableList().clear();
        ctx.getSource().sendSuccess(new TranslationTextComponent("wardance.command.clearSkill"+(player.level.getGameRules().getBoolean(WarDance.GATED_SKILLS)), player.getDisplayName()), false);
        return Command.SINGLE_SUCCESS;
    }

}