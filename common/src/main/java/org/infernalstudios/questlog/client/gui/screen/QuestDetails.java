package org.infernalstudios.questlog.client.gui.screen;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
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
import org.infernalstudios.questlog.network.packet.QuestRewardCollectPacket;
import org.infernalstudios.questlog.platform.Services;
import org.infernalstudios.questlog.util.texture.Blittable;

public class QuestDetails extends Screen implements NarrationSupplier {

  private static final int TITLE_X = 71;
  private static final int TITLE_Y = 13;
  private static final int TITLE_WIDTH = 132;
  private static final int TITLE_HEIGHT = 16;

  private static final int CONTENT_X = 20;
  private static final int CONTENT_Y = 36;
  private static final int CONTENT_WIDTH = 252;
  private static final int CONTENT_HEIGHT = 86;

  private static final int BUTTON_X = 109;
  private static final int BUTTON_Y = 134;

  private static final int DESCRIPTION_INFO_PADDING = 5;

  private final Quest quest;

  @Nullable
  private final Screen previousScreen;

  private int x;
  private int y;

  @Nullable
  private QuestlogButton backButton;

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

    if (this.minecraft == null) {
      throw new IllegalStateException("Minecraft is null, UNREACHABLE");
    }

    this.x = (this.width - this.getGuiSet().detailBackground.width()) / 2 + 375;
    this.y = (this.height - this.getGuiSet().detailBackground.height()) / 2 + 174;

    if (this.backButton != null) this.removeWidget(this.backButton);
    this.backButton = new QuestlogButton(
      this.x + BUTTON_X + 5,
      this.y + BUTTON_Y + 5,
      this.getPalette().textColor,
      this.getPalette().hoveredTextColor,
      this.getDisplay().getButtonText(),
      () -> {
        if (this.quest.isCompleted() && !this.quest.isRewarded()) {
          for (int i = 0; i < this.quest.rewards.size(); i++) {
            Reward reward = this.quest.rewards.get(i);
            if (!reward.hasRewarded()) {
              // Send a packet to the server to collect the reward.
              Services.PLATFORM.sendPacketToServer(new QuestRewardCollectPacket(this.quest.getId(), i));

              // Play sound
              SoundEvent sound = reward.getDisplay().getClaimSound();
              if (sound != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1, 1));
              }
            }
          }
        } else if (this.minecraft != null) {
          this.minecraft.setScreen(this.previousScreen);
        }
      },
      this.getGuiSet()
    );
    this.addRenderableWidget(this.backButton);

    if (this.description != null) this.removeWidget(this.description);
    this.description = new ScrollableComponent(
      this.x + CONTENT_X,
      this.y + CONTENT_Y,
      CONTENT_WIDTH,
      CONTENT_HEIGHT - this.getInfoHeight() - DESCRIPTION_INFO_PADDING,
      new ScrollableText(this.minecraft.font, this.getDisplay().getDescription(), this.getPalette().textColor)
    );
    // We render this ourselves, don't use addRenderableWidget.
    this.addWidget(this.description);

    if (this.info != null) this.removeWidget(this.info);
    this.info = new ScrollableComponent(
      this.x + CONTENT_X,
      this.y + CONTENT_Y + CONTENT_HEIGHT - this.getInfoHeight() + DESCRIPTION_INFO_PADDING,
      CONTENT_WIDTH,
      this.getInfoHeight(),
      new InfoScrollable()
    );
    // We render this ourselves, don't use addRenderableWidget.
    this.addWidget(this.info);
  }

  private void drawHorizontalLine(GuiGraphics ps, int x, int y, boolean small) {
    (small ? this.getGuiSet().smallHR : this.getGuiSet().bigHR).blit(ps, x - (small ? 60 : 10), y - 4);
  }

  @Override
  public void render(GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(ps);
    if (this.backButton != null) {
      if (this.quest.isCompleted() && !this.quest.isRewarded()) {
        this.backButton.setMessage(Component.translatable("questlog.reward.collect"));
      } else {
        this.backButton.setMessage(this.getDisplay().getButtonText());
      }
    }
    super.render(ps, mouseX, mouseY, partialTicks);
    this.renderTitle(ps);
    this.renderDescription(ps);
    this.renderInfo(ps);
  }

  @Override
  public void renderBackground(GuiGraphics ps) {
    super.renderBackground(ps);
    this.getGuiSet().detailBackground.blit(ps, this.x - 375, this.y - 174);
  }

  private void renderTitle(GuiGraphics ps) {
    QuestDisplayData displayData = this.getDisplay();

    // Icon
    float titleWidth = this.font.width(displayData.getTitle()) + (displayData.getIcon() != null ? displayData.getIcon().width() + 4 : 0);
    float x = this.x + TITLE_X + (TITLE_WIDTH - titleWidth) / 2;
    float y = this.y + TITLE_Y;
    if (displayData.getIcon() != null) {
      displayData.getIcon().blit(ps, (int) x, this.y + TITLE_Y);
      x += displayData.getIcon().width() + 4; // + padding
    }

    // Title
    y += (float) (TITLE_HEIGHT - this.font.lineHeight + 2) / 2;
    ps.drawString(font, displayData.getTitle(), (int) x, (int) y, this.getPalette().titleColor, false);

    this.drawHorizontalLine(ps, this.x + TITLE_X, this.y + TITLE_Y + TITLE_HEIGHT + 2, true);
  }

  private int getInfoHeight() {
    return (
      Math.min(
        2,
        this.quest.isCompleted() ? this.getDisplay().getRewardDisplayData().size() : this.getDisplay().getObjectiveDisplayData().size()
      ) *
      InfoEntry.INFO_ENTRY_HEIGHT
    );
  }

  private void renderInfo(GuiGraphics ps) {
    if (this.info == null) throw new IllegalStateException("Info is null");
    this.info.render(ps, 0, 0, 0);
    if (this.info.height != 0) {
      this.drawHorizontalLine(ps, this.x + CONTENT_X, this.y + CONTENT_Y + CONTENT_HEIGHT - this.getInfoHeight(), false);
    }
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
  public void updateNarration(NarrationElementOutput output) {
    // TODO
  }

  private class InfoScrollable implements Scrollable {

    private List<InfoEntry> rewards;
    private List<InfoEntry> objectives;

    @Nullable
    private ScrollableComponent parent = null;

    private List<InfoEntry> getInfoEntries() {
      if (QuestDetails.this.quest.isCompleted()) {
        if (this.rewards == null) {
          this.rewards = new ArrayList<>();
          List<RewardDisplayData> rewardDisplayData = QuestDetails.this.getDisplay().getRewardDisplayData();
          for (int i = 0; i < rewardDisplayData.size(); i++) {
            RewardDisplayData reward = rewardDisplayData.get(i);
            this.rewards.add(new InfoEntry(reward, 0, i * InfoEntry.INFO_ENTRY_HEIGHT));
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
    public void render(GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
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

    private static final int INFO_ENTRY_HEIGHT = 18;

    @CheckForNull
    private final RewardDisplayData rewardDisplayData;

    @CheckForNull
    private final ObjectiveDisplayData objectiveDisplayData;

    protected int x;
    protected int y;

    public InfoEntry(RewardDisplayData rewardDisplayData, int x, int y) {
      this.rewardDisplayData = rewardDisplayData;
      this.objectiveDisplayData = null;

      this.x = x;
      this.y = y;
    }

    public InfoEntry(ObjectiveDisplayData objectiveDisplayData, int x, int y) {
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
    public void render(GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
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
        icon.blit(ps, this.x, this.y);
        return true;
      }
      return false;
    }

    private int drawName(GuiGraphics ps, Component name, boolean hasIcon) {
      Font font = Minecraft.getInstance().font;
      int x = this.x + (hasIcon ? 18 : 0);
      int y = this.y + (INFO_ENTRY_HEIGHT - font.lineHeight) / 2;
      ps.drawString(font, name, x, y, QuestDetails.this.getPalette().textColor, false);

      return font.width(name) + (hasIcon ? 18 : 0);
    }

    private void drawProgress(GuiGraphics ps, int x) {
      Font font = Minecraft.getInstance().font;
      int y = this.y + (INFO_ENTRY_HEIGHT - font.lineHeight) / 2;

      if (!this.isObjective()) {
        throw new IllegalCallerException("Progress can only be drawn for objectives");
      }

      Component progress = this.objectiveDisplayData.getProgress();

      ps.drawString(font, "- ", x, y, QuestDetails.this.getPalette().progressTextColor, false);
      ps.drawString(
        font,
        progress,
        x + font.width("- "),
        y,
        this.objectiveDisplayData.isCompleted()
          ? QuestDetails.this.getPalette().completedTextColor
          : QuestDetails.this.getPalette().progressTextColor,
        false
      );
    }

    private void drawCollected(GuiGraphics ps, int x) {
      Font font = Minecraft.getInstance().font;
      int y = this.y + (INFO_ENTRY_HEIGHT - font.lineHeight) / 2;

      if (!this.isReward()) {
        throw new IllegalCallerException("Collected can only be drawn for rewards");
      }

      Component collected = this.rewardDisplayData.hasRewarded()
        ? Component.translatable("questlog.reward.collected")
        : Component.translatable("questlog.reward.uncollected");

      ps.drawString(font, "- ", x, y, QuestDetails.this.getPalette().progressTextColor, false);
      ps.drawString(
        font,
        collected,
        x + font.width("- "),
        y,
        this.rewardDisplayData.hasRewarded()
          ? QuestDetails.this.getPalette().completedTextColor
          : QuestDetails.this.getPalette().progressTextColor,
        false
      );
    }

    private void renderReward(GuiGraphics ps) {
      if (this.rewardDisplayData == null) throw new IllegalStateException("RewardDisplayData is null");

      boolean hasIcon = this.drawIcon(ps, this.rewardDisplayData.getIcon());
      int nameWidth = this.drawName(ps, this.rewardDisplayData.getName(), hasIcon);
      this.drawCollected(ps, this.x + nameWidth + 5);
    }

    private void renderObjective(GuiGraphics ps) {
      if (this.objectiveDisplayData == null) throw new IllegalStateException("ObjectiveDisplayData is null");

      boolean hasIcon = this.drawIcon(ps, this.objectiveDisplayData.getIcon());
      int nameWidth = this.drawName(ps, this.objectiveDisplayData.getName(), hasIcon);
      this.drawProgress(ps, this.x + nameWidth + 5);
    }

    @Override
    public void setFocused(boolean var1) {
    }

    @Override
    public boolean isFocused() {
      return false;
    }
  }
}
