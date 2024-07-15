package org.infernalstudios.questlog.core;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.event.QuestCompletedEvent;
import org.infernalstudios.questlog.event.QuestTriggeredEvent;
import org.infernalstudios.questlog.network.NetworkHandler;
import org.infernalstudios.questlog.network.packet.QuestDataPacket;
import org.infernalstudios.questlog.network.packet.QuestRemovePacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestManager {
  private static QuestManager INSTANCE;

  public final Player player;
  private final Map<ResourceLocation, Quest> quests = new HashMap<>();

  public QuestManager(Player player) {
    this.player = player;
  }

  /**
   * Adds a quest to the player's tracked quest list.
   * Does not sync between server and client.
   */
  public void addQuest(Quest quest) {
    if (this.quests.containsKey(quest.getId())) {
      return;
    }
    this.quests.put(quest.getId(), quest);
  }

  /**
   * Removes a quest from the player's tracked quest list.
   * Does not sync between server and client.
   */
  public void removeQuest(ResourceLocation id) {
    this.quests.remove(id);
  }

  public Quest getQuest(ResourceLocation id) {
    return this.quests.get(id);
  }

  public boolean isClient() {
    return this.player.isLocalPlayer();
  }

  public List<Quest> getAllQuests() {
    return this.quests.values().stream().toList();
  }

  /**
   * This method is used to create all quests for the player.
   * It fetches all quest IDs from the cached keys and checks if the quest is already present in the player's quest list.
   * If not, it fetches the quest definition from the cache, creates a new quest instance and adds it to the player's quest list.
   */
  public void createAllQuests() {
    List<ResourceLocation> ids = DefinitionUtil.getCachedKeys();

    for (ResourceLocation id : ids) {
      if (!this.quests.containsKey(id)) {
        JsonObject definition = DefinitionUtil.getCached(id);
        Quest quest;
        try {
          quest = Quest.create(definition, id, this);
        } catch (Exception e) {
          throw new RuntimeException("Failed to create quest " + id, e);
        }
        CompoundTag data = new CompoundTag();
        quest.writeInitialData(data);
        quest.deserialize(data);
        this.addQuest(quest);
      }
    }
  }

  /**
   * This method is used to sync all quests data to the client.
   * It iterates over all quests in the player's quest list and calls the sync method for each quest.
   */
  public void sync() {
    for (ResourceLocation id : this.quests.keySet()) {
      this.sync(id);
    }
  }

  /**
   * This method is used to sync specific quest data to the client.
   * If the quest does not exist, it sends a QuestRemovePacket to the client.
   * If the quest exists, it serializes the quest data and sends a QuestDataPacket to the client.
   * It also checks if the quest has been triggered or completed and sends the corresponding event to the client.
   *
   * @param id The ID of the quest to be synced.
   */
  public void sync(ResourceLocation id) {
    if (!this.isClient() && this.player instanceof ServerPlayer) {
      Questlog.LOGGER.trace("Syncing quest data for {} to client", id);
      Quest quest = this.quests.get(id);
      if (quest == null) {
        // This will only be called if a definition has been deleted while reloading the server
        NetworkHandler.sendToPlayer(new QuestRemovePacket(id), (ServerPlayer) this.player);
        Questlog.LOGGER.warn("Quest {} not found in manager, removing from client", id);
      } else {
        CompoundTag data = this.getQuest(id).serialize();
        NetworkHandler.sendToPlayer(new QuestDataPacket(id, data), (ServerPlayer) this.player);
        Questlog.LOGGER.trace("Sent quest data for {} to client", id);
        if (!quest.hasSentTrigger && quest.isTriggered()) {
          quest.hasSentTrigger = true;
          MinecraftForge.EVENT_BUS.post(new QuestTriggeredEvent(this.player, quest, false)); // This handles sending of packet
          Questlog.LOGGER.trace("Sent quest triggered event for {}", id);
        }

        if (!quest.hasSentCompletion && quest.isCompleted()) {
          quest.hasSentCompletion = true;
          MinecraftForge.EVENT_BUS.post(new QuestCompletedEvent(this.player, quest, false));
          Questlog.LOGGER.trace("Sent quest completed event for {}", id);
        }
      }
    }
  }

  /**
   * This method is used to get the local instance of the QuestManager.
   * If the instance is null or the player is not the current Minecraft player, a new QuestManager instance is created.
   * If the instance is not local, an IllegalCallerException is thrown.
   *
   * @return Returns the local instance of the QuestManager.
   */
  public static QuestManager getLocal() {
    if (INSTANCE != null && INSTANCE.player == null) {
      // Destroy the QuestManager instance if the player is null
      INSTANCE = null;
    }

    if (INSTANCE == null || INSTANCE.player != Minecraft.getInstance().player) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null) {
        throw new NullPointerException("QuestManager cannot be initialized, player is null\n");
      }

      INSTANCE = new QuestManager(player);
    }

    if (!INSTANCE.isClient()) {
      throw new IllegalCallerException("QuestManager is not local");
    }

    return INSTANCE;
  }

  public static void destroyLocal() {
    INSTANCE = null;
  }
}
