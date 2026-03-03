package org.infernalstudios.questlog.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.client.gui.components.QuestlogButton;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent.Scrollable;
import org.infernalstudios.questlog.client.gui.components.scrollable.ScrollableText;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.ObjectiveDisplayData;
import org.infernalstudios.questlog.core.quests.display.Palette;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.core.quests.display.RewardDisplayData;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.network.packet.QuestReadPacket;
import org.infernalstudios.questlog.network.packet.QuestRewardCollectPacket;
import org.infernalstudios.questlog.platform.Services;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class QuestDetails extends Screen implements NarrationSupplier {

    private static final int LEFT_PANEL_WIDTH = 275;
    private static final int RIGHT_PANEL_WIDTH = 170;
    private static final int PANEL_HEIGHT = 166;
    private static final int PANEL_SPACING = 6;

    private static final int TITLE_X = 71;
    private static final int TITLE_Y = 13;
    private static final int TITLE_WIDTH = 132;
    private static final int TITLE_HEIGHT = 16;

    private static final int CONTENT_X = 18;
    private static final int CONTENT_Y = 36;
    private static final int LEFT_CONTENT_WIDTH = 237;
    private static final int RIGHT_CONTENT_WIDTH = 134;
    private static final int CONTENT_HEIGHT = 98;
    private static boolean showDetails = true;
    private final Quest quest;
    @Nullable
    private final Screen previousScreen;
    private int panel1X;
    private int panel2X;
    private int startY;
    @Nullable
    private QuestlogButton backButton;

    @Nullable
    private QuestlogButton objectivesButton;

    @Nullable
    private ScrollableComponent description;

    @Nullable
    private ScrollableComponent info;

    public QuestDetails(@Nullable Screen previousScreen, Quest quest) {
        super(quest.getDisplay().getTitle());
        this.previousScreen = previousScreen;
        this.quest = quest;
    }

    private QuestDisplayData getDisplay() {
        return this.quest.getDisplay();
    }

    private Palette getPalette() {
        return this.getDisplay().getPalette();
    }

    private QuestlogGuiSet getGuiSet() {
        return this.getDisplay().getGuiSet();
    }

    @Override
    protected void init() {
        super.init();

        int totalWidth = showDetails ? (LEFT_PANEL_WIDTH + RIGHT_PANEL_WIDTH + PANEL_SPACING) : LEFT_PANEL_WIDTH;
        this.panel1X = (this.width - totalWidth) / 2;
        this.panel2X = this.panel1X + LEFT_PANEL_WIDTH + PANEL_SPACING;
        this.startY = (this.height - PANEL_HEIGHT) / 2;
        int buttonY = this.startY + PANEL_HEIGHT + 12;

        int btn2X = this.panel1X + LEFT_PANEL_WIDTH - this.getGuiSet().button.width() + 25;
        int btn1X = btn2X - this.getGuiSet().button.width() + 10;

        if (this.objectivesButton != null) this.removeWidget(this.objectivesButton);
        this.objectivesButton = new QuestlogButton(
                btn1X,
                buttonY,
                this.getPalette().textColor(),
                this.getPalette().hoveredTextColor(),
                Component.translatable("questlog.info.details"),
                () -> {
                    showDetails = !showDetails;
                    this.clearWidgets();
                    this.init();
                },
                this.getGuiSet()
        );
        this.addRenderableWidget(this.objectivesButton);

        if (this.backButton != null) this.removeWidget(this.backButton);
        this.backButton = new QuestlogButton(
                btn2X,
                buttonY,
                this.getPalette().textColor(),
                this.getPalette().hoveredTextColor(),
                this.getDisplay().getBackButtonText(),
                () -> {
                    boolean needsRead = !this.quest.isCompleted() && this.quest.objectives.stream()
                            .anyMatch(obj -> !obj.isCompleted() && obj.getClass().getSimpleName().equals("ReadObjective"));

                    if (this.quest.isCompleted() && !this.quest.isRewarded()) {
                        for (int i = 0; i < this.quest.rewards.size(); i++) {
                            Reward reward = this.quest.rewards.get(i);
                            if (!reward.hasRewarded()) {
                                Services.PLATFORM.sendPacketToServer(new QuestRewardCollectPacket(this.quest.getId(), i));
                                SoundEvent sound = reward.getDisplay() != null ? reward.getDisplay().getClaimSound() : null;
                                if (sound != null) {
                                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1, 1));
                                }
                            }
                        }
                    } else if (needsRead) {
                        Services.PLATFORM.sendPacketToServer(new QuestReadPacket(this.quest.getId()));
                    } else if (this.minecraft != null) {
                        this.minecraft.setScreen(this.previousScreen);
                    }
                },
                this.getGuiSet()
        );
        this.addRenderableWidget(this.backButton);

        if (this.description != null) this.removeWidget(this.description);
        this.description = new ScrollableComponent(
                this.panel1X + CONTENT_X,
                this.startY + CONTENT_Y,
                LEFT_CONTENT_WIDTH,
                CONTENT_HEIGHT,
                new ScrollableText(this.minecraft.font, this.getDisplay().getDescription(), this.getPalette().textColor())
        );
        this.addWidget(this.description);

        if (this.info != null) this.removeWidget(this.info);
        if (showDetails) {
            this.info = new ScrollableComponent(
                    this.panel2X + CONTENT_X,
                    this.startY + CONTENT_Y,
                    RIGHT_CONTENT_WIDTH,
                    CONTENT_HEIGHT,
                    new InfoScrollable(this.getDisplay())
            );
            this.addWidget(this.info);
        } else {
            this.info = null;
        }
    }

    private void drawHorizontalLine(GuiGraphics ps, int x, int y, boolean small) {
        (small ? this.getGuiSet().smallHR : this.getGuiSet().bigHR).blit(ps, x - (small ? 60 : 10), y - 4);
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ps, mouseX, mouseY, partialTicks);
        long window = this.minecraft.getWindow().getWindow();
        boolean isHoveringLink = false;

        if (this.backButton != null) {
            boolean needsRead = !this.quest.isCompleted() && this.quest.objectives.stream()
                    .anyMatch(obj -> !obj.isCompleted() && obj.getClass().getSimpleName().equals("ReadObjective"));

            if (this.quest.isCompleted() && !this.quest.isRewarded()) {
                this.backButton.setMessage(this.getDisplay().getCollectButtonText());
            } else if (needsRead) {
                this.backButton.setMessage(Component.translatable("questlog.button.read"));
            } else {
                this.backButton.setMessage(this.getDisplay().getBackButtonText());
            }
        }

        super.render(ps, mouseX, mouseY, partialTicks);
        this.renderTitle(ps);
        this.renderDescription(ps);

        if (showDetails) {
            this.renderInfo(ps);
        }

        if (this.description != null && this.description.isMouseOver(mouseX, mouseY)) {
            ScrollableText scrollableText = (ScrollableText) this.description.scrollable;
            double relX = mouseX - this.description.getXOffset();
            double relY = mouseY - this.description.getYOffset();

            Style style = scrollableText.getStyleAt(relX, relY);

            if (style != null) {
                if (style.getClickEvent() != null) {
                    isHoveringLink = true;
                    GLFW.glfwSetCursor(window, GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR));
                }

                if (style.getHoverEvent() != null) {
                    HoverEvent hover = style.getHoverEvent();
                    if (hover.getAction() == HoverEvent.Action.SHOW_TEXT) {
                        Component hoverComponent = (Component) hover.getValue(hover.getAction());
                        if (hoverComponent != null) {
                            String hoverText = hoverComponent.getString();
                            if (hoverText.startsWith("image:")) {
                                String[] parts = hoverText.split(":");
                                if (parts.length == 5) {
                                    ResourceLocation imgLoc = ResourceLocation.fromNamespaceAndPath(parts[1], parts[2]);
                                    int imgWidth = Integer.parseInt(parts[3]);
                                    int imgHeight = Integer.parseInt(parts[4]);

                                    ps.fill(mouseX + 8, mouseY - 8, mouseX + 8 + imgWidth + 4, mouseY - 8 + imgHeight + 4, 0xDD000000);
                                    ps.blit(imgLoc, mouseX + 10, mouseY - 6, 0, 0, imgWidth, imgHeight, imgWidth, imgHeight);
                                }
                            } else {
                                ps.renderComponentHoverEffect(this.font, style, mouseX, mouseY);
                            }
                        }
                    }
                }
            }
        }

        if (!isHoveringLink) {
            GLFW.glfwSetCursor(window, 0L);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.description != null && this.description.isMouseOver(mouseX, mouseY)) {
            ScrollableText scrollableText = (ScrollableText) this.description.scrollable;
            double relX = mouseX - this.description.getXOffset();
            double relY = mouseY - this.description.getYOffset();

            Style style = scrollableText.getStyleAt(relX, relY);
            if (style != null && style.getClickEvent() != null) {
                ClickEvent click = style.getClickEvent();

                if (click.getAction() == ClickEvent.Action.CHANGE_PAGE) {
                    ResourceLocation targetQuestId = ResourceLocation.parse(click.getValue());
                    Quest targetQuest = QuestlogClient.getLocal().getQuest(targetQuestId);

                    if (targetQuest != null) {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new QuestDetails(this, targetQuest));
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(ps, mouseX, mouseY, partialTick);

        this.getGuiSet().detailBackgroundLeft.blit(ps, this.panel1X, this.startY);

        if (showDetails) {
            this.getGuiSet().detailBackgroundRight.blit(ps, this.panel2X, this.startY);
        }
    }

    private void renderTitle(GuiGraphics ps) {
        QuestDisplayData displayData = this.getDisplay();

        float titleWidth = this.font.width(displayData.getTitle()) + (displayData.getIcon() != null ? displayData.getIcon().width() + 4 : 0);
        float x = this.panel1X + TITLE_X + (TITLE_WIDTH - titleWidth) / 2;
        float y = this.startY + TITLE_Y;
        if (displayData.getIcon() != null) {
            displayData.getIcon().blit(ps, (int) x, this.startY + TITLE_Y);
            x += displayData.getIcon().width() + 4;
        }

        y += (float) (TITLE_HEIGHT - this.font.lineHeight + 2) / 2;
        ps.drawString(font, displayData.getTitle(), (int) x, (int) y, this.getPalette().titleColor(), false);

        this.drawHorizontalLine(ps, this.panel1X + TITLE_X, this.startY + TITLE_Y + TITLE_HEIGHT + 2, true);
    }

    private void renderInfo(GuiGraphics ps) {
        if (this.info == null) return;

        Component titleText = this.quest.isCompleted() ? Component.translatable("questlog.info.rewards") : Component.translatable("questlog.info.objectives");
        float titleWidth = this.font.width(titleText);

        float x = this.panel2X + (RIGHT_PANEL_WIDTH - titleWidth) / 2;
        float y = this.startY + TITLE_Y + (float) (TITLE_HEIGHT - this.font.lineHeight + 2) / 2;

        ps.drawString(font, titleText, (int) x, (int) y, this.getPalette().titleColor(), false);

        this.getGuiSet().panelHR.blit(ps, this.panel2X + (RIGHT_PANEL_WIDTH - 140) / 2, this.startY + TITLE_Y + TITLE_HEIGHT + 2);

        this.info.render(ps, 0, 0, 0);
    }

    private void renderDescription(GuiGraphics ps) {
        if (this.description == null) throw new IllegalStateException("Description is null");
        this.description.render(ps, 0, 0, 0);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
        // TODO
    }

    private class InfoScrollable implements Scrollable {

        private final QuestDisplayData display;
        private List<InfoEntry> rewards;
        private List<InfoEntry> objectives;
        @Nullable
        private ScrollableComponent parent = null;

        public InfoScrollable(QuestDisplayData display) {
            this.display = display;
        }

        private List<InfoEntry> getInfoEntries() {
            if (QuestDetails.this.quest.isCompleted()) {
                if (this.rewards == null) {
                    this.rewards = new ArrayList<>();
                    List<RewardDisplayData> rewardDisplayData = QuestDetails.this.getDisplay().getRewardDisplayData();
                    for (int i = 0; i < rewardDisplayData.size(); i++) {
                        RewardDisplayData reward = rewardDisplayData.get(i);
                        this.rewards.add(new InfoEntry(reward, 0, i * InfoEntry.INFO_ENTRY_HEIGHT, display));
                    }
                }
                return this.rewards;
            } else {
                if (this.objectives == null) {
                    this.objectives = new ArrayList<>();
                    List<ObjectiveDisplayData> objectiveDisplayData = QuestDetails.this.getDisplay().getObjectiveDisplayData();
                    for (int i = 0; i < objectiveDisplayData.size(); i++) {
                        ObjectiveDisplayData objective = objectiveDisplayData.get(i);
                        this.objectives.add(new InfoEntry(objective, 0, i * InfoEntry.INFO_ENTRY_HEIGHT));
                    }
                }
                return this.objectives;
            }
        }

        @Override
        public int getHeight() {
            return this.getInfoEntries().size() * InfoEntry.INFO_ENTRY_HEIGHT;
        }

        @Override
        public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
            List<InfoEntry> infoEntries = this.getInfoEntries();
            for (int i = 0; i < infoEntries.size(); i++) {
                InfoEntry entry = infoEntries.get(i);

                entry.x = this.parent != null ? (int) this.parent.getXOffset() : 0;
                entry.y = this.parent != null ? (int) this.parent.getYOffset() + InfoEntry.INFO_ENTRY_HEIGHT * i : 0;

                entry.render(ps, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public void setScrollableComponent(ScrollableComponent parent) {
            this.parent = parent;
        }
    }

    private class InfoEntry implements Renderable, GuiEventListener {

        private static final int INFO_ENTRY_HEIGHT = 28;

        private final RewardDisplayData rewardDisplayData;
        private final ObjectiveDisplayData objectiveDisplayData;

        protected int x;
        protected int y;

        @Nullable
        private QuestDisplayData display;

        public InfoEntry(@Nullable RewardDisplayData rewardDisplayData, int x, int y, @Nullable QuestDisplayData display) {
            this.rewardDisplayData = rewardDisplayData;
            this.objectiveDisplayData = null;
            this.x = x;
            this.y = y;
            this.display = display;
        }

        public InfoEntry(@Nullable ObjectiveDisplayData objectiveDisplayData, int x, int y) {
            this.rewardDisplayData = null;
            this.objectiveDisplayData = objectiveDisplayData;
            this.x = x;
            this.y = y;
        }

        private boolean isReward() {
            return this.rewardDisplayData != null && this.objectiveDisplayData == null;
        }

        private boolean isObjective() {
            return this.objectiveDisplayData != null && this.rewardDisplayData == null;
        }

        @Override
        public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
            if (this.isReward()) {
                this.renderReward(ps);
            } else if (this.isObjective()) {
                this.renderObjective(ps);
            } else {
                throw new IllegalStateException("Unknown entry type");
            }
        }

        private boolean drawIcon(GuiGraphics ps, @Nullable Blittable icon) {
            if (icon != null) {
                icon.blit(ps, this.x, this.y + 4);
                return true;
            }
            return false;
        }

        private void drawName(GuiGraphics ps, Component name, boolean hasIcon) {
            Font font = Minecraft.getInstance().font;
            int drawX = this.x + (hasIcon ? 20 : 0);
            int drawY = this.y + 2;
            ps.drawString(font, name, drawX, drawY, QuestDetails.this.getPalette().textColor(), false);
        }

        private void drawProgress(GuiGraphics ps, boolean hasIcon) {
            Font font = Minecraft.getInstance().font;
            int drawX = this.x + (hasIcon ? 20 : 0);
            int drawY = this.y + 2 + font.lineHeight + 2;

            if (!this.isObjective()) throw new IllegalCallerException("Progress can only be drawn for objectives");

            Component progress = this.objectiveDisplayData.getProgress();
            ps.drawString(
                    font,
                    progress,
                    drawX,
                    drawY,
                    this.objectiveDisplayData.isCompleted()
                            ? QuestDetails.this.getPalette().completedTextColor()
                            : QuestDetails.this.getPalette().progressTextColor(),
                    false
            );
        }

        private void drawCollected(GuiGraphics ps, boolean hasIcon) {
            Font font = Minecraft.getInstance().font;
            int drawX = this.x + (hasIcon ? 20 : 0);
            int drawY = this.y + 2 + font.lineHeight + 2;

            if (!this.isReward()) throw new IllegalCallerException("Collected can only be drawn for rewards");

            Component collected = this.rewardDisplayData.hasRewarded()
                    ? Component.translatable("questlog.reward.collected")
                    : Component.translatable("questlog.reward.uncollected");

            if (this.display != null) {
                collected = this.rewardDisplayData.hasRewarded()
                        ? display.getCollectedText()
                        : display.getUncollectedText();
            }

            ps.drawString(
                    font,
                    collected,
                    drawX,
                    drawY,
                    this.rewardDisplayData.hasRewarded()
                            ? QuestDetails.this.getPalette().completedTextColor()
                            : QuestDetails.this.getPalette().progressTextColor(),
                    false
            );
        }

        private void renderReward(GuiGraphics ps) {
            if (this.rewardDisplayData == null) throw new IllegalStateException("RewardDisplayData is null");
            boolean hasIcon = this.drawIcon(ps, this.rewardDisplayData.getIcon());
            this.drawName(ps, this.rewardDisplayData.getName(), hasIcon);
            this.drawCollected(ps, hasIcon);
        }

        private void renderObjective(GuiGraphics ps) {
            if (this.objectiveDisplayData == null) throw new IllegalStateException("ObjectiveDisplayData is null");
            boolean hasIcon = this.drawIcon(ps, this.objectiveDisplayData.getIcon());
            this.drawName(ps, this.objectiveDisplayData.getName(), hasIcon);
            this.drawProgress(ps, hasIcon);
        }

        @Override
        public boolean isFocused() {
            return false;
        }

        @Override
        public void setFocused(boolean var1) {
        }
    }
}