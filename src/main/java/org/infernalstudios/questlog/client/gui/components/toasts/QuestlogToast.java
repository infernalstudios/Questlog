package org.infernalstudios.questlog.client.gui.components.toasts;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.texture.Texture;

public abstract class QuestlogToast extends AbstractToast {
  private static final Texture BACKGROUND = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      160, 32, 275, 36, 512, 512
  );

  @Override
  protected Texture getBackground() {
    return BACKGROUND;
  }

  @Override
  protected int titleColor() {
    return 0x9E6632;
  }

  @Override
  protected int descriptionColor() {
    return 0x4C381B;
  }
}
