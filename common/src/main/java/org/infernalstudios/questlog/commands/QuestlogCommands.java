package org.infernalstudios.questlog.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.network.packet.QuestEditModePacket;
import org.infernalstudios.questlog.network.packet.QuestOpenPacket;
import org.infernalstudios.questlog.platform.Services;

import java.util.ArrayList;
import java.util.List;

public class QuestlogCommands {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_QUEST_OR_CATEGORY = (ctx, builder) -> {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return Suggestions.empty();
        }

        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);
        if (manager == null) {
            return Suggestions.empty();
        }

        List<String> suggestions = new ArrayList<>();
        for (Quest quest : manager.getAllQuests()) {
            suggestions.add(quest.getId().toString());
        }
        for (ResourceLocation chapterId : DefinitionUtil.getCachedChapterKeys()) {
            suggestions.add(chapterId.toString());
        }
        if (!suggestions.contains(Questlog.MODID + ":main") && !suggestions.contains("main")) {
            suggestions.add(Questlog.MODID + ":main");
        }

        return SharedSuggestionProvider.suggest(suggestions, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(
                Commands.literal("questlog")
                        .requires(stack -> stack.hasPermission(2))

                        .then(Commands.literal("reload")
                                .executes(QuestlogCommands::reloadQuests)
                        )

                        .then(Commands.literal("open")
                                .executes(ctx -> open(ctx, null))
                                .then(Commands.argument("target", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_QUEST_OR_CATEGORY)
                                        .executes(ctx -> open(ctx, ResourceLocationArgument.getId(ctx, "target").toString()))
                                )
                        )

                        .then(Commands.literal("progress")
                                .then(Commands.literal("reset")
                                        .then(Commands.argument("target", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_QUEST_OR_CATEGORY)
                                                .executes(ctx -> modifyProgress(ctx, ResourceLocationArgument.getId(ctx, "target").toString(), false))
                                        )
                                        .then(Commands.literal("all")
                                                .executes(QuestlogCommands::resetAllProgress)
                                        )
                                )
                                .then(Commands.literal("complete")
                                        .then(Commands.argument("target", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_QUEST_OR_CATEGORY)
                                                .executes(ctx -> modifyProgress(ctx, ResourceLocationArgument.getId(ctx, "target").toString(), true))
                                        )
                                        .then(Commands.literal("all")
                                                .executes(QuestlogCommands::completeAllProgress)
                                        )
                                )
                        )

                        .then(Commands.literal("trigger")
                                .then(Commands.literal("all")
                                        .executes(ctx -> trigger(ctx, "all"))
                                )
                                .then(Commands.argument("target", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_QUEST_OR_CATEGORY)
                                        .executes(ctx -> trigger(ctx, ResourceLocationArgument.getId(ctx, "target").toString()))
                                )
                        )

                        .then(Commands.literal("edit_mode")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setEditMode(ctx, BoolArgumentType.getBool(ctx, "enabled"), null))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> setEditMode(ctx, BoolArgumentType.getBool(ctx, "enabled"), EntityArgument.getPlayer(ctx, "player")))
                                        )
                                )
                        )
        );

        dispatcher.register(Commands.literal("ql").requires(stack -> stack.hasPermission(2)).redirect(root));
    }

    private static int reloadQuests(CommandContext<CommandSourceStack> ctx) {
        DefinitionUtil.loadFromConfig();
        int questCount = DefinitionUtil.getCachedQuestKeys().size();
        int chapterCount = DefinitionUtil.getCachedChapterKeys().size();

        if (ServerPlayerManager.INSTANCE != null) {
            for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);
                manager.reload();
                ServerPlayerManager.INSTANCE.syncPlayer(manager);
            }
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Reloaded " + questCount + " quests and " + chapterCount + " chapters from config."), true);
        return questCount;
    }

    private static int open(CommandContext<CommandSourceStack> ctx, String target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Services.PLATFORM.sendPacketToClient(player, new QuestOpenPacket(target == null ? "" : target));
        ctx.getSource().sendSuccess(() -> Component.literal("Opened Questlog UI for " + player.getName().getString() + (target != null ? " targeting " + target : "")), false);
        return 1;
    }

    private static int setEditMode(CommandContext<CommandSourceStack> ctx, boolean enabled, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = target != null ? target : ctx.getSource().getPlayerOrException();
        Services.PLATFORM.sendPacketToClient(player, new QuestEditModePacket(enabled));
        ctx.getSource().sendSuccess(() -> Component.literal("Edit mode for " + player.getName().getString() + " set to " + enabled), true);
        return 1;
    }

    private static int trigger(CommandContext<CommandSourceStack> ctx, String target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);

        List<Quest> affectedQuests = "all".equalsIgnoreCase(target) ? manager.getAllQuests() : getTargetQuests(manager, target);

        if (affectedQuests.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No quests found for target: " + target));
            return 0;
        }

        int count = 0;
        for (Quest quest : affectedQuests) {
            if (!quest.isTriggered()) {
                quest.requirements.forEach(trigger -> trigger.setUnits(trigger.getRequiredAmount()));
                count++;
            }
        }

        final int finalCount = count;
        ctx.getSource().sendSuccess(() -> Component.literal("Successfully triggered " + finalCount + " quests."), true);
        return finalCount;
    }

    private static int modifyProgress(CommandContext<CommandSourceStack> ctx, String target, boolean complete) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);

        List<Quest> affectedQuests = getTargetQuests(manager, target);

        if (affectedQuests.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No quests found for target: " + target));
            return 0;
        }

        for (Quest quest : affectedQuests) {
            if (complete) {
                quest.objectives.forEach(obj -> obj.setUnits(obj.getRequiredAmount()));
            } else {
                quest.requirements.forEach(trigger -> trigger.setUnits(0));
                quest.objectives.forEach(obj -> obj.setUnits(0));
                quest.rewards.forEach(Reward::revokeReward);
                quest.hasSentTrigger = false;
                quest.hasSentCompletion = false;
            }
        }

        String action = complete ? "Completed" : "Reset";
        ctx.getSource().sendSuccess(() -> Component.literal(action + " progress for " + affectedQuests.size() + " quests."), true);
        return affectedQuests.size();
    }

    private static int resetAllProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);

        for (Quest quest : manager.getAllQuests()) {
            quest.requirements.forEach(trigger -> trigger.setUnits(0));
            quest.objectives.forEach(obj -> obj.setUnits(0));
            quest.rewards.forEach(Reward::revokeReward);
            quest.hasSentTrigger = false;
            quest.hasSentCompletion = false;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Successfully reset all quest progress."), true);
        return 1;
    }

    private static int completeAllProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);

        for (Quest quest : manager.getAllQuests()) {
            quest.objectives.forEach(obj -> obj.setUnits(obj.getRequiredAmount()));
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Successfully completed all quests."), true);
        return 1;
    }

    private static List<Quest> getTargetQuests(QuestManager manager, String target) {
        List<Quest> quests = new ArrayList<>();
        ResourceLocation id = ResourceLocation.tryParse(target);

        if (id != null) {
            Quest q = manager.getQuest(id);
            if (q != null) {
                quests.add(q);
                return quests;
            }
        }

        for (Quest quest : manager.getAllQuests()) {
            if (target.equalsIgnoreCase(quest.getDisplay().getChapter())) {
                quests.add(quest);
            }
        }

        return quests;
    }
}