package org.infernalstudios.questlog.core.quests.objectives.block;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.event.GenericEventBus;
import org.infernalstudios.questlog.util.CachedRegistryPredicate;
import org.infernalstudios.questlog.util.JsonUtils;

public class BlockMineObjective extends AbstractBlockObjective {

  @Nullable
  private final CachedRegistryPredicate<Item> item;

  public BlockMineObjective(JsonObject definition) {
    super(definition);
    if (definition.has("item")) {
      this.item = new CachedRegistryPredicate<>(
        JsonUtils.getString(definition, "item"),
        ForgeRegistries.ITEMS,
        Object::equals,
        (tag, item) -> item.getDefaultInstance().is(tag)
      );
    } else {
      this.item = null;
    }
  }

  private boolean testItem(ItemStack stack) {
    if (this.item == null) {
      return true;
    }

    return this.item.test(stack.getItem());
  }

  @Override
  protected void registerEventListeners(GenericEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onBlockDestroy);
  }

  private void onBlockDestroy(BlockEvent.BreakEvent event) {
    if (this.isCompleted() || this.getParent() == null) return;
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
