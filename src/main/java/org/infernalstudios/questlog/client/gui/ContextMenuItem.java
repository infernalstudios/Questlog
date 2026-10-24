package org.infernalstudios.questlog.client.gui;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record ContextMenuItem(Component label, Runnable action, @Nullable Component tooltip) {
    public ContextMenuItem(Component label, Runnable action) {
        this(label, action, null);
    }
}
