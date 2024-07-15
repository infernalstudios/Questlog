package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.Renderable;

import javax.annotation.Nullable;

public class RewardDisplayData {
  @Nullable
  private Reward reward;
  private final Component name;
  @Nullable
  private final Renderable icon;
  @Nullable
  private final ResourceLocation claimSound;

  public RewardDisplayData(JsonObject data) {
    String name = JsonUtils.getOrDefault(data, "name", "Reward");
    
    this.name = JsonUtils.getOrDefault(data, "translatable", false) ? Component.translatable(name) : Component.literal(name);
    this.icon = JsonUtils.getIcon(data, "icon");
    String sound = JsonUtils.getOrDefault(
      JsonUtils.getOrDefault(data, "sound", new JsonObject()),
      "claimed", (String) null
    );

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
  public Renderable getIcon() {
    return this.icon;
  }

  @Nullable
  public SoundInstance getClaimSound() {
    SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(this.claimSound);
    return soundEvent != null ? SimpleSoundInstance.forUI(soundEvent, 1, 1) : null;
  }
}
