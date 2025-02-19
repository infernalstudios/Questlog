package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.infernalstudios.questlog.util.JsonUtils;

public class ItemEquipObjective extends AbstractItemObjective {

  private final EquipmentSlot slot;

  //  "mainhand"
  //  "offhand"
  //  "feet"
  //  "legs"
  //  "chest"
  //  "head"

  public ItemEquipObjective(JsonObject definition) {
    super(definition);
    this.slot = EquipmentSlot.byName(JsonUtils.getString(definition, "slot"));
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onPlayerTick);
  }

  // Checks every second for performance
  private int ticksUntilCheck = 0;

  private void onPlayerTick(QLPlayerEvent.Tick event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
      event.player instanceof ServerPlayer player &&
      this.getParent().manager.player.equals(player) &&
      --ticksUntilCheck <= 0 &&
      this.test(player.getItemBySlot(this.slot))
    ) {
      this.setUnits(this.getUnits() + 1);
      ticksUntilCheck = 20;
    }
  }
}
