package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class QuestDisplayData {

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
    private final Component backButtonText;
    private final Component collectButtonText;
    private final Component collectedText;
    private final Component uncollectedText;
    private final Palette palette;
    @Nullable
    private List<ObjectiveDisplayData> objectiveDisplay = null;
    @Nullable
    private List<RewardDisplayData> rewardDisplay = null;

    public QuestDisplayData(JsonObject data, ResourceLocation id) {
        String formattedPath = id.getPath().replace('/', '.');
        String defaultTitleKey = "quest." + id.getNamespace() + "." + formattedPath + ".title";
        String defaultDescKey = "quest." + id.getNamespace() + "." + formattedPath + ".description";

        String rawTitle = JsonUtils.getOrDefault(data, "title", (String) null);
        String rawDescription = JsonUtils.getOrDefault(data, "description", (String) null);

        boolean translatable;
        if (data.has("translatable")) {
            translatable = data.get("translatable").getAsBoolean();
        } else {
            translatable = (rawTitle == null);
        }

        String finalTitle = (rawTitle != null) ? rawTitle : defaultTitleKey;
        String finalDescription = (rawDescription != null) ? rawDescription : defaultDescKey;

        this.title = translatable ? Component.translatable(finalTitle) : Component.literal(finalTitle);
        this.description = translatable ? Component.translatable(finalDescription) : Component.literal(finalDescription);

        this.icon = JsonUtils.getIcon(data, "icon");

        JsonObject sound = JsonUtils.getOrDefault(data, "sound", new JsonObject());
        String completedSoundLoc = JsonUtils.getOrDefault(sound, "completed", (String) null);
        this.completedSound = completedSoundLoc == null ? null : ResourceLocation.parse(completedSoundLoc);

        String triggeredSoundLoc = JsonUtils.getOrDefault(sound, "triggered", (String) null);
        this.triggeredSound = triggeredSoundLoc == null ? null : ResourceLocation.parse(triggeredSoundLoc);

        JsonObject style = JsonUtils.getOrDefault(data, "style", new JsonObject());

        String backgroundLoc = style.has("background")
                ? JsonUtils.getOrDefault(style.getAsJsonObject("background"), "texture", (String) null)
                : null;
        this.bgTexture = ResourceLocation.parse(backgroundLoc == null ? Questlog.MODID + ":textures/gui/quest_page.png" : backgroundLoc);

        String peripheralLoc = style.has("peripheral")
                ? JsonUtils.getOrDefault(style.getAsJsonObject("peripheral"), "texture", (String) null)
                : null;
        this.peripheralTexture = ResourceLocation.parse(peripheralLoc == null ? Questlog.MODID + ":textures/gui/quest_peripherals.png" : peripheralLoc);

        this.palette = new Palette(
                JsonUtils.getOrDefault(style, "textColor", "#4C381B"),
                JsonUtils.getOrDefault(style, "completedTextColor", "#529E52"),
                JsonUtils.getOrDefault(style, "hoveredTextColor", "#FFFFFF"),
                JsonUtils.getOrDefault(style, "titleColor", "#4C381B"),
                JsonUtils.getOrDefault(style, "progressTextColor", "#9E7852")
        );

        this.backButtonText = parseComponent(style, "backButtonText", "gui.back", translatable);
        this.collectButtonText = parseComponent(style, "collectButtonText", "questlog.reward.collect", translatable);
        this.uncollectedText = parseComponent(style, "uncollectedText", "questlog.reward.uncollected", translatable);
        this.collectedText = parseComponent(style, "collectedText", "questlog.reward.collected", translatable);

        JsonObject notification = JsonUtils.getOrDefault(data, "notification", new JsonObject());
        this.toastOnTrigger = JsonUtils.getOrDefault(notification, "toastOnTrigger", true);
        this.toastOnComplete = JsonUtils.getOrDefault(notification, "toastOnComplete", true);
        this.popup = JsonUtils.getOrDefault(notification, "popup", false);

        this.hidden = JsonUtils.getOrDefault(data, "hidden", false);
    }

    private Component parseComponent(JsonObject style, String key, String defaultKey, boolean translatable) {
        String raw = JsonUtils.getOrDefault(style, key, (String) null);
        if (raw == null) return Component.translatable(defaultKey);
        return translatable ? Component.translatable(raw) : Component.literal(raw);
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

    public Component getBackButtonText() {
        return this.backButtonText;
    }

    public Component getCollectButtonText() {
        return this.collectButtonText;
    }

    public Component getCollectedText() {
        return this.collectedText;
    }

    public Component getUncollectedText() {
        return this.uncollectedText;
    }
}