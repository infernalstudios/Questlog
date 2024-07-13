package org.infernalstudios.questlog.client.gui;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.ScrollbarTexture;
import org.infernalstudios.questlog.util.texture.Texture;

public class QuestlogGuiSet {
  public static final QuestlogGuiSet DEFAULT = new QuestlogGuiSet(new ResourceLocation(Questlog.MODID, "textures/gui/questlog.png"));

  public final ResourceLocation location;
  public final Texture detailBackground;
  public final Texture button;
  public final Texture buttonHovered;
  public final Texture buttonLong;
  public final Texture buttonLongHovered;
  public final Texture toast;
  public final ScrollbarTexture scrollbar;

  public QuestlogGuiSet(ResourceLocation location) {
    this.location = location;
    this.detailBackground = new Texture(location, 275, 166, 0, 166, 512, 512);
    this.button = new Texture(location, 54, 18, 287, 0, 512, 512);
    this.buttonHovered = new Texture(location, 54, 18, 341, 0, 512, 512);
    this.buttonLong = new Texture(location, 88, 18, 287, 18, 512, 512);
    this.buttonLongHovered = new Texture(location, 88, 18, 375, 18, 512, 512);
    this.toast = new Texture(location, 160, 32, 275, 36, 512, 512);
    this.scrollbar = new ScrollbarTexture(
        new Texture(location, 8, 12, 277, 0, 512, 512),
        new Texture(location, 4, 1, 279, 14, 512, 512),
        new Texture(location, 4, 1, 279, 13, 512, 512),
        new Texture(location, 4, 1, 279, 15, 512, 512)
    );
  }
}
