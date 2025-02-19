package org.infernalstudios.questlog.core.quests.objectives.block;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLBlockEvent;

public class BlockPlaceObjective extends AbstractBlockObjective {

  public BlockPlaceObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onBlockPlace);
  }

  private void onBlockPlace(QLBlockEvent.Place event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (event.entity instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && this.test(event.state)) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
