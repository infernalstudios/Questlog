package org.infernalstudios.questlog;

import java.io.IOException;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.infernalstudios.questlog.commands.QuestlogCommands;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.network.packet.QuestCompletedPacket;
import org.infernalstudios.questlog.network.packet.QuestTriggeredPacket;
import org.infernalstudios.questlog.platform.Services;

public class QuestlogEvents {

  // These initialize the ServerPlayerManager instance
  public static void onServerStart(MinecraftServer server) {
    ServerPlayerManager.INSTANCE = new ServerPlayerManager(server);
    ServerPlayerManager.INSTANCE.load();
  }

  public static void onPlayerSave(ServerPlayer player) {
    if (ServerPlayerManager.INSTANCE == null) return;
    QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);
    ServerPlayerManager.INSTANCE.save(manager);
  }

  public static void onServerStop() {
    ServerPlayerManager.INSTANCE.save();
    ServerPlayerManager.INSTANCE = null;
    Questlog.EVENTS.removeAllListeners();
  }

  public static void onServerPlayerLogin(ServerPlayer player) {
    if (ServerPlayerManager.INSTANCE == null) return;
    QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);
    ServerPlayerManager.INSTANCE.load(manager);
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
        if (reward.rewardsInstantly()) {
          // Reward claiming is handled by the server for instant rewards
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
