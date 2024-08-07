package org.infernalstudios.questlog.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.util.Callable;

public class QuestlogButton extends AbstractButton {
  private final QuestlogGuiSet guiSet;
  private final Callable onPress;
  private final int textColor;
  private final int textColorHovered;

  public QuestlogButton(int x, int y, int textColor, int textColorHovered, Component message, Callable onPress, QuestlogGuiSet guiSet) {
    super(x - 10, y - 10, guiSet.button.width() - 20, guiSet.button.height() - 20, message);
    this.guiSet = guiSet;
    this.onPress = onPress;
    this.textColor = textColor;
    this.textColorHovered = textColorHovered;
  }

  private boolean isLong() {
    return Minecraft.getInstance().font.width(this.getMessage()) > 46;
  }

  @Override
  protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
    int actualX = this.x - 10;
    int actualY = this.y - 10;
    if (this.isLong()) {
      actualX -= (34 / 2); // Hacky
      if (this.isMouseOver(mouseX, mouseY)) {
        this.guiSet.buttonLongHovered.blit(poseStack, actualX, actualY);
      } else {
        this.guiSet.buttonLong.blit(poseStack, actualX, actualY);
      }
    } else {
      if (this.isMouseOver(mouseX, mouseY)) {
        this.guiSet.buttonHovered.blit(poseStack, actualX, actualY);
      } else {
        this.guiSet.button.blit(poseStack, actualX, actualY);
      }
    }
  }

  @Override
  public void renderButton(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    this.renderBg(ps, minecraft, mouseX, mouseY);

    int x = this.x + (this.width - minecraft.font.width(this.getMessage())) / 2 + 1;
    int y = this.y + (this.height - 8) / 2;

    minecraft.font.draw(ps, this.getMessage(), x, y, this.isMouseOver(mouseX, mouseY) ? this.textColorHovered : this.textColor);
  }

  @Override
  public boolean isMouseOver(double x, double y) {
    return x >= this.x - 10 && x <= this.x + this.width && y >= this.y - 10 && y <= this.y + this.height;
  }

  @Override
  public void onPress() {
    this.onPress.call();
  }

  @Override
  public void updateNarration(NarrationElementOutput narrationElementOutput) {
    this.defaultButtonNarrationText(narrationElementOutput);
  }
}
