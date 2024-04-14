package org.infernalstudios.questlog.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.Texture;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class QuestList extends AbstractContainerEventHandler implements Widget, NarratableEntry {
  private static final Texture BACKGROUND_TEXTURE = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      275, 166, 0, 0, 512, 512
  );

  private static final Texture SCROLLBAR_TEXTURE = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      8, 12, 277, 0, 512, 512
  );

  private static final Texture SCROLLBAR_BG_TEXTURE = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      4, 123, 279, 21, 512, 512
  );

  private static final int BG_OFFSET_X = -13;
  private static final int BG_OFFSET_Y = -15;
  private static final int SCROLL_WIDTH = 245;
  private static final int SCROLL_HEIGHT = 137;
  protected final Minecraft minecraft;
  protected final int itemHeight;
  private final Consumer<Quest> onSelect;
  private final List<QuestListEntry> children = new ArrayList<>();
  protected int width;
  protected int height;
  protected int left;
  protected int right;
  protected int top;
  protected int bottom;
  private double scrollAmount;
  private boolean scrolling;
  @Nullable
  private QuestListEntry selected;
  @Nullable
  private QuestListEntry hovered;

  public QuestList(Minecraft minecraft, List<Quest> quests, int centerX, int centerY, Consumer<Quest> onSelect) {
    this.minecraft = minecraft;
    this.width = SCROLL_WIDTH;
    this.height = SCROLL_HEIGHT;
    this.left = centerX - SCROLL_WIDTH / 2;
    this.right = centerX + SCROLL_WIDTH / 2;
    this.top = centerY - SCROLL_HEIGHT / 2;
    this.bottom = centerY + SCROLL_HEIGHT / 2;
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
      if (qa.isComplete() && !qb.isComplete()) {
        return 1;
      } else if (!qa.isComplete() && qb.isComplete()) {
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

  // Row helpers
  public int getRowWidth() {
    return this.width;
  }

  public int getRowLeft() {
    return this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
  }

  public int getRowRight() {
    return this.getRowLeft() + this.getRowWidth();
  }

  protected int getRowTop(int index) {
    return this.top + 4 - (int)this.getScrollAmount() + index * this.itemHeight;
  }

  private int getRowBottom(int index) {
    return this.getRowTop(index) + this.itemHeight;
  }

  protected int getScrollbarX() {
    return this.left + this.width - SCROLLBAR_TEXTURE.width();
  }

  protected int getScrollbarY() {
    return (int)this.getScrollAmount() * (this.bottom - this.top - SCROLLBAR_TEXTURE.height()) / this.getMaxScroll() + this.top;
  }

  protected boolean shouldRenderScrollBar() {
    return this.getMaxScroll() > 0;
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

  protected boolean isSelectedItem(int index) {
    return Objects.equals(this.selected, this.children.get(index));
  }

  @Nullable
  protected final QuestListEntry getEntryAtPosition(double x, double y) {
    int left = this.getRowLeft();
    int right = this.getRowRight() - (this.shouldRenderScrollBar() ? SCROLLBAR_TEXTURE.width() : 0);
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
    this.renderBackground(ps);
    this.hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

    if (this.children.isEmpty()) {
      Font font = this.minecraft.font;
      float scale = 2.0F;
      ps.pushPose();
      ps.scale(scale, scale, scale);
      String text = "No quests available";
      font.draw(ps, text, (this.left + scale + ((float) this.width / 2) - (font.width(text) * scale / 2)) / scale, (this.top + 5 + font.lineHeight * 5) / scale, 0x4C381B);
      ps.popPose();
    } else {
      this.renderList(ps, mouseX, mouseY, partialTicks);

      if (this.shouldRenderScrollBar()) {
        this.renderScrollbar(ps, mouseX, mouseY);
      }
    }
  }

  protected void renderBackground(PoseStack ps) {
    int x = this.left + BG_OFFSET_X;
    int y = this.top + BG_OFFSET_Y;

    BACKGROUND_TEXTURE.blit(ps, x, y);
  }

  protected void renderScrollbar(PoseStack ps, int mouseX, int mouseY) {
    if (!this.shouldRenderScrollBar()) {
      return;
    }

    SCROLLBAR_BG_TEXTURE.blit(ps,
      this.getScrollbarX() + (SCROLLBAR_TEXTURE.width() / 2) - SCROLLBAR_BG_TEXTURE.width() / 2,
      this.top + this.height / 2 - SCROLLBAR_BG_TEXTURE.height() / 2
    );

    int x = this.getScrollbarX();
    int y = this.getScrollbarY();
    if (y < this.top) {
      y = this.top;
    }

    if (y > this.bottom - SCROLLBAR_TEXTURE.height()) {
      y = this.bottom - SCROLLBAR_TEXTURE.height();
    }

    SCROLLBAR_TEXTURE.blit(ps, x, y);
  }

  protected void renderList(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    int rowLeft = this.getRowLeft();
    int rowWidth = this.getRowWidth();

    // Clip offscreen items (where items are rendered partially)
    GuiComponent.enableScissor(this.left, this.top - 1, this.left + this.width, this.top - 1 + this.height);

    for(int itemIndex = 0; itemIndex < this.getItemCount(); ++itemIndex) {
      int rowTop = this.getRowTop(itemIndex);
      int rowBottom = this.getRowBottom(itemIndex);
      if (rowBottom >= this.top && rowTop <= this.bottom) {
        this.renderItem(ps, mouseX, mouseY, partialTicks, itemIndex, rowLeft, rowTop, rowWidth, this.itemHeight);
      }
    }

    GuiComponent.disableScissor();
  }

  protected void renderItem(PoseStack ps, int mouseX, int mouseY, float partialTicks, int itemIndex, int rowLeft, int rowTop, int rowWidth, int itemHeightAdjusted) {
    QuestListEntry entry = this.getEntry(itemIndex);
    if (this.isSelectedItem(itemIndex)) {
      this.renderSelection(ps, rowTop, rowWidth, itemHeightAdjusted);
    }

    entry.render(ps, itemIndex, rowTop, rowLeft, rowWidth, itemHeightAdjusted, mouseX, mouseY, Objects.equals(this.hovered, entry), partialTicks);
  }

  protected void renderSelection(PoseStack ps, int rowTop, int rowWidth, int itemHeightAdjusted) {
    int i = this.left + (this.width - rowWidth) / 2;
    int j = this.left + (this.width + rowWidth) / 2;
    fill(ps, i, rowTop - 2, j, rowTop + itemHeightAdjusted + 2, 0xFF808080);
    fill(ps, i + 1, rowTop - 1, j - 1, rowTop + itemHeightAdjusted + 1, 0XFF000000);
  }

  protected void ensureVisible(QuestListEntry questEntry) {
    int rowTop = this.getRowTop(this.children.indexOf(questEntry));
    int scrollUpAmount = rowTop - this.top - 4 - this.itemHeight;
    if (scrollUpAmount < 0) {
      this.scroll(scrollUpAmount);
    }

    int scrollDownAmount = this.bottom - rowTop - this.itemHeight - this.itemHeight;
    if (scrollDownAmount < 0) {
      this.scroll(-scrollDownAmount);
    }
  }

  // Scroll handling

  private void scroll(int amount) {
    this.setScrollAmount(this.getScrollAmount() + (double)amount);
  }

  public double getScrollAmount() {
    return this.scrollAmount;
  }

  public void setScrollAmount(double amount) {
    if (amount < 0.0) {
      amount = 0.0;
    } else if (amount > this.getMaxScroll()) {
      amount = this.getMaxScroll();
    }
    this.scrollAmount = amount;
  }

  public int getMaxScroll() {
    return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
  }

  protected void updateScrollingState(double mouseX, double mouseY, int button) {
    this.scrolling = button == GLFW.GLFW_MOUSE_BUTTON_1 && (double) this.getScrollbarX() <= mouseX && mouseX < (double)(this.getScrollbarX() + SCROLLBAR_TEXTURE.width());
  }

  // Selection handling
  protected void moveSelection(int direction) {
    if (this.children().isEmpty()) {
      return;
    }

    int currentIndex = this.children().indexOf(this.selected);
    int nextIndex = Mth.clamp(currentIndex + direction, 0, this.getItemCount() - 1);
    if (currentIndex == nextIndex) {
      return;
    }

    QuestListEntry entry = this.children().get(nextIndex);
    if (entry == null) {
      return;
    }
    this.selected = entry;
    this.ensureVisible(entry);
  }

  public boolean isMouseOver(double x, double y) {
    return y >= (double)this.top && y <= (double)this.bottom && x >= (double)this.left && x <= (double)this.right;
  }

  // Event handlers
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    this.updateScrollingState(mouseX, mouseY, button);
    if (!this.isMouseOver(mouseX, mouseY)) {
      return false;
    }
    QuestListEntry e = this.getEntryAtPosition(mouseX, mouseY);
    if (e != null) {
      return e.mouseClicked(mouseX, mouseY, button);
    }

    return this.scrolling;
  }

  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    return this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button);
  }

  public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
      return true;
    }

    if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.scrolling) {
      this.setScrollAmount(this.getMaxScroll() * (mouseY - (double)this.top - (double)(SCROLLBAR_TEXTURE.height() / 2)) / (double)(this.bottom - this.top - SCROLLBAR_TEXTURE.height()));
      return true;
    }

    return false;
  }

  public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
    this.setScrollAmount(this.getScrollAmount() - amount * (double)this.itemHeight / 2.0);
    return true;
  }

  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (super.keyPressed(keyCode, scanCode, modifiers)) {
      return true;
    } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
      this.moveSelection(1);
      return true;
    } else if (keyCode == GLFW.GLFW_KEY_UP) {
      this.moveSelection(-1);
      return true;
    } else {
      return false;
    }
  }

  // Narration
  @Override
  public NarratableEntry.NarrationPriority narrationPriority() {
    return this.hovered != null ? NarrationPriority.HOVERED : NarrationPriority.NONE;
  }

  @Override
  public void updateNarration(NarrationElementOutput narrationElementOutput) {
    // TODO
  }

  protected void narrateListElementPosition(NarrationElementOutput output, QuestListEntry entry) {
    List<QuestListEntry> list = this.children;
    if (list.size() > 1) {
      int i = list.indexOf(entry);
      if (i != -1) {
        output.add(NarratedElementType.POSITION, Component.translatable("narrator.position.list", i + 1, list.size()));
      }
    }
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
      if (this.quest.getDisplay().getIcon() != null) {
        this.quest.getDisplay().getIcon().blit(ps, xPosition + 5, yPosition + 5);
      }
      font.draw(ps, this.quest.getDisplay().getTitle(), xPosition + 30, yPosition + 5, 0x4C381B);
      if (this.quest.isComplete()) {
        font.draw(ps, "Completed!", xPosition + 30, yPosition + 15, 0X529E52);
      } else {
//        font.draw(ps, this.quest.getProgress(), xPosition + 30, yPosition + 15, 0x9E7852);
        // TODO
      }

      if (isHovered) {
        fill(ps, xPosition + 3, yPosition + 1, xPosition + width - (this.list.shouldRenderScrollBar() ? SCROLLBAR_TEXTURE.width() * 2 : 0), yPosition + height, 0x80FFFFFF);
      }

      if (this.hasNext()) {
        // Draw line
        GuiComponent.fill(ps, xPosition + 5, yPosition + height, xPosition + width - (this.list.shouldRenderScrollBar() ? SCROLLBAR_TEXTURE.width() * 2 : 0) - 4, yPosition + height + 1, 0xFF000000);
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