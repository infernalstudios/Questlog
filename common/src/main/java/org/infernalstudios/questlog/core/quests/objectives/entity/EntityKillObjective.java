package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLEntityEvent;

public class EntityKillObjective extends AbstractEntityObjective {

  public EntityKillObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onEntityDeath);
  }

  private void onEntityDeath(QLEntityEvent.Death event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
      event.damageSource.getEntity() instanceof ServerPlayer player &&
      this.getParent().manager.player.equals(player) &&
      this.test(event.entity)
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
