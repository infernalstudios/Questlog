package org.infernalstudios.questlog.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "questlog-client")
public class QuestlogConfig implements ConfigData {

    @ConfigEntry.Category("button")
    @ConfigEntry.Gui.TransitiveObject
    public Button button = new Button();

    @ConfigEntry.Category("gui")
    @ConfigEntry.Gui.TransitiveObject
    public Gui gui = new Gui();

    public static class Button {
        @ConfigEntry.Gui.Tooltip()
        public boolean enabled = true;

        @ConfigEntry.Gui.Tooltip()
        public boolean relativeToInventory = true;

        @ConfigEntry.Gui.Tooltip()
        public int x = 2;

        @ConfigEntry.Gui.Tooltip()
        public int y = -26;

        @ConfigEntry.Gui.Tooltip()
        public int badgeX = 24;

        @ConfigEntry.Gui.Tooltip()
        public int badgeY = 0;

        @ConfigEntry.Gui.Tooltip()
        public boolean bobbingBadge = true;
    }

    public static class Gui {
        @ConfigEntry.Gui.Tooltip()
        public int mainPanelX = 0;

        @ConfigEntry.Gui.Tooltip()
        public int mainPanelY = 0;

        @ConfigEntry.Gui.Tooltip()
        public int searchBarX = 0;

        @ConfigEntry.Gui.Tooltip()
        public int searchBarY = 0;

        @ConfigEntry.Gui.Tooltip()
        public int chapterButtonsX = 0;

        @ConfigEntry.Gui.Tooltip()
        public int chapterButtonsY = 0;
    }
}