package org.infernalstudios.questlog.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent.Scrollable;
import org.infernalstudios.questlog.core.quests.Quest;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class QuestList extends AbstractContainerEventHandler implements Scrollable {
  protected final Minecraft minecraft;
  protected final int itemHeight;
  private final Consumer<Quest> onSelect;
  private final List<QuestListEntry> children = new ArrayList<>();
  @Nullable
  private QuestListEntry hovered;
  @Nullable
  private ScrollableComponent scroller;

  public QuestList(Minecraft minecraft, List<Quest> quests, Consumer<Quest> onSelect) {
    this.minecraft = minecraft;
    this.itemHeight = 28;
    this.onSelect = onSelect;

    for (Quest quest : quests) {
      if (quest.isTriggered()) {
        this.children.add(new QuestListEntry(this, quest));
      }
    }

    // Uncompleted first, then uncollected rewards first
    this.children.sort((a, b) -> {
      Quest qa = a.quest;
      Quest qb = b.quest;
      if (qa.isCompleted() && !qb.isCompleted()) {
        return 1;
      } else if (!qa.isCompleted() && qb.isCompleted()) {
        return -1;
      } else if (!qa.isRewarded() && qb.isRewarded()) {
        return -1;
      } else if (qa.isRewarded() && !qb.isRewarded()) {
        return 1;
      } else {
        return 0;
      }
    });
  }

  public int getWidth() {
    return this.scroller != null ? this.scroller.width : 0;
  }

  @Override
  public int getHeight() {
    return this.children.size() * itemHeight;
  }

  @Override
  public void setScrollableComponent(ScrollableComponent component) {
    this.scroller = component;
  }

  private boolean isRenderingScrollbar() {
    return this.scroller != null && this.scroller.canScroll();
  }

  // Row helpers
  public int getRowWidth() {
    return this.getWidth();
  }

  public int getRowLeft() {
    return this.getWidth() / 2 - this.getRowWidth() / 2 + 2;
  }

  public int getRowRight() {
    return this.getRowLeft() + this.getRowWidth();
  }

  protected int getRowTop(int index) {
    return index * this.itemHeight;
  }

  private int getRowBottom(int index) {
    return this.getRowTop(index) + this.itemHeight;
  }

  @Nullable
  public QuestListEntry getFocused() {
    return (QuestListEntry) super.getFocused();
  }

  protected QuestListEntry getEntry(int index) {
    return this.children.get(index);
  }

  protected int getItemCount() {
    return this.children.size();
  }

  @Nullable
  protected final QuestListEntry getEntryAtPosition(double x, double y) {
    int left = this.getRowLeft();
    int right = this.getRowRight() - (this.isRenderingScrollbar() ? this.scroller.getScrollbarWidth() : 0);
    if (right < x || x < left) {
      return null;
    }

    for (int i = 0; i < this.children.size(); i++) {
      int top = this.getRowTop(i);
      int bottom = this.getRowBottom(i);
      if (y >= (double)top && y <= (double)bottom) {
        return this.children.get(i);
      }
    }

    return null;
  }

  protected int getMaxPosition() {
    return this.getItemCount() * this.itemHeight;
  }

  // Renderers

  public void render(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    this.hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

    if (this.children.isEmpty()) {
      Font font = this.minecraft.font;
      float scale = 2.0F;
      ps.pushPose();
      ps.scale(scale, scale, scale);
      String text = "No quests available";
      font.draw(ps, text, (float) this.scroller.getXOffset() + (int) (scale + ((float) this.getWidth() / 2) - (font.width(text) * scale / 2)) / scale, (float) this.scroller.getYOffset() + (int) (5 + font.lineHeight * 5) / scale, 0x4C381B);
      ps.popPose();
    } else {
      this.renderList(ps, mouseX, mouseY, partialTicks);
    }
  }

  protected void renderList(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    int rowLeft = this.getRowLeft();
    int rowWidth = this.getRowWidth();

    for (int itemIndex = 0; itemIndex < this.getItemCount(); ++itemIndex) {
      this.renderItem(ps, mouseX, mouseY, partialTicks, itemIndex, rowLeft, this.getRowTop(itemIndex), rowWidth, this.itemHeight);
    }
  }

  protected void renderItem(PoseStack ps, int mouseX, int mouseY, float partialTicks, int itemIndex, int rowLeft, int rowTop, int rowWidth, int itemHeightAdjusted) {
    QuestListEntry entry = this.getEntry(itemIndex);
    entry.render(ps, itemIndex, rowTop, rowLeft, rowWidth, itemHeightAdjusted, mouseX, mouseY, Objects.equals(this.hovered, entry), partialTicks);
  }

  public boolean isMouseOver(double x, double y) {
    return y >= 0 && y <= (double) this.getHeight() && x >= 0 && x <= (double) this.getWidth();
  }

  // Event handlers
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!this.isMouseOver(mouseX, mouseY)) {
      return false;
    }
    QuestListEntry e = this.getEntryAtPosition(mouseX, mouseY);
    if (e != null) {
      return e.mouseClicked(mouseX, mouseY, button);
    }

    return false;
  }

  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    return this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public List<QuestListEntry> children() {
    return this.children;
  }

  @OnlyIn(Dist.CLIENT)
  public static class QuestListEntry implements GuiEventListener {
    private final QuestList list;
    private final Quest quest;

    protected QuestListEntry(QuestList list, Quest quest) {
      this.list = list;
      this.quest = quest;
    }

    public void render(PoseStack ps, int color, int yPosition, int xPosition, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
      Font font = Minecraft.getInstance().font;
      int dx = 5;
      if (this.quest.getDisplay().getIcon() != null) {
        this.quest.getDisplay().getIcon().blit(ps, xPosition + dx + (int) this.list.scroller.getXOffset(), yPosition + 5 + (int) this.list.scroller.getYOffset());
        dx += 20;
      }
      if (this.quest.isCompleted()) {
        int linesHeight = font.lineHeight * 2;
        int dy = (height - linesHeight) / 2;

        font.draw(ps, this.quest.getDisplay().getTitle(), xPosition + (int) this.list.scroller.getXOffset() + dx, yPosition + dy + (int) this.list.scroller.getYOffset() , 0x4C381B);
        if (!this.quest.isRewarded()) {
          font.draw(ps, Component.translatable("questlog.quest.uncollected"), xPosition + (int) this.list.scroller.getXOffset() + dx, yPosition + (int) this.list.scroller.getYOffset() + dy + font.lineHeight, 0x9e6632);
        } else {
          font.draw(ps, Component.translatable("questlog.quest.completed"), xPosition + (int) this.list.scroller.getXOffset() + dx, yPosition + (int) this.list.scroller.getYOffset() + dy + font.lineHeight, 0x529E52);
        }
      } else {
        int y = yPosition + (int) this.list.scroller.getYOffset() + (height - font.lineHeight) / 2;
        font.draw(ps, this.quest.getDisplay().getTitle(), xPosition + (int) this.list.scroller.getXOffset() + dx, y, 0x4C381B);
      }

      if (this.hasNext()) {
        // A lot of effort went into deriving this
        QuestlogGuiSet.DEFAULT.bigHR.blit(ps, (int) this.list.scroller.getXOffset() - 2, yPosition + (int) this.list.scroller.getYOffset() + height - 5);
      }

      if (isHovered) {
        fill(ps, xPosition + (int) this.list.scroller.getXOffset() - 2, yPosition + (int) this.list.scroller.getYOffset(), xPosition + (int) this.list.scroller.getXOffset() + width - (this.list.isRenderingScrollbar() ? this.list.scroller.getScrollbarWidth() * 2 : 0), yPosition + (int) this.list.scroller.getYOffset() + height, 0x80FFFFFF);
      }
    }

    private boolean hasNext() {
      return this.list.children.indexOf(this) < this.list.children.size() - 1;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
      return Objects.equals(this.list.getEntryAtPosition(mouseX, mouseY), this);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isMouseOver(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_1) {
        this.list.onSelect.accept(this.quest);
        return true;
      } else {
        return false;
      }
    }
  }
}