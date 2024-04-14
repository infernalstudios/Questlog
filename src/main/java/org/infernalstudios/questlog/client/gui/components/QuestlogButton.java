package org.infernalstudios.questlog.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.Callable;
import org.infernalstudios.questlog.util.Texture;

public class QuestlogButton extends AbstractButton {
  private static final Texture BACKGROUND_TEXTURE = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      54, 18, 287, 0, 512, 512
  );
  private static final Texture BACKGROUND_TEXTURE_HOVERED = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      54, 18, 343, 0, 512, 512
  );

  private final Callable onPress;

  public QuestlogButton(int x, int y, Component message, Callable onPress) {
    super(x, y, BACKGROUND_TEXTURE.width(), BACKGROUND_TEXTURE.height(), message);
    this.onPress = onPress;
  }

  @Override
  protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
    if (this.isHovered) {
      BACKGROUND_TEXTURE_HOVERED.blit(poseStack, this.x, this.y);
    } else {
      BACKGROUND_TEXTURE.blit(poseStack, this.x, this.y);
    }
  }

  @Override
  public void renderButton(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    this.renderBg(ps, minecraft, mouseX, mouseY);
    int color = this.getFGColor();
    drawCenteredString(ps, minecraft.font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, color | Mth.ceil(this.alpha * 255.0F) << 24);
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
