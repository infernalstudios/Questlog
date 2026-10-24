package org.infernalstudios.questlog.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.util.Callable;
import org.jetbrains.annotations.NotNull;

public class QuestlogButton extends AbstractButton {

    private final QuestlogGuiSet guiSet;
    private final Callable onPress;
    private final int textColor;
    private final int textColorHovered;

    public QuestlogButton(int x, int y, int textColor, int textColorHovered, Component message, Callable onPress, QuestlogGuiSet guiSet) {
        super(x, y, getExpectedWidth(message, guiSet), guiSet.button.height(), message);
        this.guiSet = guiSet;
        this.onPress = onPress;
        this.textColor = textColor;
        this.textColorHovered = textColorHovered;
    }

    private static int getExpectedWidth(Component message, QuestlogGuiSet guiSet) {
        return Minecraft.getInstance().font.width(message) > 46 ? guiSet.buttonLong.width() : guiSet.button.width();
    }

    public int getExpectedWidth() {
        return getExpectedWidth(this.getMessage(), this.guiSet);
    }

    public boolean isLong() {
        return Minecraft.getInstance().font.width(this.getMessage()) > 46;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        this.width = getExpectedWidth(this.getMessage(), this.guiSet);

        if (this.isLong()) {
            if (this.isHovered) {
                this.guiSet.buttonLongHovered.blit(ps, this.getX(), this.getY());
            } else {
                this.guiSet.buttonLong.blit(ps, this.getX(), this.getY());
            }
        } else {
            if (this.isHovered) {
                this.guiSet.buttonHovered.blit(ps, this.getX(), this.getY());
            } else {
                this.guiSet.button.blit(ps, this.getX(), this.getY());
            }
        }

        int textWidth = minecraft.font.width(this.getMessage());
        int maxTextWidth = this.width - 8;

        int textY = this.getY() + (this.height - minecraft.font.lineHeight) / 2 + 2;

        if (textWidth > maxTextWidth && maxTextWidth > 0) {
            ps.pose().pushPose();
            float scale = (float) maxTextWidth / textWidth;
            float scaledTextWidth = textWidth * scale;

            float scaledTextX = this.getX() + (this.width - scaledTextWidth) / 2.0f;
            float scaledTextY = textY + (minecraft.font.lineHeight * (1.0f - scale)) / 2.0f;

            ps.pose().translate(scaledTextX, scaledTextY, 0);
            ps.pose().scale(scale, scale, 1.0f);
            ps.drawString(minecraft.font, this.getMessage(), 0, 0, this.isHovered ? this.textColorHovered : this.textColor, false);
            ps.pose().popPose();
        } else {
            int textX = this.getX() + (this.width - textWidth) / 2;
            ps.drawString(minecraft.font, this.getMessage(), textX, textY, this.isHovered ? this.textColorHovered : this.textColor, false);
        }
    }

    @Override
    public void onPress() {
        this.onPress.call();
    }

    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}