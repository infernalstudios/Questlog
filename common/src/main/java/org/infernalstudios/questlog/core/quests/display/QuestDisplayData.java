package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.AnimatedTexture;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.infernalstudios.questlog.util.texture.Texture;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestDisplayData {

    private final Component title;
    private final Component description;
    @Nullable
    private final Blittable icon;
    @Nullable
    private final ResourceLocation completedSound;
    @Nullable
    private final ResourceLocation triggeredSound;
    private final boolean toastOnUnlock;
    private final boolean toastOnComplete;
    private final boolean showPopupOnUnlock;
    private final boolean hidden;
    private final boolean includeInMain;
    private final ResourceLocation bgTexture;
    private final ResourceLocation rightPanelTexture;
    private final ResourceLocation peripheralTexture;

    @Nullable
    private final ResourceLocation overlayTexture;

    private final int overlayWidth;
    private final int overlayHeight;
    private final int overlayXOffset;
    private final int overlayYOffset;

    @Nullable
    private final Blittable badge;

    private final Component backButtonText;
    private final Component collectButtonText;
    private final Component collectedText;
    private final Component uncollectedText;
    private final Palette palette;

    private final String chapter;
    private final int sortOrder;

    private final int leftPanelWidth;
    private final int rightPanelWidth;
    private final int panelHeight;

    private final int leftPanelXOffset;
    private final int leftPanelYOffset;
    private final int rightPanelXOffset;
    private final int rightPanelYOffset;

    @Nullable
    private List<ObjectiveDisplayData> objectiveDisplay = null;
    @Nullable
    private List<RewardDisplayData> rewardDisplay = null;

    public QuestDisplayData(JsonObject data) {
        boolean translatable = JsonUtils.getOrDefault(data, "translatable", false);
        String title = JsonUtils.getString(data, "title");
        this.title = translatable ? Component.translatable(title) : Component.literal(title);

        this.sortOrder = JsonUtils.getOrDefault(data, "sort_order", 0);

        JsonElement descriptionElement = data.get("description");
        Component parsedDescription = null;

        if (descriptionElement != null) {
            try {
                if (descriptionElement.isJsonArray() || descriptionElement.isJsonObject()) {
                    parsedDescription = Component.Serializer.fromJson(descriptionElement);
                } else if (descriptionElement.isJsonPrimitive()) {
                    String rawStr = descriptionElement.getAsString();
                    if (rawStr.startsWith("[") && rawStr.endsWith("]") && !rawStr.contains("](")) {
                        parsedDescription = Component.Serializer.fromJson(rawStr);
                    } else if (rawStr.startsWith("{") && rawStr.endsWith("}")) {
                        parsedDescription = Component.Serializer.fromJson(rawStr);
                    } else {
                        parsedDescription = parseInlineRichText(translatable ? Component.translatable(rawStr).getString() : rawStr);
                    }
                }
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to parse description for quest", e);
            }
        }

        if (parsedDescription == null) {
            String rawStr = JsonUtils.getOrDefault(data, "description", "");
            parsedDescription = parseInlineRichText(translatable ? Component.translatable(rawStr).getString() : rawStr);
        }

        if (data.has("badge") && data.get("badge").isJsonObject()) {
            JsonObject badgeObj = data.getAsJsonObject("badge");
            String texture = JsonUtils.getString(badgeObj, "texture");
            int u = JsonUtils.getOrDefault(badgeObj, "u", 0);
            int v = JsonUtils.getOrDefault(badgeObj, "v", 0);
            int w = JsonUtils.getOrDefault(badgeObj, "width", 16);
            int h = JsonUtils.getOrDefault(badgeObj, "height", 16);
            int tw = JsonUtils.getOrDefault(badgeObj, "texture_width", 256);
            int th = JsonUtils.getOrDefault(badgeObj, "texture_height", 256);
            int frames = JsonUtils.getOrDefault(badgeObj, "frames", 1);
            int frameTime = JsonUtils.getOrDefault(badgeObj, "frame_time", 100);

            if (frames > 1) {
                this.badge = new AnimatedTexture(new ResourceLocation(texture), w, h, u, v, tw, th, frames, frameTime);
            } else {
                this.badge = new Texture(new ResourceLocation(texture), w, h, u, v, tw, th);
            }
        } else {
            this.badge = null;
        }

        this.description = parsedDescription;
        this.icon = JsonUtils.getIcon(data, "icon");

        this.chapter = JsonUtils.getOrDefault(data, "chapter", "questlog:main");
        boolean isMainChapter = this.chapter.equals("questlog:main") || this.chapter.equals("main");
        this.includeInMain = JsonUtils.getOrDefault(data, "include_in_main", isMainChapter);

        this.leftPanelWidth = JsonUtils.getOrDefault(data, "left_panel_width", 275);
        this.rightPanelWidth = JsonUtils.getOrDefault(data, "right_panel_width", 170);
        this.panelHeight = JsonUtils.getOrDefault(data, "panel_height", 166);

        String overlayLoc = JsonUtils.getOrDefault(data, "overlay", (String) null);
        this.overlayTexture = overlayLoc == null ? null : new ResourceLocation(overlayLoc);

        this.overlayWidth = JsonUtils.getOrDefault(data, "overlay_width", this.leftPanelWidth);
        this.overlayHeight = JsonUtils.getOrDefault(data, "overlay_height", this.panelHeight);
        this.overlayXOffset = JsonUtils.getOrDefault(data, "overlay_x_offset", 0);
        this.overlayYOffset = JsonUtils.getOrDefault(data, "overlay_y_offset", 0);

        String completedSoundLoc = JsonUtils.getOrDefault(data, "completed_sound", (String) null);
        this.completedSound = completedSoundLoc == null ? null : new ResourceLocation(completedSoundLoc);

        String triggeredSoundLoc = JsonUtils.getOrDefault(data, "triggered_sound", (String) null);
        this.triggeredSound = triggeredSoundLoc == null ? null : new ResourceLocation(triggeredSoundLoc);

        String backgroundLoc = JsonUtils.getOrDefault(data, "background_texture", Questlog.MODID + ":textures/gui/quest_page.png");
        String rightPanelLoc = JsonUtils.getOrDefault(data, "right_panel_texture", backgroundLoc);
        String peripheralLoc = JsonUtils.getOrDefault(data, "peripheral_texture", Questlog.MODID + ":textures/gui/quest_peripherals.png");

        this.bgTexture = new ResourceLocation(backgroundLoc);
        this.rightPanelTexture = new ResourceLocation(rightPanelLoc);
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

        this.toastOnUnlock = JsonUtils.getOrDefault(data, "toast_on_unlock", true);
        this.toastOnComplete = JsonUtils.getOrDefault(data, "toast_on_complete", true);
        this.showPopupOnUnlock = JsonUtils.getOrDefault(data, "show_popup_on_unlock", false);

        this.hidden = JsonUtils.getOrDefault(data, "hidden", false);

        this.leftPanelXOffset = JsonUtils.getOrDefault(data, "left_panel_x_offset", 0);
        this.leftPanelYOffset = JsonUtils.getOrDefault(data, "left_panel_y_offset", 0);
        this.rightPanelXOffset = JsonUtils.getOrDefault(data, "right_panel_x_offset", 0);
        this.rightPanelYOffset = JsonUtils.getOrDefault(data, "right_panel_y_offset", 0);
    }

    private Component parseInlineRichText(String text) {
        Pattern pattern = Pattern.compile("\\[([^]]+)]\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(text);
        MutableComponent component = Component.empty();
        int lastEnd = 0;

        while (matcher.find()) {
            component.append(Component.literal(text.substring(lastEnd, matcher.start())));
            String display = matcher.group(1);
            String action = matcher.group(2);

            MutableComponent part = Component.literal(display);
            Style style = Style.EMPTY.withUnderlined(true);

            if (action.startsWith("quest:")) {
                style = style.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, action.substring(6)));
            } else if (action.startsWith("image:")) {
                style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(action)));
            }
            component.append(part.withStyle(style));
            lastEnd = matcher.end();
        }
        component.append(Component.literal(text.substring(lastEnd)));
        return component;
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

    public boolean matchesSearch(String query) {
        if (query == null || query.isBlank()) return true;
        String lowerQuery = query.toLowerCase();

        if (this.title.getString().toLowerCase().contains(lowerQuery)) return true;
        if (this.description.getString().toLowerCase().contains(lowerQuery)) return true;

        if (this.objectiveDisplay != null) {
            for (ObjectiveDisplayData obj : this.objectiveDisplay) {
                if (obj.getName().getString().toLowerCase().contains(lowerQuery)) return true;
            }
        }

        if (this.rewardDisplay != null) {
            for (RewardDisplayData rew : this.rewardDisplay) {
                if (rew.getName().getString().toLowerCase().contains(lowerQuery)) return true;
            }
        }

        return false;
    }

    public String getChapter() {
        return this.chapter;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getDescription() {
        return this.description;
    }

    public List<ObjectiveDisplayData> getObjectiveDisplayData() {
        if (this.objectiveDisplay == null)
            throw new IllegalStateException("QuestDisplayData has not been assigned a quest");
        return this.objectiveDisplay;
    }

    public List<RewardDisplayData> getRewardDisplayData() {
        if (this.rewardDisplay == null)
            throw new IllegalStateException("QuestDisplayData has not been assigned a quest");
        return this.rewardDisplay;
    }

    @Nullable
    public Blittable getBadge() {
        return this.badge;
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

    public int getLeftPanelWidth() {
        return this.leftPanelWidth;
    }

    public int getRightPanelWidth() {
        return this.rightPanelWidth;
    }

    public int getPanelHeight() {
        return this.panelHeight;
    }

    public int getLeftPanelXOffset() {
        return this.leftPanelXOffset;
    }

    public int getLeftPanelYOffset() {
        return this.leftPanelYOffset;
    }

    public int getRightPanelXOffset() {
        return this.rightPanelXOffset;
    }

    public int getRightPanelYOffset() {
        return this.rightPanelYOffset;
    }

    public QuestlogGuiSet getGuiSet() {
        return (this.bgTexture.equals(QuestlogGuiSet.DEFAULT.backgroundLoc) &&
                this.rightPanelTexture.equals(QuestlogGuiSet.DEFAULT.rightPanelLoc) &&
                this.peripheralTexture.equals(QuestlogGuiSet.DEFAULT.peripheralLoc) &&
                this.leftPanelWidth == 275 &&
                this.rightPanelWidth == 170 &&
                this.panelHeight == 166)
                ? QuestlogGuiSet.DEFAULT
                : new QuestlogGuiSet(this.bgTexture, this.rightPanelTexture, this.peripheralTexture, this.leftPanelWidth, this.rightPanelWidth, this.panelHeight);
    }

    public boolean shouldToastOnUnlock() {
        return this.toastOnUnlock;
    }

    public boolean shouldToastOnComplete() {
        return this.toastOnComplete;
    }

    public boolean shouldShowPopupOnUnlock() {
        return this.showPopupOnUnlock;
    }

    public boolean shouldIncludeInMain() {
        return this.includeInMain;
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

    @Nullable
    public ResourceLocation getOverlayTexture() {
        return this.overlayTexture;
    }

    public int getOverlayWidth() {
        return this.overlayWidth;
    }

    public int getOverlayHeight() {
        return this.overlayHeight;
    }

    public int getOverlayXOffset() {
        return this.overlayXOffset;
    }

    public int getOverlayYOffset() {
        return this.overlayYOffset;
    }
}