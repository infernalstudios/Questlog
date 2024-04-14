package org.infernalstudios.questlog.client.gui.components.toasts;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.infernalstudios.questlog.util.Texture;

public abstract class AbstractToast implements Toast {
  protected abstract Component getTitle();
  protected abstract Component getDescription();
  @Nullable
  protected abstract Texture getIcon();
  protected Texture getBackground() {
    return new Texture(Toast.TEXTURE, 160, 32, 0, 0, 256, 256);
  }

  public Toast.Visibility render(PoseStack ps, ToastComponent toastComponent, long time) {
    this.getBackground().blit(ps, 0, 0);
    
    Font font = toastComponent.getMinecraft().font;
    
    List<FormattedCharSequence> list = font.split(this.getDescription(), 125);
    int titleColor = 0xFFFF00;
    if (list.size() == 1) {
      font.draw(ps, this.getTitle(), 30.0F, 7.0F, titleColor | -0x1000000);
      font.draw(ps, list.get(0), 30.0F, 18.0F, -1);
    } else {
      if (time < 1500L) {
        int opacity = Mth.floor(Mth.clamp((float)(1500L - time) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 0x4000000;
        font.draw(ps, this.getTitle(), 30.0F, 11.0F, titleColor | opacity);
      } else {
        int opacity = Mth.floor(Mth.clamp((float)(time - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 0x4000000;
        int lineHeight = this.height() / 2 - list.size() * 9 / 2;
        
        for(FormattedCharSequence formattedcharsequence : list) {
          font.draw(ps, formattedcharsequence, 30.0F, (float)lineHeight, 0xFFFFFF | opacity);
          lineHeight += 9;
        }
      }
    }
    
    if (this.getIcon() != null) {
      this.getIcon().blit(ps, 8, 8);
    }

    return time >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
  }
}
