package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.GenericEventBus;
import org.infernalstudios.questlog.event.QuestCompletedEvent;
import org.infernalstudios.questlog.util.JsonUtils;

public class QuestCompleteObjective extends Objective {
  private final ResourceLocation quest;

  public QuestCompleteObjective(JsonObject definition) {
    super(definition);
    this.quest = new ResourceLocation(JsonUtils.getString(definition, "quest"));
  }

  @Override
  protected void registerEventListeners(GenericEventBus bus) {
    super.registerEventListeners(bus);
     bus.addListener(this::onQuestCompleted);
  }

  private void onQuestCompleted(QuestCompletedEvent event) {
    if (this.isCompleted() || this.getParent() == null) return;
    Quest quest = event.getQuest().manager.getQuest(event.getQuest().getId());
    if (quest.getId().equals(this.quest)) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
