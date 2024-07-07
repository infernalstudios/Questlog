package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.infernalstudios.questlog.event.GenericEventBus;

public class EntityKillObjective extends AbstractEntityObjective {
  public EntityKillObjective(JsonObject definition) {
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
        event.getSource().getEntity() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player) &&
        event.getEntity().getType().equals(this.getEntity())
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
