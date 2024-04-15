package org.infernalstudios.questlog.core.quests.objectives.block;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class BlockPlaceObjective extends AbstractBlockObjective {
  public BlockPlaceObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onBlockPlace);
  }

  private void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
    if (this.isCompleted()) return;
    if (
        event.getEntity() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player) &&
        this.test(event.getState())
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
