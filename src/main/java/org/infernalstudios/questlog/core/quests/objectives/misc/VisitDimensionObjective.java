package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.infernalstudios.questlog.core.quests.objectives.Objective;

public class VisitDimensionObjective extends Objective {
  private final ResourceLocation dimension;

  public VisitDimensionObjective(JsonObject definition) {
    super(definition);
    this.dimension = new ResourceLocation(definition.get("dimension").getAsString());
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
      if (player.level.dimensionTypeId().location().equals(this.dimension)) {
        this.setUnits(this.getUnits() + 1);
      }
      ticksUntilCheck = 20;
    }
  }
}
