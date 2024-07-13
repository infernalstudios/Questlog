package org.infernalstudios.questlog.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.infernalstudios.questlog.util.texture.Texture;

public class QuestlogOpenButton implements Widget, GuiEventListener {
  public static final Texture TEXTURE = new Texture(
    new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
    28, 26, 0, 331, 512, 512
  );

  private final InventoryScreen parent;

  public QuestlogOpenButton(InventoryScreen parent) {
    this.parent = parent;
  }

  private int getX() {
    return this.parent.getGuiLeft() + 2;
  }

  private int getY() {
    return this.parent.getGuiTop() - TEXTURE.height();
  }

  @Override
  public void render(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    TEXTURE.blit(ps, this.getX(), this.getY());

    if (this.isMouseOver(mouseX, mouseY)) {
      this.parent.renderTooltip(ps, Component.translatable("questlog.gui.open"), mouseX, mouseY);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0 && this.isMouseOver(mouseX, mouseY)) {
      this.parent.getMinecraft().setScreen(new QuestlogScreen(this.parent.getMinecraft().screen));
      return true;
    }
    return false;
  }

  @Override
  public boolean isMouseOver(double mouseX, double mouseY) {
    return mouseX >= this.getX() && mouseX < this.getX() + TEXTURE.width() && mouseY > this.getY() && mouseY <= this.getY() + TEXTURE.height();
  }
}
