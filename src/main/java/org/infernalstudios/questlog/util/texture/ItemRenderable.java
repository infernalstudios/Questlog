package org.infernalstudios.questlog.util.texture;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
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

  // Basically copied from ItemRenderer.renderGuiItem(ItemStack, int, int, BakedModel)
  // Hacky way to render an item in a GUI using an existing PoseStack, but vanilla code does not provide a way to do this
  @Override
  public void blit(PoseStack ps, int x, int y) {
    ItemStack item = this.getItem();
    if (item == null) {
      return;
    }

    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    TextureManager textureManager = Minecraft.getInstance().getTextureManager();

    BakedModel model = itemRenderer.getModel(item, null, null, 0);

    textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
    RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
    RenderSystem.enableBlend();
    RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    ps.pushPose();
    ps.translate((double)x, (double)y, (double)(100.0F + itemRenderer.blitOffset));
    ps.translate(8.0, 8.0, 0.0);
    ps.scale(1.0F, -1.0F, 1.0F);
    ps.scale(16.0F, 16.0F, 16.0F);
    RenderSystem.applyModelViewMatrix();
    MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
    Lighting.setupForFlatItems(); // Figure out how to get the correct lighting for 3d items

    itemRenderer.render(item, TransformType.GUI, false, ps, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, model);

    multibuffersource$buffersource.endBatch();
    RenderSystem.enableDepthTest();

    ps.popPose();
    RenderSystem.applyModelViewMatrix();
  }
}
