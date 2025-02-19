package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.infernalstudios.questlog.util.JsonUtils;

public class EntityApproachObjective extends AbstractEntityObjective {

  private final int range;

  public EntityApproachObjective(JsonObject definition) {
    super(definition);
    this.range = JsonUtils.getInt(definition, "range");
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onPlayerMove);
  }

  private int ticksUntilCheck = 0;

  private void onPlayerMove(QLPlayerEvent.Tick event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
      if (!event.player.level().getEntities((Entity) null, event.player.getBoundingBox().inflate(this.range), this::test).isEmpty()) {
        this.setUnits(this.getUnits() + 1);
      }
      ticksUntilCheck = 20;
    }
  }
}
