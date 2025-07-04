package org.infernalstudios.questlog.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.core.quests.rewards.Reward;

public class QuestlogCommands {
  private static final SimpleCommandExceptionType UNKNOWN_QUEST = new SimpleCommandExceptionType(Component.translatable("command.questlog.argument.quest.unknown"));
  private static final SimpleCommandExceptionType INVALID_TRIGGER = new SimpleCommandExceptionType(Component.translatable("command.questlog.argument.trigger.invalid"));
  private static final SimpleCommandExceptionType INVALID_OBJECTIVE = new SimpleCommandExceptionType(Component.translatable("command.questlog.argument.objective.invalid"));
  private static final SimpleCommandExceptionType INVALID_REWARD = new SimpleCommandExceptionType(Component.translatable("command.questlog.argument.reward.invalid"));

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    LiteralCommandNode<CommandSourceStack> root = dispatcher.register(
      Commands.literal("questlog")
        .requires(stack -> stack.hasPermission(2))
        .then(Commands.argument("quest", ResourceLocationArgument.id())
          .suggests((ctx, builder) -> {
            QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(ctx.getSource().getPlayerOrException());
            manager.getAllQuests().stream().map(Quest::getId).forEach(id -> builder.suggest(id.toString()));
            return builder.buildFuture();
          })
          .then(Commands.literal("reset")
            .executes(ctx -> resetQuest(ctx, getQuest(ctx)))
          )
          .then(Commands.literal("trigger")
            .then(Commands.literal("reset")
              .executes(ctx -> resetAllTriggers(ctx, getQuest(ctx)))
            )
            .then(Commands.literal("set")
              .executes(ctx -> setAllTriggers(ctx, getQuest(ctx)))
            )
            .then(Commands.argument("trigger", IntegerArgumentType.integer(0))
              .then(Commands.literal("reset")
                .executes(ctx -> resetTrigger(ctx, getQuest(ctx), IntegerArgumentType.getInteger(ctx, "trigger")))
              )
              .then(Commands.literal("set")
                .then(Commands.argument("units", IntegerArgumentType.integer(0))
                  .executes(ctx -> setTrigger(ctx, getQuest(ctx), IntegerArgumentType.getInteger(ctx, "trigger"), IntegerArgumentType.getInteger(ctx, "units")))
                )
                .executes(ctx -> setTrigger(ctx, getQuest(ctx), IntegerArgumentType.getInteger(ctx, "trigger"), 1))
              )
            )
          )
          .then(Commands.literal("objective")
            .then(Commands.literal("reset")
              .executes(ctx -> resetAllObjectives(ctx, getQuest(ctx)))
            )
            .then(Commands.literal("set")
              .executes(ctx -> setAllObjectives(ctx, getQuest(ctx)))
            )
            .then(Commands.argument("objective", IntegerArgumentType.integer(0))
              .then(Commands.literal("reset")
                .executes(ctx -> resetObjective(ctx, getQuest(ctx), IntegerArgumentType.getInteger(ctx, "objective")))
              )
              .then(Commands.literal("set")
                .then(Commands.argument("units", IntegerArgumentType.integer(0))
                  .executes(ctx -> setObjective(ctx, getQuest(ctx), IntegerArgumentType.getInteger(ctx, "objective"), IntegerArgumentType.getInteger(ctx, "units")))
                )
                .executes(ctx -> setObjective(ctx, getQuest(ctx), IntegerArgumentType.getInteger(ctx, "objective"), 1))
              )
            )
          )
          .then(Commands.literal("reward")
            .then(Commands.literal("uncollect")
              .executes(ctx -> uncollectAllRewards(ctx, getQuest(ctx)))
            )
            .then(Commands.literal("collect")
              .executes(ctx -> collectAllRewards(ctx, getQuest(ctx)))
            )
            .then(Commands.argument("reward", IntegerArgumentType.integer(0))
              .then(Commands.literal("uncollect")
                .executes(ctx -> uncollectReward(ctx, getQuest(ctx), IntegerArgumentType.getInteger(ctx, "reward")))
              )
              .then(Commands.literal("collect")
                .executes(ctx -> collectReward(ctx, getQuest(ctx), IntegerArgumentType.getInteger(ctx, "reward")))
              )
            )
          )
        )
    );

    dispatcher.register(Commands.literal("ql").requires(stack -> stack.hasPermission(2)).redirect(root));
  }

  private static int resetQuest(CommandContext<CommandSourceStack> ctx, Quest quest) throws CommandSyntaxException {
    quest.triggers.forEach(trigger -> trigger.setUnits(0));
    quest.objectives.forEach(objective -> objective.setUnits(0));
    quest.rewards.forEach(Reward::revokeReward);

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.reset.success", quest.getId()), true);

    return 0;
  }

  private static int resetAllObjectives(CommandContext<CommandSourceStack> ctx, Quest quest) {
    quest.objectives.forEach(objective -> objective.setUnits(0));

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.objective.reset_all"), true);

    return 0;
  }

  private static int setAllObjectives(CommandContext<CommandSourceStack> ctx, Quest quest) throws CommandSyntaxException {
    quest.objectives.forEach(objective -> objective.setUnits(objective.getTotalUnits()));

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.objective.set_all"), true);

    return 0;
  }

  private static int resetObjective(CommandContext<CommandSourceStack> ctx, Quest quest, int index) throws CommandSyntaxException {
    if (index < 0 || index >= quest.objectives.size()) {
      throw INVALID_OBJECTIVE.create();
    }

    quest.objectives.get(index).setUnits(0);

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.objective.reset", index), true);

    return 0;
  }

  private static int setObjective(CommandContext<CommandSourceStack> ctx, Quest quest, int index, int units) throws CommandSyntaxException {
    if (index < 0 || index >= quest.objectives.size()) {
      throw INVALID_OBJECTIVE.create();
    }

    Objective objective = quest.objectives.get(index);
    objective.setUnits(units == -1 ? objective.getTotalUnits() : units);

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.objective.set", index, units), true);

    return 0;
  }

  private static int resetAllTriggers(CommandContext<CommandSourceStack> ctx, Quest quest) {
    quest.triggers.forEach(trigger -> trigger.setUnits(0));

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.trigger.reset_all"), true);

    return 0;
  }

  private static int setAllTriggers(CommandContext<CommandSourceStack> ctx, Quest quest) throws CommandSyntaxException {
    quest.triggers.forEach(trigger -> trigger.setUnits(trigger.getTotalUnits()));

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.trigger.set_all"), true);

    return 0;
  }

  private static int resetTrigger(CommandContext<CommandSourceStack> ctx, Quest quest, int index) throws CommandSyntaxException {
    if (index < 0 || index >= quest.triggers.size()) {
      throw INVALID_TRIGGER.create();
    }

    quest.triggers.get(index).setUnits(0);

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.trigger.reset", index), true);

    return 0;
  }

  private static int setTrigger(CommandContext<CommandSourceStack> ctx, Quest quest, int index, int units) throws CommandSyntaxException {
    if (index < 0 || index >= quest.triggers.size()) {
      throw INVALID_TRIGGER.create();
    }

    Objective trigger = quest.triggers.get(index);
    trigger.setUnits(units == -1 ? trigger.getTotalUnits() : units);

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.trigger.set", index, units), true);

    return 0;
  }

  private static int uncollectAllRewards(CommandContext<CommandSourceStack> ctx, Quest quest) {
    quest.rewards.forEach(Reward::revokeReward);

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.reward.uncollect_all"), true);

    return 0;
  }

  private static int collectAllRewards(CommandContext<CommandSourceStack> ctx, Quest quest) throws CommandSyntaxException {
    quest.rewards.forEach(reward -> {
      if (!reward.hasRewarded()) {
        reward.applyReward(ctx.getSource().getPlayer());
      }
    });

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.reward.collect_all"), true);

    return 0;
  }

  private static int uncollectReward(CommandContext<CommandSourceStack> ctx, Quest quest, int index) throws CommandSyntaxException {
    if (index < 0 || index >= quest.rewards.size()) {
      throw INVALID_REWARD.create();
    }

    quest.rewards.get(index).revokeReward();

    ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.reward.uncollect", index), true);

    return 0;
  }

  private static int collectReward(CommandContext<CommandSourceStack> ctx, Quest quest, int index) throws CommandSyntaxException {
    if (index < 0 || index >= quest.rewards.size()) {
      throw INVALID_REWARD.create();
    }

    if (!quest.rewards.get(index).hasRewarded()) {
      quest.rewards.get(index).applyReward(ctx.getSource().getPlayer());
      ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.reward.success", index), true);
    } else {
      ctx.getSource().sendSuccess(() -> Component.translatable("command.questlog.reward.already_collected", index), false);
    }

    return 0;
  }

  private static Quest getQuest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(ctx.getSource().getPlayerOrException());
    ResourceLocation id = ctx.getArgument("quest", ResourceLocation.class);
    Quest quest = manager.getQuest(id);
    if (quest == null) {
      throw UNKNOWN_QUEST.create();
    }

    return quest;
  }
}
