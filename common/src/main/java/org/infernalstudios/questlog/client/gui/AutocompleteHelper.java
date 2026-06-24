package org.infernalstudios.questlog.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.infernalstudios.questlog.client.gui.components.NoShadowEditBox;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutocompleteHelper {
    private List<String> currentMatches = Collections.emptyList();
    private int selectedSuggestionIndex = -1;
    private String lastValue = "";
    private NoShadowEditBox lastBox = null;

    public void update(NoShadowEditBox activeBox, Supplier<List<String>> matchesSupplier) {
        if (activeBox != this.lastBox || (activeBox != null && !activeBox.getValue().equals(this.lastValue))) {
            this.selectedSuggestionIndex = -1;
            this.lastBox = activeBox;
            this.lastValue = activeBox != null ? activeBox.getValue() : "";
            if (activeBox != null && activeBox.isFocused()) {
                this.currentMatches = matchesSupplier.get();
            } else {
                this.currentMatches = Collections.emptyList();
            }
        }
    }

    public void clear() {
        this.currentMatches = Collections.emptyList();
        this.selectedSuggestionIndex = -1;
        this.lastBox = null;
        this.lastValue = "";
    }

    public boolean hasSuggestions() {
        return !this.currentMatches.isEmpty();
    }

    public NoShadowEditBox getLastBox() {
        return this.lastBox;
    }

    public void render(GuiGraphics ps, Font font, int mouseX, int mouseY) {
        if (this.lastBox == null || !this.lastBox.isFocused() || this.currentMatches.isEmpty()) {
            return;
        }

        int boxX = this.lastBox.getX();
        int boxY = this.lastBox.getY();
        int boxW = this.lastBox.getWidth();
        int startY = boxY + 17;
        int rowHeight = 14;
        int overlayHeight = this.currentMatches.size() * rowHeight + 2;

        ps.pose().pushPose();
        ps.pose().translate(0, 0, 400.0F);

        ps.fill(boxX, startY, boxX + boxW, startY + overlayHeight, 0xFF202020);
        ps.fill(boxX - 1, startY, boxX, startY + overlayHeight, 0xFF505050);
        ps.fill(boxX + boxW, startY, boxX + boxW + 1, startY + overlayHeight, 0xFF505050);
        ps.fill(boxX, startY - 1, boxX + boxW, startY, 0xFF505050);
        ps.fill(boxX, startY + overlayHeight, boxX + boxW, startY + overlayHeight + 1, 0xFF505050);

        for (int i = 0; i < this.currentMatches.size(); i++) {
            String match = this.currentMatches.get(i);
            int itemY = startY + 1 + i * rowHeight;
            boolean hovered = mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= itemY && mouseY <= itemY + rowHeight;
            boolean selected = hovered || this.selectedSuggestionIndex == i;

            if (selected) {
                ps.fill(boxX, itemY, boxX + boxW, itemY + rowHeight, 0xFF404040);
            }

            String drawText = match;
            if (font.width(drawText) > boxW - 10) {
                drawText = font.plainSubstrByWidth(drawText, boxW - 16) + "...";
            }
            ps.drawString(font, drawText, boxX + 4, itemY + 3, selected ? 0xFFFFFF00 : 0xFFFFFFFF, false);
        }

        ps.pose().popPose();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, Consumer<String> onSelected) {
        if (this.lastBox == null || !this.lastBox.isFocused() || this.currentMatches.isEmpty()) {
            return false;
        }

        int boxX = this.lastBox.getX();
        int boxY = this.lastBox.getY();
        int boxW = this.lastBox.getWidth();
        int startY = boxY + 17;
        int rowHeight = 14;

        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && mouseX >= boxX && mouseX <= boxX + boxW) {
            for (int i = 0; i < this.currentMatches.size(); i++) {
                int itemY = startY + 1 + i * rowHeight;
                if (mouseY >= itemY && mouseY <= itemY + rowHeight) {
                    onSelected.accept(this.currentMatches.get(i));
                    this.clear();
                    return true;
                }
            }
        }

        return false;
    }

    public boolean keyPressed(int key, Consumer<String> onSelected) {
        if (this.lastBox == null || !this.lastBox.isFocused() || this.currentMatches.isEmpty()) {
            return false;
        }

        if (key == GLFW.GLFW_KEY_DOWN) {
            this.selectedSuggestionIndex = (this.selectedSuggestionIndex + 1) % this.currentMatches.size();
            return true;
        } else if (key == GLFW.GLFW_KEY_UP) {
            if (this.selectedSuggestionIndex <= 0) {
                this.selectedSuggestionIndex = this.currentMatches.size() - 1;
            } else {
                this.selectedSuggestionIndex--;
            }
            return true;
        } else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            if (this.selectedSuggestionIndex >= 0 && this.selectedSuggestionIndex < this.currentMatches.size()) {
                onSelected.accept(this.currentMatches.get(this.selectedSuggestionIndex));
                this.clear();
                return true;
            }
        }

        return false;
    }
}
