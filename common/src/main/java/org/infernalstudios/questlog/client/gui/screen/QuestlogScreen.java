package org.infernalstudios.questlog.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.client.gui.components.*;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
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
    private final Map<String, ChapterInfo> availableChapters = new LinkedHashMap<>();
    @Nullable
    private ScrollableComponent questList;
    private String currentChapter = "main";
    private String searchQuery = "";
    private NoShadowEditBox searchBox;
    private int tabOffset = 0;
    private boolean searchExpanded = false;

    public QuestlogScreen(@Nullable Screen previousScreen) {
        super(Component.empty());
        this.previousScreen = previousScreen;
        this.manager = Objects.requireNonNull(QuestlogClient.getLocal());
    }

    @Override
    protected void init() {
        super.init();

        this.availableChapters.clear();
        this.availableChapters.put("main", new ChapterInfo(null, true));
        for (Quest quest : this.manager.getAllQuests()) {
            if (quest.isTriggered() && !quest.getDisplay().isHidden()) {
                QuestDisplayData display = quest.getDisplay();
                String chap = display.getChapter();
                ChapterInfo info = this.availableChapters.get(chap);

                if (info == null) {
                    this.availableChapters.put(chap, new ChapterInfo(display.getChapterIcon(), display.isPrimaryChapter()));
                } else {
                    if (display.isPrimaryChapter() && !info.isPrimary) {
                        info.isPrimary = true;
                    }
                    if (info.icon == null && display.getChapterIcon() != null) {
                        info.icon = display.getChapterIcon();
                    }
                }
            }
        }

        this.refreshList();
    }

    private void refreshList() {
        this.clearWidgets();
        this.buildSearch();
        this.buildTabs();
        this.refreshQuestListOnly();
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
        int listX = (this.width - listWidth) / 2 + 1;
        int listY = (this.height - listHeight) / 2 + 1;

        int searchY = listY - 32;
        int searchWidth = this.searchExpanded ? 193 : 28;
        int searchX = listX + listWidth - searchWidth + 12;

        this.addRenderableWidget(new AbstractButton(searchX, searchY, searchWidth, 18, Component.empty()) {
            @Override
            public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
                boolean hoverToggle = isMouseOver(mouseX, mouseY) && (!searchExpanded || mouseX >= getX() + width - 28);
                if (searchExpanded) {
                    (hoverToggle ? QuestlogGuiSet.DEFAULT.searchTabExpandedHovered : QuestlogGuiSet.DEFAULT.searchTabExpanded).blit(ps, getX(), getY());
                } else {
                    (hoverToggle ? QuestlogGuiSet.DEFAULT.searchTabMinimizedHovered : QuestlogGuiSet.DEFAULT.searchTabMinimized).blit(ps, getX(), getY());
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
            this.searchBox.setTextColor(0x4C381B);
            this.searchBox.setResponder(query -> {
                this.searchQuery = query;
                this.refreshQuestListOnly();
            });
            this.addRenderableWidget(this.searchBox);
        } else {
            this.searchBox = null;
        }
    }

    private void buildTabs() {
        List<String> chapterKeys = new ArrayList<>(this.availableChapters.keySet());
        int listWidth = 245;
        int listHeight = 136;
        int listX = (this.width - listWidth) / 2 + 1;
        int listY = (this.height - listHeight) / 2 + 1;

        int tabY = listY + listHeight + 15;
        int arrowY = tabY + 5;

        if (this.tabOffset > 0) {
            this.addRenderableWidget(new ChapterArrowButton(listX - 12, arrowY, true, () -> {
                this.tabOffset--;
                this.refreshList();
            }, QuestlogGuiSet.DEFAULT));
        }

        for (int i = 0; i < MAX_TABS && i + this.tabOffset < chapterKeys.size(); i++) {
            String chap = chapterKeys.get(i + this.tabOffset);
            ChapterInfo info = this.availableChapters.get(chap);
            boolean isSelected = chap.equals(this.currentChapter);

            this.addRenderableWidget(new ChapterTabButton(listX + (i * 30), tabY, info.icon, isSelected, info.isPrimary, () -> {
                this.currentChapter = chap;
                this.refreshList();
            }, QuestlogGuiSet.DEFAULT));
        }

        if (this.tabOffset + MAX_TABS < chapterKeys.size()) {
            this.addRenderableWidget(new ChapterArrowButton(listX + (MAX_TABS * 30), arrowY, false, () -> {
                this.tabOffset++;
                this.refreshList();
            }, QuestlogGuiSet.DEFAULT));
        }
    }

    @Nullable
    private ScrollableComponent getList() {
        int width = 245;
        int height = 136;
        int x = (this.width - width) / 2 + 1;
        int y = (this.height - height) / 2 + 1;

        List<Quest> quests = this.manager.getAllQuests().stream()
                .filter(quest -> quest.isTriggered() && !quest.getDisplay().isHidden())
                .filter(quest -> {
                    QuestDisplayData display = quest.getDisplay();
                    return display.getChapter().equals(this.currentChapter) ||
                            (this.currentChapter.equals("main") && display.shouldShowInMain());
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
                })
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
                        0xFF4C381B,
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

        ChapterInfo(Blittable icon, boolean isPrimary) {
            this.icon = icon;
            this.isPrimary = isPrimary;
        }
    }
}