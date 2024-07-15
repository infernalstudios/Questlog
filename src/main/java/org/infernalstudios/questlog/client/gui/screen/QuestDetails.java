package org.infernalstudios.questlog.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.client.gui.components.QuestlogButton;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent.Scrollable;
import org.infernalstudios.questlog.client.gui.components.scrollable.ScrollableText;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.ObjectiveDisplayData;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.core.quests.display.RewardDisplayData;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.network.NetworkHandler;
import org.infernalstudios.questlog.network.packet.QuestRewardCollectPacket;
import org.infernalstudios.questlog.util.texture.Renderable;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class QuestDetails extends Screen implements NarrationSupplier {
  private static final int TITLE_X = 71;
  private static final int TITLE_Y = 13;
  private static final int TITLE_WIDTH = 132;
  private static final int TITLE_HEIGHT = 16;

  private static final int CONTENT_X = 20;
  private static final int CONTENT_Y = 36;
  private static final int CONTENT_WIDTH = 232;
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

    this.x = (this.width - this.getGuiSet().detailBackground.width()) / 2;
    this.y = (this.height - this.getGuiSet().detailBackground.height()) / 2;
  }

  private QuestDisplayData getDisplay() {
    return this.quest.getDisplay();
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

    this.x = (this.width - this.getGuiSet().detailBackground.width()) / 2;
    this.y = (this.height - this.getGuiSet().detailBackground.height()) / 2;

    if (this.backButton != null) this.removeWidget(this.backButton);
    this.backButton = new QuestlogButton(this.x + BUTTON_X, this.y + BUTTON_Y, Component.translatable("gui.back"), () -> {
      if (this.quest.isCompleted() && !this.quest.isRewarded()) {
        for (int i = 0; i < this.quest.rewards.size(); i++) {
          Reward reward = this.quest.rewards.get(i);
          if (!reward.hasRewarded()) {
            // Send a packet to the server to collect the reward.
            NetworkHandler.sendToServer(new QuestRewardCollectPacket(this.quest.getId(), i));

            // Play sound
            SoundInstance sound = reward.getDisplay().getClaimSound();
            if (sound != null) {
              this.minecraft.getSoundManager().play(sound);
            }
          }
        }
      } else if (this.minecraft != null) {
        this.minecraft.setScreen(this.previousScreen);
      }
    }, this.getGuiSet());
    this.addRenderableWidget(this.backButton);

    if (this.description != null) this.removeWidget(this.description);
    this.description = new ScrollableComponent(
      this.x + CONTENT_X,
      this.y + CONTENT_Y,
        CONTENT_WIDTH,
        CONTENT_HEIGHT - this.getInfoHeight() - DESCRIPTION_INFO_PADDING,
      new ScrollableText(this.minecraft.font, this.quest.getDisplay().getDescription())
    );
    // We render this ourselves, don't use addRenderableWidget.
    this.addWidget(this.description);

    if (this.info != null) this.removeWidget(this.info);
    this.info = new ScrollableComponent(
      this.x + CONTENT_X,
      this.y + CONTENT_Y + CONTENT_HEIGHT - this.getInfoHeight() + DESCRIPTION_INFO_PADDING,
        CONTENT_WIDTH,
      this.getInfoHeight(),
      new InfoScrollable(this.quest)
    );
    // We render this ourselves, don't use addRenderableWidget.
    this.addWidget(this.info);
  }

  private void drawHorizontalLine(PoseStack ps, int x, int y, int length, int color) {
    GuiComponent.fill(ps, x, y, x + length, y + 1, color);
  }

  @Override
  public void render(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(ps);
    if (this.backButton != null) {
      if (this.quest.isCompleted() && !this.quest.isRewarded()) {
        this.backButton.setMessage(Component.translatable("questlog.reward.collect"));
      } else {
        this.backButton.setMessage(Component.translatable("gui.back"));
      }
    }
    super.render(ps, mouseX, mouseY, partialTicks);
    this.renderTitle(ps);
    this.renderDescription(ps);
    this.renderInfo(ps);
  }

  @Override
  public void renderBackground(PoseStack ps) {
    super.renderBackground(ps);
    this.getGuiSet().detailBackground.blit(ps, this.x, this.y);
  }

  private void renderTitle(PoseStack ps) {
    QuestDisplayData displayData = this.quest.getDisplay();

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
    font.draw(ps, displayData.getTitle(), (int) x, (int) y, 0x4C381B);

    this.drawHorizontalLine(ps, this.x + TITLE_X, this.y + TITLE_Y + TITLE_HEIGHT + 2, TITLE_WIDTH, 0x987C5300);
  }

  private int getInfoHeight() {
    return Math.min(2, this.quest.isCompleted() ? this.getDisplay().getRewardDisplayData().size() : this.getDisplay().getObjectiveDisplayData().size()) * InfoEntry.INFO_ENTRY_HEIGHT;
  }

  private void renderInfo(PoseStack ps) {
    if (this.info == null) throw new IllegalStateException("Info is null");
    this.info.render(ps, 0, 0, 0);
    if (this.info.height != 0) {
      this.drawHorizontalLine(ps, this.x + CONTENT_X, this.y + CONTENT_Y + CONTENT_HEIGHT - this.getInfoHeight(), CONTENT_WIDTH, 0x9C765000);
    }
  }

  private void renderDescription(PoseStack ps) {
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

  private static class InfoScrollable implements Scrollable {
    private final Quest quest;

    private List<InfoEntry> rewards;
    private List<InfoEntry> objectives;

    @Nullable
    private ScrollableComponent parent = null;

    public InfoScrollable(Quest quest) {
      this.quest = quest;
    }

    private List<InfoEntry> getInfoEntries() {
      if (this.quest.isCompleted()) {
        if (this.rewards == null) {
          this.rewards = new ArrayList<>();
          List<RewardDisplayData> rewardDisplayData = this.quest.getDisplay().getRewardDisplayData();
          for (int i = 0; i < rewardDisplayData.size(); i++) {
            RewardDisplayData reward = rewardDisplayData.get(i);
            this.rewards.add(new InfoEntry(reward, 0, i * InfoEntry.INFO_ENTRY_HEIGHT));
          }
        }

        return this.rewards;
      } else {
        if (this.objectives == null) {
          this.objectives = new ArrayList<>();
          List<ObjectiveDisplayData> objectiveDisplayData = this.quest.getDisplay().getObjectiveDisplayData();
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
    public void render(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
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

  private static class InfoEntry implements Widget, GuiEventListener {
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
    public void render(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
      if (this.isReward()) {
        this.renderReward(ps);
      } else if (this.isObjective()) {
        this.renderObjective(ps);
      } else {
        throw new IllegalStateException("Unknown entry type");
      }
    }

    private boolean drawIcon(PoseStack ps, @Nullable Renderable icon) {
      if (icon != null) {
        icon.blit(ps, this.x, this.y);
        return true;
      }
      return false;
    }

    private int drawName(PoseStack ps, Component name, boolean hasIcon) {
      Font font = Minecraft.getInstance().font;
      int x = this.x + (hasIcon ? 18 : 0);
      int y = this.y + (INFO_ENTRY_HEIGHT - font.lineHeight) / 2;
      font.draw(ps, name, x, y, 0x4C381B);

      return font.width(name) + (hasIcon ? 18 : 0);
    }

    private void drawProgress(PoseStack ps, int x) {
      Font font = Minecraft.getInstance().font;
      int y = this.y + (INFO_ENTRY_HEIGHT - font.lineHeight) / 2;

      if (!this.isObjective()) {
        throw new IllegalCallerException("Progress can only be drawn for objectives");
      }

      Component progress = this.objectiveDisplayData.getProgress();

      font.draw(ps, "- ", x, y, 0x9E7852);
      font.draw(ps, progress, x + font.width("- "), y, this.objectiveDisplayData.isCompleted() ? 0x529E52 : 0x9E7852);
    }

    private void drawCollected(PoseStack ps, int x) {
      Font font = Minecraft.getInstance().font;
      int y = this.y + (INFO_ENTRY_HEIGHT - font.lineHeight) / 2;

      if (!this.isReward()) {
        throw new IllegalCallerException("Collected can only be drawn for rewards");
      }

      Component collected = this.rewardDisplayData.hasRewarded() ?
        Component.translatable("questlog.reward.collected") :
        Component.translatable("questlog.reward.uncollected");


      font.draw(ps, "- ", x, y, 0x9E7852);
      font.draw(ps, collected, x + font.width("- "), y, this.rewardDisplayData.hasRewarded() ? 0x529E52 : 0x9E7852);
    }

    private void renderReward(PoseStack ps) {
      if (this.rewardDisplayData == null) throw new IllegalStateException("RewardDisplayData is null");

      boolean hasIcon = this.drawIcon(ps, this.rewardDisplayData.getIcon());
      int nameWidth = this.drawName(ps, this.rewardDisplayData.getName(), hasIcon);
      this.drawCollected(ps, this.x + nameWidth + 5);
    }

    private void renderObjective(PoseStack ps) {
      if (this.objectiveDisplayData == null) throw new IllegalStateException("ObjectiveDisplayData is null");

      boolean hasIcon = this.drawIcon(ps, this.objectiveDisplayData.getIcon());
      int nameWidth = this.drawName(ps, this.objectiveDisplayData.getName(), hasIcon);
      this.drawProgress(ps, this.x + nameWidth + 5);
    }
  }
}
