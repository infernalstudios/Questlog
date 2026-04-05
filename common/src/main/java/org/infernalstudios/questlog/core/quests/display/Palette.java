package org.infernalstudios.questlog.core.quests.display;

import org.infernalstudios.questlog.Questlog;

public record Palette(
        int textColor,
        int completedTextColor,
        int hoveredTextColor,
        int titleColor,
        int progressTextColor) {

    public Palette() {
        this(
                Questlog.getConfig().colors.textColor,
                Questlog.getConfig().colors.completedTextColor,
                Questlog.getConfig().colors.hoveredTextColor,
                Questlog.getConfig().colors.titleColor,
                Questlog.getConfig().colors.progressTextColor);
    }

    public Palette(String textColor, String completedTextColor, String hoveredTextColor, String titleColor, String progressTextColor) {
        this(
                textColor.startsWith("#") ? Integer.parseInt(textColor.substring(1), 16) : Integer.parseInt(textColor, 16),
                completedTextColor.startsWith("#") ? Integer.parseInt(completedTextColor.substring(1), 16) : Integer.parseInt(completedTextColor, 16),
                hoveredTextColor.startsWith("#") ? Integer.parseInt(hoveredTextColor.substring(1), 16) : Integer.parseInt(hoveredTextColor, 16),
                titleColor.startsWith("#") ? Integer.parseInt(titleColor.substring(1), 16) : Integer.parseInt(titleColor, 16),
                progressTextColor.startsWith("#") ? Integer.parseInt(progressTextColor.substring(1), 16) : Integer.parseInt(progressTextColor, 16));
    }
}
