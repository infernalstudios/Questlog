package org.infernalstudios.questlog.util.texture;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.Questlog;

import javax.annotation.Nullable;

public class ItemRenderable implements Renderable {
  private final ResourceLocation item;
  private ItemStack cachedStack;

  public ItemRenderable(ResourceLocation item) {
    this.item = item;
  }

  @Nullable
  private ItemStack getItem() {
    if (this.cachedStack == null) {
      Item item = ForgeRegistries.ITEMS.getValue(this.item);
      if (item != null) {
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
  public void blit(PoseStack ps, int x, int y) {
    ItemStack item = this.getItem();
    if (item == null) {
      return;
    }
    Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(item, x, y);
  }
}
