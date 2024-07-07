package org.infernalstudios.questlog.util.texture;

import com.mojang.blaze3d.vertex.PoseStack;

public interface Renderable {
  int width();
  int height();
  void blit(PoseStack ps, int x, int y);
}
