package org.infernalstudios.questlog.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChapterTabButton extends AbstractButton {
    private final QuestlogGuiSet guiSet;
    @Nullable
    private final Blittable icon;
    private final boolean isSelected;
    private final boolean isPrimary;
    private final Runnable onPress;

    public ChapterTabButton(int x, int y, @Nullable Blittable icon, boolean isSelected, boolean isPrimary, Runnable onPress, QuestlogGuiSet guiSet, Component name) {
        super(x, y, 28, isSelected ? 29 : 23, name);
        this.icon = icon;
        this.isSelected = isSelected;
        this.isPrimary = isPrimary;
        this.onPress = onPress;
        this.guiSet = guiSet;
        this.setTooltip(Tooltip.create(name));
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        int renderX = this.getX() - 13;
        int renderY = this.getY() - 5;

        if (this.isPrimary) {
            if (this.isSelected) {
                this.guiSet.tabMainActive.blit(ps, renderX, renderY);
            } else {
                this.guiSet.tabMain.blit(ps, renderX, renderY);
            }
        } else {
            if (this.isSelected) {
                this.guiSet.tabSecondaryActive.blit(ps, renderX, renderY);
            } else {
                this.guiSet.tabSecondary.blit(ps, renderX, renderY);
            }
        }
        if (this.icon != null) {
            this.icon.blit(ps, this.getX() + 6, this.getY() + 3);
        }
    }

    @Override
    public void onPress() {
        this.onPress.run();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}