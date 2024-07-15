package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.Renderable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class QuestDisplayData {
  @Nullable
  private List<ObjectiveDisplayData> objectiveDisplay = null;
  @Nullable
  private List<RewardDisplayData> rewardDisplay = null;

  private final Component title;
  private final Component description;
  @Nullable
  private final Renderable icon;

  @Nullable
  private final ResourceLocation completedSound;
  @Nullable
  private final ResourceLocation triggeredSound;

  private final ResourceLocation guiTexture;

  private final boolean toastOnTrigger;
  private final boolean toastOnComplete;
  private final boolean popup;
  private final boolean hidden;

  public QuestDisplayData(JsonObject data) {
    boolean translatable = JsonUtils.getOrDefault(data, "translatable", false);
    String title = data.get("title").getAsString();
    String description = data.get("description").getAsString();

    this.title = translatable ? Component.translatable(title) : Component.literal(title);
    this.description = translatable ? Component.translatable(description) : Component.literal(description);
    this.icon = JsonUtils.getIcon(data, "icon");

    String completedSoundLoc = JsonUtils.getOrDefault(
        JsonUtils.getOrDefault(data, "sound", new JsonObject()),
        "completed", (String) null
    );

    this.completedSound = completedSoundLoc == null ? null : new ResourceLocation(completedSoundLoc);

    String triggeredSoundLoc = JsonUtils.getOrDefault(
        JsonUtils.getOrDefault(data, "sound", new JsonObject()),
        "triggered", (String) null
    );

    this.triggeredSound = triggeredSoundLoc == null ? null : new ResourceLocation(triggeredSoundLoc);

    String backgroundLoc = data.has("background") ? JsonUtils.getOrDefault(JsonUtils.getOrDefault(data, "background", new JsonObject()), "texture", (String) null) : null;

    if (backgroundLoc == null) {
      backgroundLoc = Questlog.MODID + ":textures/gui/questlog.png";
    }

    this.guiTexture = new ResourceLocation(backgroundLoc);

    this.toastOnTrigger = JsonUtils.getOrDefault(
        JsonUtils.getOrDefault(data, "notification", new JsonObject()),
        "toastOnTrigger", true
    );

    this.toastOnComplete = JsonUtils.getOrDefault(
        JsonUtils.getOrDefault(data, "notification", new JsonObject()),
        "toastOnComplete", true
    );

    this.popup = JsonUtils.getOrDefault(
        JsonUtils.getOrDefault(data, "notification", new JsonObject()),
        "popup", false
    );

    this.hidden = JsonUtils.getOrDefault(data, "hidden", false);
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
  public Renderable getIcon() {
    return this.icon;
  }

  @Nullable
  public SoundInstance getCompletedSound() {
    SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(this.completedSound);
    return soundEvent != null ? SimpleSoundInstance.forUI(soundEvent, 1, 1) : null;
  }

  @Nullable
  public SoundInstance getTriggeredSound() {
    SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(this.triggeredSound);
    return soundEvent != null ? SimpleSoundInstance.forUI(soundEvent, 1, 1) : null;
  }

  public QuestlogGuiSet getGuiSet() {
    return this.guiTexture.equals(QuestlogGuiSet.DEFAULT.location) ? QuestlogGuiSet.DEFAULT : new QuestlogGuiSet(this.guiTexture);
  }

  public boolean shouldToastOnTrigger() {
    return this.toastOnTrigger;
  }

  public boolean shouldToastOnComplete() {
    return this.toastOnComplete;
  }

  public boolean shouldPopup() {
    return this.popup;
  }

  public boolean isHidden() {
    return this.hidden;
  }
}
