package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class ItemCraftObjective extends AbstractItemObjective {
  public ItemCraftObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onItemCraft);
  }

  private void onItemCraft(PlayerEvent.ItemCraftedEvent event) {
    if (this.isCompleted()) return;
    if (
        event.getEntity() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player) &&
        event.getCrafting().getItem().equals(this.getItem())
    ) {
      this.setUnits(this.getUnits() + event.getCrafting().getCount());
    }
  }
}
