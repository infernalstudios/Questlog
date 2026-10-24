package org.infernalstudios.questlog.util.texture;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class NineSliceTexture implements Blittable {

    private final ResourceLocation path;
    private final int width;
    private final int height;
    private final int uOffset;
    private final int vOffset;
    private final int uvWidth;
    private final int uvHeight;
    private final int textureWidth;
    private final int textureHeight;
    private final int cornerWidth;
    private final int cornerHeight;

    public NineSliceTexture(ResourceLocation path, int targetWidth, int targetHeight, int uOffset, int vOffset, int uvWidth, int uvHeight, int textureWidth, int textureHeight, int cornerWidth, int cornerHeight) {
        this.path = path;
        this.width = targetWidth;
        this.height = targetHeight;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.uvWidth = uvWidth;
        this.uvHeight = uvHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.cornerWidth = cornerWidth;
        this.cornerHeight = cornerHeight;
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public void blit(GuiGraphics ps, int x, int y) {
        int cw = this.cornerWidth;
        int ch = this.cornerHeight;

        int midW = this.width - cw * 2;
        int midH = this.height - ch * 2;

        int uvMidW = this.uvWidth - cw * 2;
        int uvMidH = this.uvHeight - ch * 2;

        blitPart(ps, x, y, cw, ch, uOffset, vOffset, cw, ch);
        if (midW > 0) blitPart(ps, x + cw, y, midW, ch, uOffset + cw, vOffset, uvMidW, ch);
        blitPart(ps, x + cw + midW, y, cw, ch, uOffset + uvWidth - cw, vOffset, cw, ch);

        if (midH > 0) blitPart(ps, x, y + ch, cw, midH, uOffset, vOffset + ch, cw, uvMidH);
        if (midW > 0 && midH > 0) blitPart(ps, x + cw, y + ch, midW, midH, uOffset + cw, vOffset + ch, uvMidW, uvMidH);
        if (midH > 0) blitPart(ps, x + cw + midW, y + ch, cw, midH, uOffset + uvWidth - cw, vOffset + ch, cw, uvMidH);

        blitPart(ps, x, y + ch + midH, cw, ch, uOffset, vOffset + uvHeight - ch, cw, ch);
        if (midW > 0) blitPart(ps, x + cw, y + ch + midH, midW, ch, uOffset + cw, vOffset + uvHeight - ch, uvMidW, ch);
        blitPart(ps, x + cw + midW, y + ch + midH, cw, ch, uOffset + uvWidth - cw, vOffset + uvHeight - ch, cw, ch);
    }

    private void blitPart(GuiGraphics ps, int x, int y, int w, int h, int u, int v, int uvW, int uvH) {
        ps.blit(this.path, x, y, w, h, u, v, uvW, uvH, this.textureWidth, this.textureHeight);
    }
}