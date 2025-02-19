package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;

public class ItemCraftObjective extends AbstractItemObjective {

  public ItemCraftObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onItemCraft);
  }

  private void onItemCraft(QLPlayerEvent.Craft event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
      event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && this.test(event.outputItem)
    ) {
      this.setUnits(this.getUnits() + event.outputItem.getCount());
    }
  }
}
