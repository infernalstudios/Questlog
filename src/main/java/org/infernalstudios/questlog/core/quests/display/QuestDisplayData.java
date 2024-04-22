package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.Texture;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class QuestDisplayData {
  private final Component title;
  private final Component description;
  @Nullable
  private final Texture icon;
  @Nullable
  private List<ObjectiveDisplayData> objectiveDisplay = null;
  @Nullable
  private List<RewardDisplayData> rewardDisplay = null;

  public QuestDisplayData(JsonObject data) {
    boolean translatable = data.has("translatable") ? data.get("translatable").getAsBoolean() : false;
    String title = data.get("title").getAsString();
    String description = data.get("description").getAsString();
    String icon = data.has("icon") ? data.get("icon").getAsString() : null;

    this.title = translatable ? Component.translatable(title) : Component.literal(title);
    this.description = translatable ? Component.translatable(description) : Component.literal(description);
    this.icon = icon != null ? new Texture(new ResourceLocation(icon), 16, 16, 0, 0, 16, 16) : null;
  }

  public void setQuest(Quest quest) {
    this.objectiveDisplay = quest.objectives.stream().map(WithDisplayData::getDisplay).filter(Objects::nonNull).toList();
    this.rewardDisplay = quest.rewards.stream().map(WithDisplayData::getDisplay).filter(Objects::nonNull).toList();
  }

  public Component getTitle() {
    return this.title;
  }

  public Component getDescription() {
    return this.description;
  }

  public List<ObjectiveDisplayData> getObjectiveDisplayData() {
    if (this.objectiveDisplay == null) {
      throw new IllegalStateException("QuestDisplayData has not been assigned a quest");
    }
    return this.objectiveDisplay;
  }

  public List<RewardDisplayData> getRewardDisplayData() {
    if (this.rewardDisplay == null) {
      throw new IllegalStateException("QuestDisplayData has not been assigned a quest");
    }
    return this.rewardDisplay;
  }

  @Nullable
  public Texture getIcon() {
    return this.icon;
  }
}
