package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.util.CachedValue;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.Util;

public class ItemReward extends Reward {

  private final CachedValue<Item> item;
  private final int count;

  public ItemReward(JsonObject definition) {
    super(definition);
    this.item = new CachedValue<>(() -> BuiltInRegistries.ITEM.get(new ResourceLocation(JsonUtils.getString(definition, "item"))));
    this.count = JsonUtils.getOrDefault(definition, "count", 1);
  }

  @Override
  public void applyReward(ServerPlayer player) {
    Item item = this.item.get();
    ItemStack stack = new ItemStack(item, this.count);

    Util.giveToPlayer(player, stack);

    super.applyReward(player);
  }
}
