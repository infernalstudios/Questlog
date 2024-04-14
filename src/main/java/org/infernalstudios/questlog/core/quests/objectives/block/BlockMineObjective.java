package org.infernalstudios.questlog.core.quests.objectives.block;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class BlockMineObjective extends AbstractBlockObjective {
  private final ResourceLocation item;
  @Nullable
  private Item cachedItem = null;

  public BlockMineObjective(JsonObject definition) {
    super(definition);
    this.item = definition.has("item") ? new ResourceLocation(definition.get("item").getAsString()) : null;
  }

  private Item getItem() {
    if (this.cachedItem == null && this.item != null) {
      this.cachedItem = ForgeRegistries.ITEMS.getValue(this.item);
    }

    return this.cachedItem;
  }

  @Override
  protected void registerEventListeners(IEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(EventPriority.LOWEST, this::onBlockDestroy);
  }

  private void onBlockDestroy(BlockEvent.BreakEvent event) {
    if (this.isCompleted()) return;
    if (
        event.getPlayer() instanceof ServerPlayer player &&
        this.getParent().manager.player.equals(player) &&
        event.getState().getBlock().equals(this.getBlock()) &&
        event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(this.getItem())
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
