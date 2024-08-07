package org.infernalstudios.questlog.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.infernalstudios.questlog.config.QuestlogConfig.Button;
import org.infernalstudios.questlog.util.texture.Texture;

public class QuestlogOpenButton implements Widget, GuiEventListener {
  public static final Texture TEXTURE = new Texture(
    new ResourceLocation(Questlog.MODID, "textures/gui/questlog_button.png"),
    64, 64, 96, 96, 256, 256
  );

  private final InventoryScreen parent;

  public QuestlogOpenButton(InventoryScreen parent) {
    this.parent = parent;
  }

  private int getX() {
    return Button.x + (Button.relativeToInventory ? this.parent.getGuiLeft() : 0) - 18;
  }

  private int getY() {
    return Button.y + (Button.relativeToInventory ? this.parent.getGuiTop() : 0) - 18;
  }

  @Override
  public void render(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    if (!Button.enabled) return;

    TEXTURE.blit(ps, this.getX(), this.getY());

    if (this.isMouseOver(mouseX, mouseY)) {
      this.parent.renderTooltip(ps, Component.translatable("questlog.gui.open"), mouseX, mouseY);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!Button.enabled) return false;

    if (button == 0 && this.isMouseOver(mouseX, mouseY)) {
      this.parent.getMinecraft().setScreen(new QuestlogScreen(this.parent.getMinecraft().screen));
      return true;
    }
    return false;
  }

  @Override
  public boolean isMouseOver(double mouseX, double mouseY) {
    if (!Button.enabled) return false;

    return mouseX >= this.getX() + 18 && mouseX < this.getX() + TEXTURE.width() - 18 && mouseY > this.getY() + 19 && mouseY <= this.getY() + TEXTURE.height() - 20;
  }
}
