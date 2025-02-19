package org.infernalstudios.questlog.util.texture;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.infernalstudios.questlog.Questlog;

public class ItemRenderable implements Blittable {

  private final ResourceLocation item;
  private ItemStack cachedStack;

  public ItemRenderable(ResourceLocation item) {
    this.item = item;
  }

  @Nullable
  private ItemStack getItem() {
    if (this.cachedStack == null) {
      Item item = BuiltInRegistries.ITEM.get(this.item);
      if (item != Items.AIR) {
        this.cachedStack = item.getDefaultInstance();
      } else {
        Questlog.LOGGER.warn("Item {} not found", this.item);
      }
    }

    return this.cachedStack;
  }

  @Override
  public int width() {
    return 16;
  }

  @Override
  public int height() {
    return 16;
  }

  @Override
  public void blit(GuiGraphics ps, int x, int y) {
    ItemStack item = this.getItem();
    if (item == null) {
      return;
    }

    ps.renderItem(item, x, y);
  }
}
