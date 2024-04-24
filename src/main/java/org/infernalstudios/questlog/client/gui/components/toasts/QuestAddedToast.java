package org.infernalstudios.questlog.client.gui.components.toasts;

import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.util.Texture;

import javax.annotation.Nullable;

public class QuestAddedToast extends AbstractToast {
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
  protected Texture getIcon() {
    return this.displayData.getIcon();
  }
}
