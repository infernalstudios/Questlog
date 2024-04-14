package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.util.Util;

import javax.annotation.Nullable;

public class ItemReward extends Reward {
  private final ResourceLocation item;
  @Nullable
  private Item cachedItem = null;
  private final int count;

  public ItemReward(JsonObject definition) {
    super(definition);

    this.item = new ResourceLocation(definition.get("item").getAsString());

    if (definition.has("count")) {
      this.count = definition.get("count").getAsInt();
    } else {
      this.count = 1;
    }
  }

  private Item getItem() {
    if (this.cachedItem == null) {
      this.cachedItem = ForgeRegistries.ITEMS.getValue(this.item);
    }
    return this.cachedItem;
  }

  @Override
  public void applyReward(ServerPlayer player) {
    Item item = this.getItem();
    ItemStack stack = new ItemStack(item, this.count);

    Util.giveToPlayer(player, stack);

    super.applyReward(player);
  }
}
