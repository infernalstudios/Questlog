package org.infernalstudios.questlog.client.gui.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.client.gui.*;
import org.infernalstudios.questlog.client.gui.components.NoShadowEditBox;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.compat.origins.OriginsClientHelper;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.quests.EditorMetadata;
import org.infernalstudios.questlog.core.quests.EditorMetadata.SuggestionType;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.QuestObjectiveRegistry;
import org.infernalstudios.questlog.core.quests.QuestRewardRegistry;
import org.infernalstudios.questlog.network.packet.QuestEditSavePacket;
import org.infernalstudios.questlog.platform.Services;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.NineSliceTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Supplier;

public class QuestEditorScreen extends Screen {
    static final ResourceLocation GEAR_ICON = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_gear.png");
    static final ResourceLocation GEAR_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_gear_highlighted.png");
    static final ResourceLocation CROSS_ICON = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_cross.png");
    static final ResourceLocation CROSS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_cross_highlighted.png");
    static final ResourceLocation PLUS_ICON = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_plus.png");
    static final ResourceLocation PLUS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_plus_highlighted.png");
    static final ResourceLocation DUPLICATE_ICON = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_duplicate.png");
    static final ResourceLocation DUPLICATE_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_duplicate_highlighted.png");
    static final ResourceLocation PRESET_ICON = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_preset.png");
    static final ResourceLocation PRESET_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_preset_highlighted.png");

    private static final ResourceLocation TAB_OBJECTIVES_TEXTURE = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_objectives.png");
    private static final ResourceLocation TAB_OBJECTIVES_SELECTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_objectives_selected.png");
    private static final ResourceLocation TAB_OBJECTIVES_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_objectives_highlighted.png");

    private static final ResourceLocation TAB_PREREQUISITES_TEXTURE = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_prerequisites.png");
    private static final ResourceLocation TAB_PREREQUISITES_SELECTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_prerequisites_selected.png");
    private static final ResourceLocation TAB_PREREQUISITES_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_prerequisites_highlighted.png");

    private static final ResourceLocation TAB_REWARDS_TEXTURE = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_rewards.png");
    private static final ResourceLocation TAB_REWARDS_SELECTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_rewards_selected.png");
    private static final ResourceLocation TAB_REWARDS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_rewards_highlighted.png");

    private static final ResourceLocation TAB_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_settings.png");
    private static final ResourceLocation TAB_SETTINGS_SELECTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_settings_selected.png");
    private static final ResourceLocation TAB_SETTINGS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_settings_highlighted.png");
    private static final List<BoolFieldDef> BOOL_FIELDS = List.of(
            new BoolFieldDef("hidden", false, "Hidden", "questlog.editor.tooltip.hidden"),
            new BoolFieldDef("hide_when_completed", false, "Hide When Completed", "questlog.editor.tooltip.hide_when_completed"),
            new BoolFieldDef("include_in_main", true, "In Main Chapter", "questlog.editor.tooltip.include_in_main"),
            new BoolFieldDef("details_open_by_default", false, "Details Default", "questlog.editor.tooltip.details_default"),
            new BoolFieldDef("disable_details_button", false, "Details Disabled", "questlog.editor.tooltip.details_disabled"),
            new BoolFieldDef("repeatable", false, "Repeatable", "questlog.editor.tooltip.repeatable"),
            new BoolFieldDef("global", false, "Global", "questlog.editor.tooltip.global"),
            new BoolFieldDef("translatable", false, "Translatable", "questlog.editor.tooltip.advanced.translatable"),
            new BoolFieldDef("toast_on_unlock", true, "Toast On Unlock", "questlog.editor.tooltip.advanced.toast_on_unlock"),
            new BoolFieldDef("toast_on_complete", true, "Toast On Complete", "questlog.editor.tooltip.advanced.toast_on_complete"),
            new BoolFieldDef("show_popup_on_unlock", false, "Show Popup On Unlock", "questlog.editor.tooltip.advanced.show_popup_on_unlock")
    );
    private static final List<TextFieldDef> TEXT_FIELDS = List.of(
            new TextFieldDef("left_panel_width", "275", true, false, "questlog.editor.advanced.left_panel_width", "questlog.editor.tooltip.advanced.panel_size"),
            new TextFieldDef("right_panel_width", "170", true, false, "questlog.editor.advanced.right_panel_width", "questlog.editor.tooltip.advanced.panel_size"),
            new TextFieldDef("panel_height", "166", true, false, "questlog.editor.advanced.panel_height", "questlog.editor.tooltip.advanced.panel_size"),
            new TextFieldDef("left_panel_x_offset", "0", true, false, "questlog.editor.advanced.left_panel_x_offset", "questlog.editor.tooltip.advanced.panel_offset"),
            new TextFieldDef("left_panel_y_offset", "0", true, false, "questlog.editor.advanced.left_panel_y_offset", "questlog.editor.tooltip.advanced.panel_offset"),
            new TextFieldDef("right_panel_x_offset", "0", true, false, "questlog.editor.advanced.right_panel_x_offset", "questlog.editor.tooltip.advanced.panel_offset"),
            new TextFieldDef("right_panel_y_offset", "0", true, false, "questlog.editor.advanced.right_panel_y_offset", "questlog.editor.tooltip.advanced.panel_offset"),

            new TextFieldDef("background_texture", "questlog:textures/gui/quest_page.png", false, false, "questlog.editor.advanced.background_texture", "questlog.editor.tooltip.advanced.background_texture"),
            new TextFieldDef("right_panel_texture", "", false, false, "questlog.editor.advanced.right_panel_texture", "questlog.editor.tooltip.advanced.right_panel_texture"),
            new TextFieldDef("peripheral_texture", "questlog:textures/gui/quest_peripherals.png", false, false, "questlog.editor.advanced.peripheral_texture", "questlog.editor.tooltip.advanced.peripheral_texture"),

            new TextFieldDef("overlay", "", false, false, "questlog.editor.advanced.overlay", "questlog.editor.tooltip.advanced.overlay"),
            new TextFieldDef("overlay_width", "", true, false, "questlog.editor.advanced.overlay_width", "questlog.editor.tooltip.advanced.overlay_size"),
            new TextFieldDef("overlay_height", "", true, false, "questlog.editor.advanced.overlay_height", "questlog.editor.tooltip.advanced.overlay_size"),
            new TextFieldDef("overlay_x_offset", "0", true, false, "questlog.editor.advanced.overlay_x_offset", "questlog.editor.tooltip.advanced.panel_offset"),
            new TextFieldDef("overlay_y_offset", "0", true, false, "questlog.editor.advanced.overlay_y_offset", "questlog.editor.tooltip.advanced.panel_offset"),

            new TextFieldDef("completed_sound", "", false, false, "questlog.editor.advanced.completed_sound", "questlog.editor.tooltip.advanced.sound"),
            new TextFieldDef("triggered_sound", "", false, false, "questlog.editor.advanced.triggered_sound", "questlog.editor.tooltip.advanced.sound"),

            new TextFieldDef("text_color", "", false, false, "questlog.editor.advanced.text_color", "questlog.editor.tooltip.advanced.color"),
            new TextFieldDef("completed_text_color", "", false, false, "questlog.editor.advanced.completed_text_color", "questlog.editor.tooltip.advanced.color"),
            new TextFieldDef("hovered_text_color", "", false, false, "questlog.editor.advanced.hovered_text_color", "questlog.editor.tooltip.advanced.color"),
            new TextFieldDef("title_color", "", false, false, "questlog.editor.advanced.title_color", "questlog.editor.tooltip.advanced.color"),
            new TextFieldDef("progress_text_color", "", false, false, "questlog.editor.advanced.progress_text_color", "questlog.editor.tooltip.advanced.color"),

            new TextFieldDef("back_button_text", "", false, false, "questlog.editor.advanced.back_button_text", "questlog.editor.tooltip.advanced.button_text"),
            new TextFieldDef("collect_button_text", "", false, false, "questlog.editor.advanced.collect_button_text", "questlog.editor.tooltip.advanced.button_text"),
            new TextFieldDef("uncollected_text", "", false, false, "questlog.editor.advanced.uncollected_text", "questlog.editor.tooltip.advanced.button_text"),
            new TextFieldDef("collected_text", "", false, false, "questlog.editor.advanced.collected_text", "questlog.editor.tooltip.advanced.button_text"),

            new TextFieldDef("description_completed", "", false, true, "questlog.editor.advanced.description_completed", "questlog.editor.tooltip.advanced.description_variant"),
            new TextFieldDef("description_failed", "", false, true, "questlog.editor.advanced.description_failed", "questlog.editor.tooltip.advanced.description_variant")
    );
    final List<net.minecraft.client.gui.components.AbstractWidget> leftFields = new ArrayList<>();
    final List<net.minecraft.client.gui.components.AbstractWidget> settingsFields = new ArrayList<>();
    final List<String> settingsLabels = new ArrayList<>();
    final List<Integer> settingsRowHeights = new ArrayList<>();
    private final Screen previousScreen;
    private final List<JsonObject> tempObjectives = new ArrayList<>();
    private final List<JsonObject> tempPrerequisites = new ArrayList<>();
    private final List<JsonObject> tempRewards = new ArrayList<>();
    private final Stack<NestingFrame> nestingStack = new Stack<>();
    private final AutocompleteHelper autocompleteHelper = new AutocompleteHelper();
    private final Map<String, Boolean> tempBooleans = new LinkedHashMap<>();
    private final Map<String, String> tempTexts = new LinkedHashMap<>();
    public Component pendingTooltip = null;
    @Nullable
    Quest questToEdit;
    @Nullable
    ContextMenu contextMenu = null;
    RightPageState rightPageState = RightPageState.LIST;
    int selectedEntryIndex = -1;
    String editingType = "questlog:item_obtain";
    @Nullable
    JsonObject editingEntry = null;
    boolean entryLevelsToggle = false;
    NoShadowEditBox idBox;
    NoShadowEditBox titleBox;
    MultiLineEditBox descriptionBox;
    NoShadowEditBox iconBox;
    NoShadowEditBox chapterBox;
    NoShadowEditBox orderBox;
    NoShadowEditBox entryTargetBox;
    NoShadowEditBox entryComponentsBox;
    ActiveTab activeTab = ActiveTab.PREREQUISITES;
    NoShadowEditBox entryNameBox;
    NoShadowEditBox entryAmountBox;
    NoShadowEditBox entryIconBox;
    private ScrollableComponent editEntryScrollable;
    private List<JsonObject> currentNestedList = null;
    @Nullable
    private JsonObject presetJson;
    @Nullable
    private JsonObject originalDefinition;
    private NineSliceTexture bgLeft;
    private NineSliceTexture bgRight;
    private int typeListScroll = 0;
    private String tempId = "";
    private String tempTitle = "";
    private String tempDescription = "";
    private String tempIconItem = "";
    private String tempChapter = "";
    private int tempSortOrder = 0;
    private int listPage = 0;
    private String typeSearchQuery = "";
    private boolean tempSearchFocused = false;
    private ScrollableComponent leftScrollable;
    private ScrollableComponent rightScrollable;
    private ScrollableComponent settingsScrollable;
    private NoShadowEditBox typeSearchBox;

    public QuestEditorScreen(Screen previousScreen) {
        this(previousScreen, null, null);
    }

    public QuestEditorScreen(Screen previousScreen, @Nullable Quest questToEdit) {
        this(previousScreen, questToEdit, null);
    }

    public QuestEditorScreen(Screen previousScreen, @Nullable Quest questToEdit, @Nullable JsonObject presetJson) {
        super(Component.translatable(questToEdit != null ? "questlog.editor.title" : "questlog.editor.add_quest"));
        this.previousScreen = previousScreen;
        this.questToEdit = questToEdit;
        this.presetJson = presetJson;

        this.loadQuestData();
    }

    net.minecraft.client.gui.Font getFont() {
        return this.font;
    }

    private void loadQuestData() {
        for (BoolFieldDef def : BOOL_FIELDS) {
            this.tempBooleans.put(def.key(), def.defaultValue());
        }
        for (TextFieldDef def : TEXT_FIELDS) {
            this.tempTexts.put(def.key(), def.defaultValue());
        }

        if (this.questToEdit != null) {
            this.tempId = this.questToEdit.getId().toString();
            try {
                JsonObject definition = DefinitionUtil.getCachedQuest(this.questToEdit.getId());
                this.originalDefinition = definition.deepCopy();
                this.loadFromDefinition(definition);
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to load quest definition for editing", e);
            }
        } else if (this.presetJson != null) {
            this.tempId = "questlog:new_quest_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            try {
                this.originalDefinition = this.presetJson.deepCopy();
                this.loadFromDefinition(this.presetJson);
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to load quest preset definition", e);
            }
        } else {
            this.tempId = "questlog:new_quest_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            this.tempTitle = "New Quest";
            this.tempDescription = "Describe your quest here...";
            this.tempIconItem = "minecraft:knowledge_book";
            this.tempChapter = "main";
            this.tempSortOrder = 0;
            this.tempObjectives.clear();
            this.tempPrerequisites.clear();
            this.tempRewards.clear();
            this.originalDefinition = null;
        }
    }

    private void loadFromDefinition(JsonObject definition) {
        this.tempTitle = definition.has("title") ? definition.get("title").getAsString() : "";
        this.tempDescription = definition.has("description") ? definition.get("description").getAsString() : "";

        if (definition.has("icon") && definition.get("icon").isJsonObject()) {
            JsonObject iconObj = definition.getAsJsonObject("icon");
            if (iconObj.has("item")) {
                JsonElement itemEl = iconObj.get("item");
                this.tempIconItem = itemEl.isJsonPrimitive() ? itemEl.getAsString() : itemEl.toString();
            } else {
                this.tempIconItem = "";
            }
        }

        this.tempChapter = definition.has("chapter") ? definition.get("chapter").getAsString() : "main";
        this.tempSortOrder = definition.has("sort_order") ? definition.get("sort_order").getAsInt() : (definition.has("order") ? definition.get("order").getAsInt() : 0);

        for (BoolFieldDef def : BOOL_FIELDS) {
            this.tempBooleans.put(def.key(), definition.has(def.key()) ? definition.get(def.key()).getAsBoolean() : def.defaultValue());
        }
        for (TextFieldDef def : TEXT_FIELDS) {
            if (definition.has(def.key()) && !definition.get(def.key()).isJsonNull()) {
                JsonElement el = definition.get(def.key());
                this.tempTexts.put(def.key(), el.isJsonPrimitive() ? el.getAsString() : el.toString());
            } else {
                this.tempTexts.put(def.key(), def.defaultValue());
            }
        }

        this.loadList(definition.getAsJsonArray("objectives"), this.tempObjectives);
        this.loadList(definition.has("prerequisites") ? definition.getAsJsonArray("prerequisites") : definition.getAsJsonArray("requirements"), this.tempPrerequisites);
        this.loadList(definition.getAsJsonArray("failures"), null);
        this.loadList(definition.getAsJsonArray("rewards"), this.tempRewards);
    }

    private void loadList(@Nullable JsonArray array, List<JsonObject> target) {
        if (target == null) return;
        target.clear();
        if (array != null) {
            for (JsonElement el : array) {
                if (el.isJsonObject()) {
                    target.add(JsonParser.parseString(el.toString()).getAsJsonObject());
                }
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        int PANEL_SPACING = 6;
        int leftWidth = 240;
        int rightWidth = 160;
        int height = 190;
        int totalWidth = leftWidth + rightWidth + PANEL_SPACING;
        int baseX = (this.width - totalWidth) / 2;
        int baseY = (this.height - height) / 2;

        int panel1X = baseX;
        int panel1Y = baseY;
        int panel2X = baseX + leftWidth + PANEL_SPACING;
        int panel2Y = baseY;

        this.bgLeft = new NineSliceTexture(QuestlogGuiSet.DEFAULT.backgroundLoc, leftWidth, height, 375, 174, 275, 166, 1024, 512, 16, 16);
        this.bgRight = new NineSliceTexture(QuestlogGuiSet.DEFAULT.rightPanelLoc, rightWidth, height, 375, 174, 275, 166, 1024, 512, 16, 16);

        this.idBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 22, 195, 16, Component.empty());
        this.idBox.setMaxLength(64);
        this.idBox.setValue(this.tempId);
        this.idBox.setEditable(this.questToEdit == null);
        this.idBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.id")));

        this.titleBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 54, 195, 16, Component.empty());
        this.titleBox.setMaxLength(64);
        this.titleBox.setValue(this.tempTitle);
        this.titleBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.title")));

        this.descriptionBox = new MultiLineEditBox(this.font, panel1X + 15, panel1Y + 86, 195, 54, Component.empty(), Component.empty());
        this.descriptionBox.setCharacterLimit(Integer.MAX_VALUE);
        this.descriptionBox.setValue(this.tempDescription);
        this.descriptionBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.description")));

        this.iconBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 156, 195, 16, Component.empty());
        this.iconBox.setMaxLength(128);
        this.iconBox.setValue(this.tempIconItem);
        this.iconBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.icon")));

        this.chapterBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 188, 130, 16, Component.empty());
        this.chapterBox.setMaxLength(64);
        this.chapterBox.setValue(this.tempChapter);
        this.chapterBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.chapter")));

        this.orderBox = new NoShadowEditBox(this.font, panel1X + 165, panel1Y + 188, 50, 16, Component.empty());
        this.orderBox.setMaxLength(8);
        this.orderBox.setValue(String.valueOf(this.tempSortOrder));
        this.orderBox.setFilter(s -> s.isEmpty() || s.matches("-?\\d*"));
        this.orderBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.order")));

        this.leftFields.clear();
        this.leftFields.add(this.idBox);
        this.leftFields.add(this.titleBox);
        this.leftFields.add(this.descriptionBox);
        this.leftFields.add(this.iconBox);
        this.leftFields.add(this.chapterBox);
        this.leftFields.add(this.orderBox);

        this.leftScrollable = new ScrollableComponent(panel1X + 10, panel1Y + 12, 220, 170, new LeftPanelScrollable(this));
        this.addRenderableWidget(this.leftScrollable);

        if (this.rightPageState == RightPageState.LIST) {
            this.buildRightPageList(panel2X, panel2Y);
        } else if (this.rightPageState == RightPageState.SELECT_TYPE) {
            this.buildRightPageSelectType(panel2X, panel2Y);
        } else if (this.rightPageState == RightPageState.EDIT_ENTRY) {
            this.buildRightPageEditEntry(panel2X, panel2Y);
        }

        int btnWidth = 100;
        int bottomY = panel1Y + height + 10;

        this.addRenderableWidget(Button.builder(Component.translatable("questlog.editor.cancel"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.previousScreen);
            }
        }).bounds(panel1X, bottomY, btnWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("questlog.editor.save"), btn -> {
            this.saveTemporaryState();
            this.saveQuestToServer();
        }).bounds(panel1X + btnWidth + 5, bottomY, btnWidth, 20).build());

        if (this.questToEdit != null) {
            Button btnDuplicate = Button.builder(Component.translatable("questlog.editor.duplicate"), btn -> {
                this.saveTemporaryState();
                this.tempId = this.tempId + "_copy";
                this.questToEdit = null;
                this.rebuildWidgets();
            }).bounds(panel2X, bottomY, 75, 20).build();
            btnDuplicate.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.duplicate_quest")));
            this.addRenderableWidget(btnDuplicate);

            Button btnDelete = Button.builder(Component.translatable("questlog.editor.delete"), btn -> {
                this.deleteQuestOnServer();
            }).bounds(panel2X + 80, bottomY, 80, 20).build();
            btnDelete.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.delete_quest")));
            this.addRenderableWidget(btnDelete);
        } else {
            Button btnPresets = Button.builder(Component.translatable("questlog.editor.presets"), btn -> {
                this.openPresetsContextMenu();
            }).bounds(panel2X, bottomY, 160, 20).build();
            this.addRenderableWidget(btnPresets);
        }
    }

    private void openPresetsContextMenu() {
        int PANEL_SPACING = 6;
        int leftWidth = 240;
        int rightWidth = 160;
        int height = 190;
        int totalWidth = leftWidth + rightWidth + PANEL_SPACING;
        int baseX = (this.width - totalWidth) / 2;
        int baseY = (this.height - height) / 2;
        int panel2X = baseX + leftWidth + PANEL_SPACING;
        int bottomY = baseY + height + 10;

        List<ContextMenuItem> items = new ArrayList<>();
        for (EditorUtils.EditorPreset preset : EditorUtils.getPresets("quests")) {
            items.add(new ContextMenuItem(
                    Component.literal(preset.getTitle()),
                    () -> {
                        try {
                            JsonObject presetJsonCloned = preset.getJson().deepCopy();
                            if (this.chapterBox != null) {
                                presetJsonCloned.addProperty("chapter", this.chapterBox.getValue());
                            }
                            this.presetJson = presetJsonCloned;
                            this.loadQuestData();
                            this.rebuildWidgets();
                        } catch (Exception e) {
                            Questlog.LOGGER.error("Failed to load preset in editor", e);
                        }
                    },
                    preset.getDescription().isEmpty() ? null : Component.literal(preset.getDescription())
            ));
        }

        if (!items.isEmpty()) {
            int menuHeight = Math.min(items.size(), 8) * 18 + 6;
            this.contextMenu = new ContextMenu(panel2X, bottomY - menuHeight - 2, items, this.font, this.width, this.height);
        }
    }

    public void openEntryPresetsContextMenu(int x, int y) {
        String subfolder = switch (this.activeTab) {
            case OBJECTIVES -> "objectives";
            case PREREQUISITES -> "prerequisites";
            case REWARDS -> "rewards";
            default -> null;
        };
        if (subfolder == null) return;

        List<ContextMenuItem> items = new ArrayList<>();
        for (EditorUtils.EditorPreset preset : EditorUtils.getPresets(subfolder)) {
            items.add(new ContextMenuItem(
                    Component.literal(preset.getTitle()),
                    () -> {
                        try {
                            JsonObject entryJson = preset.getJson().deepCopy();
                            entryJson.remove("title");
                            entryJson.remove("description");
                            this.addEntryPreset(entryJson);
                        } catch (Exception e) {
                            Questlog.LOGGER.error("Failed to load entry preset in editor", e);
                        }
                    },
                    preset.getDescription().isEmpty() ? null : Component.literal(preset.getDescription())
            ));
        }

        if (!items.isEmpty()) {
            int menuHeight = Math.min(items.size(), 8) * 18 + 6;
            this.contextMenu = new ContextMenu(x, y - menuHeight - 2, items, this.font, this.width, this.height);
        }
    }

    private void addEntryPreset(JsonObject entryJson) {
        this.saveTemporaryState();
        List<JsonObject> list = getActiveList();
        list.add(entryJson);
        this.updateParentEntryWithChildren();
        this.rebuildWidgets();
    }

    private void buildRightPageList(int panel2X, int panel2Y) {
        if (this.nestingStack.isEmpty()) {
            ActiveTab[] tabs = ActiveTab.values();
            int tabBtnSize = 20;
            int tabBtnSpacing = 28;
            int startX = panel2X + (160 - (4 * tabBtnSize + 3 * (tabBtnSpacing - tabBtnSize))) / 2;

            for (int i = 0; i < tabs.length; i++) {
                ActiveTab t = tabs[i];
                boolean isCurrentTab = t == this.activeTab;

                Component tooltipText = switch (t) {
                    case OBJECTIVES -> Component.translatable("questlog.editor.objectives");
                    case PREREQUISITES -> Component.translatable("questlog.editor.prerequisites");
                    case REWARDS -> Component.translatable("questlog.editor.rewards");
                    case SETTINGS -> Component.translatable("questlog.editor.settings");
                };

                AbstractButton tabButton = new AbstractButton(startX + i * tabBtnSpacing, panel2Y + 8, tabBtnSize, tabBtnSize, tooltipText) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
                        boolean hovered = this.isHoveredOrFocused();
                        ResourceLocation drawTex;
                        if (isCurrentTab) {
                            drawTex = switch (t) {
                                case OBJECTIVES -> TAB_OBJECTIVES_SELECTED;
                                case PREREQUISITES -> TAB_PREREQUISITES_SELECTED;
                                case REWARDS -> TAB_REWARDS_SELECTED;
                                case SETTINGS -> TAB_SETTINGS_SELECTED;
                            };
                        } else {
                            drawTex = hovered ? switch (t) {
                                case OBJECTIVES -> TAB_OBJECTIVES_HIGHLIGHTED;
                                case PREREQUISITES -> TAB_PREREQUISITES_HIGHLIGHTED;
                                case REWARDS -> TAB_REWARDS_HIGHLIGHTED;
                                case SETTINGS -> TAB_SETTINGS_HIGHLIGHTED;
                            } : switch (t) {
                                case OBJECTIVES -> TAB_OBJECTIVES_TEXTURE;
                                case PREREQUISITES -> TAB_PREREQUISITES_TEXTURE;
                                case REWARDS -> TAB_REWARDS_TEXTURE;
                                case SETTINGS -> TAB_SETTINGS_TEXTURE;
                            };
                        }
                        ps.blit(drawTex, this.getX() + 2, this.getY() + 2, 0, 0, 16, 16, 16, 16);
                    }

                    @Override
                    public void onPress() {
                        if (!isCurrentTab) {
                            QuestEditorScreen.this.saveTemporaryState();
                            QuestEditorScreen.this.nestingStack.clear();
                            QuestEditorScreen.this.currentNestedList = null;
                            QuestEditorScreen.this.activeTab = t;
                            QuestEditorScreen.this.listPage = 0;
                            QuestEditorScreen.this.rebuildWidgets();
                        }
                    }

                    @Override
                    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
                    }
                };

                tabButton.setTooltip(Tooltip.create(tooltipText));
                this.addRenderableWidget(tabButton);
            }
        } else {
            this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> {
                this.saveTemporaryState();
                if (!this.nestingStack.isEmpty()) {
                    NestingFrame frame = this.nestingStack.pop();
                    this.editingEntry = frame.parentEntry;
                    this.rightPageState = RightPageState.EDIT_ENTRY;
                    this.selectedEntryIndex = frame.selectedEntryIndex;
                    this.editingType = frame.editingType;
                    this.entryLevelsToggle = frame.entryLevelsToggle;
                    this.currentNestedList = frame.activeList;
                    this.rebuildWidgets();
                }
            }).bounds(panel2X + 14, panel2Y + 12, 40, 16).build());
        }

        if (this.nestingStack.isEmpty() && this.activeTab == ActiveTab.SETTINGS) {
            this.buildSettingsPanel(panel2X, panel2Y);
        } else {
            this.rightScrollable = new ScrollableComponent(panel2X + 10, panel2Y + 28, 220, 126, new RightPanelScrollable(this));
            this.addRenderableWidget(this.rightScrollable);

            AbstractButton btnAddEntry = createImageButton(panel2X + 60, panel2Y + 162, PLUS_ICON, PLUS_HIGHLIGHTED, () -> {
                this.saveTemporaryState();
                this.selectedEntryIndex = -1;
                this.editingEntry = new JsonObject();
                this.typeSearchQuery = "";
                this.rightPageState = RightPageState.SELECT_TYPE;
                this.rebuildWidgets();
            });
            btnAddEntry.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.add_entry")));
            this.addRenderableWidget(btnAddEntry);

            AbstractButton btnPresetsEntry = createImageButton(panel2X + 84, panel2Y + 162, PRESET_ICON, PRESET_HIGHLIGHTED, () -> {
                this.openEntryPresetsContextMenu(panel2X + 84, panel2Y + 162);
            });
            btnPresetsEntry.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.presets_entry")));
            this.addRenderableWidget(btnPresetsEntry);
        }
    }

    private void buildSettingsPanel(int panel2X, int panel2Y) {
        this.settingsFields.clear();
        this.settingsLabels.clear();
        this.settingsRowHeights.clear();

        for (BoolFieldDef def : BOOL_FIELDS) {
            boolean current = this.tempBooleans.getOrDefault(def.key(), def.defaultValue());
            Button toggle = Button.builder(Component.literal(def.label() + ": " + (current ? "True" : "False")), btn -> {
                boolean next = !this.tempBooleans.getOrDefault(def.key(), def.defaultValue());
                this.tempBooleans.put(def.key(), next);
                btn.setMessage(Component.literal(def.label() + ": " + (next ? "True" : "False")));
            }).bounds(0, 0, 107, 16).build();
            if (def.tooltipKey() != null) {
                toggle.setTooltip(Tooltip.create(Component.translatable(def.tooltipKey())));
            }
            this.settingsFields.add(toggle);
            this.settingsLabels.add(null);
            this.settingsRowHeights.add(18);
        }

        for (TextFieldDef def : TEXT_FIELDS) {
            if (def.longText()) {
                MultiLineEditBox box = new MultiLineEditBox(this.font, 0, 0, 107, 40, Component.empty(), Component.empty());
                box.setCharacterLimit(Integer.MAX_VALUE);
                box.setValue(this.tempTexts.getOrDefault(def.key(), def.defaultValue()));
                if (def.tooltipKey() != null) {
                    box.setTooltip(Tooltip.create(Component.translatable(def.tooltipKey())));
                }
                this.settingsFields.add(box);
                this.settingsLabels.add(def.labelKey());
                this.settingsRowHeights.add(56);
            } else {
                NoShadowEditBox box = new NoShadowEditBox(this.font, 0, 0, 107, 14, Component.empty());
                box.setMaxLength(def.numeric() ? 8 : 256);
                if (def.numeric()) {
                    box.setFilter(s -> s.isEmpty() || s.matches("-?\\d*"));
                }
                box.setValue(this.tempTexts.getOrDefault(def.key(), def.defaultValue()));
                if (def.tooltipKey() != null) {
                    box.setTooltip(Tooltip.create(Component.translatable(def.tooltipKey())));
                }
                this.settingsFields.add(box);
                this.settingsLabels.add(def.labelKey());
                this.settingsRowHeights.add(27);
            }
        }

        this.settingsScrollable = new ScrollableComponent(panel2X + 18, panel2Y + 28, 132, 152, new SettingsPanelScrollable(this));
        this.addRenderableWidget(this.settingsScrollable);
    }

    private void buildRightPageSelectType(int panel2X, int panel2Y) {
        this.typeSearchBox = new NoShadowEditBox(this.font, panel2X + 15, panel2Y + 20, 130, 14, Component.empty());
        this.typeSearchBox.setMaxLength(30);
        this.typeSearchBox.setValue(this.typeSearchQuery);
        this.typeSearchBox.setResponder(query -> {
            this.typeSearchQuery = query;
            this.typeListScroll = 0;
        });
        this.addRenderableWidget(this.typeSearchBox);
        if (this.tempSearchFocused) {
            this.typeSearchBox.setFocused(true);
        }

        this.addRenderableWidget(Button.builder(Component.translatable("questlog.editor.cancel"), btn -> {
            this.saveTemporaryState();
            this.rightPageState = RightPageState.LIST;
            this.rebuildWidgets();
        }).bounds(panel2X + 15, panel2Y + 162, 130, 16).build());
    }

    private void buildRightPageEditEntry(int panel2X, int panel2Y) {
        this.entryNameBox = new NoShadowEditBox(this.font, 0, 0, 125, 14, Component.empty());
        this.entryNameBox.setMaxLength(64);
        String nameVal = this.editingEntry != null && this.editingEntry.has("name") ? this.editingEntry.get("name").getAsString() : "";
        this.entryNameBox.setValue(nameVal);
        this.entryNameBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.entry_name")));

        EditorMetadata meta = getMetadata(this.editingType);

        boolean isLogical = "questlog:or".equals(this.editingType) || "questlog:and".equals(this.editingType) || "questlog:not".equals(this.editingType)
                || "or".equals(this.editingType) || "and".equals(this.editingType) || "not".equals(this.editingType);

        boolean isChoice = "questlog:choice".equals(this.editingType) || "choice".equals(this.editingType);

        if (isLogical) {
            String btnText = ("questlog:not".equals(this.editingType) || "not".equals(this.editingType)) ? "Edit Child" : "Edit Children";
            this.addRenderableWidget(Button.builder(Component.literal(btnText), btn -> {
                this.saveTemporaryState();
                this.saveEditingEntry();
                this.nestingStack.push(new NestingFrame(
                        this.editingEntry,
                        this.getActiveList(),
                        this.selectedEntryIndex,
                        this.listPage,
                        this.rightPageState,
                        this.editingType,
                        this.editingEntry,
                        this.entryLevelsToggle
                ));
                this.rightPageState = RightPageState.LIST;
                this.listPage = 0;
                this.selectedEntryIndex = -1;
                this.editingEntry = null;
                this.currentNestedList = null;
                this.rebuildWidgets();
            }).bounds(panel2X + 15, panel2Y + 62, 130, 16).build());

            this.entryTargetBox = null;
            this.entryComponentsBox = null;
            this.entryAmountBox = null;
        } else if (isChoice) {
            this.addRenderableWidget(Button.builder(Component.literal("Edit Choices"), btn -> {
                this.saveTemporaryState();
                this.saveEditingEntry();
                this.nestingStack.push(new NestingFrame(
                        this.editingEntry,
                        this.getActiveList(),
                        this.selectedEntryIndex,
                        this.listPage,
                        this.rightPageState,
                        this.editingType,
                        this.editingEntry,
                        this.entryLevelsToggle
                ));
                this.rightPageState = RightPageState.LIST;
                this.listPage = 0;
                this.selectedEntryIndex = -1;
                this.editingEntry = null;
                this.currentNestedList = null;
                this.rebuildWidgets();
            }).bounds(panel2X + 15, panel2Y + 62, 130, 16).build());

            this.entryTargetBox = null;
            this.entryComponentsBox = null;

            if (meta == null || meta.amountFieldKey() != null) {
                this.entryAmountBox = new NoShadowEditBox(this.font, 0, 0, 50, 14, Component.empty());
                this.entryAmountBox.setMaxLength(6);
                this.entryAmountBox.setFilter(s -> s.isEmpty() || s.matches("\\d*"));
                int amtVal = getAmountValue();
                this.entryAmountBox.setValue(String.valueOf(amtVal));
                this.entryAmountBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.entry_amount")));
            } else {
                this.entryAmountBox = null;
            }
        } else {
            if (meta == null || meta.targetFieldKey() != null) {
                this.entryTargetBox = new NoShadowEditBox(this.font, 0, 0, 125, 14, Component.empty());
                this.entryTargetBox.setMaxLength(128);
                String targetVal = getTargetFieldValue();
                this.entryTargetBox.setValue(targetVal);
                String tooltipKey = (meta != null && "bounds".equals(meta.targetFieldKey())) ? "questlog.editor.tooltip.entry_target.bounds" : "questlog.editor.tooltip.entry_target";
                this.entryTargetBox.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
            } else {
                this.entryTargetBox = null;
            }

            if (supportsComponents(this.editingType)) {
                this.entryComponentsBox = new NoShadowEditBox(this.font, 0, 0, 125, 14, Component.empty());
                this.entryComponentsBox.setMaxLength(512);
                String compVal = getComponentsFieldValue();
                this.entryComponentsBox.setValue(compVal);
                String compTooltip = isEntityObjective(this.editingType) ?
                        "Expected predicate JSON format:\n{\"nbt\": \"{CustomName: '\\\"Legend\\\"'}\"}" :
                        "Expected components JSON format:\n{\"minecraft:custom_name\": \"'Magic Feather'\", \"minecraft:enchantments\": {\"levels\": {\"minecraft:sharpness\": 5}}}";
                this.entryComponentsBox.setTooltip(Tooltip.create(Component.literal(compTooltip)));
            } else {
                this.entryComponentsBox = null;
            }

            if (meta == null || meta.amountFieldKey() != null) {
                this.entryAmountBox = new NoShadowEditBox(this.font, 0, 0, 50, 14, Component.empty());
                this.entryAmountBox.setMaxLength(6);
                this.entryAmountBox.setFilter(s -> s.isEmpty() || s.matches("\\d*"));
                int amtVal = getAmountValue();
                this.entryAmountBox.setValue(String.valueOf(amtVal));
                this.entryAmountBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.entry_amount")));
            } else {
                this.entryAmountBox = null;
            }
        }

        if ("questlog:experience".equals(this.editingType)) {
            Button btnLevels = Button.builder(Component.literal("Levels: " + (this.entryLevelsToggle ? "True" : "False")), btn -> {
                this.entryLevelsToggle = !this.entryLevelsToggle;
                btn.setMessage(Component.literal("Levels: " + (this.entryLevelsToggle ? "True" : "False")));
            }).bounds(panel2X + 75, panel2Y + 84, 70, 14).build();
            btnLevels.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.levels")));
            this.addRenderableWidget(btnLevels);
        }

        this.entryIconBox = new NoShadowEditBox(this.font, 0, 0, 125, 14, Component.empty());
        this.entryIconBox.setMaxLength(128);
        String iconVal = getIconFieldValue();
        this.entryIconBox.setValue(iconVal);
        this.entryIconBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.entry_icon")));

        this.editEntryScrollable = new ScrollableComponent(panel2X + 10, panel2Y + 28, 140, 130, new EditEntryPanelScrollable(this));
        this.addRenderableWidget(this.editEntryScrollable);

        this.addRenderableWidget(Button.builder(Component.translatable("questlog.editor.cancel"), btn -> {
            this.saveTemporaryState();
            this.rightPageState = RightPageState.LIST;
            this.rebuildWidgets();
        }).bounds(panel2X + 15, panel2Y + 162, 60, 16).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> {
            this.saveTemporaryState();
            this.saveEditingEntry();
            this.rightPageState = RightPageState.LIST;
            this.rebuildWidgets();
        }).bounds(panel2X + 85, panel2Y + 162, 60, 16).build());
    }

    private String getEditingTypeTitle() {
        String type = this.editingType;
        if (type == null) return "";
        int colonIndex = type.indexOf(':');
        if (colonIndex != -1) {
            type = type.substring(colonIndex + 1);
        }
        String replaced = type.replace('_', ' ');
        String[] words = replaced.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1).toLowerCase());
                }
            }
            if (i < words.length - 1) {
                sb.append(" ");
            }
        }
        String title = sb.toString();
        if (this.font.width(title) > 130) {
            title = this.font.plainSubstrByWidth(title, 120) + "...";
        }
        return title;
    }

    EditorMetadata getMetadata(String typeStr) {
        ResourceLocation rl = ResourceLocation.tryParse(typeStr);
        if (rl == null) return null;
        if (this.activeTab == ActiveTab.REWARDS) {
            return QuestRewardRegistry.getMetadata(rl);
        } else {
            return QuestObjectiveRegistry.getMetadata(rl);
        }
    }

    String getTargetFieldLabel() {
        EditorMetadata meta = getMetadata(this.editingType);
        if (meta != null && meta.targetFieldLabel() != null) {
            return meta.targetFieldLabel();
        }
        return "Target Key:";
    }

    private String getTargetFieldValue() {
        if (this.editingEntry == null) return "";
        EditorMetadata meta = getMetadata(this.editingType);
        if (meta != null && meta.targetFieldKey() != null) {
            String key = meta.targetFieldKey();
            if (this.editingEntry.has(key)) {
                JsonElement el = this.editingEntry.get(key);
                return el.isJsonPrimitive() ? el.getAsString() : el.toString();
            }
        }
        String[] keys = new String[]{"block", "item", "entity", "biome", "dimension", "structure", "advancement", "stat", "quest", "enchantment", "effect", "command", "loot_table", "bounds"};
        for (String k : keys) {
            if (this.editingEntry.has(k)) {
                JsonElement el = this.editingEntry.get(k);
                return el.isJsonPrimitive() ? el.getAsString() : el.toString();
            }
        }
        return "";
    }

    private int getAmountValue() {
        if (this.editingEntry == null) return 1;
        EditorMetadata meta = getMetadata(this.editingType);
        if (meta != null && meta.amountFieldKey() != null) {
            String key = meta.amountFieldKey();
            if (this.editingEntry.has(key)) {
                return this.editingEntry.get(key).getAsInt();
            }
        }
        if (this.editingEntry.has("required_amount")) {
            return this.editingEntry.get("required_amount").getAsInt();
        }
        if (this.editingEntry.has("count")) {
            return this.editingEntry.get("count").getAsInt();
        }
        if (this.editingEntry.has("experience")) {
            return this.editingEntry.get("experience").getAsInt();
        }
        return 1;
    }

    private String getIconFieldValue() {
        if (this.editingEntry == null || !this.editingEntry.has("icon")) return "";
        JsonElement el = this.editingEntry.get("icon");
        if (el.isJsonPrimitive()) {
            return el.getAsString();
        } else if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.has("item")) {
                JsonElement itemEl = obj.get("item");
                return itemEl.isJsonPrimitive() ? itemEl.getAsString() : itemEl.toString();
            } else if (obj.has("texture")) {
                return obj.get("texture").getAsString();
            }
            return obj.toString();
        }
        return "";
    }

    private boolean supportsComponents(String type) {
        if (type == null) return false;
        String t = type.replace("questlog:", "");
        return t.contains("item") || t.contains("entity") || t.contains("mob") || t.contains("approach") || t.contains("kill") || t.contains("death") || t.contains("breed") || t.contains("tame") || t.contains("block");
    }

    boolean isEntityObjective(String type) {
        if (type == null) return false;
        String t = type.replace("questlog:", "");
        return t.contains("entity") || t.contains("mob") || t.contains("approach") || t.contains("kill") || t.contains("death") || t.contains("breed") || t.contains("tame");
    }

    private String getComponentsFieldValue() {
        if (this.editingEntry == null) return "";
        if (this.editingEntry.has("components")) {
            JsonElement el = this.editingEntry.get("components");
            return el.isJsonPrimitive() ? el.getAsString() : el.toString();
        }
        if (this.editingEntry.has("custom_name")) {
            JsonElement el = this.editingEntry.get("custom_name");
            return el.isJsonPrimitive() ? el.getAsString() : el.toString();
        }
        if (this.editingEntry.has("entity_name")) {
            JsonElement el = this.editingEntry.get("entity_name");
            return el.isJsonPrimitive() ? el.getAsString() : el.toString();
        }
        if (this.editingEntry.has("predicate")) {
            JsonElement el = this.editingEntry.get("predicate");
            return el.isJsonPrimitive() ? el.getAsString() : el.toString();
        }
        if (this.editingEntry.has("nbt")) {
            JsonElement el = this.editingEntry.get("nbt");
            return el.isJsonPrimitive() ? el.getAsString() : el.toString();
        }
        if (this.editingEntry.has("item") && this.editingEntry.get("item").isJsonObject()) {
            JsonObject itemObj = this.editingEntry.getAsJsonObject("item");
            if (itemObj.has("components")) {
                JsonElement el = itemObj.get("components");
                return el.isJsonPrimitive() ? el.getAsString() : el.toString();
            }
        }
        if (this.editingEntry.has("entity") && this.editingEntry.get("entity").isJsonObject()) {
            JsonObject entityObj = this.editingEntry.getAsJsonObject("entity");
            if (entityObj.has("custom_name")) {
                JsonElement el = entityObj.get("custom_name");
                return el.isJsonPrimitive() ? el.getAsString() : el.toString();
            }
            if (entityObj.has("name")) {
                JsonElement el = entityObj.get("name");
                return el.isJsonPrimitive() ? el.getAsString() : el.toString();
            }
            if (entityObj.has("predicate")) {
                JsonElement el = entityObj.get("predicate");
                return el.isJsonPrimitive() ? el.getAsString() : el.toString();
            }
        }
        return "";
    }

    private void saveEditingEntry() {
        if (this.editingEntry == null) return;

        String name = this.entryNameBox != null ? this.entryNameBox.getValue() : "";
        String target = this.entryTargetBox != null ? this.entryTargetBox.getValue() : "";
        String components = this.entryComponentsBox != null ? this.entryComponentsBox.getValue().trim() : "";
        String icon = this.entryIconBox != null ? this.entryIconBox.getValue().trim() : "";
        int amount = 1;
        if (this.entryAmountBox != null) {
            try {
                amount = Integer.parseInt(this.entryAmountBox.getValue());
            } catch (NumberFormatException ignored) {
            }
        }

        this.editingEntry.addProperty("type", this.editingType);
        if (!name.isEmpty()) {
            this.editingEntry.addProperty("name", name);
        } else {
            this.editingEntry.remove("name");
        }

        this.editingEntry.remove("icon");
        if (!icon.isEmpty()) {
            if (icon.startsWith("{") && icon.endsWith("}")) {
                try {
                    JsonElement parsed = JsonParser.parseString(icon);
                    this.editingEntry.add("icon", parsed);
                } catch (Exception e) {
                    JsonObject iconObj = new JsonObject();
                    iconObj.addProperty("item", icon);
                    this.editingEntry.add("icon", iconObj);
                }
            } else if (icon.contains("textures/") || icon.endsWith(".png")) {
                JsonObject iconObj = new JsonObject();
                iconObj.addProperty("texture", icon);
                this.editingEntry.add("icon", iconObj);
            } else {
                JsonObject iconObj = new JsonObject();
                iconObj.addProperty("item", icon);
                this.editingEntry.add("icon", iconObj);
            }
        }

        this.editingEntry.remove("components");
        this.editingEntry.remove("custom_name");
        this.editingEntry.remove("entity_name");
        this.editingEntry.remove("predicate");
        this.editingEntry.remove("nbt");

        if (!components.isEmpty()) {
            try {
                JsonElement parsed = JsonParser.parseString(components);
                if (isEntityObjective(this.editingType)) {
                    this.editingEntry.add("predicate", parsed);
                } else {
                    this.editingEntry.add("components", parsed);
                }
            } catch (Exception e) {
                if (isEntityObjective(this.editingType)) {
                    this.editingEntry.addProperty("predicate", components);
                } else {
                    this.editingEntry.addProperty("components", components);
                }
            }
        }

        String[] allKeys = new String[]{
                "block", "item", "entity", "biome", "dimension", "structure",
                "advancement", "stat", "quest", "enchantment", "effect", "command", "loot_table", "bounds",
                "required_amount", "count", "experience", "levels", "pick_count"
        };
        for (String k : allKeys) {
            this.editingEntry.remove(k);
        }

        EditorMetadata meta = getMetadata(this.editingType);
        if (meta != null) {
            if (meta.targetFieldKey() != null && !target.isEmpty()) {
                if (target.trim().startsWith("{") && target.trim().endsWith("}")) {
                    try {
                        JsonElement parsed = JsonParser.parseString(target);
                        this.editingEntry.add(meta.targetFieldKey(), parsed);
                    } catch (Exception e) {
                        this.editingEntry.addProperty(meta.targetFieldKey(), target);
                    }
                } else {
                    this.editingEntry.addProperty(meta.targetFieldKey(), target);
                }
            }
            if (meta.amountFieldKey() != null) {
                this.editingEntry.addProperty(meta.amountFieldKey(), amount);
            }
            if ("questlog:experience".equals(this.editingType)) {
                this.editingEntry.addProperty("levels", this.entryLevelsToggle);
            }
        } else {
            if (this.activeTab == ActiveTab.OBJECTIVES || this.activeTab == ActiveTab.PREREQUISITES) {
                this.editingEntry.addProperty("required_amount", amount);
            }
        }

        List<JsonObject> list = getActiveList();
        if (this.selectedEntryIndex == -1) {
            list.add(this.editingEntry);
            this.selectedEntryIndex = list.size() - 1;
        } else {
            list.set(this.selectedEntryIndex, this.editingEntry);
        }
        this.updateParentEntryWithChildren();
    }

    List<JsonObject> getActiveList() {
        if (!this.nestingStack.isEmpty()) {
            if (this.currentNestedList == null) {
                this.currentNestedList = getChildrenList(this.nestingStack.peek().parentEntry);
            }
            return this.currentNestedList;
        }
        if (this.activeTab == ActiveTab.OBJECTIVES) {
            return this.tempObjectives;
        } else if (this.activeTab == ActiveTab.PREREQUISITES) {
            return this.tempPrerequisites;
        } else {
            return this.tempRewards;
        }
    }

    private List<JsonObject> getChildrenList(JsonObject entry) {
        List<JsonObject> children = new ArrayList<>();
        if (entry == null) return children;
        String type = entry.has("type") ? entry.get("type").getAsString() : "";
        if ("questlog:or".equals(type) || "questlog:and".equals(type) || "or".equals(type) || "and".equals(type)) {
            JsonArray array = JsonUtils.getOrDefault(entry, "objectives", new JsonArray());
            for (JsonElement el : array) {
                if (el.isJsonObject()) {
                    children.add(el.getAsJsonObject());
                }
            }
        } else if ("questlog:not".equals(type) || "not".equals(type)) {
            if (entry.has("objective") && entry.get("objective").isJsonObject()) {
                children.add(entry.getAsJsonObject("objective"));
            }
        } else if ("questlog:choice".equals(type) || "choice".equals(type)) {
            JsonArray array = JsonUtils.getOrDefault(entry, "choices", new JsonArray());
            for (JsonElement el : array) {
                if (el.isJsonObject()) {
                    children.add(el.getAsJsonObject());
                }
            }
        }
        return children;
    }

    private void saveChildrenList(JsonObject entry, List<JsonObject> children) {
        if (entry == null) return;
        String type = entry.has("type") ? entry.get("type").getAsString() : "";
        if ("questlog:or".equals(type) || "questlog:and".equals(type) || "or".equals(type) || "and".equals(type)) {
            JsonArray array = new JsonArray();
            for (JsonObject child : children) {
                array.add(child);
            }
            entry.add("objectives", array);
        } else if ("questlog:not".equals(type) || "not".equals(type)) {
            if (!children.isEmpty()) {
                entry.add("objective", children.getFirst());
            } else {
                entry.remove("objective");
            }
        } else if ("questlog:choice".equals(type) || "choice".equals(type)) {
            JsonArray array = new JsonArray();
            for (JsonObject child : children) {
                array.add(child);
            }
            entry.add("choices", array);
        }
    }

    void updateParentEntryWithChildren() {
        if (!nestingStack.isEmpty()) {
            NestingFrame frame = nestingStack.peek();
            saveChildrenList(frame.parentEntry, getActiveList());
        }
    }

    public void saveTemporaryState() {
        if (this.titleBox != null) {
            this.tempTitle = this.titleBox.getValue();
        }
        if (this.descriptionBox != null) {
            this.tempDescription = this.descriptionBox.getValue();
        }
        if (this.iconBox != null) {
            this.tempIconItem = this.iconBox.getValue();
        }
        if (this.chapterBox != null) {
            this.tempChapter = this.chapterBox.getValue();
        }
        if (this.orderBox != null) {
            try {
                this.tempSortOrder = Integer.parseInt(this.orderBox.getValue());
            } catch (NumberFormatException e) {
                this.tempSortOrder = 0;
            }
        }
        if (this.idBox != null) {
            this.tempId = this.idBox.getValue();
        }
        if (this.typeSearchBox != null) {
            this.typeSearchQuery = this.typeSearchBox.getValue();
            this.tempSearchFocused = this.typeSearchBox.isFocused();
        } else {
            this.tempSearchFocused = false;
        }

        for (int i = 0; i < TEXT_FIELDS.size(); i++) {
            int widgetIndex = BOOL_FIELDS.size() + i;
            if (widgetIndex >= this.settingsFields.size()) continue;
            net.minecraft.client.gui.components.AbstractWidget widget = this.settingsFields.get(widgetIndex);
            if (widget instanceof NoShadowEditBox box) {
                this.tempTexts.put(TEXT_FIELDS.get(i).key(), box.getValue());
            } else if (widget instanceof MultiLineEditBox box) {
                this.tempTexts.put(TEXT_FIELDS.get(i).key(), box.getValue());
            }
        }
    }

    private void setBooleanFlag(JsonObject json, String key, boolean value, boolean defaultValue) {
        if (value == defaultValue) {
            json.remove(key);
        } else {
            json.addProperty(key, value);
        }
    }

    private void setStringOrRemove(JsonObject json, String key, String value, String defaultValue) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty() || trimmed.equals(defaultValue)) {
            json.remove(key);
        } else {
            json.addProperty(key, trimmed);
        }
    }

    private void setIntOrRemove(JsonObject json, String key, String value, String defaultValue) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            json.remove(key);
            return;
        }
        try {
            int parsed = Integer.parseInt(trimmed);
            if (String.valueOf(parsed).equals(defaultValue)) {
                json.remove(key);
            } else {
                json.addProperty(key, parsed);
            }
        } catch (NumberFormatException e) {
            json.remove(key);
        }
    }

    private void saveQuestToServer() {
        JsonObject json = this.originalDefinition != null ? this.originalDefinition.deepCopy() : new JsonObject();

        json.addProperty("title", this.tempTitle);
        json.addProperty("description", this.tempDescription);

        JsonObject iconObj = json.has("icon") && json.get("icon").isJsonObject() ? json.getAsJsonObject("icon") : new JsonObject();
        if (this.tempIconItem.trim().startsWith("{") && this.tempIconItem.trim().endsWith("}")) {
            try {
                JsonElement parsed = com.google.gson.JsonParser.parseString(this.tempIconItem);
                iconObj.add("item", parsed);
            } catch (Exception e) {
                iconObj.addProperty("item", this.tempIconItem);
            }
        } else {
            iconObj.addProperty("item", this.tempIconItem);
        }
        json.add("icon", iconObj);

        json.addProperty("chapter", this.tempChapter);
        json.addProperty("sort_order", this.tempSortOrder);
        json.remove("order");

        for (BoolFieldDef def : BOOL_FIELDS) {
            setBooleanFlag(json, def.key(), this.tempBooleans.getOrDefault(def.key(), def.defaultValue()), def.defaultValue());
        }
        for (TextFieldDef def : TEXT_FIELDS) {
            String value = this.tempTexts.getOrDefault(def.key(), def.defaultValue());
            if (def.numeric()) {
                setIntOrRemove(json, def.key(), value, def.defaultValue());
            } else {
                setStringOrRemove(json, def.key(), value, def.defaultValue());
            }
        }

        JsonArray objArr = new JsonArray();
        for (JsonObject o : this.tempObjectives) {
            objArr.add(o);
        }
        json.add("objectives", objArr);

        JsonArray reqArr = new JsonArray();
        for (JsonObject r : this.tempPrerequisites) {
            reqArr.add(r);
        }
        json.add("prerequisites", reqArr);
        json.remove("requirements");

        JsonArray rewArr = new JsonArray();
        for (JsonObject rw : this.tempRewards) {
            rewArr.add(rw);
        }
        json.add("rewards", rewArr);

        ResourceLocation rl = ResourceLocation.tryParse(this.tempId);
        if (rl == null) {
            rl = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, this.tempId.replace(":", "_"));
        }

        Services.PLATFORM.sendPacketToServer(new QuestEditSavePacket(rl, json.toString()));

        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
    }

    private void deleteQuestOnServer() {
        if (this.questToEdit != null) {
            EditorUtils.deleteQuest(this.questToEdit.getId());
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.previousScreen);
            }
        }
    }

    private AbstractButton createImageButton(int x, int y, ResourceLocation texture, ResourceLocation highlightedTexture, Runnable onPress) {
        return new AbstractButton(x, y, 16, 16, Component.empty()) {
            @Override
            public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
                boolean hovered = this.isHoveredOrFocused();
                ResourceLocation tex = hovered ? highlightedTexture : texture;
                ps.blit(tex, this.getX(), this.getY(), 0, 0, 16, 16, 16, 16);
            }

            @Override
            public void onPress() {
                onPress.run();
            }

            @Override
            protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            }
        };
    }

    private void collectAdvancements(AdvancementNode node, List<ResourceLocation> list) {
        list.add(node.holder().id());
        for (AdvancementNode child : node.children()) {
            collectAdvancements(child, list);
        }
    }

    private List<String> getSuggestions(String query) {
        List<String> result = new ArrayList<>();
        if (query.length() < 2) return result;

        EditorMetadata meta = getMetadata(this.editingType);
        if (meta == null || meta.suggestionType() == null || meta.suggestionType() == SuggestionType.NONE) {
            return result;
        }

        if (query.startsWith("#")) {
            Registry<?> registry = null;
            switch (meta.suggestionType()) {
                case BLOCK -> registry = BuiltInRegistries.BLOCK;
                case ITEM -> registry = BuiltInRegistries.ITEM;
                case ENTITY_TYPE -> registry = BuiltInRegistries.ENTITY_TYPE;
            }
            if (registry != null) {
                String lower = query.toLowerCase();
                for (net.minecraft.tags.TagKey<?> tagKey : registry.getTagNames().toList()) {
                    String str = "#" + tagKey.location();
                    if (str.toLowerCase().contains(lower)) {
                        result.add(str);
                        if (result.size() >= 5) break;
                    }
                }
            }
            return result;
        }

        Collection<ResourceLocation> keys = null;
        Minecraft mc = Minecraft.getInstance();

        switch (meta.suggestionType()) {
            case BLOCK -> keys = BuiltInRegistries.BLOCK.keySet();
            case ITEM -> keys = BuiltInRegistries.ITEM.keySet();
            case ENTITY_TYPE -> keys = BuiltInRegistries.ENTITY_TYPE.keySet();
            case BIOME -> {
                if (mc.level != null) {
                    keys = mc.level.registryAccess().registry(Registries.BIOME)
                            .map(Registry::keySet).orElse(Collections.emptySet());
                }
            }
            case DIMENSION -> {
                List<ResourceLocation> dims = new ArrayList<>();
                dims.add(ResourceLocation.withDefaultNamespace("overworld"));
                dims.add(ResourceLocation.withDefaultNamespace("the_nether"));
                dims.add(ResourceLocation.withDefaultNamespace("the_end"));
                keys = dims;
            }
            case MOB_EFFECT -> keys = BuiltInRegistries.MOB_EFFECT.keySet();
            case ENCHANTMENT -> {
                if (mc.level != null) {
                    keys = mc.level.registryAccess().registry(Registries.ENCHANTMENT)
                            .map(Registry::keySet).orElse(Collections.emptySet());
                }
            }
            case QUEST -> keys = DefinitionUtil.getCachedQuestKeys();
            case STRUCTURE -> {
                if (mc.level != null) {
                    keys = mc.level.registryAccess().registry(Registries.STRUCTURE)
                            .map(Registry::keySet).orElse(Collections.emptySet());
                }
            }
            case LOOT_TABLE -> {
                if (mc.level != null) {
                    keys = mc.level.registryAccess().registry(Registries.LOOT_TABLE)
                            .map(Registry::keySet).orElse(Collections.emptySet());
                }
            }
            case CUSTOM_STAT -> keys = BuiltInRegistries.CUSTOM_STAT.keySet();
            case ADVANCEMENT -> {
                if (!QuestlogClient.ALL_ADVANCEMENTS.isEmpty()) {
                    keys = QuestlogClient.ALL_ADVANCEMENTS;
                } else if (mc.getConnection() != null) {
                    List<ResourceLocation> list = new ArrayList<>();
                    for (AdvancementNode root : mc.getConnection().getAdvancements().getTree().roots()) {
                        collectAdvancements(root, list);
                    }
                    keys = list;
                }
            }
            case ORIGIN -> {
                Collection<ResourceLocation> originKeys = OriginsClientHelper.getOriginKeys();
                if (!originKeys.isEmpty()) {
                    keys = originKeys;
                }
            }
        }

        if (keys != null) {
            String lower = query.toLowerCase();
            for (ResourceLocation rl : keys) {
                String str = rl.toString();
                if (str.toLowerCase().contains(lower)) {
                    result.add(str);
                    if (result.size() >= 5) break;
                }
            }
        }
        return result;
    }

    private List<String> getLeftSuggestions(NoShadowEditBox box) {
        List<String> result = new ArrayList<>();
        String val = box.getValue();

        if (box == this.iconBox || box == this.entryIconBox) {
            if (val.length() < 2) return result;
            String lower = val.toLowerCase();
            for (ResourceLocation rl : BuiltInRegistries.ITEM.keySet()) {
                String str = rl.toString();
                if (str.toLowerCase().contains(lower)) {
                    result.add(str);
                    if (result.size() >= 5) break;
                }
            }
        } else if (box == this.chapterBox) {
            String lower = val.toLowerCase();
            for (ResourceLocation rl : DefinitionUtil.getCachedChapterKeys()) {
                String path = rl.getPath();
                if (lower.isEmpty() || path.toLowerCase().contains(lower)) {
                    result.add(path);
                    if (result.size() >= 5) break;
                }
            }
        }
        return result;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(ps, mouseX, mouseY, partialTick);

        int PANEL_SPACING = 6;
        int leftWidth = 240;
        int rightWidth = 160;
        int height = 190;
        int totalWidth = leftWidth + rightWidth + PANEL_SPACING;
        int baseX = (this.width - totalWidth) / 2;
        int baseY = (this.height - height) / 2;

        if (this.bgLeft != null) {
            this.bgLeft.blit(ps, baseX, baseY);
        }
        if (this.bgRight != null) {
            this.bgRight.blit(ps, baseX + leftWidth + PANEL_SPACING, baseY);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float delta) {
        this.pendingTooltip = null;
        int renderMouseX = this.contextMenu != null ? -9999 : mouseX;
        int renderMouseY = this.contextMenu != null ? -9999 : mouseY;

        super.render(ps, renderMouseX, renderMouseY, delta);

        int PANEL_SPACING = 6;
        int leftWidth = 240;
        int rightWidth = 160;
        int height = 190;
        int totalWidth = leftWidth + rightWidth + PANEL_SPACING;
        int baseX = (this.width - totalWidth) / 2;
        int baseY = (this.height - height) / 2;

        int panel2X = baseX + leftWidth + PANEL_SPACING;
        int panel2Y = baseY;

        int color = Questlog.getConfig().colors.textColor | 0xFF000000;

        if (this.rightPageState == RightPageState.SELECT_TYPE) {
            ps.drawString(this.font, "Search Type:", panel2X + 15, panel2Y + 12, color, false);

            int listX = panel2X + 15;
            int listY = panel2Y + 40;
            int listW = 130;
            int listH = 116;

            ps.fill(listX, listY, listX + listW, listY + listH, 0xFF101010);
            ps.fill(listX - 1, listY, listX, listY + listH, 0xFF505050);
            ps.fill(listX + listW, listY, listX + listW + 1, listY + listH, 0xFF505050);
            ps.fill(listX, listY - 1, listX + listW, listY, 0xFF505050);
            ps.fill(listX, listY + listH, listX + listW, listY + listH + 1, 0xFF505050);

            List<String> filteredList = getFilteredList();
            filteredList.sort(String::compareTo);

            int maxScroll = Math.max(0, filteredList.size() - 7);
            this.typeListScroll = Math.max(0, Math.min(this.typeListScroll, maxScroll));

            int start = this.typeListScroll;
            int end = Math.min(filteredList.size(), start + 7);
            int itemHeight = 16;

            for (int i = start; i < end; i++) {
                String type = filteredList.get(i);
                String shortName = type.replace("questlog:", "");
                int rowY = listY + (i - start) * itemHeight;

                boolean hovered = renderMouseX >= listX && renderMouseX <= listX + listW && renderMouseY >= rowY && renderMouseY <= rowY + itemHeight;
                if (hovered) {
                    ps.fill(listX, rowY, listX + listW, rowY + itemHeight, 0xFF404040);
                }

                ps.drawString(this.font, shortName, listX + 4, rowY + 4, hovered ? 0xFFFFFF00 : 0xFFFFFFFF, false);
            }

            if (filteredList.size() > 7) {
                int scrollbarWidth = 6;
                int scrollbarX = listX + listW - scrollbarWidth;
                int scrollbarY = listY;
                int scrollbarH = listH;
                ps.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarH, 0xFF202020);
                int thumbH = Math.max(8, (7 * scrollbarH) / filteredList.size());
                int thumbY = scrollbarY + (this.typeListScroll * (scrollbarH - thumbH)) / maxScroll;
                ps.fill(scrollbarX + 1, thumbY, scrollbarX + scrollbarWidth - 1, thumbY + thumbH, 0xFF808080);
            }
        } else if (this.rightPageState == RightPageState.EDIT_ENTRY) {
            String titleText = getEditingTypeTitle();
            ps.drawString(this.font, titleText, panel2X + 15, panel2Y + 12, color, false);
        } else if (this.rightPageState == RightPageState.LIST && this.activeTab != ActiveTab.SETTINGS) {
            if (!this.nestingStack.isEmpty()) {
                String parentType = this.nestingStack.peek().editingType.replace("questlog:", "");
                String title = parentType.toUpperCase() + " List";
                ps.drawString(this.font, title, panel2X + 59, panel2Y + 16, color, false);
            }
        }

        NoShadowEditBox activeBox = null;
        Supplier<List<String>> suggestionProvider = null;

        if (this.iconBox != null && this.iconBox.isFocused()) {
            activeBox = this.iconBox;
            suggestionProvider = () -> getLeftSuggestions(this.iconBox);
        } else if (this.chapterBox != null && this.chapterBox.isFocused()) {
            activeBox = this.chapterBox;
            suggestionProvider = () -> getLeftSuggestions(this.chapterBox);
        } else if (this.rightPageState == RightPageState.EDIT_ENTRY && this.entryTargetBox != null && this.entryTargetBox.isFocused()) {
            activeBox = this.entryTargetBox;
            suggestionProvider = () -> getSuggestions(this.entryTargetBox.getValue());
        } else if (this.rightPageState == RightPageState.EDIT_ENTRY && this.entryComponentsBox != null && this.entryComponentsBox.isFocused()) {
            activeBox = this.entryComponentsBox;
            suggestionProvider = () -> getSuggestions(this.entryComponentsBox.getValue());
        } else if (this.rightPageState == RightPageState.EDIT_ENTRY && this.entryIconBox != null && this.entryIconBox.isFocused()) {
            activeBox = this.entryIconBox;
            suggestionProvider = () -> getLeftSuggestions(this.entryIconBox);
        }

        this.autocompleteHelper.update(activeBox, suggestionProvider != null ? suggestionProvider : Collections::emptyList);
        this.autocompleteHelper.render(ps, this.font, renderMouseX, renderMouseY);

        if (this.contextMenu != null) {
            this.contextMenu.render(ps, mouseX, mouseY, this.font);
        }

        if (this.pendingTooltip != null) {
            ps.renderTooltip(this.font, this.pendingTooltip, mouseX, mouseY);
        }
    }

    private @NotNull List<String> getFilteredList() {
        Set<ResourceLocation> registered;
        if (this.activeTab == ActiveTab.REWARDS) {
            registered = QuestRewardRegistry.getRegisteredTypes();
        } else {
            registered = QuestObjectiveRegistry.getRegisteredTypes();
        }
        List<String> filteredList = new ArrayList<>();
        String lowerQuery = this.typeSearchQuery.toLowerCase();
        for (ResourceLocation rl : registered) {
            String typeStr = rl.toString();
            if (typeStr.replace("questlog:", "").toLowerCase().contains(lowerQuery)) {
                filteredList.add(typeStr);
            }
        }
        return filteredList;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.contextMenu != null) {
            ContextMenu menu = this.contextMenu;
            if (menu.isMouseOver(mouseX, mouseY)) {
                menu.mouseClicked(mouseX, mouseY, button);
                if (this.contextMenu == menu) {
                    this.contextMenu = null;
                }
                return true;
            }
            this.contextMenu = null;
            return true;
        }

        if (this.autocompleteHelper.mouseClicked(mouseX, mouseY, button, val -> {
            NoShadowEditBox box = this.autocompleteHelper.getLastBox();
            if (box != null) {
                box.setValue(val);
                this.saveTemporaryState();
                if (box == this.entryTargetBox || box == this.entryComponentsBox || box == this.entryIconBox) {
                    this.saveEditingEntry();
                }
                box.setFocused(false);
                this.rebuildWidgets();
            }
        })) {
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.rightPageState == RightPageState.SELECT_TYPE) {
            int PANEL_SPACING = 6;
            int leftWidth = 240;
            int totalWidth = leftWidth + 160 + PANEL_SPACING;
            int baseX = (this.width - totalWidth) / 2;
            int panel2X = baseX + leftWidth + PANEL_SPACING;
            int panel2Y = (this.height - 190) / 2;

            int listX = panel2X + 15;
            int listY = panel2Y + 40;
            int listW = 130;
            int listH = 116;

            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                List<String> filteredList = getFilteredList();
                filteredList.sort(String::compareTo);

                int maxScroll = Math.max(0, filteredList.size() - 7);
                int currentScroll = Math.max(0, Math.min(this.typeListScroll, maxScroll));

                int clickedIdx = currentScroll + (int) ((mouseY - listY) / 16);
                if (clickedIdx >= 0 && clickedIdx < filteredList.size()) {
                    this.saveTemporaryState();
                    this.editingType = filteredList.get(clickedIdx);
                    this.rightPageState = RightPageState.EDIT_ENTRY;
                    this.rebuildWidgets();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.contextMenu != null) {
            if (this.contextMenu.mouseScrolled(scrollY)) {
                return true;
            }
        }
        if (this.leftScrollable != null && this.leftScrollable.isMouseOver(mouseX, mouseY)) {
            if (this.leftScrollable.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        if (this.rightScrollable != null && this.rightScrollable.isMouseOver(mouseX, mouseY)) {
            if (this.rightScrollable.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        if (this.settingsScrollable != null && this.settingsScrollable.isMouseOver(mouseX, mouseY)) {
            if (this.settingsScrollable.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        if (this.rightPageState == RightPageState.SELECT_TYPE) {
            int count = getCount();
            int maxScroll = Math.max(0, count - 7);
            if (maxScroll > 0) {
                this.typeListScroll = Math.max(0, Math.min(this.typeListScroll - (int) scrollY, maxScroll));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private int getCount() {
        Set<ResourceLocation> registered;
        if (this.activeTab == ActiveTab.REWARDS) {
            registered = QuestRewardRegistry.getRegisteredTypes();
        } else {
            registered = QuestObjectiveRegistry.getRegisteredTypes();
        }
        int count = 0;
        String lowerQuery = this.typeSearchQuery.toLowerCase();
        for (ResourceLocation rl : registered) {
            if (rl.toString().replace("questlog:", "").toLowerCase().contains(lowerQuery)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (this.rightPageState != RightPageState.LIST) {
                this.saveTemporaryState();
                this.rightPageState = RightPageState.LIST;
                this.rebuildWidgets();
                return true;
            }
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.previousScreen);
            }
            return true;
        }

        if (this.autocompleteHelper.keyPressed(key, val -> {
            NoShadowEditBox box = this.autocompleteHelper.getLastBox();
            if (box != null) {
                box.setValue(val);
                this.saveTemporaryState();
                if (box == this.entryTargetBox || box == this.entryComponentsBox || box == this.entryIconBox) {
                    this.saveEditingEntry();
                }
                box.setFocused(false);
                this.rebuildWidgets();
            }
        })) {
            return true;
        }

        if (this.getFocused() != null && this.getFocused().keyPressed(key, scancode, modifiers)) {
            return true;
        }

        return super.keyPressed(key, scancode, modifiers);
    }

    public void refreshScreen() {
        this.rebuildWidgets();
    }

    enum RightPageState {
        LIST,
        SELECT_TYPE,
        EDIT_ENTRY
    }

    enum ActiveTab {
        PREREQUISITES,
        OBJECTIVES,
        REWARDS,
        SETTINGS
    }

    private record BoolFieldDef(String key, boolean defaultValue, String label, @Nullable String tooltipKey) {
    }

    private record TextFieldDef(String key, String defaultValue, boolean numeric, boolean longText, String labelKey,
                                @Nullable String tooltipKey) {
    }

    private record NestingFrame(JsonObject parentEntry, List<JsonObject> activeList, int selectedEntryIndex,
                                int listPage, RightPageState rightPageState, String editingType,
                                JsonObject editingEntry, boolean entryLevelsToggle) {
    }
}