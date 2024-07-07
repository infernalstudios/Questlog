package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import org.infernalstudios.questlog.event.GenericEventBus;

public class ItemUseObjective extends AbstractItemObjective {
  public ItemUseObjective(JsonObject definition) {
    super(definition);
  }

  @Override
  protected void registerEventListeners(GenericEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onItemUse);
  }

  private void onItemUse(LivingEntityUseItemEvent event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
        event.getEntity() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player) &&
        this.test(event.getItem())
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
