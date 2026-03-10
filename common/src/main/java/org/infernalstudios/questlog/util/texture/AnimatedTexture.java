package org.infernalstudios.questlog.util.texture;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class AnimatedTexture implements Blittable {

    private final ResourceLocation path;
    private final int width;
    private final int height;
    private final int xOffset;
    private final int yOffset;
    private final int textureWidth;
    private final int textureHeight;
    private final int frames;
    private final int frameTime;

    public AnimatedTexture(ResourceLocation path, int width, int height, int xOffset, int yOffset, int textureWidth, int textureHeight, int frames, int frameTime) {
        this.path = path;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.frames = frames;
        this.frameTime = frameTime;
    }

    @Override
    public void blit(GuiGraphics ps, int x, int y) {
        int currentFrame = (int) ((net.minecraft.Util.getMillis() / this.frameTime) % this.frames);
        ps.blit(this.path, x, y, this.xOffset, this.yOffset + currentFrame * this.height, this.width, this.height, this.textureWidth, this.textureHeight);
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