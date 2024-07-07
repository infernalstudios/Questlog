package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.BlockEvent;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.GenericEventBus;

public class TrampleObjective extends Objective {
  public TrampleObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(GenericEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onBlockTrample);
  }

  private void onBlockTrample(BlockEvent.FarmlandTrampleEvent event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
        event.getEntity() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player)
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
