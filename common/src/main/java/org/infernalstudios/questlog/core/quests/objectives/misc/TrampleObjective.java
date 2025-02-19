package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLBlockEvent;

public class TrampleObjective extends Objective {

  public TrampleObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onBlockTrample);
  }

  private void onBlockTrample(QLBlockEvent.FarmlandTrample event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (event.entity instanceof ServerPlayer player && this.getParent().manager.player.equals(player)) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
