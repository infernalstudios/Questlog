package org.infernalstudios.questlog.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.Texture;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class QuestDetails extends Screen implements NarrationSupplier {
  private static final Texture BACKGROUND_TEXTURE = new Texture(
      new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"),
      275, 166, 0, 166, 512, 512
  );

  private static final float TITLE_SCALE = 4.0F / 3.0F; // 1.33
  private static final int TITLE_X = 71;
  private static final int TITLE_Y = 13;
//  private static final int TITLE_WIDTH = 132;
//  private static final int TITLE_HEIGHT = 16;

  private static final int INFO_X = 20;
  private static final int INFO_Y = 36;
//  private static final int INFO_WIDTH = 232;
//  private static final int INFO_HEIGHT = 16;

  private static final int DESCRIPTION_X = 20;
  private static final int DESCRIPTION_Y = 60;
  private static final int DESCRIPTION_WIDTH = 232;
  private static final int DESCRIPTION_HEIGHT = 70;

  private static final int BUTTON_X = 109;
  private static final int BUTTON_Y = 134;

  private final Quest quest;
  private final List<GuiEventListener> children = new ArrayList<>();
  @Nullable
  private final Screen previousScreen;

  private int x;
  private int y;

  public QuestDetails(@Nullable Screen previousScreen, Quest quest) {
    super(quest.getDisplay().getTitle());
    this.previousScreen = previousScreen;
    this.quest = quest;

    this.x = this.width / 2 - BACKGROUND_TEXTURE.width() / 2;
    this.y = this.height / 2 - BACKGROUND_TEXTURE.height() / 2;
  }

  @Override
  protected void init() {
    super.init();

    this.x = this.width / 2 - BACKGROUND_TEXTURE.width() / 2;
    this.y = this.height / 2 - BACKGROUND_TEXTURE.height() / 2;

    this.addRenderableWidget(new QuestlogButton(this.x + BUTTON_X, this.y + BUTTON_Y, Component.translatable("gui.back"), () -> {
      if (this.minecraft != null) {
        this.minecraft.setScreen(this.previousScreen);
      }
    }));
  }

  @Override
  public void renderBackground(PoseStack ps) {
    super.renderBackground(ps);
    BACKGROUND_TEXTURE.blit(ps, this.x, this.y);
  }

  @Override
  public void render(PoseStack ps, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(ps);
    super.render(ps, mouseX, mouseY, partialTicks);

    // Title

//    // Scale the title to fit the title box
//    ps.pushPose();
//    ps.scale(TITLE_SCALE, TITLE_SCALE, TITLE_SCALE);

    font.draw(
      ps, this.quest.getDisplay().getTitle(),
      (this.x + TITLE_X - (float) this.font.width(this.quest.getDisplay().getTitle()) / 2),
      (this.y + TITLE_Y + (float) this.font.lineHeight / 2),
      0xFFFFFF
    );

//    ps.popPose();

    // Info
    // TODO
//    font.draw(ps, this.displayData.getProgress(), this.x + INFO_X, this.y + INFO_Y, 0xFFFFFF);

    // Description
    // TODO: Scrollable text widget
    List<FormattedCharSequence> description = font.split(this.quest.getDisplay().getDescription(), DESCRIPTION_WIDTH);
    for (int i = 0; i < description.size(); i++) {
      FormattedCharSequence descriptionLine = description.get(i);
      font.draw(ps, descriptionLine, this.x + DESCRIPTION_X, this.y + DESCRIPTION_Y + i * this.font.lineHeight, 0xFFFFFF);
    }
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public void updateNarration(NarrationElementOutput output) {
    // TODO
  }
}
