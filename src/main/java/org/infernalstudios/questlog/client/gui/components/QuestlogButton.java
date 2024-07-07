package org.infernalstudios.questlog.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.Callable;
import org.infernalstudios.questlog.util.texture.Texture;

public class QuestlogButton extends AbstractButton {
  public static final Texture TEXTURE = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      54, 18, 287, 0, 512, 512
  );
  public static final Texture TEXTURE_HOVERED = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      54, 18, 341, 0, 512, 512
  );

  public static final Texture TEXTURE_LONG = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      88, 18, 287, 18, 512, 512
  );
  public static final Texture TEXTURE_HOVERED_LONG = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      88, 18, 375, 18, 512, 512
  );

  private final Callable onPress;

  public QuestlogButton(int x, int y, Component message, Callable onPress) {
    super(x, y, TEXTURE.width(), TEXTURE.height(), message);
    this.onPress = onPress;
  }

  private boolean isLong() {
    return Minecraft.getInstance().font.width(this.getMessage()) > 46;
  }

  @Override
  protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
    if (this.isLong()) {
      int actualX = this.x - (34 / 2); // Hacky
      if (this.isHovered) {
        TEXTURE_HOVERED_LONG.blit(poseStack, actualX, this.y);
      } else {
        TEXTURE_LONG.blit(poseStack, actualX, this.y);
      }
    } else {
      if (this.isHovered) {
        TEXTURE_HOVERED.blit(poseStack, this.x, this.y);
      } else {
        TEXTURE.blit(poseStack, this.x, this.y);
      }
    }
  }

  @Override
  public void renderButton(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    this.renderBg(ps, minecraft, mouseX, mouseY);

    int x = this.x + (this.width - minecraft.font.width(this.getMessage())) / 2 + 1;
    int y = this.y + (this.height - 8) / 2;

    minecraft.font.draw(ps, this.getMessage(), x, y, this.isHovered ? 0xFFFFFF : 0x4C381B);
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
