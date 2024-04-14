package org.infernalstudios.questlog.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.infernalstudios.questlog.core.quests.Quest;

public class QuestCompletedEvent extends PlayerEvent {
  private final Quest quest;
  private final boolean isClient;

  public QuestCompletedEvent(Player player, Quest quest, boolean isClient) {
    super(player);
    this.quest = quest;
    this.isClient = isClient;
  }

  public Quest getQuest() {
    return this.quest;
  }

  public boolean isClient() {
    return this.isClient;
  }
}
