package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.Blittable;

public class QuestDisplayData {

  @Nullable
  private List<ObjectiveDisplayData> objectiveDisplay = null;

  @Nullable
  private List<RewardDisplayData> rewardDisplay = null;

  private final Component title;
  private final Component description;

  @Nullable
  private final Blittable icon;

  @Nullable
  private final ResourceLocation completedSound;

  @Nullable
  private final ResourceLocation triggeredSound;

  private final boolean toastOnTrigger;
  private final boolean toastOnComplete;
  private final boolean popup;
  private final boolean hidden;

  private final ResourceLocation bgTexture;
  private final ResourceLocation peripheralTexture;
  private final Component buttonText;

  private final Palette palette;

  public QuestDisplayData(JsonObject data) {
    boolean translatable = JsonUtils.getOrDefault(data, "translatable", false);
    String title = JsonUtils.getString(data, "title");
    String description = JsonUtils.getString(data, "description");

    this.title = translatable ? Component.translatable(title) : Component.literal(title);
    this.description = translatable ? Component.translatable(description) : Component.literal(description);
    this.icon = JsonUtils.getIcon(data, "icon");

    String completedSoundLoc = JsonUtils.getOrDefault(JsonUtils.getOrDefault(data, "sound", new JsonObject()), "completed", (String) null);

    this.completedSound = completedSoundLoc == null ? null : new ResourceLocation(completedSoundLoc);

    String triggeredSoundLoc = JsonUtils.getOrDefault(JsonUtils.getOrDefault(data, "sound", new JsonObject()), "triggered", (String) null);

    this.triggeredSound = triggeredSoundLoc == null ? null : new ResourceLocation(triggeredSoundLoc);

    JsonObject style = JsonUtils.getOrDefault(data, "style", new JsonObject());

    String backgroundLoc = style.has("background")
      ? JsonUtils.getOrDefault(JsonUtils.getOrDefault(style, "background", new JsonObject()), "texture", (String) null)
      : null;

    if (backgroundLoc == null) {
      backgroundLoc = Questlog.MODID + ":textures/gui/quest_page.png";
    }

    String peripheralLoc = style.has("peripheral")
      ? JsonUtils.getOrDefault(JsonUtils.getOrDefault(style, "peripheral", new JsonObject()), "texture", (String) null)
      : null;

    if (peripheralLoc == null) {
      peripheralLoc = Questlog.MODID + ":textures/gui/quest_peripherals.png";
    }

    this.bgTexture = new ResourceLocation(backgroundLoc);
    this.peripheralTexture = new ResourceLocation(peripheralLoc);

    this.palette = new Palette(
      JsonUtils.getOrDefault(style, "textColor", "#4C381B"),
      JsonUtils.getOrDefault(style, "completedTextColor", "#529E52"),
      JsonUtils.getOrDefault(style, "hoveredTextColor", "#FFFFFF"),
      JsonUtils.getOrDefault(style, "titleColor", "#4C381B"),
      JsonUtils.getOrDefault(style, "progressTextColor", "#9E7852")
    );

    String buttonTextRaw = JsonUtils.getOrDefault(style, "buttonText", (String) null);
    if (buttonTextRaw == null) {
      this.buttonText = Component.translatable("gui.back");
    } else {
      this.buttonText = translatable ? Component.translatable(buttonTextRaw) : Component.literal(buttonTextRaw);
    }

    JsonObject notification = JsonUtils.getOrDefault(data, "notification", new JsonObject());

    this.toastOnTrigger = JsonUtils.getOrDefault(notification, "toastOnTrigger", true);
    this.toastOnComplete = JsonUtils.getOrDefault(notification, "toastOnComplete", true);
    this.popup = JsonUtils.getOrDefault(notification, "popup", false);

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
  public Blittable getIcon() {
    return this.icon;
  }

  @Nullable
  public SoundEvent getCompletedSound() {
    return BuiltInRegistries.SOUND_EVENT.get(this.completedSound);
  }

  @Nullable
  public SoundEvent getTriggeredSound() {
    return BuiltInRegistries.SOUND_EVENT.get(this.triggeredSound);
  }

  public QuestlogGuiSet getGuiSet() {
    return (
        this.bgTexture.equals(QuestlogGuiSet.DEFAULT.backgroundLoc) && this.peripheralTexture.equals(QuestlogGuiSet.DEFAULT.peripheralLoc)
      )
      ? QuestlogGuiSet.DEFAULT
      : new QuestlogGuiSet(this.bgTexture, this.peripheralTexture);
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

  public Palette getPalette() {
    return this.palette;
  }

  public Component getButtonText() {
    return this.buttonText;
  }
}
