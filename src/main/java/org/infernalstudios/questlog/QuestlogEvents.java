package org.infernalstudios.questlog;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.infernalstudios.questlog.client.gui.components.toasts.QuestAddedToast;
import org.infernalstudios.questlog.client.gui.components.toasts.QuestCompletedToast;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.event.QuestCompletedEvent;
import org.infernalstudios.questlog.event.QuestTriggeredEvent;
import org.infernalstudios.questlog.network.NetworkHandler;
import org.infernalstudios.questlog.network.packet.QuestCompletedPacket;
import org.infernalstudios.questlog.network.packet.QuestTriggeredPacket;

import java.io.IOException;

public class QuestlogEvents {
  // These initialize the ServerPlayerManager instance
  @SubscribeEvent
  public static void onServerStart(ServerStartingEvent event) {
    try {
      // Ensure that the quest definitions are loaded before calling ServerPlayerManager.load()
      DefinitionUtil.getAndCacheAllQuests(event.getServer().getResourceManager());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    ServerPlayerManager.INSTANCE = new ServerPlayerManager(event.getServer());
    ServerPlayerManager.INSTANCE.load();
  }

//  @SubscribeEvent
//  public static void addReloadListener(AddReloadListenerEvent event) {
//    event.addListener((preparationBarrier, resourceManager, profilerFiller, profilerFiller1, executor, executor1) -> CompletableFuture.runAsync(() -> {
//      try {
//        DefinitionUtil.getListedQuests(resourceManager);
//      } catch (IOException e) {
//        throw new RuntimeException(e);
//      }
//    }));
//  }

  @SubscribeEvent
  public static void onSave(PlayerEvent.SaveToFile event) {
    if (ServerPlayerManager.INSTANCE == null) return;
    QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(event.getEntity());
    ServerPlayerManager.INSTANCE.save(manager);
  }
  
  @SubscribeEvent
  public static void onServerStop(ServerStoppingEvent event) {
    ServerPlayerManager.INSTANCE.save();
    ServerPlayerManager.INSTANCE = null;
    Questlog.GENERIC_EVENT_BUS.removeAllListeners();
  }
  
  @SubscribeEvent
  public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    if (ServerPlayerManager.INSTANCE == null) return;
    QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(event.getEntity());
    ServerPlayerManager.INSTANCE.load(manager);
  }

  @SubscribeEvent
  public static void onQuestAdded(QuestTriggeredEvent event) {
    if (!event.isClient()) {
      NetworkHandler.sendToPlayer(new QuestTriggeredPacket(event.getQuest().getId()), (ServerPlayer) event.getEntity());
      return;
    }

    Minecraft.getInstance().getToasts().addToast(new QuestAddedToast(event.getQuest().getDisplay()));
  }

  @SubscribeEvent
  public static void onQuestCompleted(QuestCompletedEvent event) {
    if (!event.isClient()) {
      NetworkHandler.sendToPlayer(new QuestCompletedPacket(event.getQuest().getId()), (ServerPlayer) event.getEntity());
      return;
    }

    Minecraft.getInstance().getToasts().addToast(new QuestCompletedToast(event.getQuest().getDisplay()));
  }
}
