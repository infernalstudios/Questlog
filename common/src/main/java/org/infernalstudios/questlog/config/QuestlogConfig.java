package org.infernalstudios.questlog.config;

public class QuestlogConfig {

//  @Category("button")
  public static class Button {

//    @Configurable(description = "Whether the GUI button should be displayed in the inventory screen")
    public static boolean enabled = true;

//    @Configurable(
//      description = "Whether the GUI button should be positioned relative to the inventory screen,\nif false, it will be positioned relative to the top-left corner of the screen"
//    )
    public static boolean relativeToInventory = true;

//    @Configurable(description = "The X position of the button")
    public static int x = 2;

//    @Configurable(description = "The Y position of the button")
    public static int y = -26;
  }
}
