package org.infernalstudios.questlog.client.gui.components.toasts;

import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.infernalstudios.questlog.util.texture.Texture;

public class QuestCompletedToast extends QuestlogToast {

  private final QuestDisplayData displayData;

  public QuestCompletedToast(QuestDisplayData displayData) {
    this.displayData = displayData;
  }

  @Override
  protected Component getTitle() {
    return Component.translatable("questlog.toast.quest_completed");
  }

  @Override
  protected Component getDescription() {
    return this.displayData.getTitle();
  }

  @Override
  @Nullable
  protected Blittable getIcon() {
    return this.displayData.getIcon();
  }

  @Override
  @Nullable
  protected SoundInstance getSound() {
    return null; // Sound logic is moved to QuestlogClientEvents
  }

  @Override
  protected Texture getBackground() {
    return this.displayData.getGuiSet().toast;
  }
}
