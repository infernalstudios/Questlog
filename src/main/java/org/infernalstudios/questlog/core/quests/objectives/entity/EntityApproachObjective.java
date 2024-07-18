package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import org.infernalstudios.questlog.event.GenericEventBus;
import org.infernalstudios.questlog.util.JsonUtils;

public class EntityApproachObjective extends AbstractEntityObjective {
  private final int range;

  public EntityApproachObjective(JsonObject definition) {
    super(definition);
    this.range = JsonUtils.getInt(definition, "range");
  }

  @Override
  protected void registerEventListeners(GenericEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onPlayerMove);
  }

  private int ticksUntilCheck = 0;
  private void onPlayerMove(TickEvent.PlayerTickEvent event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
        event.player instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player) &&
        --ticksUntilCheck <= 0
    ) {
      if (!event.player.level.getEntities(this.getEntity(), event.player.getBoundingBox().inflate(this.range), entity -> true).isEmpty()) {
        this.setUnits(this.getUnits() + 1);
      }
      ticksUntilCheck = 20;
    }
  }
}
