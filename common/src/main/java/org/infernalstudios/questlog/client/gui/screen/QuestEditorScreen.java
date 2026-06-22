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
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.client.gui.components.NoShadowEditBox;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.quests.EditorMetadata;
import org.infernalstudios.questlog.core.quests.EditorMetadata.SuggestionType;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.QuestObjectiveRegistry;
import org.infernalstudios.questlog.core.quests.QuestRewardRegistry;
import org.infernalstudios.questlog.network.packet.QuestEditRemovePacket;
import org.infernalstudios.questlog.network.packet.QuestEditSavePacket;
import org.infernalstudios.questlog.platform.Services;
import org.infernalstudios.questlog.util.texture.NineSliceTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class QuestEditorScreen extends Screen {

    private static final ResourceLocation GEAR_ICON = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_gear.png");
    private static final ResourceLocation GEAR_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_gear_highlighted.png");
    private static final ResourceLocation CROSS_ICON = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_cross.png");
    private static final ResourceLocation CROSS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_cross_highlighted.png");
    private static final ResourceLocation PLUS_ICON = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_plus.png");
    private static final ResourceLocation PLUS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_plus_highlighted.png");

    private static final ResourceLocation TAB_OBJECTIVES_TEXTURE = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_objectives.png");
    private static final ResourceLocation TAB_OBJECTIVES_SELECTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_objectives_selected.png");
    private static final ResourceLocation TAB_OBJECTIVES_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_objectives_highlighted.png");

    private static final ResourceLocation TAB_REQUIREMENTS_TEXTURE = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_requirements.png");
    private static final ResourceLocation TAB_REQUIREMENTS_SELECTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_requirements_selected.png");
    private static final ResourceLocation TAB_REQUIREMENTS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_requirements_highlighted.png");

    private static final ResourceLocation TAB_REWARDS_TEXTURE = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_rewards.png");
    private static final ResourceLocation TAB_REWARDS_SELECTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_rewards_selected.png");
    private static final ResourceLocation TAB_REWARDS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_rewards_highlighted.png");

    private static final ResourceLocation TAB_SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_settings.png");
    private static final ResourceLocation TAB_SETTINGS_SELECTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_settings_selected.png");
    private static final ResourceLocation TAB_SETTINGS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_tab_settings_highlighted.png");
    private final Screen previousScreen;

    @Nullable
    private final Quest questToEdit;

    private final List<JsonObject> tempObjectives = new ArrayList<>();
    private final List<JsonObject> tempRequirements = new ArrayList<>();
    private final List<JsonObject> tempRewards = new ArrayList<>();
    private NineSliceTexture bgLeft;
    private NineSliceTexture bgRight;
    private int typeListScroll = 0;

    private String tempId = "";
    private String tempTitle = "";
    private String tempDescription = "";
    private String tempIconItem = "";
    private String tempChapter = "";
    private int tempSortOrder = 0;
    private boolean tempHidden = false;
    private boolean tempIncludeInMain = true;
    private boolean tempDetailsDefault = false;
    private boolean tempDetailsDisabled = false;

    private RightPageState rightPageState = RightPageState.LIST;
    private ActiveTab activeTab = ActiveTab.OBJECTIVES;
    private int listPage = 0;

    private int selectedEntryIndex = -1;
    private String editingType = "questlog:item_obtain";
    @Nullable
    private JsonObject editingEntry = null;
    private boolean entryLevelsToggle = false;
    private String typeSearchQuery = "";
    private boolean tempSearchFocused = false;
    private NoShadowEditBox idBox;
    private NoShadowEditBox titleBox;
    private NoShadowEditBox descriptionBox;
    private NoShadowEditBox iconBox;
    private NoShadowEditBox chapterBox;
    private NoShadowEditBox orderBox;
    private NoShadowEditBox typeSearchBox;
    private NoShadowEditBox entryNameBox;
    private NoShadowEditBox entryTargetBox;
    private NoShadowEditBox entryAmountBox;

    public QuestEditorScreen(Screen previousScreen) {
        this(previousScreen, null);
    }

    public QuestEditorScreen(Screen previousScreen, @Nullable Quest questToEdit) {
        super(Component.translatable(questToEdit != null ? "questlog.editor.title" : "questlog.editor.add_quest"));
        this.previousScreen = previousScreen;
        this.questToEdit = questToEdit;

        this.loadQuestData();
    }

    private void loadQuestData() {
        if (this.questToEdit != null) {
            this.tempId = this.questToEdit.getId().toString();
            try {
                JsonObject definition = DefinitionUtil.getCachedQuest(this.questToEdit.getId());
                this.tempTitle = definition.has("title") ? definition.get("title").getAsString() : "";
                this.tempDescription = definition.has("description") ? definition.get("description").getAsString() : "";

                if (definition.has("icon") && definition.get("icon").isJsonObject()) {
                    JsonObject iconObj = definition.getAsJsonObject("icon");
                    this.tempIconItem = iconObj.has("item") ? iconObj.get("item").getAsString() : "";
                }

                this.tempChapter = definition.has("chapter") ? definition.get("chapter").getAsString() : "main";
                this.tempSortOrder = definition.has("order") ? definition.get("order").getAsInt() : 0;

                this.tempHidden = definition.has("hidden") && definition.get("hidden").getAsBoolean();
                this.tempIncludeInMain = !definition.has("include_in_main") || definition.get("include_in_main").getAsBoolean();
                this.tempDetailsDefault = definition.has("details_default") && definition.get("details_default").getAsBoolean();
                this.tempDetailsDisabled = definition.has("details_disabled") && definition.get("details_disabled").getAsBoolean();

                this.loadList(definition.getAsJsonArray("objectives"), this.tempObjectives);
                this.loadList(definition.getAsJsonArray("requirements"), this.tempRequirements);
                this.loadList(definition.getAsJsonArray("failures"), null);
                this.loadList(definition.getAsJsonArray("rewards"), this.tempRewards);
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to load quest definition for editing", e);
            }
        } else {
            this.tempId = "questlog:new_quest_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            this.tempTitle = "New Quest";
            this.tempDescription = "Describe your quest here...";
            this.tempIconItem = "minecraft:knowledge_book";
            this.tempChapter = "main";
            this.tempSortOrder = 0;
        }
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

        this.idBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 20, 210, 16, Component.empty());
        this.idBox.setMaxLength(64);
        this.idBox.setValue(this.tempId);
        this.idBox.setEditable(this.questToEdit == null);
        this.addRenderableWidget(this.idBox);

        this.titleBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 52, 210, 16, Component.empty());
        this.titleBox.setMaxLength(64);
        this.titleBox.setValue(this.tempTitle);
        this.addRenderableWidget(this.titleBox);

        this.descriptionBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 84, 210, 16, Component.empty());
        this.descriptionBox.setMaxLength(256);
        this.descriptionBox.setValue(this.tempDescription);
        this.addRenderableWidget(this.descriptionBox);

        this.iconBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 116, 210, 16, Component.empty());
        this.iconBox.setMaxLength(128);
        this.iconBox.setValue(this.tempIconItem);
        this.addRenderableWidget(this.iconBox);

        this.chapterBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 148, 140, 16, Component.empty());
        this.chapterBox.setMaxLength(64);
        this.chapterBox.setValue(this.tempChapter);
        this.addRenderableWidget(this.chapterBox);

        this.orderBox = new NoShadowEditBox(this.font, panel1X + 165, panel1Y + 148, 60, 16, Component.empty());
        this.orderBox.setMaxLength(8);
        this.orderBox.setValue(String.valueOf(this.tempSortOrder));
        this.orderBox.setFilter(s -> s.isEmpty() || s.matches("-?\\d*"));
        this.addRenderableWidget(this.orderBox);

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
            this.addRenderableWidget(Button.builder(Component.translatable("questlog.editor.delete"), btn -> {
                this.deleteQuestOnServer();
            }).bounds(panel2X + rightWidth - btnWidth, bottomY, btnWidth, 20).build());
        }
    }

    private void buildRightPageList(int panel2X, int panel2Y) {
        ActiveTab[] tabs = ActiveTab.values();
        int tabBtnSize = 20;
        int tabBtnSpacing = 28;
        int startX = panel2X + (160 - (4 * tabBtnSize + 3 * (tabBtnSpacing - tabBtnSize))) / 2;

        for (int i = 0; i < tabs.length; i++) {
            ActiveTab t = tabs[i];
            boolean isCurrentTab = t == this.activeTab;

            Component tooltipText = switch (t) {
                case OBJECTIVES -> Component.translatable("questlog.editor.objectives");
                case REQUIREMENTS -> Component.translatable("questlog.editor.requirements");
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
                            case REQUIREMENTS -> TAB_REQUIREMENTS_SELECTED;
                            case REWARDS -> TAB_REWARDS_SELECTED;
                            case SETTINGS -> TAB_SETTINGS_SELECTED;
                        };
                    } else if (hovered) {
                        drawTex = switch (t) {
                            case OBJECTIVES -> TAB_OBJECTIVES_HIGHLIGHTED;
                            case REQUIREMENTS -> TAB_REQUIREMENTS_HIGHLIGHTED;
                            case REWARDS -> TAB_REWARDS_HIGHLIGHTED;
                            case SETTINGS -> TAB_SETTINGS_HIGHLIGHTED;
                        };
                    } else {
                        drawTex = switch (t) {
                            case OBJECTIVES -> TAB_OBJECTIVES_TEXTURE;
                            case REQUIREMENTS -> TAB_REQUIREMENTS_TEXTURE;
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

        if (this.activeTab == ActiveTab.SETTINGS) {
            this.addRenderableWidget(Button.builder(Component.literal("Hidden: " + (this.tempHidden ? "True" : "False")), btn -> {
                this.tempHidden = !this.tempHidden;
                btn.setMessage(Component.literal("Hidden: " + (this.tempHidden ? "True" : "False")));
            }).bounds(panel2X + 10, panel2Y + 36, 140, 18).build());

            this.addRenderableWidget(Button.builder(Component.literal("In Main Chapter: " + (this.tempIncludeInMain ? "True" : "False")), btn -> {
                this.tempIncludeInMain = !this.tempIncludeInMain;
                btn.setMessage(Component.literal("In Main Chapter: " + (this.tempIncludeInMain ? "True" : "False")));
            }).bounds(panel2X + 10, panel2Y + 62, 140, 18).build());

            this.addRenderableWidget(Button.builder(Component.literal("Details Default: " + (this.tempDetailsDefault ? "True" : "False")), btn -> {
                this.tempDetailsDefault = !this.tempDetailsDefault;
                btn.setMessage(Component.literal("Details Default: " + (this.tempDetailsDefault ? "True" : "False")));
            }).bounds(panel2X + 10, panel2Y + 88, 140, 18).build());

            this.addRenderableWidget(Button.builder(Component.literal("Details Disabled: " + (this.tempDetailsDisabled ? "True" : "False")), btn -> {
                this.tempDetailsDisabled = !this.tempDetailsDisabled;
                btn.setMessage(Component.literal("Details Disabled: " + (this.tempDetailsDisabled ? "True" : "False")));
            }).bounds(panel2X + 10, panel2Y + 114, 140, 18).build());
        } else {
            List<JsonObject> list = getActiveList();
            int itemsPerPage = 5;
            int startIdx = this.listPage * itemsPerPage;
            int endIdx = Math.min(startIdx + itemsPerPage, list.size());

            for (int i = startIdx; i < endIdx; i++) {
                int index = i;
                JsonObject entry = list.get(index);
                int rowY = panel2Y + 28 + (i - startIdx) * 22;

                this.addRenderableWidget(createImageButton(panel2X + 120, rowY + 2, GEAR_ICON, GEAR_HIGHLIGHTED, () -> {
                    this.saveTemporaryState();
                    this.selectedEntryIndex = index;
                    this.editingEntry = entry;
                    this.editingType = entry.has("type") ? entry.get("type").getAsString() : "questlog:item_obtain";
                    this.entryLevelsToggle = entry.has("levels") && entry.get("levels").getAsBoolean();
                    this.rightPageState = RightPageState.EDIT_ENTRY;
                    this.rebuildWidgets();
                }));

                this.addRenderableWidget(createImageButton(panel2X + 140, rowY + 2, CROSS_ICON, CROSS_HIGHLIGHTED, () -> {
                    this.saveTemporaryState();
                    list.remove(index);
                    this.listPage = Math.max(0, (list.size() - 1) / itemsPerPage);
                    this.rebuildWidgets();
                }));
            }

            this.addRenderableWidget(createImageButton(panel2X + 72, panel2Y + 162, PLUS_ICON, PLUS_HIGHLIGHTED, () -> {
                this.saveTemporaryState();
                this.selectedEntryIndex = -1;
                this.editingEntry = new JsonObject();
                this.typeSearchQuery = "";
                this.rightPageState = RightPageState.SELECT_TYPE;
                this.rebuildWidgets();
            }));

            if (this.listPage > 0) {
                this.addRenderableWidget(Button.builder(Component.literal("<"), btn -> {
                    this.saveTemporaryState();
                    this.listPage--;
                    this.rebuildWidgets();
                }).bounds(panel2X + 15, panel2Y + 162, 16, 16).build());
            }

            if (endIdx < list.size()) {
                this.addRenderableWidget(Button.builder(Component.literal(">"), btn -> {
                    this.saveTemporaryState();
                    this.listPage++;
                    this.rebuildWidgets();
                }).bounds(panel2X + 129, panel2Y + 162, 16, 16).build());
            }
        }
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
        this.entryNameBox = new NoShadowEditBox(this.font, panel2X + 15, panel2Y + 22, 130, 16, Component.empty());
        this.entryNameBox.setMaxLength(64);
        String nameVal = this.editingEntry != null && this.editingEntry.has("name") ? this.editingEntry.get("name").getAsString() : "";
        this.entryNameBox.setValue(nameVal);
        this.addRenderableWidget(this.entryNameBox);

        EditorMetadata meta = getMetadata(this.editingType);

        if (meta == null || meta.targetFieldKey() != null) {
            this.entryTargetBox = new NoShadowEditBox(this.font, panel2X + 15, panel2Y + 58, 130, 16, Component.empty());
            this.entryTargetBox.setMaxLength(128);
            String targetVal = getTargetFieldValue();
            this.entryTargetBox.setValue(targetVal);
            this.addRenderableWidget(this.entryTargetBox);
        } else {
            this.entryTargetBox = null;
        }

        if (meta == null || meta.amountFieldKey() != null) {
            this.entryAmountBox = new NoShadowEditBox(this.font, panel2X + 15, panel2Y + 94, 50, 16, Component.empty());
            this.entryAmountBox.setMaxLength(6);
            this.entryAmountBox.setFilter(s -> s.isEmpty() || s.matches("\\d*"));
            int amtVal = getAmountValue();
            this.entryAmountBox.setValue(String.valueOf(amtVal));
            this.addRenderableWidget(this.entryAmountBox);
        } else {
            this.entryAmountBox = null;
        }

        if ("questlog:experience".equals(this.editingType)) {
            this.addRenderableWidget(Button.builder(Component.literal("Levels: " + (this.entryLevelsToggle ? "True" : "False")), btn -> {
                this.entryLevelsToggle = !this.entryLevelsToggle;
                btn.setMessage(Component.literal("Levels: " + (this.entryLevelsToggle ? "True" : "False")));
            }).bounds(panel2X + 75, panel2Y + 94, 70, 16).build());
        }

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

    private EditorMetadata getMetadata(String typeStr) {
        ResourceLocation rl = ResourceLocation.tryParse(typeStr);
        if (rl == null) return null;
        if (this.activeTab == ActiveTab.REWARDS) {
            return QuestRewardRegistry.getMetadata(rl);
        } else {
            return QuestObjectiveRegistry.getMetadata(rl);
        }
    }

    private String getTargetFieldLabel() {
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
                return this.editingEntry.get(key).getAsString();
            }
        }
        String[] keys = new String[]{"block", "item", "entity", "biome", "dimension", "structure", "advancement", "stat", "quest", "enchantment", "effect", "command", "loot_table"};
        for (String k : keys) {
            if (this.editingEntry.has(k)) {
                return this.editingEntry.get(k).getAsString();
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

    private void saveEditingEntry() {
        if (this.editingEntry == null) return;

        String name = this.entryNameBox != null ? this.entryNameBox.getValue() : "";
        String target = this.entryTargetBox != null ? this.entryTargetBox.getValue() : "";
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

        String[] allKeys = new String[]{
                "block", "item", "entity", "biome", "dimension", "structure",
                "advancement", "stat", "quest", "enchantment", "effect", "command", "loot_table",
                "required_amount", "count", "experience", "levels"
        };
        for (String k : allKeys) {
            this.editingEntry.remove(k);
        }

        EditorMetadata meta = getMetadata(this.editingType);
        if (meta != null) {
            if (meta.targetFieldKey() != null && !target.isEmpty()) {
                this.editingEntry.addProperty(meta.targetFieldKey(), target);
            }
            if (meta.amountFieldKey() != null) {
                this.editingEntry.addProperty(meta.amountFieldKey(), amount);
            }
            if ("questlog:experience".equals(this.editingType)) {
                this.editingEntry.addProperty("levels", this.entryLevelsToggle);
            }
        } else {
            if (this.activeTab == ActiveTab.OBJECTIVES || this.activeTab == ActiveTab.REQUIREMENTS) {
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
    }

    private List<JsonObject> getActiveList() {
        if (this.activeTab == ActiveTab.OBJECTIVES) {
            return this.tempObjectives;
        } else if (this.activeTab == ActiveTab.REQUIREMENTS) {
            return this.tempRequirements;
        } else {
            return this.tempRewards;
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
    }

    private void saveQuestToServer() {
        JsonObject json = new JsonObject();
        json.addProperty("title", this.tempTitle);
        json.addProperty("description", this.tempDescription);

        JsonObject iconObj = new JsonObject();
        iconObj.addProperty("item", this.tempIconItem);
        json.add("icon", iconObj);

        json.addProperty("chapter", this.tempChapter);
        json.addProperty("order", this.tempSortOrder);

        if (this.tempHidden) {
            json.addProperty("hidden", true);
        }
        if (!this.tempIncludeInMain) {
            json.addProperty("include_in_main", false);
        }
        if (this.tempDetailsDefault) {
            json.addProperty("details_default", true);
        }
        if (this.tempDetailsDisabled) {
            json.addProperty("details_disabled", true);
        }

        JsonArray objArr = new JsonArray();
        for (JsonObject o : this.tempObjectives) {
            objArr.add(o);
        }
        json.add("objectives", objArr);

        JsonArray reqArr = new JsonArray();
        for (JsonObject r : this.tempRequirements) {
            reqArr.add(r);
        }
        json.add("requirements", reqArr);

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
            Services.PLATFORM.sendPacketToServer(new QuestEditRemovePacket(this.questToEdit.getId()));
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
                if (mc.getConnection() != null) {
                    List<ResourceLocation> list = new ArrayList<>();
                    for (AdvancementNode root : mc.getConnection().getAdvancements().getTree().roots()) {
                        collectAdvancements(root, list);
                    }
                    keys = list;
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

        if (box == this.iconBox) {
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
                String str = rl.toString();
                if (lower.isEmpty() || str.toLowerCase().contains(lower)) {
                    result.add(str);
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
        this.renderBackground(ps, mouseX, mouseY, delta);
        super.render(ps, mouseX, mouseY, delta);

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

        int color = Questlog.getConfig().colors.textColor | 0xFF000000;
        ps.drawString(this.font, Component.translatable("questlog.editor.id"), panel1X + 15, panel1Y + 10, color, false);
        ps.drawString(this.font, Component.translatable("questlog.editor.title_label"), panel1X + 15, panel1Y + 42, color, false);
        ps.drawString(this.font, Component.translatable("questlog.editor.description_label"), panel1X + 15, panel1Y + 74, color, false);
        ps.drawString(this.font, Component.translatable("questlog.editor.icon_label"), panel1X + 15, panel1Y + 106, color, false);
        ps.drawString(this.font, Component.translatable("questlog.editor.chapter_label"), panel1X + 15, panel1Y + 138, color, false);
        ps.drawString(this.font, Component.translatable("questlog.editor.order_label"), panel1X + 165, panel1Y + 138, color, false);

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

                boolean hovered = mouseX >= listX && mouseX <= listX + listW && mouseY >= rowY && mouseY <= rowY + itemHeight;
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
            ps.drawString(this.font, "Name (Optional):", panel2X + 15, panel2Y + 12, color, false);
            EditorMetadata meta = getMetadata(this.editingType);
            if (meta == null || meta.targetFieldKey() != null) {
                ps.drawString(this.font, getTargetFieldLabel(), panel2X + 15, panel2Y + 48, color, false);
            }
            if (meta == null || meta.amountFieldKey() != null) {
                String amtLabel = this.activeTab == ActiveTab.REWARDS ? "Count/Experience:" : "Req Amount:";
                ps.drawString(this.font, amtLabel, panel2X + 15, panel2Y + 84, color, false);
            }
        } else if (this.rightPageState == RightPageState.LIST && this.activeTab != ActiveTab.SETTINGS) {
            List<JsonObject> list = getActiveList();
            int itemsPerPage = 5;
            int startIdx = this.listPage * itemsPerPage;
            int endIdx = Math.min(startIdx + itemsPerPage, list.size());

            for (int i = startIdx; i < endIdx; i++) {
                JsonObject entry = list.get(i);
                int rowY = panel2Y + 28 + (i - startIdx) * 22;

                String fullType = entry.has("type") ? entry.get("type").getAsString() : "";
                String type = fullType.replace("questlog:", "");
                if (type.isEmpty()) {
                    type = "unknown";
                }
                String text = type;
                if (entry.has("name")) {
                    text = entry.get("name").getAsString();
                } else {
                    String target = "";
                    if (!fullType.isEmpty()) {
                        EditorMetadata meta = getMetadata(fullType);
                        if (meta != null && meta.targetFieldKey() != null) {
                            String key = meta.targetFieldKey();
                            if (entry.has(key)) {
                                target = entry.get(key).getAsString();
                            }
                        }
                    }
                    if (target.isEmpty()) {
                        String[] keys = new String[]{"block", "item", "entity", "biome", "dimension", "structure", "advancement", "stat", "quest", "command", "loot_table", "enchantment", "effect"};
                        for (String k : keys) {
                            if (entry.has(k)) {
                                target = entry.get(k).getAsString();
                                break;
                            }
                        }
                    }
                    if (!target.isEmpty()) {
                        if (target.contains(":")) {
                            target = target.substring(target.indexOf(":") + 1);
                        }
                        text = type + ": " + target;
                    }
                }

                if (this.font.width(text) > 105) {
                    text = this.font.plainSubstrByWidth(text, 95) + "...";
                }

                ps.drawString(this.font, text, panel2X + 10, rowY + 6, color, false);
            }
        }

        NoShadowEditBox activeLeftBox = null;
        if (this.iconBox != null && this.iconBox.isFocused()) activeLeftBox = this.iconBox;
        else if (this.chapterBox != null && this.chapterBox.isFocused()) activeLeftBox = this.chapterBox;

        if (activeLeftBox != null) {
            List<String> matches = getLeftSuggestions(activeLeftBox);
            if (!matches.isEmpty()) {
                int boxX = activeLeftBox.getX();
                int boxY = activeLeftBox.getY();
                int boxW = activeLeftBox.getWidth();
                int startY = boxY + 17;
                int rowHeight = 14;
                int overlayHeight = matches.size() * rowHeight + 2;

                ps.fill(boxX, startY, boxX + boxW, startY + overlayHeight, 0xFF202020);
                ps.fill(boxX - 1, startY, boxX, startY + overlayHeight, 0xFF505050);
                ps.fill(boxX + boxW, startY, boxX + boxW + 1, startY + overlayHeight, 0xFF505050);
                ps.fill(boxX, startY - 1, boxX + boxW, startY, 0xFF505050);
                ps.fill(boxX, startY + overlayHeight, boxX + boxW, startY + overlayHeight + 1, 0xFF505050);

                for (int i = 0; i < matches.size(); i++) {
                    String match = matches.get(i);
                    int itemY = startY + 1 + i * rowHeight;
                    boolean hovered = mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= itemY && mouseY <= itemY + rowHeight;

                    if (hovered) {
                        ps.fill(boxX, itemY, boxX + boxW, itemY + rowHeight, 0xFF404040);
                    }

                    String drawText = match;
                    if (this.font.width(drawText) > boxW - 10) {
                        drawText = this.font.plainSubstrByWidth(drawText, boxW - 16) + "...";
                    }
                    ps.drawString(this.font, drawText, boxX + 4, itemY + 3, hovered ? 0xFFFFFF00 : 0xFFFFFFFF, false);
                }
            }
        }

        if (this.rightPageState == RightPageState.EDIT_ENTRY && this.entryTargetBox != null && this.entryTargetBox.isFocused()) {
            List<String> matches = getSuggestions(this.entryTargetBox.getValue());
            if (!matches.isEmpty()) {
                int startY = panel2Y + 75;
                int rowHeight = 14;
                int overlayHeight = matches.size() * rowHeight + 2;

                ps.fill(panel2X + 15, startY, panel2X + 145, startY + overlayHeight, 0xFF202020);
                ps.fill(panel2X + 14, startY, panel2X + 15, startY + overlayHeight, 0xFF505050);
                ps.fill(panel2X + 145, startY, panel2X + 146, startY + overlayHeight, 0xFF505050);
                ps.fill(panel2X + 15, startY - 1, panel2X + 145, startY, 0xFF505050);
                ps.fill(panel2X + 15, startY + overlayHeight, panel2X + 145, startY + overlayHeight + 1, 0xFF505050);

                for (int i = 0; i < matches.size(); i++) {
                    String match = matches.get(i);
                    int itemY = startY + 1 + i * rowHeight;
                    boolean hovered = mouseX >= panel2X + 15 && mouseX <= panel2X + 145 && mouseY >= itemY && mouseY <= itemY + rowHeight;

                    if (hovered) {
                        ps.fill(panel2X + 15, itemY, panel2X + 145, itemY + rowHeight, 0xFF404040);
                    }

                    String drawText = match;
                    if (this.font.width(drawText) > 120) {
                        drawText = this.font.plainSubstrByWidth(drawText, 110) + "...";
                    }
                    ps.drawString(this.font, drawText, panel2X + 18, itemY + 3, hovered ? 0xFFFFFF00 : 0xFFFFFFFF, false);
                }
            }
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
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {

            NoShadowEditBox activeLeftBox = null;
            if (this.iconBox != null && this.iconBox.isFocused()) activeLeftBox = this.iconBox;
            else if (this.chapterBox != null && this.chapterBox.isFocused()) activeLeftBox = this.chapterBox;

            if (activeLeftBox != null) {
                List<String> matches = getLeftSuggestions(activeLeftBox);
                if (!matches.isEmpty()) {
                    int boxX = activeLeftBox.getX();
                    int boxY = activeLeftBox.getY();
                    int boxW = activeLeftBox.getWidth();
                    int startY = boxY + 17;
                    int rowHeight = 14;

                    if (mouseX >= boxX && mouseX <= boxX + boxW) {
                        for (int i = 0; i < matches.size(); i++) {
                            int itemY = startY + 1 + i * rowHeight;
                            if (mouseY >= itemY && mouseY <= itemY + rowHeight) {
                                activeLeftBox.setValue(matches.get(i));
                                this.saveTemporaryState();
                                activeLeftBox.setFocused(false);
                                this.rebuildWidgets();
                                return true;
                            }
                        }
                    }
                }
            }

            if (this.rightPageState == RightPageState.EDIT_ENTRY && this.entryTargetBox != null && this.entryTargetBox.isFocused()) {
                List<String> matches = getSuggestions(this.entryTargetBox.getValue());
                if (!matches.isEmpty()) {
                    int PANEL_SPACING = 6;
                    int leftWidth = 240;
                    int totalWidth = leftWidth + 160 + PANEL_SPACING;
                    int baseX = (this.width - totalWidth) / 2;
                    int panel2X = baseX + leftWidth + PANEL_SPACING;
                    int panel2Y = (this.height - 190) / 2;

                    int startY = panel2Y + 75;
                    int rowHeight = 14;

                    if (mouseX >= panel2X + 15 && mouseX <= panel2X + 145) {
                        for (int i = 0; i < matches.size(); i++) {
                            int itemY = startY + 1 + i * rowHeight;
                            if (mouseY >= itemY && mouseY <= itemY + rowHeight) {
                                this.entryTargetBox.setValue(matches.get(i));
                                this.saveTemporaryState();
                                this.saveEditingEntry();
                                this.entryTargetBox.setFocused(false);
                                this.rebuildWidgets();
                                return true;
                            }
                        }
                    }
                }
            }

            if (this.rightPageState == RightPageState.SELECT_TYPE) {
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
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
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

        if (this.getFocused() != null && this.getFocused().keyPressed(key, scancode, modifiers)) {
            return true;
        }

        return super.keyPressed(key, scancode, modifiers);
    }

    public void refreshScreen() {
        this.rebuildWidgets();
    }

    private enum RightPageState {
        LIST,
        SELECT_TYPE,
        EDIT_ENTRY
    }

    private enum ActiveTab {
        OBJECTIVES,
        REQUIREMENTS,
        REWARDS,
        SETTINGS
    }
}