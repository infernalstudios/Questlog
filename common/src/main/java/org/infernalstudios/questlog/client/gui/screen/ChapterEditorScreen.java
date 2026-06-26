package org.infernalstudios.questlog.client.gui.screen;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.AutocompleteHelper;
import org.infernalstudios.questlog.client.gui.EditorUtils;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.client.gui.components.NoShadowEditBox;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.network.packet.ChapterEditSavePacket;
import org.infernalstudios.questlog.network.packet.QuestEditSavePacket;
import org.infernalstudios.questlog.platform.Services;
import org.infernalstudios.questlog.util.texture.NineSliceTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ChapterEditorScreen extends Screen {

    private static final ResourceLocation CROSS_ICON = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_cross.png");
    private static final ResourceLocation CROSS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_cross_highlighted.png");
    private static final ResourceLocation PLUS_ICON = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_plus.png");
    private static final ResourceLocation PLUS_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/editor_plus_highlighted.png");

    private final Screen previousScreen;
    @Nullable
    private final ResourceLocation chapterToEdit;
    private final AutocompleteHelper autocompleteHelper = new AutocompleteHelper();
    private NineSliceTexture bgLeft;
    private NineSliceTexture bgRight;
    private String tempId = "";
    private String tempTitle = "";
    private String tempIconItem = "";
    private int tempSortOrder = 0;
    private boolean tempDefault = false;
    private boolean tempHidden = false;
    private NoShadowEditBox idBox;
    private NoShadowEditBox titleBox;
    private NoShadowEditBox iconBox;
    private NoShadowEditBox orderBox;
    private int listPage = 0;

    public ChapterEditorScreen(Screen previousScreen, @Nullable ResourceLocation chapterToEdit) {
        super(Component.translatable(chapterToEdit != null ? "questlog.editor.edit_chapter" : "questlog.editor.add_chapter"));
        this.previousScreen = previousScreen;
        this.chapterToEdit = chapterToEdit;
        this.loadChapterData();
    }

    private void loadChapterData() {
        if (this.chapterToEdit != null) {
            this.tempId = this.chapterToEdit.toString();
            JsonObject definition = DefinitionUtil.getCachedChapter(this.chapterToEdit);
            if (definition != null) {
                this.tempTitle = definition.has("name") ? definition.get("name").getAsString() : "";
                if (definition.has("icon") && definition.get("icon").isJsonObject()) {
                    JsonObject iconObj = definition.getAsJsonObject("icon");
                    this.tempIconItem = iconObj.has("item") ? iconObj.get("item").getAsString() : "minecraft:knowledge_book";
                } else {
                    this.tempIconItem = "minecraft:knowledge_book";
                }
                this.tempSortOrder = definition.has("order") ? definition.get("order").getAsInt() : 0;
                this.tempDefault = definition.has("default_chapter") && definition.get("default_chapter").getAsBoolean();
                this.tempHidden = definition.has("hidden") && definition.get("hidden").getAsBoolean();
            }
        } else {
            this.tempId = "questlog:new_chapter";
            this.tempTitle = "New Chapter";
            this.tempIconItem = "minecraft:knowledge_book";
            this.tempSortOrder = 0;
            this.tempDefault = false;
            this.tempHidden = false;
        }
    }

    public void saveTemporaryState() {
        if (this.idBox != null) this.tempId = this.idBox.getValue();
        if (this.titleBox != null) this.tempTitle = this.titleBox.getValue();
        if (this.iconBox != null) this.tempIconItem = this.iconBox.getValue();
        if (this.orderBox != null) {
            try {
                this.tempSortOrder = Integer.parseInt(this.orderBox.getValue());
            } catch (NumberFormatException ignored) {
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
        this.idBox.setEditable(this.chapterToEdit == null);
        this.idBox.active = (this.chapterToEdit == null);
        this.idBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.chapter_id")));
        this.addRenderableWidget(this.idBox);

        this.titleBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 52, 210, 16, Component.empty());
        this.titleBox.setMaxLength(64);
        this.titleBox.setValue(this.tempTitle);
        this.titleBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.chapter_title")));
        this.addRenderableWidget(this.titleBox);

        this.iconBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 84, 210, 16, Component.empty());
        this.iconBox.setMaxLength(128);
        this.iconBox.setValue(this.tempIconItem);
        this.iconBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.chapter_icon")));
        this.addRenderableWidget(this.iconBox);

        this.orderBox = new NoShadowEditBox(this.font, panel1X + 15, panel1Y + 116, 50, 16, Component.empty());
        this.orderBox.setMaxLength(8);
        this.orderBox.setValue(String.valueOf(this.tempSortOrder));
        this.orderBox.setFilter(s -> s.isEmpty() || s.matches("-?\\d*"));
        this.orderBox.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.chapter_order")));
        this.addRenderableWidget(this.orderBox);

        Button btnDefault = Button.builder(Component.literal("Default: " + (this.tempDefault ? "True" : "False")), btn -> {
            this.tempDefault = !this.tempDefault;
            btn.setMessage(Component.literal("Default: " + (this.tempDefault ? "True" : "False")));
        }).bounds(panel1X + 75, panel1Y + 116, 70, 16).build();
        btnDefault.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.chapter_default")));
        this.addRenderableWidget(btnDefault);

        Button btnHidden = Button.builder(Component.literal("Hidden: " + (this.tempHidden ? "True" : "False")), btn -> {
            this.tempHidden = !this.tempHidden;
            btn.setMessage(Component.literal("Hidden: " + (this.tempHidden ? "True" : "False")));
        }).bounds(panel1X + 155, panel1Y + 116, 70, 16).build();
        btnHidden.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.chapter_hidden")));
        this.addRenderableWidget(btnHidden);

        List<ResourceLocation> allQuests = DefinitionUtil.getCachedQuestKeys();
        int itemsPerPage = 5;
        int startIdx = this.listPage * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, allQuests.size());

        String currentChapPath = this.chapterToEdit != null ? this.chapterToEdit.getPath() : "";

        for (int i = startIdx; i < endIdx; i++) {
            ResourceLocation qKey = allQuests.get(i);
            JsonObject qJson = DefinitionUtil.getCachedQuest(qKey);
            String qChap = qJson.has("chapter") ? qJson.get("chapter").getAsString() : "main";
            if (qChap.contains(":")) {
                ResourceLocation rl = ResourceLocation.tryParse(qChap);
                if (rl != null) {
                    qChap = rl.getPath();
                }
            }
            boolean inThisChapter = qChap.equals(currentChapPath);

            int rowY = panel2Y + 28 + (i - startIdx) * 22;

            ResourceLocation icon = inThisChapter ? CROSS_ICON : PLUS_ICON;
            ResourceLocation iconHighlight = inThisChapter ? CROSS_HIGHLIGHTED : PLUS_HIGHLIGHTED;
            Component tooltip = Component.literal(inThisChapter ? "Remove" : "Add");

            AbstractButton actionButton = new AbstractButton(panel2X + 125, rowY + 1, 16, 16, Component.empty()) {
                @Override
                public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
                    boolean hovered = this.isHoveredOrFocused();
                    ResourceLocation tex = hovered ? iconHighlight : icon;
                    ps.blit(tex, this.getX(), this.getY(), 0, 0, 16, 16, 16, 16);
                }

                @Override
                public void onPress() {
                    ChapterEditorScreen.this.saveTemporaryState();
                    if (inThisChapter) {
                        qJson.addProperty("chapter", currentChapPath.equals("main") ? "" : "main");
                    } else {
                        if (!currentChapPath.isEmpty()) {
                            qJson.addProperty("chapter", currentChapPath);
                        } else {
                            String futureId = ChapterEditorScreen.this.idBox.getValue().trim();
                            try {
                                ResourceLocation futureRl = futureId.contains(":") ? ResourceLocation.tryParse(futureId) : ResourceLocation.fromNamespaceAndPath(Questlog.MODID, futureId);
                                if (futureRl != null) {
                                    qJson.addProperty("chapter", futureRl.getPath());
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    DefinitionUtil.putCachedQuest(qKey, qJson);
                    Services.PLATFORM.sendPacketToServer(new QuestEditSavePacket(qKey, qJson.toString()));
                    ChapterEditorScreen.this.rebuildWidgets();
                }

                @Override
                protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
                }
            };

            actionButton.setTooltip(Tooltip.create(tooltip));

            if (this.chapterToEdit == null && this.idBox.getValue().trim().isEmpty()) {
                actionButton.active = false;
            }
            this.addRenderableWidget(actionButton);
        }

        // Pagination buttons
        if (this.listPage > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("<"), btn -> {
                this.saveTemporaryState();
                this.listPage--;
                this.rebuildWidgets();
            }).bounds(panel2X + 15, panel2Y + 162, 16, 16).build());
        }

        if (endIdx < allQuests.size()) {
            this.addRenderableWidget(Button.builder(Component.literal(">"), btn -> {
                this.saveTemporaryState();
                this.listPage++;
                this.rebuildWidgets();
            }).bounds(panel2X + 129, panel2Y + 162, 16, 16).build());
        }

        // --- Bottom Navigation Buttons ---
        int btnWidth = 100;
        int bottomY = panel1Y + height + 10;

        // Cancel
        this.addRenderableWidget(Button.builder(Component.translatable("questlog.editor.cancel"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.previousScreen);
            }
        }).bounds(panel1X, bottomY, btnWidth, 20).build());

        // Save
        this.addRenderableWidget(Button.builder(Component.translatable("questlog.editor.save_chapter"), btn -> {
            this.saveTemporaryState();
            this.saveChapterToServer();
        }).bounds(panel1X + btnWidth + 5, bottomY, btnWidth, 20).build());

        // Delete
        if (this.chapterToEdit != null && !this.chapterToEdit.getPath().equals("main")) {
            Button btnDeleteChapter = Button.builder(Component.translatable("questlog.editor.delete_chapter"), btn -> {
                this.deleteChapterOnServer();
            }).bounds(panel2X + rightWidth - btnWidth, bottomY, btnWidth, 20).build();
            btnDeleteChapter.setTooltip(Tooltip.create(Component.translatable("questlog.editor.tooltip.delete_chapter")));
            this.addRenderableWidget(btnDeleteChapter);
        }
    }

    private void saveChapterToServer() {
        String idStr = this.tempId.trim();
        if (idStr.isEmpty()) return;

        ResourceLocation rl;
        try {
            rl = idStr.contains(":") ? ResourceLocation.tryParse(idStr) : ResourceLocation.fromNamespaceAndPath(Questlog.MODID, idStr);
        } catch (Exception e) {
            return;
        }
        if (rl == null) return;

        JsonObject json = new JsonObject();
        json.addProperty("name", this.tempTitle);

        JsonObject iconObj = new JsonObject();
        iconObj.addProperty("item", this.tempIconItem);
        json.add("icon", iconObj);

        json.addProperty("order", this.tempSortOrder);
        json.addProperty("default_chapter", this.tempDefault);
        json.addProperty("hidden", this.tempHidden);

        DefinitionUtil.putCachedChapter(rl, json);
        Services.PLATFORM.sendPacketToServer(new ChapterEditSavePacket(rl, json.toString()));

        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
    }

    private void deleteChapterOnServer() {
        if (this.chapterToEdit != null) {
            EditorUtils.deleteChapter(this.chapterToEdit);
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
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
        ps.drawString(this.font, Component.translatable("questlog.editor.chapter_id"), panel1X + 15, panel1Y + 10, color, false);
        ps.drawString(this.font, Component.translatable("questlog.editor.title_label"), panel1X + 15, panel1Y + 42, color, false);
        ps.drawString(this.font, Component.translatable("questlog.editor.icon_label"), panel1X + 15, panel1Y + 74, color, false);
        ps.drawString(this.font, Component.translatable("questlog.editor.order_label"), panel1X + 15, panel1Y + 106, color, false);

        ps.drawString(this.font, "Quests in Chapter:", panel2X + 15, panel2Y + 12, color, false);

        List<ResourceLocation> allQuests = DefinitionUtil.getCachedQuestKeys();
        int itemsPerPage = 5;
        int startIdx = this.listPage * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, allQuests.size());

        for (int i = startIdx; i < endIdx; i++) {
            ResourceLocation qKey = allQuests.get(i);
            JsonObject qJson = DefinitionUtil.getCachedQuest(qKey);
            String title = qJson.has("title") ? qJson.get("title").getAsString() : qKey.getPath();

            int rowY = panel2Y + 28 + (i - startIdx) * 22;

            String displayText = title;
            if (this.font.width(displayText) > 100) {
                displayText = this.font.plainSubstrByWidth(displayText, 90) + "...";
            }

            ps.drawString(this.font, displayText, panel2X + 10, rowY + 5, color, false);
        }

        this.autocompleteHelper.update(this.iconBox, () -> getLeftSuggestions(this.iconBox));
        this.autocompleteHelper.render(ps, this.font, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.autocompleteHelper.mouseClicked(mouseX, mouseY, button, val -> {
            if (this.iconBox != null) {
                this.iconBox.setValue(val);
                this.saveTemporaryState();
                this.iconBox.setFocused(false);
                this.rebuildWidgets();
            }
        })) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.previousScreen);
            }
            return true;
        }

        if (this.autocompleteHelper.keyPressed(key, val -> {
            if (this.iconBox != null) {
                this.iconBox.setValue(val);
                this.saveTemporaryState();
                this.iconBox.setFocused(false);
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
}
