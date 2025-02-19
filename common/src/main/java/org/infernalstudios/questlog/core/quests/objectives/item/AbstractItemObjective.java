package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.CachedRegistryPredicate;
import org.infernalstudios.questlog.util.JsonUtils;

public abstract class AbstractItemObjective extends Objective {

  private final CachedRegistryPredicate<Item> item;

  public AbstractItemObjective(JsonObject definition) {
    super(definition);
    this.item = CachedRegistryPredicate.item(JsonUtils.getString(definition, "item"));
  }

  protected boolean test(Item item) {
    return this.item.test(item);
  }

  protected boolean test(ItemStack item) {
    return this.item.test(item.getItem());
  }
}
