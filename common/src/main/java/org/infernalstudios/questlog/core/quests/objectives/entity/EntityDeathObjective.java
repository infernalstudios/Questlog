package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLEntityEvent;

public class EntityDeathObjective extends AbstractEntityObjective {

  public EntityDeathObjective(JsonObject definition) {
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
      event.entity instanceof ServerPlayer player &&
      this.getParent().manager.player.equals(player) &&
      event.damageSource.getEntity() != null &&
      this.test(event.damageSource.getEntity())
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
