package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.Util;

public class VisitPositionObjective extends Objective {
  private final BoundingBox bounds;

  public VisitPositionObjective(JsonObject definition) {
    super(definition);
    this.bounds = Util.bbFromJson(definition.getAsJsonObject("bounds"));
  }

  @Override
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onPlayerMove);
  }

  // Checks every second for performance
  private int ticksUntilCheck = 0;
  private void onPlayerMove(TickEvent.PlayerTickEvent event) {
    if (this.isCompleted()) return;
    if (
      event.player instanceof ServerPlayer player &&
      this.getParent().manager.player.equals(player) &&
      --ticksUntilCheck <= 0
    ) {
      if (this.bounds.isInside(event.player.blockPosition())) {
        this.setUnits(this.getUnits() + 1);
      }
      ticksUntilCheck = 20;
    }

  }
}
