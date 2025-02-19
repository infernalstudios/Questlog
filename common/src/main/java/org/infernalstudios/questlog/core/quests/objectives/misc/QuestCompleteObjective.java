package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.util.JsonUtils;

public class QuestCompleteObjective extends Objective {
  private final ResourceLocation quest;

  public QuestCompleteObjective(JsonObject definition) {
    super(definition);
    this.quest = new ResourceLocation(JsonUtils.getString(definition, "quest"));
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onQuestCompleted);
  }

  private void onQuestCompleted(QuestEvent.Completed event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (event.player.equals(this.getParent().manager.player) && event.quest.getId().equals(this.quest)) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
