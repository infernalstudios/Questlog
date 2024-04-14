package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.infernalstudios.questlog.core.quests.objectives.Objective;

public class TrampleObjective extends Objective {
  public TrampleObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onBlockTrample);
  }

  private void onBlockTrample(BlockEvent.FarmlandTrampleEvent event) {
    if (this.isCompleted()) return;
    if (
        event.getEntity() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player)
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
