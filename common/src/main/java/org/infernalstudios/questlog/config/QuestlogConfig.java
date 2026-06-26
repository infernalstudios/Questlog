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

    @ConfigEntry.Category("colors")
    @ConfigEntry.Gui.TransitiveObject
    public Colors colors = new Colors();

    public static class Button {
        @ConfigEntry.Gui.Tooltip()
        public boolean enabled = true;

        @ConfigEntry.Gui.Tooltip()
        public boolean showBadge = true;

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

    public static class Colors {
        @ConfigEntry.ColorPicker
        public int textColor = 0x4C381B;

        @ConfigEntry.ColorPicker
        public int titleColor = 0x4C381B;

        @ConfigEntry.ColorPicker
        public int completedTextColor = 0x529E52;

        @ConfigEntry.ColorPicker
        public int failedTextColor = 0xAA0000;

        @ConfigEntry.ColorPicker
        public int uncollectedTextColor = 0x9E6632;

        @ConfigEntry.ColorPicker
        public int progressTextColor = 0x9E7852;

        @ConfigEntry.ColorPicker
        public int hoveredTextColor = 0xFFFFFF;

        @ConfigEntry.ColorPicker
        public int searchTextColor = 0x4C381B;

        @ConfigEntry.ColorPicker
        public int noQuestsColor = 0x4C381B;

        @ConfigEntry.ColorPicker(allowAlpha = true)
        public int hoverFillColor = 0x80FFFFFF;

        @ConfigEntry.ColorPicker(allowAlpha = true)
        public int toastTitleColor = 0x9E6632;

        @ConfigEntry.ColorPicker(allowAlpha = true)
        public int toastDescriptionColor = 0x4C381B;
    }

}