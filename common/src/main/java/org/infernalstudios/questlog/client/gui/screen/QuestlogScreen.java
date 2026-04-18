package org.infernalstudios.questlog.client.gui.screen;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.QuestlogClientEvents;
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
            ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "textures/gui/questlog.png"),
            1024,
            512,
            0,
            0,
            1024,
            512
    );
    private static final int MAX_TABS = 8;
    private final Screen previousScreen;
    private final QuestManager manager;
    private final Map<ResourceLocation, ChapterInfo> availableChapters = new LinkedHashMap<>();
    @Nullable
    private ScrollableComponent questList;
    private ResourceLocation currentChapter = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "main");
    private String searchQuery = "";
    private NoShadowEditBox searchBox;
    private int tabOffset = 0;
    private boolean searchExpanded = false;
    private boolean descriptionsCondensed = false;

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
        if (QuestlogClient.isEditModeActive || Questlog.getConfig().editor.enableEditorButton) {

            int listWidth = 245;
            int listHeight = 136;
            int listX = (this.width - listWidth) / 2 + 1 + Questlog.getConfig().gui.mainPanelX;
            int listY = (this.height - listHeight) / 2 + 1 + Questlog.getConfig().gui.mainPanelY;

            int btnX = listX - 10;
            int btnY = listY + listHeight + 10;

            this.addRenderableWidget(Button.builder(Component.translatable("questlog.editor.enter"), btn -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new QuestEditorScreen(this));
                }
            }).bounds(btnX, btnY, 80, 20).build());
        }
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
            if (!entry.getValue().hidden) {
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

            this.addRenderableWidget(new ChapterTabButton(tabX + (i * 30), tabY, info.icon, isSelected, info.isPrimary, () -> {
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
                .filter(quest -> quest.isTriggered() && !quest.getDisplay().isHidden())
                .filter(quest -> {
                    String chapterStr = quest.getDisplay().getChapter();
                    ResourceLocation questChapter = chapterStr.contains(":")
                            ? ResourceLocation.tryParse(chapterStr)
                            : ResourceLocation.fromNamespaceAndPath(Questlog.MODID, chapterStr);

                    ChapterInfo questChapterInfo = this.availableChapters.get(questChapter);
                    boolean shouldShowChapter = questChapterInfo != null && !questChapterInfo.hidden;

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
    public void renderBackground(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(ps, mouseX, mouseY, partialTick);

        int x = (this.width - BACKGROUND_TEXTURE.width()) / 2;
        int y = (this.height - BACKGROUND_TEXTURE.height()) / 2;
        BACKGROUND_TEXTURE.blit(ps, x, y);
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float delta) {
        super.render(ps, mouseX, mouseY, delta);

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
    }

    @Override
    public boolean isPauseScreen() {
        return true;
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