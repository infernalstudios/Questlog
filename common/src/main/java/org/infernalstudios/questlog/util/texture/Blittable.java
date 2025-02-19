package org.infernalstudios.questlog.util.texture;

import net.minecraft.client.gui.GuiGraphics;

public interface Blittable {
  int width();
  int height();
  void blit(GuiGraphics ps, int x, int y);
}
