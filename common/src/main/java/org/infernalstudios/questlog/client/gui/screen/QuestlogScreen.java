package org.infernalstudios.questlog.client.gui.screen;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.QuestlogClientEvents;
import org.infernalstudios.questlog.client.gui.ContextMenu;
import org.infernalstudios.questlog.client.gui.ContextMenuItem;
import org.infernalstudios.questlog.client.gui.EditorUtils;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.client.gui.components.*;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.infernalstudios.questlog.util.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class QuestlogScreen extends Screen {

    private static final Texture BACKGROUND_TEXTURE = new Texture(
            new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
            1024,
            512,
            0,
            0,
            1024,
            512
    );
    private static final ResourceLocation EDITOR_QUEST_PLUS_TEXTURE = new ResourceLocation(Questlog.MODID, "textures/gui/editor_quest_plus.png");
    private static final ResourceLocation EDITOR_CHAPTER_GEAR_TEXTURE = new ResourceLocation(Questlog.MODID, "textures/gui/editor_chapter_gear.png");
    private static final ResourceLocation EDITOR_CHAPTER_PLUS_TEXTURE = new ResourceLocation(Questlog.MODID, "textures/gui/editor_chapter_plus.png");
    private static final int MAX_TABS = 8;
    private final Screen previousScreen;
    private final QuestManager manager;
    private final Map<ResourceLocation, ChapterInfo> availableChapters = new LinkedHashMap<>();
    @Nullable
    private ScrollableComponent questList;
    private ResourceLocation currentChapter = new ResourceLocation(Questlog.MODID, "main");
    private String searchQuery = "";
    private NoShadowEditBox searchBox;
    private int tabOffset = 0;
    private boolean searchExpanded = false;
    private boolean descriptionsCondensed = false;
    private boolean hideCompleted = false;
    private ContextMenu contextMenu = null;
    public Component pendingTooltip = null;

    public QuestlogScreen(@Nullable Screen previousScreen) {
        super(Component.empty());
        this.previousScreen = previousScreen;
        this.manager = Objects.requireNonNull(QuestlogClient.getLocal());
    }

    @Override
    protected void init() {
        super.init();
        QuestlogClientEvents.mostRecentNotificationQuest = null;
        this.availableChapters.clear();

        for (ResourceLocation chapterId : DefinitionUtil.getCachedChapterKeys()) {
            JsonObject chapterDef = DefinitionUtil.getCachedChapter(chapterId);

            if (chapterDef == null) {
                continue;
            }

            Blittable icon = JsonUtils.getIcon(chapterDef, "icon");
            boolean isPrimary = JsonUtils.getOrDefault(chapterDef, "default_chapter", false);
            boolean hidden = JsonUtils.getOrDefault(chapterDef, "hidden", false);

            String nameStr = JsonUtils.getOrDefault(chapterDef, "name", (String) null);
            boolean translatable = JsonUtils.getOrDefault(chapterDef, "translatable", false);
            Component name;
            if (nameStr != null) {
                name = translatable ? Component.translatable(nameStr) : Component.literal(nameStr);
            } else {
                name = Component.translatable("questlog.chapter." + chapterId.getNamespace() + "." + chapterId.getPath());
            }

            this.availableChapters.put(chapterId, new ChapterInfo(icon, isPrimary, hidden, name));
        }

        this.refreshList();
    }

    private void refreshList() {
        this.clearWidgets();
        this.buildSearch();
        this.buildTabs();
        this.buildEditorButton();
        this.refreshQuestListOnly();
    }

    private void buildEditorButton() {
        if (QuestlogClient.isEditModeActive) {
            int listWidth = 245;
            int listHeight = 136;
            int listX = (this.width - listWidth) / 2 + 1 + Questlog.getConfig().gui.mainPanelX;
            int listY = (this.height - listHeight) / 2 + 1 + Questlog.getConfig().gui.mainPanelY;

            int btnX = listX + listWidth + 17;
            AbstractButton addQuestBtn = getAddQuestBtn(btnX, listY + 5);
            this.addRenderableWidget(addQuestBtn);

            int tabX = listX + Questlog.getConfig().gui.chapterButtonsX;
            int tabY = listY + listHeight + 15 + Questlog.getConfig().gui.chapterButtonsY;

            AbstractButton editChapBtn = getEditChapBtn(tabX + 3, tabY + 34);
            this.addRenderableWidget(editChapBtn);

            AbstractButton addChapBtn = getAddChapBtn(tabX + 33, tabY + 34);
            this.addRenderableWidget(addChapBtn);
        }
    }

    private @NotNull AbstractButton getAddChapBtn(int x, int y) {
        AbstractButton addChapBtn = new AbstractButton(x, y, 20, 20, Component.translatable("questlog.editor.add_chapter")) {
            @Override
            public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
                boolean hovered = this.isHoveredOrFocused();
                ps.blit(EDITOR_CHAPTER_PLUS_TEXTURE, this.getX() + 2, this.getY() + 2, 0, 0, 16, 16, 16, 16);
                if (hovered) {
                    ps.fill(this.getX(), this.getY(), this.getX() + 20, this.getY() + 20, 0x40FFFFFF);
                }
            }

            @Override
            public void onPress() {
                if (minecraft != null) {
                    minecraft.setScreen(new ChapterEditorScreen(QuestlogScreen.this, null));
                }
            }

            @Override
            protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            }
        };
        addChapBtn.setTooltip(Tooltip.create(Component.translatable("questlog.editor.add_chapter")));
        return addChapBtn;
    }

    private @NotNull AbstractButton getEditChapBtn(int x, int y) {
        AbstractButton editChapBtn = new AbstractButton(x, y, 20, 20, Component.translatable("questlog.editor.edit_chapter")) {
            @Override
            public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
                boolean hovered = this.isHoveredOrFocused();
                ps.blit(EDITOR_CHAPTER_GEAR_TEXTURE, this.getX() + 2, this.getY() + 2, 0, 0, 16, 16, 16, 16);
                if (hovered) {
                    ps.fill(this.getX(), this.getY(), this.getX() + 20, this.getY() + 20, 0x40FFFFFF);
                }
            }

            @Override
            public void onPress() {
                if (minecraft != null) {
                    minecraft.setScreen(new ChapterEditorScreen(QuestlogScreen.this, currentChapter));
                }
            }

            @Override
            protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            }
        };
        editChapBtn.setTooltip(Tooltip.create(Component.translatable("questlog.editor.edit_chapter")));
        return editChapBtn;
    }

    private @NotNull AbstractButton getAddQuestBtn(int x, int y) {
        AbstractButton addQuestBtn = new AbstractButton(x, y, 20, 20, Component.translatable("questlog.editor.add_quest")) {
            @Override
            public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
                boolean hovered = this.isHoveredOrFocused();
                ps.blit(EDITOR_QUEST_PLUS_TEXTURE, this.getX() + 2, this.getY() + 2, 0, 0, 16, 16, 16, 16);
                if (hovered) {
                    ps.fill(this.getX(), this.getY(), this.getX() + 20, this.getY() + 20, 0x40FFFFFF);
                }
            }

            @Override
            public void onPress() {
                if (minecraft != null) {
                    minecraft.setScreen(new QuestEditorScreen(QuestlogScreen.this));
                }
            }

            @Override
            protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            }
        };
        addQuestBtn.setTooltip(Tooltip.create(Component.translatable("questlog.editor.add_quest")));
        return addQuestBtn;
    }

    private void refreshQuestListOnly() {
        if (this.questList != null) {
            this.removeWidget(this.questList);
        }

        this.questList = this.getList();
        if (this.questList != null) {
            this.addRenderableWidget(this.questList);
        }
    }

    private void buildSearch() {
        int listWidth = 245;
        int listHeight = 136;
        int listX = (this.width - listWidth) / 2 + 1 + Questlog.getConfig().gui.mainPanelX;
        int listY = (this.height - listHeight) / 2 + 1 + Questlog.getConfig().gui.mainPanelY;

        int searchY = listY - 32 + Questlog.getConfig().gui.searchBarY;
        int searchWidth = this.searchExpanded ? 193 : 28;
        int searchX = listX + listWidth - searchWidth + 12 + Questlog.getConfig().gui.searchBarX;

        this.addRenderableWidget(new AbstractButton(searchX, searchY, searchWidth, 18, Component.empty()) {
            @Override
            public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
                boolean hoverToggle = isMouseOver(mouseX, mouseY) && (!searchExpanded || mouseX >= getX() + width - 28);
                if (searchExpanded) {
                    (hoverToggle ? QuestlogGuiSet.DEFAULT.searchTabExpandedHovered : QuestlogGuiSet.DEFAULT.searchTabExpanded)
                            .blit(ps, getX() - 30, getY() - 19);
                } else {
                    (hoverToggle ? QuestlogGuiSet.DEFAULT.searchTabMinimizedHovered : QuestlogGuiSet.DEFAULT.searchTabMinimized)
                            .blit(ps, getX() - 15, getY() - 19);
                }
            }

            @Override
            public void onPress() {
                searchExpanded = !searchExpanded;
                if (!searchExpanded) {
                    searchQuery = "";
                }
                refreshList();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (isMouseOver(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_1) {
                    if (searchExpanded && mouseX < getX() + width - 28) {
                        return false;
                    }
                    onPress();
                    return true;
                }
                return false;
            }

            @Override
            protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            }
        });

        if (this.searchExpanded) {
            this.searchBox = new NoShadowEditBox(this.font, searchX + 86, searchY + 7, searchWidth - 36, 16, Component.translatable("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setValue(this.searchQuery);
            this.searchBox.setBordered(false);
            this.searchBox.setTextColor(Questlog.getConfig().colors.searchTextColor);
            this.searchBox.setResponder(query -> {
                this.searchQuery = query;
                this.refreshQuestListOnly();
            });
            this.addRenderableWidget(this.searchBox);

            this.addRenderableWidget(new AbstractButton(searchX - 36, searchY + 2, 14, 14, Component.empty()) {
                @Override
                public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
                    boolean hovered = isMouseOver(mouseX, mouseY);
                    if (hideCompleted) {
                        (hovered ? QuestlogGuiSet.DEFAULT.hiddenButtonHovered : QuestlogGuiSet.DEFAULT.hiddenButton)
                                .blit(ps, getX() - 6, getY() - 6);
                    } else {
                        (hovered ? QuestlogGuiSet.DEFAULT.visibleButtonHovered : QuestlogGuiSet.DEFAULT.visibleButton)
                                .blit(ps, getX() - 6, getY() - 6);
                    }
                }

                @Override
                public void onPress() {
                    hideCompleted = !hideCompleted;
                    refreshList();
                }

                @Override
                protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
                }
            });

            this.addRenderableWidget(new AbstractButton(searchX - 18, searchY + 2, 14, 14, Component.empty()) {
                @Override
                public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
                    boolean hovered = isMouseOver(mouseX, mouseY);
                    if (descriptionsCondensed) {
                        (hovered ? QuestlogGuiSet.DEFAULT.expandButtonHovered : QuestlogGuiSet.DEFAULT.expandButton)
                                .blit(ps, getX() - 6, getY() - 6);
                    } else {
                        (hovered ? QuestlogGuiSet.DEFAULT.condenseButtonHovered : QuestlogGuiSet.DEFAULT.condenseButton)
                                .blit(ps, getX() - 6, getY() - 6);
                    }
                }

                @Override
                public void onPress() {
                    descriptionsCondensed = !descriptionsCondensed;
                    refreshList();
                }

                @Override
                protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
                }
            });
        } else {
            this.searchBox = null;
        }
    }

    private void buildTabs() {
        List<ResourceLocation> chapterKeys = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ChapterInfo> entry : this.availableChapters.entrySet()) {
            if (QuestlogClient.isEditModeActive || !entry.getValue().hidden) {
                chapterKeys.add(entry.getKey());
            }
        }
        int listWidth = 245;
        int listHeight = 136;
        int listX = (this.width - listWidth) / 2 + 1 + Questlog.getConfig().gui.mainPanelX;
        int listY = (this.height - listHeight) / 2 + 1 + Questlog.getConfig().gui.mainPanelY;

        int tabX = listX + Questlog.getConfig().gui.chapterButtonsX;
        int tabY = listY + listHeight + 15 + Questlog.getConfig().gui.chapterButtonsY;
        int arrowY = tabY + 5;

        if (this.tabOffset > 0) {
            this.addRenderableWidget(new ChapterArrowButton(tabX - 12, arrowY, true, () -> {
                this.tabOffset--;
                this.refreshList();
            }, QuestlogGuiSet.DEFAULT));
        }

        for (int i = 0; i < MAX_TABS && i + this.tabOffset < chapterKeys.size(); i++) {
            ResourceLocation chap = chapterKeys.get(i + this.tabOffset);
            ChapterInfo info = this.availableChapters.get(chap);
            boolean isSelected = chap.equals(this.currentChapter);

            this.addRenderableWidget(new ChapterTabButton(chap, tabX + (i * 30), tabY, info.icon, isSelected, info.isPrimary, () -> {
                this.currentChapter = chap;
                this.refreshList();
            }, QuestlogGuiSet.DEFAULT, info.name));
        }

        if (this.tabOffset + MAX_TABS < chapterKeys.size()) {
            this.addRenderableWidget(new ChapterArrowButton(tabX + (MAX_TABS * 30), arrowY, false, () -> {
                this.tabOffset++;
                this.refreshList();
            }, QuestlogGuiSet.DEFAULT));
        }
    }

    @Nullable
    private ScrollableComponent getList() {
        int width = 245;
        int height = 136;
        int x = (this.width - width) / 2 + 1 + Questlog.getConfig().gui.mainPanelX;
        int y = (this.height - height) / 2 + 1 + Questlog.getConfig().gui.mainPanelY;

        List<Quest> quests = this.manager.getAllQuests().stream()
                .filter(quest -> QuestlogClient.isEditModeActive || (quest.isTriggered() && !quest.getDisplay().isHidden()))
                .filter(quest -> !this.hideCompleted || !quest.isCompleted())
                .filter(quest -> {
                    String chapterStr = quest.getDisplay().getChapter();
                    ResourceLocation questChapter = chapterStr.contains(":")
                            ? ResourceLocation.tryParse(chapterStr)
                            : new ResourceLocation(Questlog.MODID, chapterStr);

                    ChapterInfo questChapterInfo = this.availableChapters.get(questChapter);
                    boolean shouldShowChapter = questChapterInfo != null && (QuestlogClient.isEditModeActive || !questChapterInfo.hidden);

                    return Objects.requireNonNull(questChapter).equals(this.currentChapter) ||
                            (this.currentChapter.getPath().equals("main") && quest.getDisplay().shouldIncludeInMain() && shouldShowChapter);
                })
                .filter(quest -> quest.getDisplay().matchesSearch(this.searchQuery))
                .toList();

        if (quests.isEmpty()) {
            return null;
        }

        return new ScrollableComponent(
                x, y, width, height,
                new QuestList(Minecraft.getInstance(), quests, displayData -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new QuestDetails(this, displayData));
                    }
                }, this.descriptionsCondensed)
        );
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics ps) {
        super.renderBackground(ps);

        int x = (this.width - BACKGROUND_TEXTURE.width()) / 2;
        int y = (this.height - BACKGROUND_TEXTURE.height()) / 2;
        BACKGROUND_TEXTURE.blit(ps, x, y);
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float delta) {
        this.pendingTooltip = null;
        this.renderBackground(ps);
        int renderMouseX = this.contextMenu != null ? -9999 : mouseX;
        int renderMouseY = this.contextMenu != null ? -9999 : mouseY;
        super.render(ps, renderMouseX, renderMouseY, delta);

        if (this.questList == null) {
            Font font = this.minecraft != null ? this.minecraft.font : null;
            if (font != null) {
                float scale = 2.0F;
                ps.pose().pushPose();
                ps.pose().scale(scale, scale, scale);
                Component text = Component.translatable("questlog.no_quests");

                ps.drawString(
                        font,
                        text,
                        (int) (((this.width - (font.width(text) * scale)) / 2) / scale),
                        (int) (((this.height - (font.lineHeight * scale)) / 2) / scale),
                        Questlog.getConfig().colors.noQuestsColor | 0xFF000000,
                        false
                );
                ps.pose().popPose();
            }
        }

        if (QuestlogClient.isEditModeActive) {
            Font font = this.minecraft != null ? this.minecraft.font : null;
            if (font != null) {
                ps.drawString(font, Component.translatable("questlog.editor.active"), 10, 10, 0xE6AA1C | 0xFF000000, false);
            }
        }

        if (this.questList != null && this.questList.scrollable instanceof QuestList list) {
            QuestList.QuestListEntry hovered = list.getHovered();
            if (hovered != null && hovered.getQuest() != null) {
                String titleStr = hovered.getQuest().getDisplay().getTitle().getString();
                if (titleStr.startsWith("Broken Quest")) {
                    String descStr = hovered.getQuest().getDisplay().getDescription().getString();
                    int errorIdx = descStr.indexOf("Error details:\n");
                    if (errorIdx != -1) {
                        String errorMsg = descStr.substring(errorIdx + "Error details:\n".length());
                        List<Component> tooltipLines = new ArrayList<>();
                        tooltipLines.add(Component.literal("JSON Error Details:").withStyle(net.minecraft.ChatFormatting.RED).withStyle(net.minecraft.ChatFormatting.BOLD));
                        for (String line : errorMsg.split("\n")) {
                            tooltipLines.add(Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY));
                        }
                        ps.renderComponentTooltip(this.font, tooltipLines, renderMouseX, renderMouseY);
                    }
                }
            }
        }

        if (this.contextMenu != null) {
            this.contextMenu.render(ps, mouseX, mouseY, this.font);
        }

        if (this.pendingTooltip != null) {
            ps.renderTooltip(this.font, this.pendingTooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.contextMenu != null) {
            if (this.contextMenu.mouseScrolled(amount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
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

        if (QuestlogClient.isEditModeActive && button == GLFW.GLFW_MOUSE_BUTTON_2) {
            // Check if click is on a chapter tab button
            for (var listener : this.children()) {
                if (listener instanceof ChapterTabButton btn && btn.isMouseOver(mouseX, mouseY)) {
                    List<ContextMenuItem> items = new ArrayList<>();
                    items.add(new ContextMenuItem(Component.translatable("questlog.menu.edit_chapter"), () -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new ChapterEditorScreen(this, btn.getChapterId()));
                        }
                    }));
                    if (!btn.getChapterId().getPath().equals("main")) {
                        items.add(new ContextMenuItem(Component.translatable("questlog.menu.delete_chapter"), () -> {
                            this.confirmDeleteChapter(btn.getChapterId(), btn.getMessage());
                        }));
                    }
                    this.openContextMenu((int) mouseX, (int) mouseY, items);
                    return true;
                }
            }

            // Check if click is on the empty chapter tab bar area
            int listWidth = 245;
            int listHeight = 136;
            int listX = (this.width - listWidth) / 2 + 1 + Questlog.getConfig().gui.mainPanelX;
            int listY = (this.height - listHeight) / 2 + 1 + Questlog.getConfig().gui.mainPanelY;
            int tabX = listX + Questlog.getConfig().gui.chapterButtonsX;
            int tabY = listY + listHeight + 15 + Questlog.getConfig().gui.chapterButtonsY;
            if (mouseX >= tabX - 12 && mouseX <= tabX + (MAX_TABS * 30) + 12 && mouseY >= tabY && mouseY <= tabY + 30) {
                List<ContextMenuItem> items = new ArrayList<>();
                items.add(new ContextMenuItem(Component.translatable("questlog.menu.add_chapter"), () -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new ChapterEditorScreen(this, null));
                    }
                }));
                this.openContextMenu((int) mouseX, (int) mouseY, items);
                return true;
            }

            // Check if click is on the quest list area
            boolean isOverListArea = mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + listHeight;
            if (isOverListArea) {
                Quest hoveredQuest = null;
                if (this.questList != null && this.questList.scrollable instanceof QuestList list) {
                    QuestList.QuestListEntry hovered = list.getHovered();
                    if (hovered != null) {
                        hoveredQuest = hovered.getQuest();
                    }
                }

                List<ContextMenuItem> items = new ArrayList<>();
                if (hoveredQuest != null) {
                    Quest finalHovered = hoveredQuest;
                    items.add(new ContextMenuItem(Component.translatable("questlog.menu.edit"), () -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new QuestEditorScreen(this, finalHovered));
                        }
                    }));
                    items.add(new ContextMenuItem(Component.translatable("questlog.menu.duplicate"), () -> {
                        EditorUtils.duplicateQuest(finalHovered.getId());
                    }));
                    items.add(new ContextMenuItem(Component.translatable("questlog.menu.copy"), () -> {
                        EditorUtils.copyQuestToClipboard(finalHovered.getId());
                    }));
                    if (EditorUtils.hasCopiedQuest()) {
                        items.add(new ContextMenuItem(Component.translatable("questlog.menu.paste"), () -> {
                            EditorUtils.pasteQuest(this.currentChapter);
                        }));
                    }
                    items.add(new ContextMenuItem(Component.translatable("questlog.menu.delete"), () -> {
                        this.confirmDeleteQuest(finalHovered);
                    }));
                } else {
                    if (EditorUtils.hasCopiedQuest()) {
                        items.add(new ContextMenuItem(Component.translatable("questlog.menu.paste"), () -> {
                            EditorUtils.pasteQuest(this.currentChapter);
                        }));
                    }
                    items.add(new ContextMenuItem(Component.translatable("questlog.menu.add_quest"), () -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new QuestEditorScreen(this));
                        }
                    }));
                    items.add(new ContextMenuItem(Component.translatable("questlog.menu.add_quest_preset"), () -> {
                        this.openPresetsContextMenu((int) mouseX, (int) mouseY);
                    }));
                }

                if (!items.isEmpty()) {
                    this.openContextMenu((int) mouseX, (int) mouseY, items);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openContextMenu(int x, int y, List<ContextMenuItem> items) {
        if (this.minecraft != null) {
            this.contextMenu = new ContextMenu(x, y, items, this.font, this.width, this.height);
        }
    }

    private void openPresetsContextMenu(int x, int y) {
        List<ContextMenuItem> items = new ArrayList<>();
        items.add(new ContextMenuItem(Component.translatable("questlog.menu.back"), () -> {
            List<ContextMenuItem> mainItems = new ArrayList<>();
            if (EditorUtils.hasCopiedQuest()) {
                mainItems.add(new ContextMenuItem(Component.translatable("questlog.menu.paste"), () -> {
                    EditorUtils.pasteQuest(this.currentChapter);
                }));
            }
            mainItems.add(new ContextMenuItem(Component.translatable("questlog.menu.add_quest"), () -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new QuestEditorScreen(this));
                }
            }));
            mainItems.add(new ContextMenuItem(Component.translatable("questlog.menu.add_quest_preset"), () -> {
                this.openPresetsContextMenu(x, y);
            }));
            this.openContextMenu(x, y, mainItems);
        }));

        for (EditorUtils.EditorPreset preset : EditorUtils.getPresets("quests")) {
            items.add(new ContextMenuItem(
                Component.literal(preset.title()),
                () -> {
                    if (this.minecraft != null) {
                        try {
                            JsonObject presetJsonCloned = preset.json().deepCopy();
                            presetJsonCloned.addProperty("chapter", this.currentChapter.toString());
                            this.minecraft.setScreen(new QuestEditorScreen(this, null, presetJsonCloned));
                        } catch (Exception e) {
                            Questlog.LOGGER.error("Failed to load preset json", e);
                        }
                    }
                },
                preset.description().isEmpty() ? null : Component.literal(preset.description())
            ));
        }

        this.openContextMenu(x, y, items);
    }

    private void confirmDeleteQuest(Quest quest) {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new ConfirmScreen(
                (boolean confirm) -> {
                    if (confirm) {
                        EditorUtils.deleteQuest(quest.getId());
                    }
                    this.minecraft.setScreen(this);
                },
                Component.translatable("questlog.menu.delete.confirm.title"),
                Component.translatable("questlog.menu.delete.confirm.message", quest.getDisplay().getTitle())
            ));
        }
    }

    private void confirmDeleteChapter(ResourceLocation chapterId, Component name) {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new ConfirmScreen(
                (boolean confirm) -> {
                    if (confirm) {
                        EditorUtils.deleteChapter(chapterId);
                        if (chapterId.equals(this.currentChapter)) {
                            this.currentChapter = new ResourceLocation(Questlog.MODID, "main");
                        }
                    }
                    this.minecraft.setScreen(this);
                },
                Component.translatable("questlog.menu.delete_chapter.confirm.title"),
                Component.translatable("questlog.menu.delete_chapter.confirm.message", name)
            ));
        }
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(this.previousScreen);
            return true;
        }

        if (this.searchBox != null && this.searchBox.isFocused()) {
            return this.searchBox.keyPressed(key, scancode, modifiers) || super.keyPressed(key, scancode, modifiers);
        }

        if (key == GLFW.GLFW_KEY_E || QuestlogClient.OPEN_SCREEN_KEY.matches(key, scancode)) {
            Minecraft.getInstance().setScreen(this.previousScreen);
            return true;
        }

        return super.keyPressed(key, scancode, modifiers);
    }

    private static class ChapterInfo {
        Blittable icon;
        boolean isPrimary;
        boolean hidden;
        Component name;

        ChapterInfo(Blittable icon, boolean isPrimary, boolean hidden, Component name) {
            this.icon = icon;
            this.isPrimary = isPrimary;
            this.hidden = hidden;
            this.name = name;
        }
    }
}
