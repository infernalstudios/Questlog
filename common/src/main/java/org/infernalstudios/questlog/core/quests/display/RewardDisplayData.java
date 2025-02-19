package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.Blittable;

public class RewardDisplayData {

  @Nullable
  private Reward reward;

  private final Component name;

  @Nullable
  private final Blittable icon;

  @Nullable
  private final ResourceLocation claimSound;

  public RewardDisplayData(JsonObject data) {
    String name = JsonUtils.getOrDefault(data, "name", (String) null);

    if (name == null) {
      this.name = Component.translatable("questlog.reward.default");
    } else {
      this.name = JsonUtils.getOrDefault(data, "translatable", false) ? Component.translatable(name) : Component.literal(name);
    }
    this.icon = JsonUtils.getIcon(data, "icon");
    String sound = JsonUtils.getOrDefault(JsonUtils.getOrDefault(data, "sound", new JsonObject()), "claimed", (String) null);

    this.claimSound = sound == null ? null : new ResourceLocation(sound);
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
  public Blittable getIcon() {
    return this.icon;
  }

  @Nullable
  public SoundEvent getClaimSound() {
    return BuiltInRegistries.SOUND_EVENT.get(this.claimSound);
  }
}
