package org.infernalstudios.questlog.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.jetbrains.annotations.NotNull;

public class ChapterArrowButton extends AbstractButton {
    private final QuestlogGuiSet guiSet;
    private final boolean isLeft;
    private final Runnable onPress;

    public ChapterArrowButton(int x, int y, boolean isLeft, Runnable onPress, QuestlogGuiSet guiSet) {
        super(x, y, 8, 13, Component.empty());
        this.isLeft = isLeft;
        this.onPress = onPress;
        this.guiSet = guiSet;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isMouseOver(mouseX, mouseY);
        if (this.isLeft) {
            (hovered ? this.guiSet.arrowLeftHovered : this.guiSet.arrowLeft).blit(ps, this.getX(), this.getY());
        } else {
            (hovered ? this.guiSet.arrowRightHovered : this.guiSet.arrowRight).blit(ps, this.getX(), this.getY());
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