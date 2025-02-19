package org.infernalstudios.questlog.client.gui.components.toasts;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.infernalstudios.questlog.util.texture.Texture;

public abstract class AbstractToast implements Toast {

  private boolean playedSound = false;

  protected abstract Component getTitle();

  protected abstract Component getDescription();

  @Nullable
  protected abstract Blittable getIcon();

  @Nullable
  protected SoundInstance getSound() {
    return null;
  }

  protected Texture getBackground() {
    return new Texture(Toast.TEXTURE, 160, 32, 0, 0, 256, 256);
  }

  protected int titleColor() {
    return 0xFFFF00FF;
  }

  protected int descriptionColor() {
    return 0xFFFFFFFF;
  }

  public Toast.Visibility render(GuiGraphics ps, ToastComponent toastComponent, long time) {
    this.getBackground().blit(ps, -13, -9); // blit at offset (there is extra space in the texture)

    Font font = toastComponent.getMinecraft().font;

    List<FormattedCharSequence> list = font.split(this.getDescription(), 125);
    int titleColor = this.titleColor();
    int descriptionColor = this.descriptionColor();
    if (list.size() == 1) {
      ps.drawString(font, this.getTitle(), 30, 7, titleColor | 0xFF000000, false);
      ps.drawString(font, list.get(0), 30, 18, descriptionColor | 0xFF000000, false);
    } else {
      if (time < 1500L) {
        int opacity = (Mth.floor(Mth.clamp((float) (1500L - time) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24) | 0x4000000;
        ps.drawString(font, this.getTitle(), 30, 11, titleColor | opacity, false);
      } else {
        int opacity = (Mth.floor(Mth.clamp((float) (time - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24) | 0x4000000;
        int lineHeight = this.height() / 2 - (list.size() * 9) / 2;

        for (FormattedCharSequence formattedcharsequence : list) {
          ps.drawString(font, formattedcharsequence, 30, lineHeight, descriptionColor | opacity, false);
          lineHeight += 9;
        }
      }
    }

    if (this.getIcon() != null) {
      this.getIcon().blit(ps, 8, 8);
    }

    if (!this.playedSound && time > 0L) {
      this.playedSound = true;

      SoundInstance sound = this.getSound();
      if (sound != null) {
        toastComponent.getMinecraft().getSoundManager().play(sound);
      }
    }

    return time >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
  }
}
