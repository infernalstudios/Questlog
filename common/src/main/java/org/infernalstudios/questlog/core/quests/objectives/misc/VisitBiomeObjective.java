package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.infernalstudios.questlog.util.JsonUtils;

public class VisitBiomeObjective extends Objective {

  private final ResourceLocation biome;

  public VisitBiomeObjective(JsonObject definition) {
    super(definition);
    this.biome = new ResourceLocation(JsonUtils.getString(definition, "biome"));
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
      if (player.serverLevel().getBiome(player.blockPosition()).unwrapKey().orElseThrow().location().equals(this.biome)) {
        this.setUnits(this.getUnits() + 1);
      }
      ticksUntilCheck = 20;
    }
  }
}
