package org.infernalstudios.questlog.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.util.ScrollbarTexture;
import org.lwjgl.glfw.GLFW;

/**
 * A scrollable GUI component.<p>
 * This class is a generic class that can work with any type that extends the Scrollable interface.<p>
 * It handles rendering and user input for scrolling and forwards the rest of the input to the Scrollable object, if possible.<p>
 *
 * The scrollable object:
 *  <li>must render its own content via the {@link Widget} interface. It is automatically offset so that it can just render using 0, 0 as the top left corner.</li>
 *  <li>may handle its own input via the {@link GuiEventListener} interface.</li>
 *  <li>must implement the {@link Scrollable} interface to provide the height of the content.</li>
 *  <li>may implement the {@link NarratableEntry} interface to provide accessibility support.</li>
 *
 * The scrollbar is rendered on the right side of the component and is draggable.<p>
 */
public class ScrollableComponent implements Widget, NarratableEntry, GuiEventListener {
  private final Scrollable scrollable;
  public final int width;
  public final int height;
  private final int left;
  private final int right;
  private final int top;
  private final int bottom;
  private double scrollAmount;
  private boolean scrolling;
  private final ScrollbarTexture scrollbar;

  public ScrollableComponent(int x, int y, int width, int height, Scrollable scrollable) {
    this(x, y, width, height, scrollable, QuestlogGuiSet.DEFAULT.scrollbar);
  }

  public ScrollableComponent(int x, int y, int width, int height, Scrollable scrollable, ScrollbarTexture scrollbar) {
    this.width = width;
    this.height = height;
    this.left = x;
    this.right = x + width;
    this.top = y;
    this.bottom = y + height;
    this.scrollable = scrollable;
    this.scrollAmount = 0.0;
    this.scrollbar = scrollbar;

    this.scrollable.setScrollableComponent(this);
  }

  public double getXOffset() {
    return this.left;
  }

  public double getYOffset() {
    return this.top - this.getScrollAmount();
  }

  protected int getScrollbarX() {
    return this.left + this.width - this.scrollbar.bar().width();
  }

  protected int getScrollbarY() {
    return (int)this.getScrollAmount() * (this.bottom - this.top - this.scrollbar.bar().height()) / this.getMaxScroll() + this.top;
  }

  public int getScrollbarWidth() {
    return this.scrollbar.bar().width();
  }

  public boolean canScroll() {
    return this.getMaxScroll() > 0;
  }

  // Renderers
  @Override
  public void render(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    this.scrollable.renderBackground(ps, mouseX - this.left, (int) (mouseY - this.top + this.getScrollAmount()), partialTicks);

    // Clip offscreen rendering
    GuiComponent.enableScissor(this.left, this.top, this.left + this.width, this.top + this.height);
    this.scrollable.render(ps, mouseX - this.left, (int) (mouseY - this.top + this.getScrollAmount()), partialTicks);
    GuiComponent.disableScissor();

    if (this.canScroll()) {
      this.renderScrollbar(ps, mouseX, mouseY);
    }
  }

  private void renderScrollbarBackground(PoseStack ps) {
    int bgHeight = this.scrollbar.backgroundTopCap().height() + this.scrollbar.backgroundBottomCap().height();

    // We do a little bit of simple math
    bgHeight += (int) (Math.floor(this.height - (double) bgHeight / this.scrollbar.background().height()) * this.scrollbar.background().height());


    int bgY = this.top;
    this.scrollbar.backgroundTopCap().blit(ps, this.getScrollbarX() + (this.scrollbar.bar().width() - this.scrollbar.backgroundTopCap().width()) / 2, bgY);
    bgY += this.scrollbar.backgroundTopCap().height();

    while (bgY < bgHeight + this.top - this.scrollbar.backgroundBottomCap().height()) {
      this.scrollbar.background().blit(ps, this.getScrollbarX() + (this.scrollbar.bar().width() - this.scrollbar.background().width()) / 2, bgY);
      bgY += this.scrollbar.background().height();
    }

    this.scrollbar.backgroundBottomCap().blit(ps, this.getScrollbarX() + (this.scrollbar.bar().width() - this.scrollbar.backgroundBottomCap().width()) / 2, bgY);
  }

  private void renderScrollbar(PoseStack ps, int mouseX, int mouseY) {
    this.renderScrollbarBackground(ps);

    int x = this.getScrollbarX();
    int y = this.getScrollbarY();
    if (y < this.top) {
      y = this.top;
    }

    if (y > this.bottom - this.scrollbar.bar().height()) {
      y = this.bottom - this.scrollbar.bar().height();
    }

    this.scrollbar.bar().blit(ps, x, y);
  }

  // Scroll handling
  private double getScrollAmount() {
    return this.scrollAmount;
  }

  private void setScrollAmount(double amount) {
    if (amount < 0.0) {
      amount = 0.0;
    } else if (amount > this.getMaxScroll()) {
      amount = this.getMaxScroll();
    }
    this.scrollAmount = amount;
  }

  private int getMaxScroll() {
    return Math.max(0, this.scrollable.getHeight() - (this.bottom - this.top));
  }

  private void updateScrollingState(double mouseX, double mouseY, int button) {
    this.scrolling = button == GLFW.GLFW_MOUSE_BUTTON_1 && (double) this.getScrollbarX() <= mouseX && mouseX < (double)(this.getScrollbarX() + this.scrollbar.bar().width());
  }

  // Event handlers
  @Override
  public boolean isMouseOver(double x, double y) {
    return y >= (double)this.top && y <= (double)this.bottom && x >= (double)this.left && x <= (double)this.right;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    this.updateScrollingState(mouseX, mouseY, button);
    if (!this.isMouseOver(mouseX, mouseY)) {
      return false;
    }

    if (this.scrolling) {
      return true;
    }

    if (this.scrollable instanceof GuiEventListener listener) {
      return listener.mouseClicked(mouseX - this.left, mouseY - this.top + this.getScrollAmount(), button);
    }

    return GuiEventListener.super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.scrolling) {
      this.setScrollAmount(this.getMaxScroll() * (mouseY - (double)this.top - (double)(this.scrollbar.bar().height() / 2)) / (double)(this.bottom - this.top - this.scrollbar.bar().height()));
      return true;
    }

    if (this.scrollable instanceof GuiEventListener listener) {
      return listener.mouseDragged(mouseX - this.left, mouseY - this.top + this.getScrollAmount(), button, deltaX, deltaY);
    }

    return GuiEventListener.super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
      this.scrolling = false;
      return true;
    }

    if (this.scrollable instanceof GuiEventListener listener) {
      return listener.mouseReleased(mouseX - this.left, mouseY - this.top + this.getScrollAmount(), button);
    }

    return GuiEventListener.super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
    if (this.canScroll()) {
      this.setScrollAmount(this.getScrollAmount() - amount * 10.0);
      return true;
    }

    if (this.scrollable instanceof GuiEventListener listener) {
      return listener.mouseScrolled(mouseX - this.left, mouseY - this.top + this.getScrollAmount(), amount);
    }

    return GuiEventListener.super.mouseScrolled(mouseX, mouseY, amount);
  }

  // Forward interfaces to scrollable
  @Override
  public void mouseMoved(double mouseX, double mouseY) {
    if (this.scrollable instanceof GuiEventListener listener) {
      listener.mouseMoved(mouseX - this.left, mouseY - this.top + this.getScrollAmount());
    }

    GuiEventListener.super.mouseMoved(mouseX, mouseY);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (this.scrollable instanceof GuiEventListener listener) {
      return listener.keyPressed(keyCode, scanCode, modifiers);
    }

    return GuiEventListener.super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
    if (this.scrollable instanceof GuiEventListener listener) {
      return listener.keyReleased(keyCode, scanCode, modifiers);
    }

    return GuiEventListener.super.keyReleased(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char character, int keyCode) {
    if (this.scrollable instanceof GuiEventListener listener) {
      return listener.charTyped(character, keyCode);
    }

    return GuiEventListener.super.charTyped(character, keyCode);
  }

  @Override
  public boolean changeFocus(boolean shouldChangeFocus) {
    if (this.scrollable instanceof GuiEventListener listener) {
      return listener.changeFocus(shouldChangeFocus);
    }

    return GuiEventListener.super.changeFocus(shouldChangeFocus);
  }

  @Override
  public NarrationPriority narrationPriority() {
    if (this.scrollable instanceof NarratableEntry entry) {
      return entry.narrationPriority();
    }
    return NarrationPriority.NONE;
  }

  @Override
  public void updateNarration(NarrationElementOutput output) {
    if (this.scrollable instanceof NarratableEntry entry) {
      entry.updateNarration(output);
    }
  }

  public interface Scrollable extends Widget {
    int getHeight();
    default void setScrollableComponent(ScrollableComponent component) {}
    default void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {}
  }
}