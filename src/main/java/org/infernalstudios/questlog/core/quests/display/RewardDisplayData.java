package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.util.texture.ItemRenderable;
import org.infernalstudios.questlog.util.texture.Renderable;
import org.infernalstudios.questlog.util.texture.Texture;

import javax.annotation.Nullable;

public class RewardDisplayData {
  @Nullable
  private Reward reward;
  private final Component name;
  @Nullable
  private final Renderable icon;

  public RewardDisplayData(JsonObject data) {
    boolean translatable = data.has("translatable") ? data.get("translatable").getAsBoolean() : false;
    String name = data.get("name").getAsString();
    JsonObject icon = data.has("icon") ? data.get("icon").getAsJsonObject() : null;
    
    this.name = translatable ? Component.translatable(name) : Component.literal(name);

    if (icon != null) {
      if (icon.has("texture")) {
        this.icon = new Texture(new ResourceLocation(icon.get("texture").getAsString()), 16, 16, 0, 0, 16, 16);
      } else if (icon.has("item")) {
        this.icon = new ItemRenderable(new ResourceLocation(icon.get("item").getAsString()));
      } else {
        this.icon = null;
      }
    } else {
      this.icon = null;
    }
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
  public Renderable getIcon() {
    return this.icon;
  }
}
