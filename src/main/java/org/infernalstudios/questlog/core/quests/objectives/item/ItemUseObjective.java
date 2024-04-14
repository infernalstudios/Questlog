package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class ItemUseObjective extends AbstractItemObjective {
  public ItemUseObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onItemUse);
  }

  private void onItemUse(LivingEntityUseItemEvent event) {
    if (this.isCompleted()) return;
    if (
        event.getEntity() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player) &&
        event.getItem().getItem().equals(this.getItem())
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
