package org.infernalstudios.questlog.client.gui.screen;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.client.gui.components.QuestList;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.texture.Texture;
import org.lwjgl.glfw.GLFW;

public class QuestlogScreen extends Screen {

  private static final Texture BACKGROUND_TEXTURE = new Texture(
    new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
    1024,
    512,
    0,
    0,
    1024,
    512
  );

  private final Screen previousScreen;

  @Nullable
  private ScrollableComponent questList;

  private final QuestManager manager;

  public QuestlogScreen(@Nullable Screen previousScreen) {
    super(Component.empty());
    this.previousScreen = previousScreen;
    this.manager = Objects.requireNonNull(QuestlogClient.getLocal());
  }

  @Override
  protected void init() {
    super.init();
    if (this.questList != null) {
      this.removeWidget(this.questList);
    }

    this.questList = this.getList();
    if (this.questList != null) {
      this.addRenderableWidget(this.questList);
    }
  }

  @Nullable
  private ScrollableComponent getList() {
    int width = 245;
    int height = 137;
    int x = (this.width - width) / 2;
    int y = (this.height - height) / 2;
    List<Quest> quests =
      this.manager.getAllQuests().stream().filter(quest -> !quest.getDisplay().isHidden() && quest.isTriggered()).toList();

    if (quests.isEmpty()) {
      return null;
    }

    return new ScrollableComponent(
      x,
      y,
      width,
      height,
      new QuestList(Minecraft.getInstance(), quests, displayData -> {
        if (this.minecraft != null) {
          this.minecraft.setScreen(new QuestDetails(this, displayData));
        }
      })
    );
  }

  @Override
  public void renderBackground(GuiGraphics ps) {
    super.renderBackground(ps);

    int x = (this.width - BACKGROUND_TEXTURE.width()) / 2;
    int y = (this.height - BACKGROUND_TEXTURE.height()) / 2;

    BACKGROUND_TEXTURE.blit(ps, x, y);
  }

  @Override
  public void render(GuiGraphics ps, int mouseX, int mouseY, float delta) {
    this.renderBackground(ps);

    if (this.questList != null) {
      this.questList.render(ps, mouseX, mouseY, delta);
    } else {
      Font font = this.minecraft.font;
      float scale = 2.0F;
      ps.pose().pushPose();
      ps.pose().scale(scale, scale, scale);
      Component text = Component.translatable("questlog.no_quests");
      ps.drawString(
        font,
        text,
        (int) (((this.width - (font.width(text) * scale)) / 2) / scale),
        (int) (((this.height - (font.lineHeight * scale)) / 2) / scale),
        0x4C381B,
        false
      );
      ps.pose().popPose();
    }
    super.render(ps, mouseX, mouseY, delta);
  }

  @Override
  public boolean isPauseScreen() {
    return true;
  }

  @Override
  public boolean keyPressed(int key, int p_96553_, int p_96554_) {
    if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_E || QuestlogClient.OPEN_SCREEN_KEY.matches(key, p_96553_)) { // Escape or E (inventory key, since people have that muscle memory)
      Minecraft.getInstance().setScreen(this.previousScreen);
      return true;
    }
    return false;
  }
}
