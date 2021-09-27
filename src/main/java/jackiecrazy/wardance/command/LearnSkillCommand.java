package jackiecrazy.wardance.command;

public class LearnSkillCommand {

//    public static final SuggestionProvider<CommandSource> SKILLS = SuggestionProviders
//            .register(new ResourceLocation(WarDance.MODID, "skills"),
//                    (context, builder) -> ISuggestionProvider.func_201725_a(
//                            GameRegistry.findRegistry(Skill.class).getValues().stream(),
//                            builder, Skill::getRegistryName, (type) -> new TranslationTextComponent(
//                                    Util.makeTranslationKey("skill", type.getRegistryName()))));
//
//    private static final DynamicCommandExceptionType UNKNOWN_ENTITY = new DynamicCommandExceptionType(
//            type -> new TranslationTextComponent("command.champions.egg.unknown_entity", type));
//
//    public static void register(CommandDispatcher<CommandSource> dispatcher) {
//        final int opPermissionLevel = 2;
//        LiteralArgumentBuilder<CommandSource> warCommands = Commands.literal("wardance")
//                .requires(player -> player.hasPermissionLevel(opPermissionLevel));
//
//        LiteralArgumentBuilder<CommandSource> skillsCommand = warCommands.then(Commands.literal("skills"));
//        skillsCommand
//                .then(Commands.literal("enable"))
//                .then(Commands.argument("player", EntityArgument.players())
//                        .suggests(SKILLS)
//                        .then(Commands.argument("tier", IntegerArgumentType.integer())
//                                .executes(context -> createEgg(context.getSource(),
//                                        EntitySummonArgument.getEntityId(context, "entity"),
//                                        IntegerArgumentType.getInteger(context, "tier"), new ArrayList<>()))));
//
//        dispatcher.register(warCommands);
//    }
//
//    private static int summon(CommandSource source,
//                              ResourceLocation resourceLocation, int player)
//            throws CommandSyntaxException {
//        Skill s=GameRegistry.findRegistry(Skill.class).getValue(resourceLocation);
//
//        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(resourceLocation);
//
//        if (entityType == null) {
//            throw UNKNOWN_ENTITY.create(resourceLocation);
//        } else {
//            Entity entity = entityType.create(source.getWorld(), null, null, null,
//                    pos != null ? pos : new BlockPos(source.getPos()), SpawnReason.COMMAND, false, false);
//
//            if (entity instanceof LivingEntity) {
//                ChampionCapability.getCapability((LivingEntity) entity).ifPresent(
//                        champion -> ChampionBuilder.spawnPreset(champion, tier, new ArrayList<>(affixes)));
//                source.getWorld().addEntity(entity);
//                source.sendFeedback(new TranslationTextComponent("commands.champions.summon.success",
//                        new TranslationTextComponent("rank.champions.title." + tier).getString() + " " + entity
//                                .getDisplayName().getString()), true);
//            }
//        }
//        return Command.SINGLE_SUCCESS;
//    }
//
//    private static int createEgg(CommandSource source, ResourceLocation resourceLocation, int tier,
//                                 Collection<IAffix> affixes) throws CommandSyntaxException {
//        EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(resourceLocation);
//
//        if (entity == null) {
//            throw UNKNOWN_ENTITY.create(resourceLocation);
//        } else if (source.getEntity() instanceof ServerPlayerEntity) {
//            ServerPlayerEntity playerEntity = (ServerPlayerEntity) source.getEntity();
//            ItemStack egg = new ItemStack(ChampionsRegistry.EGG);
//            ChampionEggItem.write(egg, resourceLocation, tier, affixes);
//            ItemHandlerHelper.giveItemToPlayer(playerEntity, egg, playerEntity.inventory.currentItem);
//            source.sendFeedback(
//                    new TranslationTextComponent("commands.champions.egg.success", egg.getDisplayName()),
//                    true);
//        }
//        return Command.SINGLE_SUCCESS;
//    }
}