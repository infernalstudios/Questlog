package org.infernalstudios.questlog.core.quests.display;

public class Palette {

  public final int textColor;
  public final int completedTextColor;
  public final int hoveredTextColor;
  public final int titleColor;
  public final int progressTextColor;

  public Palette(String textColor, String completedTextColor, String hoveredTextColor, String titleColor, String progressTextColor) {
    this.textColor = Integer.parseInt(textColor.substring(1), 16);
    this.completedTextColor = Integer.parseInt(completedTextColor.substring(1), 16);
    this.hoveredTextColor = Integer.parseInt(hoveredTextColor.substring(1), 16);
    this.titleColor = Integer.parseInt(titleColor.substring(1), 16);
    this.progressTextColor = Integer.parseInt(progressTextColor.substring(1), 16);
  }
}
