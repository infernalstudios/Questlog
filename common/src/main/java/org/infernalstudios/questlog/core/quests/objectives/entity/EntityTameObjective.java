package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLEntityEvent;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;

public class EntityTameObjective extends AbstractEntityObjective {

  public EntityTameObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onAnimalTame);
  }

  private void onAnimalTame(QLEntityEvent.TameAnimal event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
      event.causedByPlayer != null &&
      event.causedByPlayer instanceof ServerPlayer player &&
      this.getParent().manager.player.equals(player) &&
      (this.test(event.entity))
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
