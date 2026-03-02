package org.infernalstudios.questlog.core.quests.display;

public record Palette(
        int textColor,
        int completedTextColor,
        int hoveredTextColor,
        int titleColor,
        int progressTextColor) {

    public Palette(String textColor, String completedTextColor, String hoveredTextColor, String titleColor, String progressTextColor) {
        this(
                Integer.parseInt(textColor.substring(1), 16),
                Integer.parseInt(completedTextColor.substring(1), 16),
                Integer.parseInt(hoveredTextColor.substring(1), 16),
                Integer.parseInt(titleColor.substring(1), 16),
                Integer.parseInt(progressTextColor.substring(1), 16));
    }
}
