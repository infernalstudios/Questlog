package org.infernalstudios.questlog.core.quests.display;

import org.infernalstudios.questlog.Questlog;
import org.jetbrains.annotations.Nullable;

public record Palette(
        @Nullable Integer textColorOverride,
        @Nullable Integer completedTextColorOverride,
        @Nullable Integer hoveredTextColorOverride,
        @Nullable Integer titleColorOverride,
        @Nullable Integer progressTextColorOverride) {

    public int textColor() {
        return textColorOverride != null ? textColorOverride : Questlog.getConfig().colors.textColor;
    }

    public int completedTextColor() {
        return completedTextColorOverride != null ? completedTextColorOverride : Questlog.getConfig().colors.completedTextColor;
    }

    public int hoveredTextColor() {
        return hoveredTextColorOverride != null ? hoveredTextColorOverride : Questlog.getConfig().colors.hoveredTextColor;
    }

    public int titleColor() {
        return titleColorOverride != null ? titleColorOverride : Questlog.getConfig().colors.titleColor;
    }

    public int progressTextColor() {
        return progressTextColorOverride != null ? progressTextColorOverride : Questlog.getConfig().colors.progressTextColor;
    }
}