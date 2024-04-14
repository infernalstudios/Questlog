package org.infernalstudios.questlog.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public record Texture(ResourceLocation path, int width, int height, int xOffset, int yOffset, int textureWidth, int textureHeight) {
  public void blit(PoseStack ps, int x, int y) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderTexture(0, this.path);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    GuiComponent.blit(ps, x, y, this.xOffset, this.yOffset, this.width, this.height, this.textureWidth, this.textureHeight);
  }
}
