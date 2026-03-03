package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
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

    public QuestDisplayData(JsonObject data) {
        boolean translatable = JsonUtils.getOrDefault(data, "translatable", false);
        String title = JsonUtils.getString(data, "title");
        this.title = translatable ? Component.translatable(title) : Component.literal(title);

        JsonElement descriptionElement = data.get("description");
        Component parsedDescription = null;

        if (descriptionElement != null) {
            try {
                if (descriptionElement.isJsonArray() || descriptionElement.isJsonObject()) {
                    parsedDescription = Component.Serializer.fromJson(descriptionElement, RegistryAccess.EMPTY);
                } else if (descriptionElement.isJsonPrimitive()) {
                    String rawStr = descriptionElement.getAsString();
                    if (rawStr.startsWith("[") || rawStr.startsWith("{")) {
                        parsedDescription = Component.Serializer.fromJson(rawStr, RegistryAccess.EMPTY);
                    } else {
                        parsedDescription = translatable ? Component.translatable(rawStr) : Component.literal(rawStr);
                    }
                }
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to parse description for quest", e);
            }
        }

        if (parsedDescription == null) {
            String rawStr = JsonUtils.getOrDefault(data, "description", "");
            parsedDescription = translatable ? Component.translatable(rawStr) : Component.literal(rawStr);
        }

        this.description = parsedDescription;

        this.icon = JsonUtils.getIcon(data, "icon");

        String completedSoundLoc = JsonUtils.getOrDefault(data, "completed_sound", (String) null);
        this.completedSound = completedSoundLoc == null ? null : new ResourceLocation(completedSoundLoc);

        String triggeredSoundLoc = JsonUtils.getOrDefault(data, "triggered_sound", (String) null);
        this.triggeredSound = triggeredSoundLoc == null ? null : new ResourceLocation(triggeredSoundLoc);

        String backgroundLoc = JsonUtils.getOrDefault(data, "background", Questlog.MODID + ":textures/gui/quest_page.png");
        String peripheralLoc = JsonUtils.getOrDefault(data, "peripheral", Questlog.MODID + ":textures/gui/quest_peripherals.png");

        this.bgTexture = new ResourceLocation(backgroundLoc);
        this.peripheralTexture = new ResourceLocation(peripheralLoc);

        this.palette = new Palette(
                JsonUtils.getOrDefault(data, "text_color", "#4C381B"),
                JsonUtils.getOrDefault(data, "completed_text_color", "#529E52"),
                JsonUtils.getOrDefault(data, "hovered_text_color", "#FFFFFF"),
                JsonUtils.getOrDefault(data, "title_color", "#4C381B"),
                JsonUtils.getOrDefault(data, "progress_text_color", "#9E7852")
        );

        this.backButtonText = parseComponent(data, "back_button_text", "gui.back", translatable);
        this.collectButtonText = parseComponent(data, "collect_button_text", "questlog.reward.collect", translatable);
        this.uncollectedText = parseComponent(data, "uncollected_text", "questlog.reward.uncollected", translatable);
        this.collectedText = parseComponent(data, "collected_text", "questlog.reward.collected", translatable);

        this.toastOnTrigger = JsonUtils.getOrDefault(data, "toast_on_trigger", true);
        this.toastOnComplete = JsonUtils.getOrDefault(data, "toast_on_complete", true);
        this.popup = JsonUtils.getOrDefault(data, "popup", false);

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