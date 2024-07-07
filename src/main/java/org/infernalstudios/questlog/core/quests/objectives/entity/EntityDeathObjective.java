package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.infernalstudios.questlog.event.GenericEventBus;

public class EntityDeathObjective extends AbstractEntityObjective {
  public EntityDeathObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(GenericEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onEntityDeath);
  }

  private void onEntityDeath(LivingDeathEvent event) {
    if (this.isCompleted() || this.getParent() == null) return;
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
