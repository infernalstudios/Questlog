package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class EntityDeathObjective extends AbstractEntityObjective {
  public EntityDeathObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onEntityDeath);
  }

  private void onEntityDeath(LivingDeathEvent event) {
    if (this.isCompleted()) return;
    if (
        event.getEntity() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player) &&
        event.getSource().getEntity() != null &&
        event.getSource().getEntity().getType().equals(this.getEntity())
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
