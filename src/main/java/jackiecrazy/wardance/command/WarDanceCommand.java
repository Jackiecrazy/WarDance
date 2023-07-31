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
import jackiecrazy.wardance.capability.action.PermissionData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.items.ManualItem;
import jackiecrazy.wardance.items.WarItems;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
                                        .executes(WarDanceCommand::resetSkills))
                                .then(Commands.argument("color", CategoryArgument.color())
                                        .executes(WarDanceCommand::getSkillCategory)
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(WarDanceCommand::setSkillCategory)))
                        )
                )
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
                                                .executes(WarDanceCommand::setMight)
                                        )
                                )
                        )
                )
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
                                                .executes(WarDanceCommand::setSpirit)
                                        )
                                )
                        )
                )
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
                                                .executes(WarDanceCommand::setPosture)
                                        )
                                )
                        )
                )
                .then(Commands.literal("stagger")
                        .executes(WarDanceCommand::missingArgument)
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(WarDanceCommand::missingArgument)
                                .then(Commands.argument("time", IntegerArgumentType.integer(0))
                                        .executes(WarDanceCommand::defaultStagger)
                                        .then(Commands.argument("count", IntegerArgumentType.integer())
                                                .executes(WarDanceCommand::stagger)
                                        )
                                )
                        )
                )
                .then(Commands.literal("manualize")
                        .executes(WarDanceCommand::manualize)
                        .then(Commands.argument("autolearn", BoolArgumentType.bool())
                                .executes(WarDanceCommand::manualize)
                        )
                )
                .then(Commands.literal("toggle")
                        .executes(WarDanceCommand::missingArgument)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(WarDanceCommand::missingArgument)
                                .then(Commands.literal("parry")
                                        .executes(a -> WarDanceCommand.getPermission(a, Permission.PARRY))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(a -> WarDanceCommand.setPermission(a, Permission.PARRY))
                                        )
                                )
                                .then(Commands.literal("posture")
                                        .executes(a -> WarDanceCommand.getPermission(a, Permission.POSTURE))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(a -> WarDanceCommand.setPermission(a, Permission.POSTURE))
                                        )
                                )
                                .then(Commands.literal("skill")
                                        .executes(a -> WarDanceCommand.getPermission(a, Permission.SKILL))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(a -> WarDanceCommand.setPermission(a, Permission.SKILL))
                                        )
                                )
                                .then(Commands.literal("combat")
                                        .executes(a -> WarDanceCommand.getPermission(a, Permission.COMBAT))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(a -> WarDanceCommand.setPermission(a, Permission.COMBAT))
                                        )
                                )
                                .then(Commands.literal("sweep")
                                        .executes(a -> WarDanceCommand.getPermission(a, Permission.SWEEP))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(a -> WarDanceCommand.setPermission(a, Permission.SWEEP))
                                        )
                                )
                        )
                );
        dispatcher.register(builder);
    }

    private static int manualize(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayer();
        if (player == null) throw EntitySelectorOptions.ERROR_INAPPLICABLE_OPTION.create(null);
        boolean autoLearn = BoolArgumentType.getBool(ctx, "autolearn");
        ItemStack give = new ItemStack(WarItems.MANUAL.get());
        ((ManualItem) give.getItem()).setSkill(give, CasterData.getCap(player).getEquippedSkillsAndStyle());
        ((ManualItem) give.getItem()).setAutoLearn(give, autoLearn);
        player.addItem(give);
        return Command.SINGLE_SUCCESS;
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
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.setSkill" + (CasterData.getCap(player).isSkillSelectable(skill)), player.getDisplayName(), skill.getDisplayName(null)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getSkill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        final Skill skill = ctx.getArgument("skill", Skill.class);
        final boolean enabled = CasterData.getCap(player).isSkillSelectable(skill);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.getSkill" + (CasterData.getCap(player).isSkillSelectable(skill)), player.getDisplayName(), skill.getDisplayName(null)), false);
        return enabled ? 1 : 0;
    }

    private static int resetSkills(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        CasterData.getCap(player).getSelectableList().clear();
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.clearSkill" + (player.level.getGameRules().getBoolean(WarDance.GATED_SKILLS)), player.getDisplayName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setSkillCategory(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        final SkillCategory skill = ctx.getArgument("color", SkillCategory.class);
        final boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        for (Skill s : Skill.categoryMap.get(skill))
            CasterData.getCap(player).setSkillSelectable(s, enabled);
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.setSkillCategory" + (enabled), player.getDisplayName(), skill.name()), false);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * tells you how many skills you can select in a given category
     */
    private static int getSkillCategory(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        final SkillCategory cat = ctx.getArgument("color", SkillCategory.class);
        int ret = 0;
        for (Skill s : Skill.categoryMap.get(cat))
            if (CasterData.getCap(player).isSkillSelectable(s))
                ret++;
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.getSkillCategory", player.getDisplayName(), ret, cat.name()), false);
        return ret;
    }

    private static int getPermission(CommandContext<CommandSourceStack> ctx, Permission permission) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        boolean enabled = true;
        switch (permission) {
            case PARRY -> enabled = PermissionData.getCap(player).canParry();
            case SKILL -> enabled = PermissionData.getCap(player).canSelectSkills();
            case COMBAT -> enabled = PermissionData.getCap(player).canEnterCombatMode();
            case POSTURE -> enabled = PermissionData.getCap(player).canDealPostureDamage();
            case SWEEP -> enabled = PermissionData.getCap(player).canSweep();
        }
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.permission." + permission.name() + "." + enabled, player.getDisplayName()), false);

        return enabled ? 1 : 0;
    }

    private static int setPermission(CommandContext<CommandSourceStack> ctx, Permission permission) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        final boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        switch (permission) {
            case PARRY -> PermissionData.getCap(player).setParry(enabled);
            case SKILL -> PermissionData.getCap(player).setSkill(enabled);
            case COMBAT -> PermissionData.getCap(player).setCombat(enabled);
            case POSTURE -> PermissionData.getCap(player).setPosture(enabled);
            case SWEEP -> PermissionData.getCap(player).setSweep(enabled);
        }
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.permission." + permission.name() + "." + enabled, player.getDisplayName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private enum Permission {
        PARRY,
        POSTURE,
        COMBAT,
        SKILL,
        SWEEP
    }

}