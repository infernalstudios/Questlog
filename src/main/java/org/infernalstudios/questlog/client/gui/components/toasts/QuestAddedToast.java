package org.infernalstudios.questlog.client.gui.components.toasts;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.util.texture.Renderable;
import org.infernalstudios.questlog.util.texture.Texture;

import javax.annotation.Nullable;

public class QuestAddedToast extends QuestlogToast {
  private final QuestDisplayData displayData;

  public QuestAddedToast(QuestDisplayData displayData) {
    this.displayData = displayData;
  }

  @Override
  protected Component getTitle() {
    return Component.translatable("questlog.toast.quest_added");
  }

  @Override
  protected Component getDescription() {
    return this.displayData.getTitle();
  }

  @Override
  @Nullable
  protected Renderable getIcon() {
    return this.displayData.getIcon();
  }

  @Override
  @Nullable
  protected SoundInstance getSound() {
    return this.displayData.getTriggeredSound();
  }

  @Override
  protected Texture getBackground() {
    return this.displayData.getGuiSet().toast;
  }
}
