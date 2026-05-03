package org.infernalstudios.questlog.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class QuestEditorScreen extends Screen {

    private final Screen previousScreen;

    public QuestEditorScreen(Screen previousScreen) {
        super(Component.translatable("questlog.editor.title"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        super.init();

        // TODO: real back button to return to the normal questlog
        this.addRenderableWidget(Button.builder(Component.translatable("questlog.editor.back"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.previousScreen);
            }
        }).bounds(10, 10, 120, 20).build());

        // TODO: add the chapter/quest selection sidebars and main editing panels here
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float delta) {
        this.renderBackground(ps, mouseX, mouseY, delta);
        super.render(ps, mouseX, mouseY, delta);

        // TODO: title
        ps.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}