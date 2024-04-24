package org.infernalstudios.questlog.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.components.QuestList;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.util.Texture;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Objects;

public class QuestlogScreen extends Screen {
  private static final Texture BACKGROUND_TEXTURE = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      275, 166, 0, 0, 512, 512
  );

  private final Screen previousScreen;
  @Nullable
  private ScrollableComponent questList;
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

    this.questList = this.getList();

    this.addRenderableWidget(this.questList);
  }

  private ScrollableComponent getList() {
    int width = 245;
    int height = 137;
    int x = (this.width - width) / 2;
    int y = (this.height - height) / 2;

    return new ScrollableComponent(x, y, width, height, new QuestList(Minecraft.getInstance(), this.manager.getAllQuests(), displayData -> {
      if (this.minecraft != null) {
        this.minecraft.setScreen(new QuestDetails(this, displayData));
      }
    }));
  }

  @Override
  public void renderBackground(PoseStack ps) {
    super.renderBackground(ps);

    int x = (this.width - BACKGROUND_TEXTURE.width()) / 2;
    int y = (this.height - BACKGROUND_TEXTURE.height()) / 2;

    BACKGROUND_TEXTURE.blit(ps, x, y);
  }

  @Override
  public void render(PoseStack ps, int mouseX, int mouseY, float delta) {
    this.renderBackground(ps);

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
