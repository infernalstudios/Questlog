package org.infernalstudios.questlog.client.gui.screen;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.server.Bootstrap;
import org.infernalstudios.questlog.client.gui.components.EditorScrollableComponent;
import org.infernalstudios.questlog.client.gui.components.QuestTextEditBox;

/** Exercise nested input without a running game or rendered fonts. */
public final class EditorScrollingVerification {
    private static int checks;

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Font font = new Font(id -> null, false);
        TestBox description = new TestBox(font, 100, 100);
        TestBox details = new TestBox(font, 100, 170);
        QuestEditorScreen screen = new QuestEditorScreen(null);
        screen.descriptionBox = description;
        screen.detailsBox = details;
        screen.leftFields.add(description);
        screen.leftFields.add(details);
        EditorScrollableComponent panel = new EditorScrollableComponent(90, 90, 220, 170, new LeftPanelScrollable(screen));

        check(panel.mouseScrolled(110, 110, -1), "Wheel over description is consumed");
        check(description.offset() > 0, "Description scrolls");
        check(panel.getScrollAmount() == 0, "Text wheel leaves outer panel still");
        panel.mouseScrolled(110, 180, -1);
        check(details.offset() > 0, "Details scrolls independently");
        check(description.offset() == 4.5, "Details wheel leaves description still");
        panel.mouseScrolled(95, 150, -1);
        check(panel.getScrollAmount() > 0, "Wheel in panel margin scrolls the panel");
        panel.setScrollAmount(0);

        check(panel.mouseClicked(298, 190, 0), "Click on details scrollbar is accepted");
        check(details.isFocused(), "Scrollbar click focuses its field");
        check(panel.mouseDragged(298, 224, 0, 0, 34), "Details scrollbar can be dragged");
        check(details.offset() == details.maximum(), "Dragging to bottom reaches all text");
        panel.mouseDragged(110, 110, 0, -188, -114);
        check(details.offset() == 0, "Drag across description remains with details and clamps at top");
        check(description.offset() == 4.5, "Other field never steals a drag");
        panel.mouseReleased(298, 224, 0);
        check(!details.mouseDragged(298, 224, 0, 0, 1), "Release through outer panel ends the inner drag");

        panel.mouseClicked(298, 120, 0);
        panel.mouseDragged(298, 500, 0, 0, 380);
        check(description.offset() == description.maximum(), "Drag outside panel clamps at bottom");
        panel.mouseReleased(298, 500, 0);
        panel.mouseScrolled(298, 120, 1);
        check(description.offset() < description.maximum(), "Wheel works on the visible scrollbar too");
        double previous = description.offset();
        check(!description.mouseScrolled(95, 95, -1), "Wheel outside a text box is ignored");
        check(description.offset() == previous, "Outside wheel leaves text unchanged");
        description.contentHeight = 9;
        check(!description.mouseScrolled(110, 110, -1), "Short text yields the wheel to its parent");
        System.out.println("Editor scrolling: " + checks + " checks passed (headless; visual appearance needs in-game testing).");
    }

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    private static final class TestBox extends QuestTextEditBox {
        int contentHeight = 540;
        TestBox(Font font, int x, int y) { super(font, x, y, 195, 54); }
        @Override public int getInnerHeight() { return contentHeight; }
        @Override protected boolean scrollbarVisible() { return contentHeight > getHeight() - 8; }
        double offset() { return scrollAmount(); }
        int maximum() { return getMaxScrollAmount(); }
    }
}
