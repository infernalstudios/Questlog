package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class EntityBreedObjective extends AbstractEntityObjective {
  public EntityBreedObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onPlayerMove);
  }

  private void onPlayerMove(BabyEntitySpawnEvent event) {
    if (this.isCompleted()) return;
    if (
      event.getCausedByPlayer() instanceof ServerPlayer player &&
      this.getParent().manager.player.equals(player) &&
      (event.getParentA().getType().equals(this.getEntity()) || event.getParentB().getType().equals(this.getEntity()))
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
