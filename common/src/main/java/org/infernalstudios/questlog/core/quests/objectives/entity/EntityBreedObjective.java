package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLEntityEvent;

public class EntityBreedObjective extends AbstractEntityObjective {

  public EntityBreedObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onPlayerMove);
  }

  private void onPlayerMove(QLEntityEvent.Breed event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
      event.causedByPlayer instanceof ServerPlayer player &&
      this.getParent().manager.player.equals(player) &&
      (this.test(event.parentA) || this.test(event.parentB)) // Ideally both of these are true, and testing for both is redundant
                                                             // But there might be some modder which adds cross-species breeding
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
