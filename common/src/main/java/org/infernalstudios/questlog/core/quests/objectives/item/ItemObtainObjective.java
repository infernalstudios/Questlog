package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.nbt.ByteTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;

public class ItemObtainObjective extends AbstractItemObjective {

  @Nullable
  private String uniqueTagCache = null;

  public ItemObtainObjective(JsonObject definition) {
    super(definition);
  }

  private String getUniqueTag() {
    if (this.uniqueTagCache == null) {
      this.uniqueTagCache =
        "questlog_tracked_" +
        Objects.hash(
          this.getTotalUnits(),
          this.getParent().getId(),
          this.getParent().objectives.indexOf(this),
          this.getParent().manager.player.getUUID()
        );
    }
    return this.uniqueTagCache;
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
    if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
      List<ItemStack> stacks = new ArrayList<>();

      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
        ItemStack stack = player.getInventory().getItem(i);
        if (this.test(stack)) {
          stacks.add(stack);
        }
      }

      for (ItemStack stack : stacks) {
        if (stack.getTag() != null && stack.getTag().contains(this.getUniqueTag()) && stack.getTag().getBoolean(this.getUniqueTag())) {
          continue;
        }
        this.setUnits(this.getUnits() + stack.getCount());
        // Persistently prevents double counting
        stack.addTagElement(this.getUniqueTag(), ByteTag.ONE);
      }

      ticksUntilCheck = 20;
    }
  }
}
