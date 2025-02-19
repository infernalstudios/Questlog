package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.Util;

public class VisitPositionObjective extends Objective {

  private final BoundingBox bounds;

  public VisitPositionObjective(JsonObject definition) {
    super(definition);
    this.bounds = Util.bbFromJson(JsonUtils.getObject(definition, "bounds"));
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onPlayerMove);
  }

  // Checks every second for performance
  private int ticksUntilCheck = 0;

  private void onPlayerMove(QLPlayerEvent.Tick event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
      if (this.bounds.isInside(event.player.blockPosition())) {
        this.setUnits(this.getUnits() + 1);
      }
      ticksUntilCheck = 20;
    }
  }
}
