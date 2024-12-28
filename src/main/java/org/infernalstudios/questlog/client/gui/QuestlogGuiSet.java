package org.infernalstudios.questlog.client.gui;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.ScrollbarTexture;
import org.infernalstudios.questlog.util.texture.Texture;

public class QuestlogGuiSet {

  public static final QuestlogGuiSet DEFAULT = new QuestlogGuiSet(
    new ResourceLocation(Questlog.MODID, "textures/gui/quest_page.png"),
    new ResourceLocation(Questlog.MODID, "textures/gui/quest_peripherals.png")
  );

  public final ResourceLocation backgroundLoc;
  public final ResourceLocation peripheralLoc;
  public final Texture detailBackground;
  public final Texture button;
  public final Texture buttonHovered;
  public final Texture buttonLong;
  public final Texture buttonLongHovered;
  public final Texture toast;
  public final Texture important;
  public final Texture smallHR;
  public final Texture bigHR;
  public final ScrollbarTexture scrollbar;

  public QuestlogGuiSet(ResourceLocation backgroundLoc, ResourceLocation peripheralLoc) {
    this.backgroundLoc = backgroundLoc;
    this.peripheralLoc = peripheralLoc;
    this.detailBackground = new Texture(backgroundLoc, 1024, 512, 0, 0, 1024, 512);
    this.button = new Texture(peripheralLoc, 74, 38, 36, 55, 256, 256);
    this.buttonHovered = new Texture(peripheralLoc, 74, 38, 112, 55, 256, 256);
    this.buttonLong = new Texture(peripheralLoc, 108, 38, 2, 95, 256, 256);
    this.buttonLongHovered = new Texture(peripheralLoc, 108, 38, 112, 95, 256, 256);
    this.toast = new Texture(peripheralLoc, 173, 51, 81, 2, 256, 256);
    this.important = new Texture(peripheralLoc, 28, 36, 2, 2, 256, 256);
    this.smallHR = new Texture(peripheralLoc, 252, 9, 2, 135, 256, 256);
    this.bigHR = new Texture(peripheralLoc, 252, 9, 2, 146, 256, 256);
    this.scrollbar = new ScrollbarTexture(
      new Texture(peripheralLoc, 28, 36, 32, 2, 256, 256),
      new Texture(peripheralLoc, 16, 1, 62, 20, 256, 256),
      new Texture(peripheralLoc, 16, 1, 62, 19, 256, 256),
      new Texture(peripheralLoc, 16, 1, 62, 21, 256, 256)
    );
  }
}
