package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.core.quests.objectives.Objective;

import javax.annotation.Nullable;

public abstract class AbstractItemObjective extends Objective {
  private final ResourceLocation item;
  @Nullable
  private Item cachedItem = null;

  public AbstractItemObjective(JsonObject definition) {
    super(definition);
    this.item = new ResourceLocation(definition.get("item").getAsString());
  }

  protected Item getItem() {
    if (this.cachedItem == null) {
      this.cachedItem = ForgeRegistries.ITEMS.getValue(this.item);
    }

    return this.cachedItem;
  }
}
