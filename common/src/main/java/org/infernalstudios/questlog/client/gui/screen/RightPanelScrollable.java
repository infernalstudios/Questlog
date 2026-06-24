package org.infernalstudios.questlog.client.gui.screen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.client.gui.ContextMenu;
import org.infernalstudios.questlog.client.gui.ContextMenuItem;
import org.infernalstudios.questlog.client.gui.components.ScrollableComponent;
import org.infernalstudios.questlog.client.gui.components.scrollable.Scrollable;
import org.infernalstudios.questlog.core.quests.EditorMetadata;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

class RightPanelScrollable implements Scrollable, GuiEventListener, NarratableEntry {
    private final QuestEditorScreen screen;
    private ScrollableComponent scroller;

    public RightPanelScrollable(QuestEditorScreen screen) {
        this.screen = screen;
    }

    @Override
    public int getHeight() {
        return screen.getActiveList().size() * 22 + 8;
    }

    @Override
    public void setScrollableComponent(ScrollableComponent component) {
        this.scroller = component;
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        if (this.scroller == null) return;

        int PANEL_SPACING = 6;
        int leftWidth = 240;
        int rightWidth = 160;
        int height = 190;
        int totalWidth = leftWidth + rightWidth + PANEL_SPACING;
        int baseX = (screen.width - totalWidth) / 2;
        int baseY = (screen.height - height) / 2;
        int panel2X = baseX + leftWidth + PANEL_SPACING;
        int panel2Y = baseY;

        int startX = panel2X + 10;
        int startY = panel2Y + 28;
        int scroll = (int) this.scroller.getScrollAmount();

        List<JsonObject> list = screen.getActiveList();
        int color = Questlog.getConfig().colors.textColor | 0xFF000000;

        for (int i = 0; i < list.size(); i++) {
            JsonObject entry = list.get(i);
            int localRowY = 4 + i * 22;
            int rowY = startY + localRowY - scroll;

            boolean rowHovered = mouseX >= 0 && mouseX <= 220 && mouseY >= localRowY && mouseY < localRowY + 22;
            if (rowHovered) {
                ps.fill(startX + 2, rowY, startX + 138, rowY + 20, Questlog.getConfig().colors.hoverFillColor);
            }

            String fullType = entry.has("type") ? entry.get("type").getAsString() : "";
            String type = fullType.replace("questlog:", "");
            if (type.isEmpty()) {
                type = "unknown";
            }
            String text = type;
            if (entry.has("name")) {
                text = entry.get("name").getAsString();
            } else {
                String target = "";
                if (!fullType.isEmpty()) {
                    EditorMetadata meta = screen.getMetadata(fullType);
                    if (meta != null && meta.targetFieldKey() != null) {
                        String key = meta.targetFieldKey();
                        if (entry.has(key)) {
                            com.google.gson.JsonElement el = entry.get(key);
                            if (el.isJsonPrimitive()) {
                                target = el.getAsString();
                            } else if (el.isJsonObject() && el.getAsJsonObject().has("id")) {
                                target = el.getAsJsonObject().get("id").getAsString();
                            } else {
                                target = el.toString();
                            }
                        }
                    }
                }
                if (target.isEmpty()) {
                    String[] keys = new String[]{"block", "item", "entity", "biome", "dimension", "structure", "advancement", "stat", "quest", "command", "loot_table", "enchantment", "effect"};
                    for (String k : keys) {
                        if (entry.has(k)) {
                            com.google.gson.JsonElement el = entry.get(k);
                            if (el.isJsonPrimitive()) {
                                target = el.getAsString();
                            } else if (el.isJsonObject() && el.getAsJsonObject().has("id")) {
                                target = el.getAsJsonObject().get("id").getAsString();
                            } else {
                                target = el.toString();
                            }
                            break;
                        }
                    }
                }
                if (!target.isEmpty()) {
                    if (target.contains(":")) {
                        target = target.substring(target.indexOf(":") + 1);
                    }
                    text = type + ": " + target;
                }
            }

            if (screen.getFont().width(text) > 130) {
                text = screen.getFont().plainSubstrByWidth(text, 120) + "...";
            }

            ps.drawString(screen.getFont(), text, startX + 8, rowY + 6, color, false);

            int editX = startX + 164;
            int dupX = startX + 182;
            int delX = startX + 200;
            int btnY = rowY + 2;

            int localBtnY = localRowY + 2;
            boolean editHovered = mouseX >= 164 && mouseX <= 180 && mouseY >= localBtnY && mouseY <= localBtnY + 16;
            boolean dupHovered = mouseX >= 182 && mouseX <= 198 && mouseY >= localBtnY && mouseY <= localBtnY + 16;
            boolean delHovered = mouseX >= 200 && mouseX <= 216 && mouseY >= localBtnY && mouseY <= localBtnY + 16;

            ps.blit(editHovered ? QuestEditorScreen.GEAR_HIGHLIGHTED : QuestEditorScreen.GEAR_ICON, editX, btnY, 0, 0, 16, 16, 16, 16);
            ps.blit(dupHovered ? QuestEditorScreen.DUPLICATE_HIGHLIGHTED : QuestEditorScreen.DUPLICATE_ICON, dupX, btnY, 0, 0, 16, 16, 16, 16);
            ps.blit(delHovered ? QuestEditorScreen.CROSS_HIGHLIGHTED : QuestEditorScreen.CROSS_ICON, delX, btnY, 0, 0, 16, 16, 16, 16);

            if (editHovered) {
                screen.pendingTooltip = Component.translatable("questlog.editor.tooltip.edit_entry");
            } else if (dupHovered) {
                screen.pendingTooltip = Component.translatable("questlog.editor.tooltip.duplicate_entry");
            } else if (delHovered) {
                screen.pendingTooltip = Component.translatable("questlog.editor.tooltip.delete_entry");
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.scroller == null) return false;
        List<JsonObject> list = screen.getActiveList();

        for (int i = 0; i < list.size(); i++) {
            JsonObject entry = list.get(i);
            int rowY = 4 + i * 22;
            int btnY = rowY + 2;

            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                if (mouseX >= 164 && mouseX <= 180 && mouseY >= btnY && mouseY <= btnY + 16) {
                    screen.saveTemporaryState();
                    screen.selectedEntryIndex = i;
                    screen.editingEntry = entry;
                    screen.editingType = entry.has("type") ? entry.get("type").getAsString() : "questlog:item_obtain";
                    screen.entryLevelsToggle = entry.has("levels") && entry.get("levels").getAsBoolean();
                    screen.rightPageState = QuestEditorScreen.RightPageState.EDIT_ENTRY;
                    screen.refreshScreen();
                    return true;
                }
                if (mouseX >= 182 && mouseX <= 198 && mouseY >= btnY && mouseY <= btnY + 16) {
                    screen.saveTemporaryState();
                    JsonObject copy = JsonParser.parseString(entry.toString()).getAsJsonObject();
                    list.add(i + 1, copy);
                    screen.updateParentEntryWithChildren();
                    screen.refreshScreen();
                    return true;
                }
                if (mouseX >= 200 && mouseX <= 216 && mouseY >= btnY && mouseY <= btnY + 16) {
                    screen.saveTemporaryState();
                    list.remove(i);
                    screen.updateParentEntryWithChildren();
                    screen.refreshScreen();
                    return true;
                }
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
                if (mouseX >= 0 && mouseX <= 220 && mouseY >= rowY && mouseY < rowY + 22) {
                    List<ContextMenuItem> menuItems = getContextMenuItems(i, entry, list);

                    int screenX = (int) (mouseX + this.scroller.getXOffset());
                    int screenY = (int) (mouseY + this.scroller.getYOffset());
                    screen.contextMenu = new ContextMenu(screenX, screenY, menuItems, screen.getFont(), screen.width, screen.height);
                    return true;
                }
            }
        }
        return false;
    }

    private @NotNull List<ContextMenuItem> getContextMenuItems(int index, JsonObject entry, List<JsonObject> list) {
        List<ContextMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new ContextMenuItem(Component.translatable("questlog.menu.edit_entry"), () -> {
            screen.saveTemporaryState();
            screen.selectedEntryIndex = index;
            screen.editingEntry = entry;
            screen.editingType = entry.has("type") ? entry.get("type").getAsString() : "questlog:item_obtain";
            screen.entryLevelsToggle = entry.has("levels") && entry.get("levels").getAsBoolean();
            screen.rightPageState = QuestEditorScreen.RightPageState.EDIT_ENTRY;
            screen.refreshScreen();
        }));
        menuItems.add(new ContextMenuItem(Component.translatable("questlog.menu.duplicate_entry"), () -> {
            screen.saveTemporaryState();
            JsonObject copy = JsonParser.parseString(entry.toString()).getAsJsonObject();
            list.add(index + 1, copy);
            screen.updateParentEntryWithChildren();
            screen.refreshScreen();
        }));
        menuItems.add(new ContextMenuItem(Component.translatable("questlog.menu.delete_entry"), () -> {
            screen.saveTemporaryState();
            list.remove(index);
            screen.updateParentEntryWithChildren();
            screen.refreshScreen();
        }));
        return menuItems;
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setFocused(boolean var1) {
    }

    @Override
    public @NotNull NarratableEntry.NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
    }
}
