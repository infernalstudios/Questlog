package org.infernalstudios.questlog.client.gui.components.toasts;

import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.util.texture.Texture;

public abstract class QuestlogToast extends AbstractToast {

  @Override
  protected Texture getBackground() {
    return QuestlogGuiSet.DEFAULT.toast;
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
