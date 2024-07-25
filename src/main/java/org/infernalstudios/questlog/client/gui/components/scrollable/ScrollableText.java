package org.infernalstudios.questlog.client.gui.components.scrollable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent.Scrollable;

import javax.annotation.Nullable;
import java.util.List;

public class ScrollableText implements Scrollable {
  private final Font font;
  private final FormattedText text;
  private final int color;

  @Nullable
  private List<FormattedCharSequence> lines;
  private ScrollableComponent scroller;

  public ScrollableText(Font font, FormattedText text, int color) {
    this.font = font;
    this.text = text;
    this.color = color;
  }

  private int getWidth() {
    return this.scroller.width - this.scroller.getScrollbarWidth();
  }

  private List<FormattedCharSequence> getLines() {
    if (this.lines == null) {
      this.lines = this.font.split(this.text, this.getWidth());
    }

    return lines;
  }

  @Override
  public void render(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    for (int i = 0; i < this.getLines().size(); i++) {
      this.font.draw(ps, this.getLines().get(i), (float) this.scroller.getXOffset(), (float) this.scroller.getYOffset() + i * this.font.lineHeight, this.color);
    }
  }

  @Override
  public int getHeight() {
    return this.getLines().size() * this.font.lineHeight;
  }

  @Override
  public void setScrollableComponent(ScrollableComponent component) {
    this.scroller = component;
  }
}
