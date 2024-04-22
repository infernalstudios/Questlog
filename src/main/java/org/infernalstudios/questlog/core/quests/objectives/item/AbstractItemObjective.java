package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.infernalstudios.questlog.core.quests.objectives.Objective;

import javax.annotation.Nullable;

public abstract class AbstractItemObjective extends Objective {
  protected final String item;
  @Nullable
  private Item cachedItem = null;
  @Nullable
  private TagKey<Item> cachedTag = null;

  public AbstractItemObjective(JsonObject definition) {
    super(definition);
    this.item = definition.get("item").getAsString();
  }

  private TagKey<Item> getTag() {
    if (this.cachedTag == null) {
      ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
      if (tags == null) throw new IllegalStateException("Item tags are not available yet");
      this.cachedTag = tags.createTagKey(new ResourceLocation(this.item.substring(1)));
    }

    return this.cachedTag;
  }

  private Item getItem() {
    if (this.cachedItem == null) {
      this.cachedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.item));
    }

    return this.cachedItem;
  }

  protected boolean test(Item item) {
    if (this.item.startsWith("#")) {
      return item.getDefaultInstance().is(this.getTag());
    } else {
      return item.equals(this.getItem());
    }
  }

  protected boolean test(ItemStack item) {
    if (this.item.startsWith("#")) {
      return item.is(this.getTag());
    } else {
      return item.is(this.getItem());
    }
  }
}
