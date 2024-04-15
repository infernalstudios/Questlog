package org.infernalstudios.questlog.core.quests.objectives.block;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import javax.annotation.Nullable;

public class BlockMineObjective extends AbstractBlockObjective {
  private final String item;
  @Nullable
  private Item cachedItem = null;
  @Nullable
  private TagKey<Item> cachedItemTag = null;

  public BlockMineObjective(JsonObject definition) {
    super(definition);
    this.item = definition.has("item") ? definition.get("item").getAsString() : null;
  }

  private TagKey<Item> getItemTag() {
    if (this.cachedItemTag == null) {
      ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
      if (tags == null) throw new IllegalStateException("Item tags are not available yet");
      this.cachedItemTag = tags.createTagKey(new ResourceLocation(this.item.substring(1)));
    }

    return this.cachedItemTag;
  }

  private Item getItem() {
    if (this.cachedItem == null) {
      this.cachedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.item));
    }

    return this.cachedItem;
  }

  private boolean testItem(ItemStack stack) {
    if (this.item == null) {
      return true;
    }

    if (this.item.startsWith("#")) {
      return stack.is(this.getItemTag());
    } else {
      return stack.is(this.getItem());
    }
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
        this.test(event.getState()) &&
        this.testItem(event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND))
    ) {
      this.setUnits(this.getUnits() + 1);
    }
  }
}
