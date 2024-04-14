package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.util.Texture;

import javax.annotation.Nullable;

public class RewardDisplayData {
  @Nullable
  private Reward reward;
  private final String name;
  private final boolean translatable;
  @Nullable
  private final ResourceLocation icon;

  public RewardDisplayData(JsonObject data) {
    this.name = data.get("name").getAsString();
    this.translatable = data.has("translatable") ? data.get("translatable").getAsBoolean() : false;
    this.icon = data.has("icon") ? new ResourceLocation(data.get("icon").getAsString()) : null;
  }

  public void setReward(Reward reward) {
    this.reward = reward;
  }

  public Component getName() {
    return this.translatable ? Component.translatable(this.name) : Component.literal(this.name);
  }

  public boolean hasRewarded() {
    if (this.reward == null) {
      throw new IllegalStateException("RewardDisplayData has not been assigned a reward");
    }
    return this.reward.hasRewarded();
  }

  @Nullable
  public Texture getIcon() {
    return new Texture(this.icon, 16, 16, 0, 0, 16, 16);
  }
}
