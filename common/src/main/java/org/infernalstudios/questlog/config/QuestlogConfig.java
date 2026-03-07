package org.infernalstudios.questlog.config;

import org.infernalstudios.config.Config;
import org.infernalstudios.config.annotation.Category;
import org.infernalstudios.config.annotation.Configurable;

public class QuestlogConfig {
    public static Config CONFIG;

    @Category("button")
    public static class Button {
        @Configurable(description = "Whether the GUI button should be displayed in the inventory screen")
        public static boolean enabled = true;

        @Configurable(description = "Whether the GUI button should be positioned relative to the inventory screen")
        public static boolean relativeToInventory = true;

        @Configurable(description = "The X position of the button")
        public static int x = 2;

        @Configurable(description = "The Y position of the button")
        public static int y = -26;
    }

    @Category("gui")
    public static class Gui {
        @Configurable(description = "X offset for the main quest list panel")
        public static int mainPanelX = 0;

        @Configurable(description = "Y offset for the main quest list panel")
        public static int mainPanelY = 0;

        @Configurable(description = "X offset for the search bar")
        public static int searchBarX = 0;

        @Configurable(description = "Y offset for the search bar")
        public static int searchBarY = 0;

        @Configurable(description = "X offset for the chapter tabs")
        public static int chapterButtonsX = 0;

        @Configurable(description = "Y offset for the chapter tabs")
        public static int chapterButtonsY = 0;
    }
}