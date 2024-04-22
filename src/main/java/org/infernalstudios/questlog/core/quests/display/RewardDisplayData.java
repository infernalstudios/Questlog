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
  private final Component name;
  @Nullable
  private final Texture icon;

  public RewardDisplayData(JsonObject data) {
    boolean translatable = data.has("translatable") ? data.get("translatable").getAsBoolean() : false;
    String name = data.get("name").getAsString();
    String icon = data.has("icon") ? data.get("icon").getAsString() : null;

    this.name = translatable ? Component.translatable(name) : Component.literal(name);
    this.icon = icon != null ? new Texture(new ResourceLocation(icon), 16, 16, 0, 0, 16, 16) : null;
  }

  public void setReward(Reward reward) {
    this.reward = reward;
  }

  public Component getName() {
    return this.name;
  }

  public boolean hasRewarded() {
    if (this.reward == null) {
      throw new IllegalStateException("RewardDisplayData has not been assigned a reward");
    }
    return this.reward.hasRewarded();
  }

  @Nullable
  public Texture getIcon() {
    return this.icon;
  }
}
