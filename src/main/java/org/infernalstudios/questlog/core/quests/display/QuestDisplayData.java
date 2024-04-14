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
  @Nullable
  private Quest quest;
  private final String title;
  private final String description;
  private final boolean translatable;
  @Nullable
  private final ResourceLocation icon;
  @Nullable
  private List<ObjectiveDisplayData> objectiveDisplay = null;
  @Nullable
  private List<RewardDisplayData> rewardDisplay = null;

  public QuestDisplayData(JsonObject data) {
    this.title = data.get("title").getAsString();
    this.description = data.get("description").getAsString();
    this.translatable = data.has("translatable") ? data.get("translatable").getAsBoolean() : false;
    this.icon = data.has("icon") ? new ResourceLocation(data.get("icon").getAsString()) : null;
  }

  public void setQuest(Quest quest) {
    this.quest = quest;
    this.objectiveDisplay = this.quest.objectives.stream().map(WithDisplayData::getDisplay).filter(Objects::nonNull).toList();
    this.rewardDisplay = this.quest.rewards.stream().map(WithDisplayData::getDisplay).filter(Objects::nonNull).toList();
  }

  public Component getTitle() {
    return this.translatable ? Component.translatable(this.title) : Component.literal(this.title);
  }

  public Component getDescription() {
    return this.translatable ? Component.translatable(this.description) : Component.literal(this.description);
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
    return new Texture(this.icon, 16, 16, 0, 0, 16, 16);
  }
}
