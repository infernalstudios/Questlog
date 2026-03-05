package org.infernalstudios.questlog;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.commands.QuestlogCommands;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.network.packet.QuestCompletedPacket;
import org.infernalstudios.questlog.network.packet.QuestTriggeredPacket;
import org.infernalstudios.questlog.platform.Services;
import org.infernalstudios.questlog.util.QuestlogMigrator;

public class QuestlogEvents {

    public static void onServerStart(MinecraftServer server) {
        QuestlogMigrator.attemptMigration(server);
        DefinitionUtil.loadFromConfig();
        ServerPlayerManager.INSTANCE = new ServerPlayerManager(server);
        ServerPlayerManager.INSTANCE.load();
    }

    public static void onPlayerSave(ServerPlayer player) {
        if (ServerPlayerManager.INSTANCE == null) return;
        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);
        ServerPlayerManager.INSTANCE.save(manager);
    }

    public static void onServerStop() {
        if (ServerPlayerManager.INSTANCE != null) {
            ServerPlayerManager.INSTANCE.save();
            ServerPlayerManager.INSTANCE = null;
        }
        Questlog.EVENTS.removeAllListeners();
    }

    public static void onServerPlayerLogin(ServerPlayer player) {
        if (ServerPlayerManager.INSTANCE == null) return;
        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);
        ServerPlayerManager.INSTANCE.load(manager);

        if (QuestlogMigrator.showDatapackWarning && player.hasPermissions(2)) {
            player.sendSystemMessage(Component.literal("§e[Questlog] Warning: Quests are now loaded from the config folder. Datapacks are no longer supported! Your old datapack quests were automatically migrated."));
        }
    }

    public static void onQuestTriggered(QuestEvent.Triggered event) {
        if (event.isServer) {
            Services.PLATFORM.sendPacketToClient((ServerPlayer) event.player, new QuestTriggeredPacket(event.quest.getId()));
        } else {
            QuestlogClientEvents.onQuestTriggered(event);
        }
    }

    public static void onQuestCompleted(QuestEvent.Completed event) {
        if (event.isServer) {
            Questlog.EVENTS.post(event);
            Services.PLATFORM.sendPacketToClient((ServerPlayer) event.player, new QuestCompletedPacket(event.quest.getId()));

            for (Reward reward : event.quest.rewards) {
                if (reward.isAutoClaim()) {
                    if (!reward.hasRewarded()) {
                        reward.applyReward((ServerPlayer) event.player);
                    }
                }
            }
        } else {
            QuestlogClientEvents.onQuestCompleted(event);
        }
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        QuestlogCommands.register(dispatcher);
    }
}