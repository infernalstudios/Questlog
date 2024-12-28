package org.infernalstudios.questlog.util.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public final class Texture implements Renderable {

  private final ResourceLocation path;
  private final int width;
  private final int height;
  private final int xOffset;
  private final int yOffset;
  private final int textureWidth;
  private final int textureHeight;

  public Texture(ResourceLocation path, int width, int height, int xOffset, int yOffset, int textureWidth, int textureHeight) {
    this.path = path;
    this.width = width;
    this.height = height;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.textureWidth = textureWidth;
    this.textureHeight = textureHeight;
  }

  @Override
  public void blit(PoseStack ps, int x, int y) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderTexture(0, this.path);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    GuiComponent.blit(ps, x, y, this.xOffset, this.yOffset, this.width, this.height, this.textureWidth, this.textureHeight);
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public int height() {
    return height;
  }
}
