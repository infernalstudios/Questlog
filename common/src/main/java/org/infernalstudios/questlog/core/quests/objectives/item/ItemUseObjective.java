package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLEntityEvent;

public class ItemUseObjective extends AbstractItemObjective {

  public ItemUseObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onItemUse);
  }

  private void onItemUse(QLEntityEvent.UseItem event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (event.entity instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && this.test(event.item)) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
