package org.infernalstudios.questlog.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.components.QuestDetails;
import org.infernalstudios.questlog.client.gui.components.QuestList;
import org.infernalstudios.questlog.core.QuestManager;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Objects;

public class QuestlogScreen extends Screen {
  private final Screen previousScreen;
  @Nullable
  private QuestList questList;
  private final QuestManager manager;

  public QuestlogScreen(@Nullable Screen previousScreen) {
    super(Component.translatable("screen.questlog.title"));
    this.previousScreen = previousScreen;
    this.manager = Objects.requireNonNull(QuestManager.getLocal());
  }

  @Override
  protected void init() {
    super.init();
    if (this.questList != null) {
      this.removeWidget(this.questList);
    }

    // Added - 2 to width to fix some weird rendering issue
    this.questList = new QuestList(Minecraft.getInstance(), this.manager.getAllQuests(), this.width / 2 - 2, this.height / 2, displayData -> {
      if (this.minecraft != null) {
        this.minecraft.setScreen(new QuestDetails(this, displayData));
      }
    });

    this.addWidget(this.questList);
  }

  @Override
  public void render(PoseStack ps, int mouseX, int mouseY, float delta) {
    this.renderBackground(ps);

    Font font = Minecraft.getInstance().font;
    GuiComponent.drawCenteredString(ps, font, this.title, this.width / 2, 0, 0xFFFFFF);

    if (this.questList != null) {
      this.questList.render(ps, mouseX, mouseY, delta);
    }
    super.render(ps, mouseX, mouseY, delta);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public boolean keyPressed(int key, int p_96553_, int p_96554_) {
    if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_E || key == Questlog.OPEN_SCREEN_KEY.getKey().getValue()) { // Escape or E (inventory key, since people have that muscle memory)
      Minecraft.getInstance().setScreen(this.previousScreen);
      return true;
    }
    return false;
  }
}
