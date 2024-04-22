package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class ItemDropObjective extends AbstractItemObjective {
  public ItemDropObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onItemDrop);
  }

  private void onItemDrop(ItemTossEvent event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
        event.getPlayer() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player) &&
        this.test(event.getEntity().getItem())
    ) {
      this.setUnits(this.getUnits() + event.getEntity().getItem().getCount());
    }
  }
}
