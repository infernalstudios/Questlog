package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class ItemPickupObjective extends AbstractItemObjective {
  public ItemPickupObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onItemPickup);
  }

  private void onItemPickup(PlayerEvent.ItemPickupEvent event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
        event.getEntity() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player) &&
        this.test(event.getStack())
    ) {
      this.setUnits(this.getUnits() + event.getStack().getCount());
    }
  }
}
